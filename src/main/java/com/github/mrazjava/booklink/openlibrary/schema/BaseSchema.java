package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

@JsonIgnoreProperties(value = {"m", "type"})
@Data
public abstract class BaseSchema {

    @Id
    @JsonProperty
    private String id;

    @Indexed // IDs
    private Set<String> authors;

    @JsonProperty
    private TypeValue<LocalDateTime> created;

    @JsonProperty("last_modified")
    private TypeValue<LocalDateTime> lastModified;

    @JsonProperty
    private Integer revision;

    @JsonProperty("latest_revision")
    private Integer latestRevision;

    @JsonProperty
    @TextIndexed(weight = 1)
    private List<String> subjects;

    @Transient
    @JsonAlias({"lc_classification", "lccn"})
    @JsonProperty("lc_classifications")
    private List<String> lcClassifications;

    /**
     * https://en.wikipedia.org/wiki/List_of_Dewey_Decimal_classes
     */
    @JsonAlias("dewey_number")
    @JsonProperty("dewey_decimal_class")
    private List<String> deweyClassification;

    @JsonProperty
    @Indexed
    private List<Long> covers;

    @JsonProperty
    private List<Link> links;

    @JsonProperty("remote_ids")
    private RemoteIds remoteIds;

    @JsonAlias("language")
    @JsonProperty
    private List<String> languages;


    @JsonSetter("authors")
    public void setAuthors(JsonNode json) {
        if(json != null) {
            if(CollectionUtils.isEmpty(authors)) {
                authors = new LinkedHashSet<>();
            }
            if(!json.isArray()) {
                authors.add(fetchKey(json));
            }
            else {
                for(JsonNode jn : json) {
                    if(jn.has("author")) {
                        jn = jn.get("author");
                    }
                    authors.add(fetchKey(jn));
                }
            }
        }
    }

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

    @JsonSetter("key")
    public void setKey(JsonNode jsonNode) {
        String val = jsonNode.textValue();
        this.id = val.substring(val.lastIndexOf("/")+1);
    }

    protected String fetchKey(JsonNode json) {
        String text = json.has("key") ? json.get("key").asText() : json.asText();
        return text.contains("/") ? text.substring(text.lastIndexOf("/") + 1) : text;
    }
}
