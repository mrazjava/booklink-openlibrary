package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Transient;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.util.List;

@JsonIgnoreProperties({"m", "type"})
abstract class BaseSchema {

    @Id
    @JsonProperty("key")
    private String id;

    @JsonProperty
    private TypeValue<LocalDateTime> created;

    @JsonProperty("last_modified")
    private TypeValue<LocalDateTime> lastModified;

    @JsonProperty
    private Integer revision;

    @JsonProperty("latest_revision")
    private Integer latestRevision;

    @Transient
    @JsonAlias("lc_classification")
    @JsonProperty("lc_classifications")
    private List<String> lcClassifications;

    @JsonProperty
    private List<Link> links;

    @JsonProperty("remote_ids")
    private RemoteIds remoteIds;

    @JsonSetter("key")
    public void setKey(JsonNode jsonNode) {
        String val = jsonNode.textValue();
        this.id = val.substring(val.lastIndexOf("/")+1);
    }
}
