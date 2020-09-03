package com.github.mrazjava.booklink.openlibrary.schema;


import com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize;

public interface DefaultImageSupport {

    void setImage(CoverImage image, ImageSize size);

    boolean hasImage(ImageSize size);
}
