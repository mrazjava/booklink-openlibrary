package com.github.mrazjava.booklink.openlibrary.depot;

import java.util.Optional;

import com.github.mrazjava.booklink.openlibrary.schema.CoverImage;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotPicture implements DepotRecord {

    private String id;
    private byte[] graphics;
    private long sizeBytes;
    private String sizeText;
    private String info;

    public DepotPicture(CoverImage coverImage) {
        id = coverImage.getId();
        Optional.ofNullable(coverImage.getImage()).ifPresent(i -> graphics = i.getData());
        sizeBytes = coverImage.getSizeBytes();
        sizeText = coverImage.getSizeText();
        info = coverImage.getInfo();
    }
}
