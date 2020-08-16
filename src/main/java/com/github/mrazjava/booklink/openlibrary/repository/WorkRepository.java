package com.github.mrazjava.booklink.openlibrary.repository;

import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface WorkRepository extends MongoRepository<WorkSchema, String> {
}
