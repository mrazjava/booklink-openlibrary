package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.util.Optional;

@Slf4j
@Component
public class WorkHandler extends AbstractImportHandler<WorkSchema> {

    @Autowired
    private WorkRepository repository;

    @Autowired
    private AuthorIdFilter authorIdFilter;

    private int authorMatchCount = 0;


    @Override
    public void prepare(File workingDirectory) {
        authorIdFilter.load(workingDirectory);
    }

    @Override
    public void handle(WorkSchema record, long sequenceNo) {

        if(sequenceNo % frequencyCheck == 0) {
            log.info("FILTER MATCHES -- BY-{}: {}, SAVED: {}",
                    authorIdFilter.getFilterName(), authorMatchCount, savedCount);
            authorMatchCount = 0;
        }

        if(authorIdFilter.isEnabled()) {

            if(CollectionUtils.isEmpty(record.getAuthors())) {
                return;
            }

            Optional<String> matchedId = record.getAuthors().stream()
                    .filter(id -> authorIdFilter.exists(id))
                    .findFirst();

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

        if(persistData) {
            if(!persistDataOverride) {
                if(repository.findById(record.getId()).isPresent()) {
                    return;
                }
            }
            repository.save(record);
            savedCount++;
        }
    }

    private void downloadImages(WorkSchema record, long sequenceNo) throws IOException {
        // TODO: implement me
    }

    @Override
    protected Class<WorkSchema> getSchemaType() {
        return WorkSchema.class;
    }
}
