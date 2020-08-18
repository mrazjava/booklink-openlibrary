package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "authors")
public class AuthorSchema extends BaseSchema {

    @TextIndexed(weight = 1)
    private String name;

    @Indexed
    private List<Key> authors;

    @Indexed
    private List<Key> works;

    @TextIndexed(weight = 2)
    @JsonProperty("fuller_name")
    private String fullName;

    @TextIndexed(weight = 3)
    @JsonProperty("personal_name")
    private String personalName;

    @TextIndexed(weight = 3)
    @JsonProperty("alternate_names")
    private List<String> alternateNames;

    @JsonProperty("entity_type")
    private String type;

    @JsonProperty("create")
    private CreateStrategy createStrategy;

    private String title;

    private String bio;

    private String website;

    private String wikipedia;

    private Long numeration;

    @JsonProperty("edition_name")
    private String editionName;

    @JsonIgnore
    private String numerationText;

    @JsonProperty("birth_date")
    private String birthDate;

    private String date;

    @JsonProperty("death_date")
    private String deathDate;

    private String location;

    private List<Integer> photos;

    @JsonProperty("source_records")
    private List<String> sourceRecords;

    @JsonProperty("ocaid")
    private String organisationalCultureAndIndividualDevelopment;

    private List<String> series;

    private String role;

    private String comment;

    /**
     * Eg: /static/files//599/OL4278213A_photograph_1218383628520599.jpg
     */
    private String photograph;

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

    @JsonSetter("numeration")
    public void setJsonNumeration(JsonNode json) {
        if(json != null) {
            if (json.isTextual()) {
                numerationText = json.asText();
                numeration = 0L;
            } else {
                numeration = NumberUtils.createLong(json.get("value").asText());
            }
        }
    }
}
