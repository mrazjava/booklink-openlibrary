package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties({"m", "type"})
abstract class BaseSchema {

    @Id
    @JsonProperty("key")
    private String id;

    @JsonProperty("last_modified")
    private TypeValue<OffsetDateTime> lastModified;

    @JsonProperty
    private Integer revision;

    @JsonProperty("latest_revision")
    private Integer latestRevision;

    @Transient
    @JsonAlias("lc_classification")
    @JsonProperty("lc_classifications")
    private List<String> lcClassifications;

    @JsonSetter("key")
    public void setKey(JsonNode jsonNode) {
        String val = jsonNode.textValue();
        this.id = val.substring(val.lastIndexOf("/")+1);
    }
}
