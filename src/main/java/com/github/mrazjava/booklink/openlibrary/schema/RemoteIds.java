package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RemoteIds {

    @JsonProperty("viaf")
    String virtualInternationalAuthroityFile;

    String wikidata;

    @JsonProperty("isni")
    String internationalStandardNameIdentifier;
}
