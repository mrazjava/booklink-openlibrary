package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;
import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
abstract class AbstractIdFilter<T extends BaseSchema> implements IdFilter {

    private Set<String> allowedIds = new HashSet<>();

    private OpenLibraryMongoRepository<T> repository;

    private String filterFilename;


    AbstractIdFilter(OpenLibraryMongoRepository repository, String filterFilename) {
        this.repository = repository;
        this.filterFilename = filterFilename;
    }

    @Override
    public void load(File workingDirectory) {

        File idFilterFile = new File(workingDirectory.getAbsolutePath() + File.separator + filterFilename);
        if(idFilterFile.exists()) {
            log.info("[{} FILTER] detected ID file: {}", getFilterName(), idFilterFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(idFilterFile, "UTF-8");
                while(iterator.hasNext()) {
                    String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(line);
                    }
                }
                log.info("[{} FILTER] loaded {} IDs:\n{}", getFilterName(), allowedIds.size(), allowedIds);
            } catch (IOException e) {
                log.error("[{} FILTER] problem loading id file [{}]: {}", getFilterName(), idFilterFile.getAbsolutePath(), e.getMessage());
                allowedIds.clear();
            }
        }
        else {
            List<String> ids = repository.findAllIds().stream()
                    .map(BaseSchema::getId)
                    .collect(Collectors.toList());

            if(!ids.isEmpty()) {
                log.info("[{} FILTER] detected {} IDs in mongo", getFilterName(), ids.size());
                allowedIds.addAll(ids);
            }
        }
    }

    @Override
    public boolean isEnabled() {
        return !allowedIds.isEmpty();
    }

    @Override
    public boolean exists(String id) {
        return allowedIds.contains(id);
    }

    protected abstract String getFilterName();
}
