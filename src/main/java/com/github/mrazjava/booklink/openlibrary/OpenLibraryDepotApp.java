package com.github.mrazjava.booklink.openlibrary;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Profile;
import org.springframework.context.event.EventListener;

@Profile(OpenLibraryDepotApp.PROFILE)
@Slf4j
@ComponentScan(
        excludeFilters = @ComponentScan.Filter(type = FilterType.REGEX, pattern="com.github.mrazjava.booklink.openlibrary.dataimport.*")
)
@SpringBootApplication
public class OpenLibraryDepotApp {

    public static final String PROFILE = "DEPOT";

    public static void main(String[] args) {

        new SpringApplicationBuilder()
                .sources(OpenLibraryDepotApp.class)
                .run(args);
    }

    @EventListener(ApplicationReadyEvent.class)
    void initialize() {
        log.info("Booklink-OpenLibrary Depot (REST API)");
    }
}
