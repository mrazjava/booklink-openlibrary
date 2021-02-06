package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.ofNullable;

/**
 * Support for a filter whose content is fetched from a file on a disk. Enablement of such filter is dictated by the
 * presence of the file and presence of filterable content. Therefore, by default, this filter is disabled since by
 * default no file will be present.
 */
@Slf4j
public abstract class AbstractIdFilter implements IdFilter {

    protected Set<String> allowedIds = new HashSet<>();

    private File filterFile;
    
    /**
     * Transform (or not) ID defined in a filter file prior to loading it into a filter
     */
    private Function<String, String> idTransformer;


    AbstractIdFilter(String filterFilename) {
        filterFile = new File(filterFilename);
    }
    
    public void setIdTransformer(Function<String, String> idTransformer) {
    	this.idTransformer = idTransformer;
    }

    @Override
    public void load(File workingDirectory) {

        filterFile = new File(workingDirectory.getAbsolutePath() + File.separator + filterFile.getName());

        if(isEnabled()) {
            log.info("[{} FILTER] detected ID file: {}", getFilterName(), filterFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(filterFile, "UTF-8");
                while(iterator.hasNext()) {
                    final String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(ofNullable(idTransformer).map(t -> t.apply(line)).orElse(line));
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

    public abstract String getFilterName();
}
