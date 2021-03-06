package com.github.mrazjava.booklink.openlibrary.repository;

import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;

public interface AuthorRepository extends OpenLibraryMongoRepository<AuthorSchema> {

    public static final String COLLECTION_NAME = "authors";
}
