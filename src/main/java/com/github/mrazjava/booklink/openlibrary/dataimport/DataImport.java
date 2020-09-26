package com.github.mrazjava.booklink.openlibrary.dataimport;

public interface DataImport<T> {

    void runImport(T source);
}
