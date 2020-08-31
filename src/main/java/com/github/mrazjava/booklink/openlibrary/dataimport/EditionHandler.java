package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.util.Optional;
import java.util.Set;

@Slf4j
@Component
public class EditionHandler extends AbstractImportHandler<EditionSchema> {

    @Autowired
    private EditionRepository repository;

    @Autowired
    private AuthorIdFilter authorIdFilter;

    @Autowired
    private WorkIdFilter workIdFilter;


    @Override
    public void prepare(File workingDirectory) {

        authorIdFilter.load(workingDirectory);
        workIdFilter.load(workingDirectory);
    }

    @Override
    public void handle(EditionSchema record, long sequenceNo) {

        String matchedId = runFilter(record, authorIdFilter, sequenceNo);
        if(StringUtils.isBlank(matchedId)) {
            matchedId = runFilter(record, workIdFilter, sequenceNo);
        }
        if(StringUtils.isBlank(matchedId)) {
            return;
        }

        Optional<EditionSchema> saved = Optional.empty();

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

    private String runFilter(EditionSchema record, AbstractIdFilter filter, long sequenceNo) {

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
            log.info("[{} FILTER] edition # {} matched ID[{}]\n{}",
                    filter.getFilterName(), sequenceNo, matchedId, toText(record));
        }

        return matchedId;
    }

    @Override
    protected Class<EditionSchema> getSchemaType() {
        return EditionSchema.class;
    }
}
