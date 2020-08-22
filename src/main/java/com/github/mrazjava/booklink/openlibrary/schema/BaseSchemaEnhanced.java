package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@JsonIgnoreProperties({"m", "type"})
@Data
abstract class BaseSchemaEnhanced extends BaseSchema {

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
}
