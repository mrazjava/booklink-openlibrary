package com.github.mrazjava.booklink.openlibrary.schema;

import java.util.LinkedList;
import java.util.List;

import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.util.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Document(collection = "editions")
public class EditionSchema extends BaseSchemaEnhanced {

    @TextIndexed(weight = 3)
    private String title;

    /**
     * short beginning of title, without special characters (used for sorting, preview, etc)
     */
    @JsonProperty("title_sample")
    private String titleSample;

    @TextIndexed(weight = 3)
    @JsonProperty("full_title")
    private String fullTitle;
    
    /**
     * short beginning of fullTitle, without special characters (used for sorting, preview, etc)
     */
    @JsonProperty("full_title_sample")
    private String fullTitleSample;

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
    
    @JsonProperty("displayname")
    private String displayName;

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

    private TypeValue<String> stats;

    private TypeValue<String> news;

    private Dimensions dimensions;

    private TypeValue<String> body;

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

    private TypeValue<String> macro;

    private List<String> contributions;

    private List<String> collections;

    @JsonProperty("uri_descriptions")
    private List<String> uriDescriptions;

    private List<String> uris;

    private List<String> url;

    @JsonProperty("download_url")
    private List<String> downloadUrl;
    
    private List<String> website;

    private String name;

    private String create;

    private String ocaid;

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

    private List<String> genres;

    @JsonProperty("source_records")
    private List<String> sourceRecords;

    @JsonProperty("author_names")
    private List<String> authorNames;
    
    @JsonProperty("personal_name")
    private String personalName;
    
    @JsonProperty("alternate_names")
    private List<String> authorAlternateNames;

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

    @JsonProperty("translated_from")
    private List<Key> translatedFrom;

    @JsonProperty("translation_of")
    private String translationOf;

    private List<String> series;

    @JsonProperty("language_code")
    private String languageCode;

    private Identifiers identifiers;

    @JsonProperty("by_statements")
    private String byStatements;

    private List<Contributor> contributors;

    @JsonProperty("table_of_contents")
    private List<TableOfContent> toc;

    private List<Volume> volumes;

    @JsonProperty("volume_number")
    private Integer volumeNumber;


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

    @JsonSetter
    public void setWebsite(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(website)) {
                website = new LinkedList<>();
            }
            if(json.isTextual()) {
                website.add(json.asText());
            }
            else {
                for (JsonNode jn : json) {
                    website.add(jn.asText());
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
