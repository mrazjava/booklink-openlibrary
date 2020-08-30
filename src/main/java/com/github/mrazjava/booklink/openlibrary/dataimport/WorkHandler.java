package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Optional;

@Slf4j
@Component
public class WorkHandler extends AbstractImportHandler<WorkSchema> {

    @Autowired
    private WorkRepository repository;

    @Autowired
    private AuthorIdFilter authorIdFilter;


    @Override
    public void prepare(File workingDirectory) {
        authorIdFilter.load(workingDirectory);
    }

    @Override
    public void handle(WorkSchema record, long sequenceNo) {

        if(authorIdFilter.isEnabled()) {

            if(CollectionUtils.isEmpty(record.getAuthors())) {
                return;
            }

            Optional<String> matchedId = record.getAuthors().stream()
                    .filter(id -> authorIdFilter.exists(id))
                    .findFirst();

            if(matchedId.isPresent()) {
                log.info("FILTER: work # {} matched author ID[{}]\n{}", sequenceNo, matchedId.get(), toText(record));
            }
            else {
                return;
            }
        }

        Optional<WorkSchema> saved = Optional.empty();

        if(persistData) {
            if(!persistDataOverride) {
                saved = repository.findById(record.getId());
                if(saved.isPresent()) {
                    return;
                }
            }

            repository.save(saved.orElse(record));
        }
    }

    @Override
    protected Class<WorkSchema> getSchemaType() {
        return WorkSchema.class;
    }
}
