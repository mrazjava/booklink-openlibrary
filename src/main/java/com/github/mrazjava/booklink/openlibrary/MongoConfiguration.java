package com.github.mrazjava.booklink.openlibrary;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

@Configuration
@EnableMongoRepositories("com.github.mrazjava.booklink.openlibrary.repository")
public class MongoConfiguration {
}
