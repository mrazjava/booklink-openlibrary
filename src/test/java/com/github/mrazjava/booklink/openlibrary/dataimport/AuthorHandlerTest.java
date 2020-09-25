package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.IOException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@TestPropertySource(properties = {
        "booklink.di.start-from-record-no: 0",
        "booklink.di.frequency-check: 20",
        "booklink.di.persist: false",
        "booklink.di.persist-override: true",
        "booklink.di.image-pull: false",
        "booklink.di.with-mongo-images: false",
        "booklink.di.fetch-original-images: false"
})
@SpringJUnitConfig
@Import({
        AuthorHandler.class
})
public class AuthorHandlerTest {

    @Autowired
    private AuthorHandler handler;

    @MockBean
    private ObjectMapper objectMapper;

    @MockBean
    private OpenLibraryUrlProvider urlProvider;

    @MockBean
    private ImageDownloader imgDownloader;

    @MockBean
    private AuthorIdFilter authorIdFilter;

    @MockBean
    private AuthorImgExclusionFilter authorImgExclusionFilter;

    @MockBean
    private AuthorRepository authorRepository;


    @BeforeEach
    public void prepare() throws IOException {
        handler.persistData = false;
        handler.persistDataOverride = true;
        handler.authorMatchCount = handler.savedCount = 0;
        handler.prepare((new ClassPathResource("openlibrary/samples/authors-tail-n1000.json")).getFile());
    }

    @Test
    public void shouldHandleDefaults() {

        handler.handle(new AuthorSchema(), 1);
        verifyNoInteractions(authorRepository);
    }

    @Test
    public void shouldSave() {

        handler.persistData = true;
        assertEquals(0, handler.savedCount);

        handler.handle(new AuthorSchema(), 1);

        verify(authorRepository, times(1)).save(any());
        assertEquals(1, handler.savedCount);
    }

    @Test
    public void shouldMatchAndSave() {

        AuthorSchema record = mock(AuthorSchema.class);
        String id = "foo";

        assertEquals(0, handler.authorMatchCount);

        handler.persistData = true;
        when(authorIdFilter.isEnabled()).thenReturn(true);
        when(authorIdFilter.exists(eq(id))).thenReturn(true);
        when(record.getId()).thenReturn(id);

        handler.handle(record, 1);

        assertEquals(1, handler.authorMatchCount);
        verify(authorRepository, times(1)).save(any());
    }

    @Test
    public void shouldFilterAndIgnore() {

        when(authorIdFilter.isEnabled()).thenReturn(true);
        when(authorIdFilter.exists(any())).thenReturn(false);

        handler.handle(new AuthorSchema(), 1);

        assertEquals(0, handler.authorMatchCount);
        assertEquals(0, handler.savedCount);
        verifyNoInteractions(authorRepository);
    }

    @Test
    public void shouldNotOverride() {

        AuthorSchema record = mock(AuthorSchema.class);
        String id = "bar";

        handler.persistData = true;
        handler.persistDataOverride = false;
        when(record.getId()).thenReturn(id);
        when(authorRepository.findById(eq(id))).thenReturn(Optional.of(record));

        handler.handle(record, 1);

        assertEquals(0, handler.savedCount);
        verify(authorRepository, times(0)).save(eq(record));
    }
}
