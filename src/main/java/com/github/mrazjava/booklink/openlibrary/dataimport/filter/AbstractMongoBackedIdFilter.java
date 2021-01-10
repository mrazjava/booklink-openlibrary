package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;
import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Loads IDs to filter from mongo collection if the filter is enabled and no prior filters are detected (filter file
 * exists but does not contain any filterable items).
 *
 * @param <T> schema to use when checking mongo for IDs to filter
 */
@Slf4j
abstract class AbstractMongoBackedIdFilter<T extends BaseSchema> extends AbstractIdFilter {

    private OpenLibraryMongoRepository<T> repository;

    AbstractMongoBackedIdFilter(OpenLibraryMongoRepository<T> repository, String filterFilename) {
        super(filterFilename);
        this.repository = repository;
    }

    @Override
    public void load(File workingDirectory) {
        super.load(workingDirectory);
        if(isEnabled() && allowedIds.isEmpty()) {
            List<String> ids = repository.findAllIds().stream()
                    .map(BaseSchema::getId)
                    .collect(Collectors.toList());

            if(!ids.isEmpty()) {
                log.info("[{} FILTER] detected {} IDs in mongo", getFilterName(), ids.size());
                allowedIds.addAll(ids);
            }
        }
    }
}
