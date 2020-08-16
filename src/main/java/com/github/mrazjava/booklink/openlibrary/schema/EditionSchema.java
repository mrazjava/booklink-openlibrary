package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@Slf4j
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
@Document(collection = "editions")
public class EditionSchema extends BaseSchema {

    @Indexed
    private List<String> authors; // IDs

    @Indexed
    private List<String> works; // IDs

    @TextIndexed(weight = 3)
    private String title;

    @TextIndexed(weight = 3)
    @JsonProperty("full_title")
    private String fullTitle;

    @JsonProperty("edition_name")
    private String editionName;

    private String publisher;

    @JsonProperty("number_of_pages")
    @JsonAlias("numer_of_pages")
    private Integer numberOfPages;

    @JsonProperty("coverid")
    private String coverId;

    @TextIndexed(weight = 5)
    private String description;

    private List<String> isbn;

    @JsonProperty("original_isbn")
    private String originalIsbn;

    @JsonProperty("physical_format")
    private String physicalFormat;

    private String price;

    private Key edition;

    private String code;

    private List<String> oclc;

    @JsonProperty("openlibrary")
    private String openLibrary;

    private List<String> publishers;

    private TypeValue created;

    private TypeValue stats;

    private TypeValue news;

    @JsonProperty("remote_ids")
    private RemoteIds remoteIds;

    private Dimensions dimensions;

    private TypeValue body;

    @JsonProperty("bookweight")
    private Weight bookWeight;

    @JsonProperty("isbn_10")
    private List<String> isbn10;

    @JsonProperty("isbn_13")
    private List<String> isbn13;

    @JsonProperty("isbn_invalid")
    private List<String> isbnInvalid;

    @JsonProperty("isbn_odd_length")
    private List<String> isbnOddLength;

    @JsonProperty("library_of_congress_name")
    private String libraryOfCongressName;

    @JsonProperty("purchase_url")
    private List<String> purchaseUrl;

    @JsonProperty("copyright_date")
    private String copyrightDate;

    @Transient
    @JsonIgnore
    private Object classifications;

    private TypeValue macro;

    private List<String> contributions;

    private List<String> collections;

    @JsonProperty("uri_descriptions")
    private List<String> uriDescriptions;

    private List<String> uris;

    private List<String> url;

    @JsonProperty("download_url")
    private List<String> downloadUrl;

    private String name;

    private String create;

    private String ocaid;

    private String pagination;

    @JsonProperty("birth_date")
    private String birthDate;

    @JsonProperty("publish_date")
    private String publishDate;

    @JsonAlias("subject_time")
    @JsonProperty("subject_times")
    private List<String> subjectTimes;

    private String firstSentence;

    private List<String> iaLoadedIds;

    private List<String> iaBoxIds;

    @JsonProperty("local_id")
    private List<String> localId;

    @JsonProperty("ia_id")
    private String iaId;

    private List<String> location;

    private String weight;

    @JsonProperty("physical_dimensions")
    private String physicalDimentions;

    @JsonProperty("by_statement")
    private String byStatement;

    @JsonAlias("subject_place")
    @JsonProperty("subject_places")
    private List<String> subjectPlaces;

    @JsonProperty("subject_people")
    private List<String> subjectPeople;

    @JsonProperty("publish_places")
    private List<String> publishPlaces;

    @JsonProperty("publish_country")
    private String publishCountry;

    @JsonProperty("dewey_decimal_class")
    private List<String> deweyDecimalClass;

    private List<String> genres;

    private List<String> lccn;

    @JsonProperty("source_records")
    private List<String> sourceRecords;

    @JsonProperty("author_names")
    private List<String> authorNames;

    private List<Link> links;

    @JsonAlias("work_title")
    @JsonProperty("work_titles")
    private List<String> workTitles;

    @JsonProperty("title_prefix")
    private String titlePrefix;

    @JsonProperty("other_titles")
    private List<String> otherTitles;

    private String subtitle;

    @JsonProperty("coverimage")
    private String coverImage;

    @JsonProperty("scan_records")
    private List<Key> scanRecords;

    @JsonProperty("scan_on_demand")
    private Boolean scanOnDemand;

    private String notes;

    private List<Long> covers;

    @JsonProperty("translated_from")
    private List<Key> translatedFrom;

    @JsonProperty("translation_of")
    private String translationOf;

    private List<String> series;

