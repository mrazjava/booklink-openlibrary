package com.github.mrazjava.booklink.openlibrary.schema;

import java.util.List;

import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

@JsonIgnoreProperties(value = {
        "m", "     _date",
        "type",
        "number_of_pages",
        "publish_date",
        "edition_name",
        "subject_place", "subject_time", // ? used in refs to actual author record (key to author provided)
        "id_wikidata", "id_viaf" // RemoteId dupes
})
@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "authors")
public class AuthorSchema extends BaseSchemaEnhanced {

    @TextIndexed(weight = 1)
    private String name;

    @JsonProperty("other_titles")
    private List<String> otherTitles;

    @JsonProperty("title_prefix")
    private String titlePrefix;

    @JsonProperty("subtitle")
    private String subTitle;

    /**
     * @see <a href="http://www.loc.gov/marc/umb/um01to06.html">What is MARC record?</a>
     */
    private List<String> marc;

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

    @TextIndexed(weight = 4)
    private String bio;

    @JsonProperty("by_statement")
    private String byStatement;

    private String website;

    @JsonProperty("website_name")
    private String websiteName;

    private String wikipedia;

    @TextIndexed(weight = 2)
    private String tags;

    @JsonProperty("id_librarything")
    private String libraryThingId;

    private Long numeration;

    @JsonIgnore
    private String numerationText;

    @JsonProperty("birth_date")
    private String birthDate;

    private String date;

    @JsonProperty("death_date")
    private String deathDate;

    @TextIndexed(weight = 5)
    private String location;

    /**
     * The URL Pattern for accessing author photos is:
     * http://covers.openlibrary.org/a/$key/$value-$size.jpg
     *
     * Where $key is OLID (prmary key of author record) or ID (numeric id from this collection). Size is S, M or L.
     *
     * Example for large image of author Pedro I Emperor of Brazil:
     * http://covers.openlibrary.org/a/olid/OL1144407A-L.jpg
     * http://covers.openlibrary.org/a/id/7244867-L.jpg
     *
     * NOTE: see openlibrary.org for their download policy!
     */
    private List<Integer> photos;

    @JsonProperty("source_records")
    private List<String> sourceRecords;

    @JsonProperty("ocaid")
    private String organisationalCultureAndIndividualDevelopment;

    private List<String> series;

    @TextIndexed(weight = 5)
    private List<String> contributions;

    @TextIndexed(weight = 6)
    private String role;

    @TextIndexed(weight = 4)
    private List<String> genres;

    @TextIndexed(weight = 6)
    private String comment;

    private TypeValue<String> notes;

    private TypeValue<String> body;

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
