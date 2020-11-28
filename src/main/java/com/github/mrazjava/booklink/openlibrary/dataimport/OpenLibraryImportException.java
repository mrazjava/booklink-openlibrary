package com.github.mrazjava.booklink.openlibrary.dataimport;

public class OpenLibraryImportException extends RuntimeException {

	private static final long serialVersionUID = -7292175727676801442L;

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
