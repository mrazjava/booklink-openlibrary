package com.github.mrazjava.booklink.openlibrary.schema;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import lombok.Data;
import org.apache.commons.lang3.BooleanUtils;

@JsonInclude(JsonInclude.Include.NON_NULL)
@Data
public class Notifications {

    private Boolean updates;

    @JsonProperty("public_readlog")
    private Boolean publicReadLog;

    @JsonSetter("updates")
    private void setUpdates(String updates) {
        this.updates = BooleanUtils.toBoolean(updates);
    }

    @JsonSetter("public_readlog")
    public void setPublicReadLog(String publicReadLog) {
        this.publicReadLog = BooleanUtils.toBoolean(publicReadLog);
    }
}
