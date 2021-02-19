package com.github.mrazjava.booklink.openlibrary.dataimport;

import static com.github.mrazjava.booklink.openlibrary.BooklinkUtils.extractSampleText;
import static java.util.Optional.ofNullable;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.github.mrazjava.booklink.openlibrary.dataimport.filter.AuthorIdInclusionFilter;
import com.github.mrazjava.booklink.openlibrary.dataimport.filter.PlainWorkCoverFilter;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class WorkHandler extends AbstractImportHandler<WorkSchema> {

    @Autowired
    private WorkRepository repository;

    @Autowired
    private AuthorIdInclusionFilter authorIdFilter;
    
    @Autowired
    private PlainWorkCoverFilter plainCoverFilter;

    private int authorMatchCount = 0;
    
    private Set<String> secondaryAuthors = new HashSet<>();


    @Override
    public void prepare(File workingDirectory) {

        super.prepare(workingDirectory);
        imageDownloader.setCoverDirectory("works");
        plainCoverFilter.load(workingDirectory);        
    }

    @Override
    public void handle(WorkSchema record, long sequenceNo) {

        if(sequenceNo % frequencyCheck == 0) {
            totalSavedCount += savedCount;
            if (authorIdFilter.isEnabled()) {
                log.info("FILTER MATCHES -- BY-{}: {}, SAVED: {}({})",
                        authorIdFilter.getFilterName(), authorMatchCount, savedCount, totalSavedCount);
                authorMatchCount = 0;
            }
            else if(persistData) {
                log.info("SAVED: {}({})", savedCount, totalSavedCount);
            }
            savedCount = 0;
        }

        if(authorIdFilter.isEnabled()) {

            if(CollectionUtils.isEmpty(record.getAuthors())) {
                return;
            }

            Optional<String> matchedId = record.getAuthors().stream()
                    .filter(id -> authorIdFilter.exists(id))
                    .findFirst();
            
            if(matchedId.isPresent()) {
                secondaryAuthors.addAll(record.getAuthors().stream()
                    .filter(id -> !authorIdFilter.exists(id))
                    .collect(Collectors.toSet()));
            }

            if(matchedId.isPresent()) {
                if(log.isDebugEnabled()) {
                    log.debug("FILTER: work # {} matched author ID[{}]\n{}",
                            sequenceNo, matchedId.get(), toText(record));
                }
                authorMatchCount++;
            }
            else {
                return;
            }
        }

        if(BooleanUtils.isTrue(imagePull)) {
            imageDownloader.downloadImages(record, sequenceNo);
            checkImages(record);
        }
        
        if(persistData) {
            if(!persistDataOverride) {
                if(repository.findById(record.getId()).isPresent()) {
                    return;
                }
            }
            enhanceData(record);
            repository.save(record);
            savedCount++;
        }
    }

    @Override
    public void conclude(File workingDirectory) {
        super.conclude(workingDirectory);
        log.info("found {} secondary authors", secondaryAuthors.size());
        if(!secondaryAuthors.isEmpty()) {
        File secondaryAuthorsFile = new File(workingDirectory.getAbsolutePath() + File.separator + "secondary-author-ids.txt");
            try {
                FileUtils.write(
                        secondaryAuthorsFile, 
                        secondaryAuthors.stream().map(id -> String.format("SA:%s",  id)).collect(Collectors.joining("\n")), 
                        StandardCharsets.UTF_8
                        );
            } catch (IOException e) {
                log.warn("problem generating secondary authors file: {}", secondaryAuthorsFile.getAbsolutePath());
                e.printStackTrace();
            }
        }
    }

    @Override
    protected void checkImages(BaseSchema record) {

        super.checkImages(record);

        ofNullable(record.getImageSmall()).ifPresent(imgS -> {
            imgS.setPlain(plainCoverFilter.exists(imgS.getId()));
        });
        ofNullable(record.getImageMedium()).ifPresent(imgM -> {
            imgM.setPlain(plainCoverFilter.exists(imgM.getId()));
        });
        ofNullable(record.getImageLarge()).ifPresent(imgL -> {
            imgL.setPlain(plainCoverFilter.exists(imgL.getId()));
        });
    }

    @Override
	protected void enhanceData(WorkSchema record) {

    	ofNullable(record.getTitle()).ifPresent(t -> record.setTitleSample(extractSampleText(t)));
	}

	@Override
    protected Class<WorkSchema> getSchemaType() {
        return WorkSchema.class;
    }
}
