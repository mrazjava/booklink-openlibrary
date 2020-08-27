package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.*;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@Data
@Document(collection = "works")
public class WorkSchema extends BaseSchema {

    @TextIndexed(weight = 2)
    private String title;

    @TextIndexed(weight = 2)
    private String subtitle;

    @JsonProperty("number_of_editions")
    private Integer numberOfEditions;

    private Notifications notifications;

    @JsonProperty("subject_places")
    private List<String> subjectPlaces;

    @JsonProperty("subject_people")
    private List<String> subjectPeople;

    @JsonProperty("subject_times")
    private List<String> subjectTimes;

    private Series series;

    @TextIndexed(weight = 3)
    @JsonProperty("other_titles")
    private List<String> otherTitles;

    private List<String> genres;

    @JsonProperty("original_languages")
    private List<Key> originalLanguages;

    @JsonProperty("first_publish_date") // can be a date or just a year
    private String firstPublishDate;

    @JsonProperty("cover_edition")
    private Key coverEdition;

    @TextIndexed(weight = 5)
    private String description;

    @Getter(AccessLevel.NONE)
    @Setter(AccessLevel.NONE)
    private String firstSentence;

    private String ospid;

    private TypeValue notes;

    private List<Excerpt> excerpts;

    private String location;

    private Key permission;

    @JsonProperty("translated_titles")
    private List<TranslatedTitle> translatedTitles;

    @JsonProperty("table_of_contents")
    private List<TableOfContent> toc;


    @JsonSetter("description")
    public void setJsonDescription(JsonNode json) {
        if(json != null) {
            String text;
            if (json.isTextual()) {
                text = json.asText();
            } else {
                text = json.get("value").asText();
            }
            description = text;
        }
    }

    @JsonSetter("first_sentence")
    public void setJsonFirstSentence(JsonNode json) {
        if (json != null) {
            String text;
            if (json.isTextual()) {
                text = json.asText();
            } else {
                text = json.get("value").asText();
            }
            firstSentence = text;
        }
    }

    @JsonGetter("first_sentence")
    public String getFirstSentence() {
        return firstSentence;
    }

    @JsonSetter("excerpts")
    public void setJsonExcerpts(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(excerpts)) {
                excerpts = new LinkedList<>();
            }
            if(json.has("")) {
                JsonNode babol = json.get("");
                for(int b = 0; b < babol.size(); b++) {
                    JsonNode jsonExcerpt = babol.get(b);
                    excerpts.add(produceExcerpt(jsonExcerpt));
                }
            }
            else {
                for (int x = 0; x < json.size(); x++) {
                    excerpts.add(produceExcerpt(json.get(x)));
                }
            }
        }
    }

    private Excerpt produceExcerpt(JsonNode json) {

        Excerpt excerpt = new Excerpt();

        if(json.has("excerpt")) {
            excerpt.setExcerpt(json.get("excerpt").asText());
        }
        if(json.has("type")) {
            excerpt.setType(json.get("type").asText());
        }
        if(json.has("value")) {
            excerpt.setValue(json.get("value").asText());
        }
        if(json.has("pages")) {
            excerpt.setPages(json.get("pages").asText());
        }
        if(json.has("page")) {
            excerpt.setPage(json.get("page").asText());
        }
        if(json.has("author")) {
            Key author = new Key();
            author.setKey(json.get("author").asText());
            excerpt.setAuthor(author);
        }
        if(json.has("comment")) {
            excerpt.setComment(json.get("comment").asText());
        }

        return excerpt;
    }
}
