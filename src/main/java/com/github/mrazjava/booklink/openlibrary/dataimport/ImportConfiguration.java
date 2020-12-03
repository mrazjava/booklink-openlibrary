package com.github.mrazjava.booklink.openlibrary.dataimport;

import java.io.File;
import java.io.IOException;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.io.ClassPathResource;

import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class ImportConfiguration {

    @Value("${booklink.di.ol-dump-file}")
    private String dumpFilePath;
    
    @Value("${booklink.di.handler-class}")
    private String configuredHandlerClass;
	
    public Class<?> getHandlerClass() {
    	
    	File dumpFile = null;
    	
        try {
			dumpFile = StringUtils.startsWith(dumpFilePath, "/") ?
			        new File(dumpFilePath) :
			        (new ClassPathResource(dumpFilePath)).getFile();
		} catch (IOException e) {
			throw new OpenLibraryImportException("cannot locate import file", e);
		}

        if(dumpFile == null || !dumpFile.exists()) {
            throw new OpenLibraryImportException(String.format("invalid import source: %s", dumpFilePath));
        }

        
        if(StringUtils.containsAny(configuredHandlerClass, AuthorHandler.class.getSimpleName(), WorkHandler.class.getSimpleName(), EditionHandler.class.getSimpleName())) {
            try {
				return Class.forName(configuredHandlerClass);
			} catch (ClassNotFoundException e) {
				throw new OpenLibraryImportException("invalid configution: ${booklink.di.handler-class}", e);
			}
        }

        if(StringUtils.containsIgnoreCase(dumpFilePath, "author")) {
            return AuthorHandler.class;
        }
        else if(StringUtils.containsIgnoreCase(dumpFilePath, "work")) {
            return WorkHandler.class;
        }
        else if(StringUtils.containsIgnoreCase(dumpFilePath, "edition")) {
            return EditionHandler.class;
        }
        
        throw new OpenLibraryImportException("invalid configuration");
    }
	
    @Primary
    @Bean
    AbstractImportHandler<? extends BaseSchema> produceImportHanlder(
            AuthorHandler authorHandler,
            WorkHandler workHandler,
            EditionHandler editionHandler
    ) throws IOException {

    	Class<?> clazz = getHandlerClass();

        if(AuthorHandler.class.equals(clazz)) {
            return authorHandler;
        }
        else if(WorkHandler.class.equals(clazz)) {
            return workHandler;
        }
        else if(EditionHandler.class.equals(clazz)) {
            return editionHandler;
        }

        log.error("{} cannot be determined!\n - booklink.di.ol-dump-file: {},\n - booklink.di.handler-class: {}",
                ImportHandler.class.getCanonicalName(),
                dumpFilePath, configuredHandlerClass);

        throw new OpenLibraryImportException("import handler can't be determined");
    }
}
