package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class LineExclusionFilter extends AbstractIdFilter<Long> {

    public static final String FILTER_NAME = "LINE-EXCLUSION";
    
    /**
     * Optional file; if exists, records matching line number in this filter 
     * file will be skipped. Line 1 starts at index 1.
     */
    public static final String FILTER_FILENAME = "line-exclusion-filter.txt";

    
    LineExclusionFilter() {
        super(FILTER_FILENAME, line -> Long.valueOf(line));
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }

    public void logSkipMsg(long index, String record) {
        log.info("{}: row # {} matched! record skipped:\n{}", FILTER_NAME, index, record);
    }
}
