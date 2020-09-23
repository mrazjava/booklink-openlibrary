package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.ResourceLoader;

import java.io.File;
import java.io.IOException;

@Slf4j
@Configuration
public class ImportConfiguration {

    @Autowired
    private ResourceLoader resourceLoader;

    @Primary
    @Bean
    ImportHandler produceImportHanlder(
            @Value("${booklink.di.ol-dump-file}") String dumpFilePath,
            @Value("${booklink.di.handler-class}") String configuredHandlerClass,
            AuthorHandler authorHandler,
            WorkHandler workHandler,
            EditionHandler editionHandler
    ) throws IOException {

        File dumpFile = StringUtils.startsWith(dumpFilePath, "/") ?
            new File(dumpFilePath) :
            (new ClassPathResource(dumpFilePath)).getFile();

        if(!dumpFile.exists()) {
            throw new OpenLibraryIntegrationException(String.format("invalid import source: %s", dumpFilePath));
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

        if(StringUtils.contains(configuredHandlerClass, authorHandler.getClass().getSimpleName())) {
            return authorHandler;
        }
        else if(StringUtils.contains(configuredHandlerClass, workHandler.getClass().getSimpleName())) {
            return workHandler;
        }
        else if(StringUtils.contains(configuredHandlerClass, editionHandler.getClass().getSimpleName())) {
            return editionHandler;
        }

        log.error("{} cannot be determined!\n - booklink.di.ol-dump-file: {},\n - booklink.di.handler-class: {}",
                ImportHandler.class.getCanonicalName(),
                dumpFilePath, configuredHandlerClass);

        throw new OpenLibraryIntegrationException("invalid configuration");
    }
}
