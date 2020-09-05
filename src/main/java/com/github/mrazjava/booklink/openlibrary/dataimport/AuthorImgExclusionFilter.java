package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.stereotype.Component;

@Component
public class AuthorImgExclusionFilter extends AbstractIdFilter<AuthorSchema> {

    public static final String FILTER_NAME = "AUTHOR-IMG-EXCLUSION";

    /**
     * Optional file; if exists, author images which exist in this file will be ignored
     * by the download process. This is useful as some images will notoriously return a 404, or 500
     * or some other error. Sometimes the same image is available in certain sizes but not others.
     * The format is one ID per line: ID-SIZE where ID is photo ID and size is a valid enumeration
     * from {@link ImageSize}. Comments are allowed and start with a #. Empty lines are allowed and
     * ignored.
     */
    public static final String FILTER_FILENAME = "author-img-exclusions.txt";


    AuthorImgExclusionFilter(AuthorRepository repository) {
        super(repository, FILTER_FILENAME);
    }

    @Override
    protected String getFilterName() {
        return null;
    }
}
