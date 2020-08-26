package com.github.mrazjava.booklink.openlibrary.schema;


public interface DefaultImageSupport {

    void setSmallImage(byte[] image);
    boolean hasSmallImage();

    void setMediumImage(byte[] image);
    boolean hasMediumImage();

    void setLargeImage(byte[] image);
    boolean hasLargeImage();
}
