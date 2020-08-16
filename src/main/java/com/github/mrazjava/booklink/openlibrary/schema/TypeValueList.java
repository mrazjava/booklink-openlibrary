package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.util.List;

/**
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class TypeValueList {

    String type;

    @JsonFormat(with = JsonFormat.Feature.ACCEPT_SINGLE_VALUE_AS_ARRAY)
    List<String> value;
}
