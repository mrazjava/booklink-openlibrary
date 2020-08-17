package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
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

    @JsonProperty("fuller_name")
    private String fullName;

    @JsonProperty("personal_name")
    private String personalName;

    @JsonProperty("alternate_names")
    private List<String> alternateNames;

    @JsonProperty("entity_type")
    private String type;

    private String title;

    private String bio;

    private String website;

    private String wikipedia;

    @JsonProperty("birth_date")
    private String birthDate;

    private String date;

    @JsonProperty("death_date")
    private String deathDate;

    private String location;

    private List<Integer> photos;

    @JsonSetter("bio")
    public void setJsonBio(JsonNode json) {
        if(json != null) {
            String text;
            if (json.isTextual()) {
                text = json.asText();
            } else {
                text = json.get("value").asText();
            }
            bio = text;
        }
    }
}
