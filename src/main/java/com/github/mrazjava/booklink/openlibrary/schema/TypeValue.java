package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TypeValue<T> {

    @JsonIgnore
    private String type;

    private T value;
}
