package com.github.mrazjava.booklink.openlibrary.dataimport;

public interface ImportHandler<T> {

    /**
     * Prepares the import process. This is a one time operation invoked prior to the
     * import.
     *
     * @param dataSource to prepare
     */
    void prepare(T dataSource);

    void processRecord(String line);
}
