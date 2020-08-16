package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Excerpt extends TypeValue {

    String excerpt;

    String pages;

    String page;

    String comment;

    Key author;
}
