package com.github.mrazjava.booklink.openlibrary.dataimport;

import java.io.File;

public interface IdFilter {

    void load(File source);

    boolean isEnabled();

    boolean exists(String id);
}
