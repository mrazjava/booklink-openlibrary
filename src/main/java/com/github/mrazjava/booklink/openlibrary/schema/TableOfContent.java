package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.*;
import lombok.Data;
import lombok.NoArgsConstructor;

@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor
@Data
public class TableOfContent {

    @JsonIgnore
    private Key type;

    private String pagenum;

    private String title;

    private String label;

    @JsonProperty("class")
    private String clazz;

    private Integer level;

    @JsonProperty("tocpage")
    private Integer tocPage;

    @JsonSetter("value")
    void setValue(String value) {
        title = value;
    }


    public TableOfContent(String value) {
        this.title = value;
    }
}
