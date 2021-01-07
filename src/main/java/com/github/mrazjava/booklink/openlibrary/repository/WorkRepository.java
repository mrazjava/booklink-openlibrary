package com.github.mrazjava.booklink.openlibrary.repository;

import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;

public interface WorkRepository extends OpenLibraryMongoRepository<WorkSchema> {

    public static final String COLLECTION_NAME = "works";
}
