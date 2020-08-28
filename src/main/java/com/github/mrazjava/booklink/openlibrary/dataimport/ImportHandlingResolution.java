package com.github.mrazjava.booklink.openlibrary.dataimport;

public interface ImportHandlingResolution {

    ImportHandler resolve(Class schema);

    boolean isAuthor(Class schema);

    boolean isWork(Class schema);

    boolean isEdition(Class schema);
}
