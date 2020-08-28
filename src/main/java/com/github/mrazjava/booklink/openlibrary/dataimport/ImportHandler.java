package com.github.mrazjava.booklink.openlibrary.dataimport;

public interface ImportHandler<T, R> {

    /**
     * Prepares the import process. This is a one time operation invoked prior to the
     * import.
     *
     * @param dataSource to prepare
     */
    void prepare(T dataSource);

    void handle(R record);

    R toRecord(String text);

    String toText(R record);
}
