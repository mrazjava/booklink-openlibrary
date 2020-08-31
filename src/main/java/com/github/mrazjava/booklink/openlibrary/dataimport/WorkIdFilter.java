package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WorkIdFilter extends AbstractIdFilter<WorkSchema> {

    public static final String FILTER_NAME = "WORK";

    /**
     * Optional file; if exists, only works in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILENAME_WORK_FILTER = "work-id-filter.txt";

    WorkIdFilter(WorkRepository repository) {
        super(repository, FILENAME_WORK_FILTER);
    }

    @Override
    protected String getFilterName() {
        return FILTER_NAME;
    }
}
