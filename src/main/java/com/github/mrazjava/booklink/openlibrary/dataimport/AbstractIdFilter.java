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

    private File filterFile;


    AbstractIdFilter(String filterFilename) {
        filterFile = new File(filterFilename);
    }

    @Override
    public void load(File workingDirectory) {

        filterFile = new File(workingDirectory.getAbsolutePath() + File.separator + filterFile.getName());

        if(isEnabled()) {
            log.info("[{} FILTER] detected ID file: {}", getFilterName(), filterFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(filterFile, "UTF-8");
                while(iterator.hasNext()) {
                    String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(line);
                    }
                }
                log.info("[{} FILTER] loaded {} IDs:\n{}", getFilterName(), allowedIds.size(), allowedIds);
            } catch (IOException e) {
                log.error("[{} FILTER] problem loading id file [{}]: {}", getFilterName(), filterFile.getAbsolutePath(), e.getMessage());
                allowedIds.clear();
            }
        }
        else {
            log.info("[{} FILTER] - DISABLED", getFilterName());
        }
    }

    @Override
    public boolean isEnabled() {
        return filterFile.exists();
    }

    @Override
    public boolean exists(String id) {
        return allowedIds.contains(id);
    }

    protected abstract String getFilterName();
}
