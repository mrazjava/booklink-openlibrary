package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import org.springframework.stereotype.Component;

@Component
public class PlainWorkCoverFilter extends AbstractIdFilter {

    public static final String FILTER_NAME = "WORK-PLAIN-COVER-FILTER";

    /**
     * Optional file; if exists, covers matching IDs in this filter will be marked as 
     * plain ({@link CoverImage#setPlain(Boolean)).
     */
    public static final String FILTER_FILENAME = "work-plain-covers-filter.txt";


    PlainWorkCoverFilter(String filterFilename) {
        super(FILTER_NAME);
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }

}
