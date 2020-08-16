package com.github.mrazjava.booklink.openlibrary.dataimport;

import java.io.File;

public interface FileImporter {

    void runImport(File jsonFile, Class schema);

    /**
     * For large files, it may be desirable to every so often perform some processing.
     * For example, if total rows is 1000, setting this value to 100, would instruct
     * the importer to perform some action ten times after each batch of 100 records
     * processed.
     *
     * @param rowsProcessed after which perform some action such as log output, measure time, etc.
     */
    void setFrequencyCheck(int rowsProcessed);
}
