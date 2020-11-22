package com.github.mrazjava.booklink.openlibrary.repository;

import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;

import java.util.List;

public interface EditionRepository extends OpenLibraryMongoRepository<EditionSchema> {

    public static final String COLLECTION_NAME = "editions";

    List<EditionSchema> findByWorks(List<String> workIds);
}
