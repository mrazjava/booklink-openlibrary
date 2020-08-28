package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.dataimport.*;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class ImportHandlingResolver implements ImportHandlingResolution {

    @Autowired
    private AuthorHandler authorHandler;

    @Autowired
    private WorkHandler workHandler;

    @Autowired
    private EditionHandler editionHandler;


    @Override
    public ImportHandler resolve(Class schema) {

        if(schema == null)
            throw new IllegalArgumentException("null schema not allowed");

        if(isAuthor(schema))
            return authorHandler;
        else if(isWork(schema))
            return workHandler;
        else if(isEdition(schema))
            return editionHandler;
        else
            throw new IllegalArgumentException(String.format("unsupported schema type: %s", schema.getCanonicalName()));

    }

    @Override
    public boolean isAuthor(Class schema) {
        return AuthorSchema.class.equals(schema);
    }

    @Override
    public boolean isWork(Class schema) {
        return WorkSchema.class.equals(schema);
    }

    @Override
    public boolean isEdition(Class schema) {
        return EditionSchema.class.equals(schema);
    }
}
