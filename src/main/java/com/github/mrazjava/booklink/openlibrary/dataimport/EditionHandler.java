package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
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

    private int authorMatchCount = 0;

    private int workMatchCount = 0;


    @Override
    public void prepare(File workingDirectory) {

        authorIdFilter.load(workingDirectory);
        workIdFilter.load(workingDirectory);
    }

    @Override
    public void handle(EditionSchema record, long sequenceNo) {

        if((sequenceNo % frequencyCheck) == 0) {
            log.info("FILTER MATCHES -- {}: {}, {}: {}, SAVED: {}",
                    authorIdFilter.getFilterName(), authorMatchCount,
                    workIdFilter.getFilterName(), workMatchCount,
                    savedCount);
            savedCount = authorMatchCount = workMatchCount = 0;
        }

        String matchedId = runFilter(record, authorIdFilter, sequenceNo);
        if(StringUtils.isBlank(matchedId)) {
            matchedId = runFilter(record, workIdFilter, sequenceNo);
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
            log.debug("[{} FILTER] edition # {} matched ID[{}]\n{}",
                    filter.getFilterName(), sequenceNo, matchedId, toText(record));
        }

        return matchedId;
    }

    @Override
    protected Class<EditionSchema> getSchemaType() {
        return EditionSchema.class;
    }
}
