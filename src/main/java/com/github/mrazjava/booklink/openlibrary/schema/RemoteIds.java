package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class RemoteIds {

    @JsonProperty("viaf")
    private String virtualInternationalAuthroityFile;

    private String wikidata;

    @JsonProperty("isni")
    private String internationalStandardNameIdentifier;

    @JsonAlias("id_librarything")
    @JsonProperty("librarything")
    private String libraryThing;
}
