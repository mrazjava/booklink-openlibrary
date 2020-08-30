package com.github.mrazjava.booklink.openlibrary.repository;

import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.Optional;

public interface EditionRepository extends OpenLibraryMongoRepository<EditionSchema> {
}
