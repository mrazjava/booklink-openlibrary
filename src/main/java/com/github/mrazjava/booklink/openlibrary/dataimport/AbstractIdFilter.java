package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
abstract class AbstractIdFilter implements IdFilter {

    private Set<String> allowedIds = new HashSet<>();

    private OpenLibraryMongoRepository repository;

    private String filterFilename;


    AbstractIdFilter(OpenLibraryMongoRepository repository, String filterFilename) {
        this.repository = repository;
        this.filterFilename = filterFilename;
    }

    @Override
    public void load(File workingDirectory) {

        File authorIdFilterFile = new File(workingDirectory.getAbsolutePath() + File.separator + filterFilename);
        if(authorIdFilterFile.exists()) {
            log.info("[{} FILTER] detected ID file: {}", getFilterName(), authorIdFilterFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(authorIdFilterFile, "UTF-8");
                while(iterator.hasNext()) {
                    String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(line);
                    }
                }
                log.info("[{} FILTER] loaded {} IDs:\n{}", getFilterName(), allowedIds.size(), allowedIds);
            } catch (IOException e) {
                log.error("[{} FILTER] problem loading id file [{}]: {}", getFilterName(), authorIdFilterFile.getAbsolutePath(), e.getMessage());
                allowedIds.clear();
            }
        }
        else {
            List<String> ids = repository.findAllIds();
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
