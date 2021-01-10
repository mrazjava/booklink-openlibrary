package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import org.springframework.stereotype.Component;

@Component
public class CustomAuthorImgFilter extends AbstractIdFilter {

    public static final String FILTER_NAME = "WORK-ID";

    /**
     * Optional file; if exists, only works in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILENAME_WORK_FILTER = "custom-author-img-filter.txt";

    public CustomAuthorImgFilter(String filterFilename) {
        super(filterFilename);
    }

    @Override
    public String getFilterName() {
        return FILENAME_WORK_FILTER;
    }
}
