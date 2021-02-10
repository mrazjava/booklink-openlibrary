package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.stereotype.Component;

@Component
public class AuthorIdInclusionFilter extends AbstractMongoBackedIdFilter<AuthorSchema> {

    public static final String FILTER_NAME = "AUTHOR-ID-INCLUSION";

    /**
     * Optional file; if exists, only authors in that list will be persisted.
     * One ID per line. Comments are allowed and start with a #. Empty lines
     * are allowed and ignored.
     */
    public static final String FILTER_FILENAME = "src/test/resources/author-id-incl-filter.txt";


    AuthorIdInclusionFilter(AuthorRepository repository) {
        super(repository, FILTER_FILENAME);
    }

    @Override
    public String getFilterName() {
        return FILTER_NAME;
    }
}
