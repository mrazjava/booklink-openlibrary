package com.github.mrazjava.booklink.openlibrary.dataimport;

import java.io.File;

public interface FileImporter {

    void runImport(File jsonFile, Class schema);
}
