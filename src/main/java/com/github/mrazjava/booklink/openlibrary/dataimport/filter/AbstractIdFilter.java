package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Function;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.StringUtils;

import com.github.mrazjava.booklink.openlibrary.dataimport.OpenLibraryImportException;

import lombok.extern.slf4j.Slf4j;

import static java.util.Optional.ofNullable;

/**
 * Support for a filter whose content is fetched from a file on a disk. Enablement of such filter is dictated by the
 * presence of the file and presence of filterable content. Therefore, by default, this filter is disabled since by
 * default no file will be present.
 */
@Slf4j
public abstract class AbstractIdFilter<T> implements IdFilter<T> {

    protected Set<T> allowedIds = new HashSet<>();

    private File filterFile;
    
    /**
     * Transform ID defined in a filter file prior to loading it into a filter
     */
    private Function<String, T> idTransformer;


    AbstractIdFilter(String filterFilename, Function<String, T> idTransformer) {
        filterFile = new File(filterFilename);
        this.idTransformer = idTransformer;
    }
    
    /**
     * @param idTransformer to override which may have been set via constructor
     */
    public void setIdTransformer(Function<String, T> idTransformer) {
    	this.idTransformer = idTransformer;
    }

    @Override
    public void load(File workingDirectory) {

        filterFile = new File(workingDirectory.getAbsolutePath() + File.separator + filterFile.getName());

        if(isEnabled()) {
            log.info("{} FILTER detected ID file: {}", getFilterName(), filterFile.getAbsoluteFile());
            try {
                LineIterator iterator = FileUtils.lineIterator(filterFile, "UTF-8");
                while(iterator.hasNext()) {
                    final String line = iterator.next();
                    if(StringUtils.isNotBlank(line) && !line.startsWith("#")) {
                        allowedIds.add(ofNullable(idTransformer).map(t -> t.apply(line)).orElseThrow(() -> new OpenLibraryImportException("transformer error")));
                    }
                }
                log.info("{} FILTER loaded {} IDs:\n{}", getFilterName(), allowedIds.size(), allowedIds);
            } catch (IOException e) {
                log.error("{} FILTER problem loading id file [{}]: {}", getFilterName(), filterFile.getAbsolutePath(), e.getMessage());
                allowedIds.clear();
            }
        }
        else {
            log.info("{} FILTER: - DISABLED", getFilterName());
        }
    }

    @Override
    public boolean isEnabled() {
        return filterFile.exists();
    }

    @Override
    public boolean exists(T id) {
        return allowedIds.contains(id);
    }

    public abstract String getFilterName();
}
