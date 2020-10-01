package com.github.mrazjava.booklink.openlibrary.dataimport;

public class OpenLibraryImportException extends RuntimeException {

    public OpenLibraryImportException() {
    }

    public OpenLibraryImportException(String message) {
        super(message);
    }

    public OpenLibraryImportException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenLibraryImportException(Throwable cause) {
        super(cause);
    }
}
