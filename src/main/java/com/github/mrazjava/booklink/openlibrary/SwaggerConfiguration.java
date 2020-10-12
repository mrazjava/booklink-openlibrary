package com.github.mrazjava.booklink.openlibrary;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import static springfox.documentation.builders.PathSelectors.regex;

@Slf4j
@Profile(OpenLibraryDepotApp.PROFILE)
@Configuration
public class SwaggerConfiguration {

    @Bean
    public Docket internalAPI(@Value("${project.version:unknown}") String appVersion) {
        Docket docket = new Docket(DocumentationType.OAS_30)
                .forCodeGeneration(true)
                .groupName("booklink-v" + appVersion)
                .select()
                .apis(RequestHandlerSelectors.basePackage(DepotAuthor.class.getPackageName()))
                .paths(regex(".*/depot/.*"))
                .build()
                .directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(java.time.OffsetDateTime.class, java.util.Date.class)
                .apiInfo(generateInternalApiInfo(appVersion, "Booklink Openlibrary Depot", "REST API for author/book integration with openlibrary.org"));

        return docket;
    }

    private ApiInfo generateInternalApiInfo(String version, String title, String desc) {

        return new ApiInfoBuilder()
                .title(title)
                .description(desc)
                .license("Apache 2.0")
                .licenseUrl("https://www.apache.org/licenses/LICENSE-2.0")
                .termsOfServiceUrl("https://pre.booklinktrove.com/about/privacy-policy")
                .version(version)
                .contact(new Contact("mrazjava", "https://github.com/mrazjava", ""))
                .build();
    }
}
