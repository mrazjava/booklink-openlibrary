package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Volume {

    @JsonProperty("ia_id")
    private String iaId;

    private Key type;

    @JsonProperty("volume_number")
    private Integer number;
}
