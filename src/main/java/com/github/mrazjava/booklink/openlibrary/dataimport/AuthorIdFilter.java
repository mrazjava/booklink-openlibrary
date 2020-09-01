package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.stereotype.Component;

@Component
public class AuthorIdFilter extends AbstractIdFilter<AuthorSchema> {

    public static final String FILTER_NAME = "AUTHOR";

    /**
     * Optional file; if exists, only authors in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILENAME_AUTHOR_FILTER = "author-id-filter.txt";


    AuthorIdFilter(AuthorRepository repository) {
        super(repository, FILENAME_AUTHOR_FILTER);
    }

    @Override
    protected String getFilterName() {
        return FILTER_NAME;
    }
}
