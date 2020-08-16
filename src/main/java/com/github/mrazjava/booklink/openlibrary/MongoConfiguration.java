package com.github.mrazjava.booklink.openlibrary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Slf4j
@Configuration
@EnableMongoRepositories("com.github.mrazjava.booklink.openlibrary.repository")
public class MongoConfiguration {
}
