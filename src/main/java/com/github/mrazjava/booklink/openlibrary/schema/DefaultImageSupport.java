package com.github.mrazjava.booklink.openlibrary.schema;


import com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize;

import java.util.List;

public interface DefaultImageSupport {

    String getId();

    List<Long> getCovers();

    void setImage(CoverImage image, ImageSize size);

    boolean hasImage(ImageSize size);
}
