package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;
import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
abstract class AbstractMongoBackedIdFilter<T extends BaseSchema> extends AbstractIdFilter<T> {

    private OpenLibraryMongoRepository<T> repository;

    AbstractMongoBackedIdFilter(OpenLibraryMongoRepository repository, String filterFilename) {
        super(filterFilename);
        this.repository = repository;
    }

    @Override
    public void load(File workingDirectory) {
        super.load(workingDirectory);
        if(!isEnabled()) {
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
