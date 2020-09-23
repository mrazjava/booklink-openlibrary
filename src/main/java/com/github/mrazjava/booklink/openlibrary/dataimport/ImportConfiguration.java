package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.File;

@Slf4j
@Configuration
public class ImportConfiguration {

    @Bean
    ImportHandler produceImportHanlder(
            @Value("${booklink.di.ol-dump-file}") String dumpFilePath,
            @Value("${booklink.di.schema-class-name}") String configuredSchemaClass,
            AuthorHandler authorHandler,
            WorkHandler workHandler,
            EditionHandler editionHandler
    ) {

        if((new File(dumpFilePath)).exists()) {
            throw new OpenLibraryIntegrationException(String.format("invalid import file source: %s", dumpFilePath));
        }

        if(StringUtils.containsIgnoreCase(dumpFilePath, "author")) {
            return authorHandler;
        }
        else if(StringUtils.containsIgnoreCase(dumpFilePath, "work")) {
            return workHandler;
        }
        else if(StringUtils.containsIgnoreCase(dumpFilePath, "edition")) {
            return editionHandler;
        }

        if(StringUtils.endsWith(configuredSchemaClass, AuthorSchema.class.getSimpleName())) {
            return authorHandler;
        }
        else if(StringUtils.endsWith(configuredSchemaClass, WorkSchema.class.getSimpleName())) {
            return workHandler;
        }
        else if(StringUtils.endsWith(configuredSchemaClass, EditionSchema.class.getSimpleName())) {
            return editionHandler;
        }

        log.error("{} cannot be determined!\n - dumpFilePath: {},\n - configuredSchemaClass: {}",
                ImportHandler.class.getCanonicalName(),
                dumpFilePath, configuredSchemaClass);

        throw new OpenLibraryIntegrationException("invalid configuration");
    }
}
