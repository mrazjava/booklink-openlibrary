package com.github.mrazjava.booklink.openlibrary.schema;

import org.bson.types.Binary;

import lombok.Builder;
import lombok.Data;
import lombok.ToString;

@Data
@Builder
public class CoverImage {

    private String id;

    @ToString.Exclude
    private Binary image;

    private long sizeBytes;

    private String sizeText;

    private String info;
    
    /**
     * Many valid covers are plain in appearance. If set and true, then a cover is 
     * identified as such.
     */
    private Boolean plain;
}
