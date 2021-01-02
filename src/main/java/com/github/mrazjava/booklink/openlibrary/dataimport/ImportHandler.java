package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;

/**
 * @param <T> format of data source from which to import
 * @param <R> record schema
 */
public interface ImportHandler<T, R extends BaseSchema> {

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

    void conclude(T dataSource);

    R toRecord(String text);

    String toText(R record);
}
