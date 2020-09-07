package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Support for a filter whose content is fetched from a file on a disk. Enablement of such filter is dictated by the
 * presence of the file and presence of filterable content. Therefore, by default, this filter is disabled since by
 * default no file will be present.
 */
@Slf4j
abstract class AbstractIdFilter implements IdFilter {

    protected Set<String> allowedIds = new HashSet<>();

    private String filterFilename;

    private boolean enabled;


    AbstractIdFilter(String filterFilename) {
        this.filterFilename = filterFilename;
    }

    @Override
    public void load(File workingDirectory) {

        File idFilterFile = buildFilterFile(workingDirectory);
        enabled = idFilterFile.exists();
        if(isEnabled()) {
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
            log.info("[{} FILTER] - DISABLED", getFilterName());
        }
    }

    private File buildFilterFile(File workingDirectory) {
        return new File(workingDirectory.getAbsolutePath() + File.separator + filterFilename);
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    @Override
    public boolean exists(String id) {
        return allowedIds.contains(id);
    }

    protected abstract String getFilterName();
}
