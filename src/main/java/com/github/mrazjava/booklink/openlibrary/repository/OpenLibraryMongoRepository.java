package com.github.mrazjava.booklink.openlibrary.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;
import org.springframework.data.repository.NoRepositoryBean;

import java.util.List;

@NoRepositoryBean
public interface OpenLibraryMongoRepository<T> extends MongoRepository<T, String> {

    @Query(value = "{}", fields = "{'_id': 1}")
    List<String> findAllIds();
}
