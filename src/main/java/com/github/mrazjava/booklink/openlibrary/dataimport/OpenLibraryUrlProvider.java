package com.github.mrazjava.booklink.openlibrary.dataimport;

import org.springframework.stereotype.Component;

@Component
public class OpenLibraryUrlProvider {

    // TODO: support configurable handling of blank image
    // By default it returns a blank image if the cover cannot be found. With:
    // ?default=false
    // appended to the end of the URL, then it returns a 404 instead.

    public static final String OPENLIBRARY_URL_IMG_TEMPLATE = "http://covers.openlibrary.org/%s/%s/%s-%s.jpg";

    private static final char KEY_AUTHOR = 'a';

    private static final char KEY_BOOK = 'b';

    private static final String KEY_ISBN = "isbn";

    private static final String KEY_ID = "id";

    private static final String KEY_OLID = "olid";

    private static final String KEY_LCCN = "lccn";

    private static final String KEY_GOODREADS = "goodreads";

    private static final String KEY_LIBRARYTHING = "librarything";

    public String getAuthorIdUrlTemplate() {
        return String.format(OPENLIBRARY_URL_IMG_TEMPLATE, KEY_AUTHOR, KEY_ID, "%s", "%s");
    }

    public String getAuthorIdUrl(Long imageId, ImageSize size) {
        return sanitizeUrl(String.format(getAuthorIdUrlTemplate(), Long.toString(imageId), size));
    }

    public String getAuthorOlidUrlTemplate() {
        return String.format(OPENLIBRARY_URL_IMG_TEMPLATE, KEY_AUTHOR, KEY_OLID, "%s", "%s");
    }

    public String getAuthorOlidUrl(Long imageId, ImageSize size) {
        return sanitizeUrl(String.format(getAuthorOlidUrlTemplate(), Long.toString(imageId), size));
    }

    public String getBookIdUrlTemplate() {
        return String.format(OPENLIBRARY_URL_IMG_TEMPLATE, KEY_BOOK, KEY_ID, "%s", "%s");
    }

    public String getBookIdUrl(Long imageId, ImageSize size) {
        return sanitizeUrl(String.format(getBookIdUrlTemplate(), Long.toString(imageId), size));
    }

    public String getBookOlidUrlTemplate() {
        return String.format(OPENLIBRARY_URL_IMG_TEMPLATE, KEY_BOOK, KEY_OLID, "%s", "%s");
    }

    public String getBookOlidUrl(Long imageId, ImageSize size) {
        return sanitizeUrl(String.format(getBookOlidUrlTemplate(), Long.toString(imageId), size));
    }

    private String sanitizeUrl(String url) {
        return url.replace(String.format("-%s", ImageSize.O), "");
    }
}
