package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import org.springframework.data.mongodb.core.index.Indexed;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Series {

    @Indexed
    private String work;

    private String name;
}
