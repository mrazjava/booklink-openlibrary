package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import org.springframework.stereotype.Component;

/**
 * Tracks IDs of work cover images which are plain, without any text or image. Many hard 
 * covers are like this.
 */
@Component
public class PlainWorkCoverFilter extends AbstractIdFilter<String> {

    public static final String FILTER_NAME = "WORK-PLAIN-COVER-FILTER";

    /**
     * Optional file; if exists, covers matching IDs in this filter will be marked as 
     * plain ({@link CoverImage#setPlain(Boolean)).
     */
    public static final String FILTER_FILENAME = "work-plain-covers-filter.txt";


    PlainWorkCoverFilter() {
        super(FILTER_NAME, line -> line);
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }

}
