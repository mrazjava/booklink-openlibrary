package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.util.CollectionUtils;

import java.util.LinkedList;
import java.util.List;

@JsonIgnoreProperties({"m", "type"})
@Data
abstract class BaseSchemaEnhanced extends BaseSchema {

    @Indexed // IDs
    private List<String> works;

    @JsonAlias("oclc_number")
    @JsonProperty("oclc_numbers")
    private List<String> oclcNumbers;

    @JsonProperty
    private List<String> publishers;

    @JsonProperty("publish_country")
    private String publishCountry;

    @JsonProperty("publish_places")
    private List<String> publishPlaces;

    @JsonProperty
    private String pagination;


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
}