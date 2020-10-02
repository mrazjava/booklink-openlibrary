package com.github.mrazjava.booklink.openlibrary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Profile(OpenLibraryRestApp.PROFILE)
@Slf4j
@SpringBootApplication(scanBasePackageClasses = {
        MongoConfiguration.class,
        ObjectMapperConfiguration.class
})
public class OpenLibraryRestApp {

    public static final String PROFILE = "REST";

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .sources(OpenLibraryRestApp.class)
                .profiles(OpenLibraryRestApp.PROFILE)
                .run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void initialize() {
        log.info("Booklink-OpenLibrary REST API");
    }
}