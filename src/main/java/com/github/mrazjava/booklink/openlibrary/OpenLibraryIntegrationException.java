package com.github.mrazjava.booklink.openlibrary;

public class OpenLibraryIntegrationException extends RuntimeException {

    public OpenLibraryIntegrationException() {
    }

    public OpenLibraryIntegrationException(String message) {
        super(message);
    }

    public OpenLibraryIntegrationException(String message, Throwable cause) {
        super(message, cause);
    }

    public OpenLibraryIntegrationException(Throwable cause) {
        super(cause);
    }
}
