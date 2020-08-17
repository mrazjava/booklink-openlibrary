package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "authors")
public class AuthorSchema extends BaseSchema {

    private String name;

    @JsonProperty("personal_name")
    private String personalName;

    @JsonProperty("alternate_names")
    private List<String> alternateNames;

    private String title;

    private TypeValue<String> bio;

    private String website;

    @JsonProperty("birth_date")
    private String birthDate;

    private String date;

    @JsonProperty("death_date")
    private String deathDate;

    private List<Integer> photos;
}
