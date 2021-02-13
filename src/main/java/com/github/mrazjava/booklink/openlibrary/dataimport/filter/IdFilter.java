package com.github.mrazjava.booklink.openlibrary.dataimport.filter;

import java.io.File;

public interface IdFilter<T> {

    void load(File source);

    boolean isEnabled();

    boolean exists(T id);
}
