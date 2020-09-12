package com.github.mrazjava.booklink.openlibrary.dataimport;

import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringJUnitConfig
@Import({
        OpenLibraryUrlProvider.class
})
public class OpenLibraryUrlProviderTest {

    @InjectMocks
    private OpenLibraryUrlProvider provider;


    @Test
    public void shouldProvideAuthorIdUrlTemplate() {
        assertEquals("http://covers.openlibrary.org/a/id/%s-%s.jpg", provider.getAuthorIdUrlTemplate());
    }

    @Test
    public void shouldProvideAuthorIdLargeImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/a/id/2342341-L.jpg",
                provider.getAuthorIdUrl(2342341L, ImageSize.L)
        );
    }

    @Test
    public void shouldProvideAuthorIdOriginalImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/a/id/2342341.jpg",
                provider.getAuthorIdUrl(2342341L, ImageSize.O)
        );
    }

    @Test
    public void shouldProvideAuthorOlidUrlTemplate() {
        assertEquals("http://covers.openlibrary.org/a/olid/%s-%s.jpg", provider.getAuthorOlidUrlTemplate());
    }

    @Test
    public void shouldProvideAuthorOlidMediumImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/a/olid/111222-M.jpg",
                provider.getAuthorOlidUrl(111222L, ImageSize.M)
        );
    }

    @Test
    public void shouldProvideAuthorOlidOriginalImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/a/olid/111222.jpg",
                provider.getAuthorOlidUrl(111222L, ImageSize.O)
        );
    }

    @Test
    public void shouldProvideBookIdUrlTemplate() {
        assertEquals("http://covers.openlibrary.org/b/id/%s-%s.jpg", provider.getBookIdUrlTemplate());
    }

    @Test
    public void shouldProvideBookIdSmallImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/b/id/2342341-S.jpg",
                provider.getBookIdUrl(2342341L, ImageSize.S)
        );
    }

    @Test
    public void shouldProvideBookIdOriginalImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/b/id/2342341.jpg",
                provider.getBookIdUrl(2342341L, ImageSize.O)
        );
    }

    @Test
    public void shouldProvideBookOlidUrlTemplate() {
        assertEquals("http://covers.openlibrary.org/b/olid/%s-%s.jpg", provider.getBookOlidUrlTemplate());
    }

    @Test
    public void shouldProvideBookOlidMediumImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/b/olid/98765432-M.jpg",
                provider.getBookOlidUrl(98765432L, ImageSize.M)
        );
    }

    @Test
    public void shouldProvideBookOlidOriginalImgUrl() {
        assertEquals(
                "http://covers.openlibrary.org/b/olid/98765432.jpg",
                provider.getBookOlidUrl(98765432L, ImageSize.O)
        );
    }
}
