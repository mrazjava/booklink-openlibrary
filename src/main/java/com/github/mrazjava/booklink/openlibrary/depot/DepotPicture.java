package com.github.mrazjava.booklink.openlibrary.depot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotPicture {

    private String id;
    private byte[] graphics;
    private long sizeBytes;
    private String sizeText;
    private String info;
}
