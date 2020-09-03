package com.github.mrazjava.booklink.openlibrary.schema;

import lombok.Builder;
import lombok.Data;
import org.bson.types.Binary;

@Data
@Builder
public class CoverImage {

    private String id;

    private Binary image;

    private long sizeBytes;

    private String sizeText;

    private String info;
}
