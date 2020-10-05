package com.github.mrazjava.booklink.openlibrary;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.info.BuildProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import springfox.documentation.builders.ApiInfoBuilder;
import springfox.documentation.builders.RequestHandlerSelectors;
import springfox.documentation.service.ApiInfo;
import springfox.documentation.service.Contact;
import springfox.documentation.spi.DocumentationType;
import springfox.documentation.spring.web.plugins.Docket;

import java.util.Optional;

@Configuration
public class SwaggerConfiguration {

    @Autowired
    private Optional<BuildProperties> build;

    @Value("${info.app.description:}")
    private String appdesc;

    @Value("${swaggerhost:}")
    private String swaggerhost;

    @Bean
    public Docket internalAPI() {
        final String version = (build.isPresent()) ? build.get().getVersion() : "";
        Docket d = new Docket(DocumentationType.SWAGGER_2)
                .groupName("booklink-" + version)
                .select()
                .apis(RequestHandlerSelectors.basePackage(OpenLibraryRestApp.class.getPackageName()))
                .build()
                .directModelSubstitute(java.time.LocalDate.class, java.sql.Date.class)
                .directModelSubstitute(java.time.OffsetDateTime.class, java.util.Date.class)
                .apiInfo(generateInternalApiInfo(version, "booklink-openlibrary", "openlibrary.org query api"));

        if (StringUtils.isNotBlank(swaggerhost)) {
            d.host(swaggerhost);
        }
        return d;
    }

    private ApiInfo generateInternalApiInfo(String version, String title, String desc) {
        String description = desc;
        if (build.isPresent()) {
            BuildProperties buildInfo = build.get();
            version = buildInfo.getVersion();
        }

        if (StringUtils.isNotBlank(appdesc)) {
            description = "<b>" + appdesc + "</b>" + " <br><br>" + desc;
        }

        return new ApiInfoBuilder()
                .title(title)
                .description(description)
                .license("")
                .licenseUrl("")
                .termsOfServiceUrl("")
                .version(version)
                .contact(new Contact("mrazjava", "https://github.com/mrazjava", ""))
                .build();
    }
}
