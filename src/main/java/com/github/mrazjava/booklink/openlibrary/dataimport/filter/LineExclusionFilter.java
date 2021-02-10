package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import org.springframework.stereotype.Component;

@Component
public class LineExclusionFilter extends AbstractIdFilter {

    public static final String FILTER_NAME = "LINE-EXCLUSION";
    
    /**
     * Optional file; if exists, only authors in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILTER_FILENAME = "line-inclusion-filter.txt";

    
    LineExclusionFilter() {
        super(FILTER_FILENAME);
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }

}
