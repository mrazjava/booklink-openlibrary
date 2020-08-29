package com.github.mrazjava.booklink.openlibrary.dataimport;

public interface ImportHandler<T, R> {

    /**
     * Prepares the import process. This is a one time operation invoked prior to the
     * import.
     *
     * @param dataSource to prepare
     */
    void prepare(T dataSource);

    /**
     * @param record to handle
     * @param sequenceNo number in the sequence in which it is handled
     */
    void handle(R record, long sequenceNo);

    R toRecord(String text);

    String toText(R record);
}
