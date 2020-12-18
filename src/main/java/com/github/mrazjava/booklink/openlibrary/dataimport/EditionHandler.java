package com.github.mrazjava.booklink.openlibrary.dataimport;

import java.io.File;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.ofNullable;

@Slf4j
@Component
public class EditionHandler extends AbstractImportHandler<EditionSchema> {

    @Autowired
    private EditionRepository repository;

    @Autowired
    private WorkIdFilter workIdFilter;

    private int workMatchCount = 0;


    @Override
    public void prepare(File workingDirectory) {

        super.prepare(workingDirectory);
        workIdFilter.load(workingDirectory);

        imageDownloader.setCoverDirectory("editions");
    }

    @Override
    public void handle(EditionSchema record, long sequenceNo) {

        cleanBadData(record);

        if(sequenceNo % frequencyCheck == 0) {
            totalSavedCount += savedCount;
            if ((authorIdFilter.isEnabled() || workIdFilter.isEnabled())) {
                log.info("FILTER MATCHES -- BY-{}: {}, BY-{}: {}, SAVED: {}({})",
                        authorIdFilter.getFilterName(), authorMatchCount,
                        workIdFilter.getFilterName(), workMatchCount,
                        savedCount, totalSavedCount);
                authorMatchCount = workMatchCount = 0;
            }
            else if(persistData) {
                log.info("SAVED: {}({})", savedCount, totalSavedCount);
            }
            savedCount = 0;
        }

        String matchedId = runAuthorIdFilter(record, authorIdFilter, sequenceNo);
        if(StringUtils.isBlank(matchedId)) {
            matchedId = runAuthorIdFilter(record, workIdFilter, sequenceNo);
            if(StringUtils.isBlank(matchedId)) {
                return;
            }
            else {
                workMatchCount++;
            }
        }
        else {
            authorMatchCount++;
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
            cleanData(record);
            repository.save(record);
            savedCount++;
        }
    }

    /**
     * Sanitizes data known to be bad in the raw reed, so that it is not imported.
     *
     * @param record to clean
     */
    private void cleanBadData(EditionSchema record) {

        if(CollectionUtils.isEmpty(record.getCovers())) {
            record.setCovers(null);
        }
        else {
            List<Long> coverIds = record.getCovers().stream()
                    .filter(id -> id != null && id > 0).collect(Collectors.toList());
            record.setCovers(coverIds.isEmpty() ? null : coverIds);
        }
    }

    private String runAuthorIdFilter(EditionSchema record, AbstractIdFilter filter, long sequenceNo) {

        String matchedId = null;
        Set<String> ids = AuthorIdFilter.FILTER_NAME.equals(filter.getFilterName()) ?
                record.getAuthors() : record.getWorks();

        if(filter.isEnabled() && !CollectionUtils.isEmpty(ids)) {
            matchedId = ids.stream()
                    .filter(id -> filter.exists(id))
                    .findFirst()
                    .orElse(null);
        }

        if(matchedId != null) {
            if(log.isDebugEnabled()) {
                log.debug("[{} FILTER] edition # {} matched ID[{}]\n{}",
                        filter.getFilterName(), sequenceNo, matchedId, toText(record));
            }
        }

        return matchedId;
    }

    @Override
	protected void cleanData(EditionSchema record) {

    	ofNullable(record.getTitle()).ifPresent(t -> record.setTitle(cleanText(t)));
    	ofNullable(record.getFullTitle()).ifPresent(ft -> record.setFullTitle(cleanText(ft)));
	}

	@Override
    protected Class<EditionSchema> getSchemaType() {
        return EditionSchema.class;
    }
}