    @JsonAlias("oclc_number")
    @JsonProperty("oclc_numbers")
    private List<String> oclcNumbers;

    @JsonAlias("language")
    private List<String> languages;

    @JsonProperty("language_code")
    private String languageCode;

    @TextIndexed(weight = 1)
    private List<String> subjects;

    private Identifiers identifiers;

    @JsonProperty("by_statements")
    private String byStatements;

    private List<Contributor> contributors;

    @JsonProperty("table_of_contents")
    private List<TableOfContent> toc;

    private List<Volume> volumes;

    @JsonProperty("volume_number")
    private Integer volumeNumber;


    @JsonSetter("languages")
    public void setLanguages(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(languages)) {
                languages = new LinkedList<>();
            }
            if(!json.isArray()) {
                languages.add(fetchKey(json));
            }
            else {
                for(JsonNode jn : json) {
                    languages.add(fetchKey(jn));
                }
            }
        }
    }

    @JsonSetter("works")
    public void setWorks(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(works)) {
                works = new LinkedList<>();
            }
            if(!json.isArray()) {
                works.add(fetchKey(json));
            }
            else {
                for(JsonNode jn : json) {
                    works.add(fetchKey(jn));
                }
            }
        }
    }

    @JsonSetter("collections")
    public void setCollections(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(collections)) {
                collections = new LinkedList<>();
            }
            if(!json.isArray()) {
                collections.add(fetchKey(json));
            }
            else {
                for(JsonNode jn : json) {
                    collections.add(fetchKey(jn));
                }
            }
        }
    }

    @JsonSetter("authors")
    public void setAuthors(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(authors)) {
                authors = new LinkedList<>();
            }
            if(!json.isArray()) {
                authors.add(fetchKey(json));
            }
            else {
                for(JsonNode jn : json) {
                    authors.add(fetchKey(jn));
                }
            }
        }
    }

    private String fetchKey(JsonNode json) {
        String text = json.has("key") ? json.get("key").asText() : json.asText();
        return text.contains("/") ? text.substring(text.lastIndexOf("/") + 1) : text;
    }

    @JsonSetter("notes")
    public void setJsonNotes(JsonNode json) {
        if(json != null) {
            String text;
            if (json.isTextual()) {
                text = json.asText();
            } else {
                text = json.get("value").asText();
            }
            notes = text;
        }
    }

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
        if(json != null) {
            String text;
            if (json.isTextual()) {
                text = json.asText();
            } else {
                text = json.get("value").asText();
            }
            firstSentence = text;
        }
    }

    @JsonSetter("ia_loaded_id")
    public void setIaLoadedIds(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(iaLoadedIds)) {
                iaLoadedIds = new LinkedList<>();
            }
            if(json.isTextual()) {
                iaLoadedIds.add(json.asText());
            }
            else {
                for (JsonNode jn : json) {
                    iaLoadedIds.add(jn.asText());
                }
            }
        }
    }

    @JsonSetter("ia_box_id")
    public void setIaBoxIds(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(iaBoxIds)) {
                iaBoxIds = new LinkedList<>();
            }
            if(json.isTextual()) {
                iaBoxIds.add(json.asText());
            }
            else {
                for (JsonNode jn : json) {
                    iaBoxIds.add(jn.asText());
                }
            }
        }
    }

    @JsonSetter("toc")
    public void setJsonExcerpts(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(toc)) {
                toc = new LinkedList<>();
            }
            for (JsonNode jn : json) {
                toc.add(jn.isTextual() ? new TableOfContent(jn.asText()) : produceToc(jn));
            }
        }
    }

    private TableOfContent produceToc(JsonNode json) {

        TableOfContent toc = new TableOfContent();

        if(json.has("class")) {
            toc.setClazz(json.get("class").asText());
        }
        if(json.has("label")) {
            toc.setLabel(json.get("label").asText());
        }
        if(json.has("label")) {
            toc.setLevel(Integer.valueOf(json.get("label").asText()));
        }
        if(json.has("pagenum")) {
            toc.setLabel(json.get("pagenum").asText());
        }
        if(json.has("title")) {
            toc.setLabel(json.get("title").asText());
        }
        if(json.has("key")) {
            Key type = new Key();
            type.setKey(json.get("title").asText());
            toc.setType(type);
        }
        if(json.has("value")) {
            toc.setValue(json.get("value").asText());
        }

        return toc;
    }
}
