package com.github.mrazjava.booklink.openlibrary.dataimport;

import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.L;
import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.M;
import static com.github.mrazjava.booklink.openlibrary.dataimport.ImageSize.S;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrazjava.booklink.openlibrary.dataimport.filter.AuthorIdFilter;
import com.github.mrazjava.booklink.openlibrary.dataimport.filter.AuthorImgExclusionFilter;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import com.github.mrazjava.booklink.openlibrary.schema.CoverImage;


@BooklinkTestPropertySource
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
    private ImageDownloader imageDownloader;

    @MockBean
    private AuthorIdFilter authorIdFilter;

    @MockBean
    private AuthorImgExclusionFilter authorImgExclusionFilter;

    @MockBean
    private AuthorRepository authorRepository;

    @MockBean
    private SampleAuthorTracker sampleAuthorTracker;

    @BeforeEach
    public void prepare() throws IOException {
        handler.persistData = handler.imagePull = handler.withMongoImages = false;
        handler.persistDataOverride = true;
        handler.imageDir = null;
        handler.imageDirectoryLocation = null;
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

    @Test
    public void shouldNotPullImagesWithEmptyPhotosCollection() {

        AuthorSchema record = mock(AuthorSchema.class);
        String id = "foo-bar";

        handler.imagePull = true;

        when(record.getId()).thenReturn(id);
        verify(imageDownloader, times(1)).setImageIdFilter(any());
        verify(imageDownloader, times(1)).setThrottleMs(anyLong());

        handler.handle(record, 1);

        verify(record, times(1)).getPhotos();
        verifyNoMoreInteractions(imageDownloader);
    }

    @Test
    public void shouldPullImages() throws IOException {

        AuthorSchema record = mock(AuthorSchema.class);
        String id = "foo-bar";
        Long imgId = 6746285L;
        @SuppressWarnings("unchecked") Map<ImageSize, byte[]> pulledImageMocks = mock(Map.class);
		CoverImage mockImage = mock(CoverImage.class);

        handler.imagePull = handler.withMongoImages = true;
        handler.imageDir = "/tmp/test"; // for the test purposes value irrelevant so long not blank
        handler.imageDirectoryLocation = new File(handler.imageDir);

        when(record.getId()).thenReturn(id);
        when(record.getPhotos()).thenReturn(List.of(imgId.intValue()));
        when(record.getImageSmall()).thenReturn(mockImage);
        when(record.getImageMedium()).thenReturn(mockImage);
        when(record.getImageLarge()).thenReturn(mockImage);
        when(pulledImageMocks.containsKey(S)).thenReturn(false);
        when(pulledImageMocks.containsKey(M)).thenReturn(false);
        when(pulledImageMocks.containsKey(L)).thenReturn(false);
        when(imageDownloader.downloadImageFiles(eq(handler.imageDir), eq(imgId), any())).thenReturn(pulledImageMocks);

        handler.handle(record, 1);

        verify(record, times(2)).getPhotos();
        verify(imageDownloader, times(1)).downloadImageFiles(
                eq(handler.imageDir), eq(imgId), any()
        );
        verify(imageDownloader, times(1)).downloadImageToBinary(
                eq(imgId), any(), eq(record), eq(pulledImageMocks)
        );
        verify(pulledImageMocks, times(3)).containsKey(any());
        verify(record, times(1)).getImageSmall();
        verify(record, times(2)).getImageMedium();
        verify(record, times(2)).getImageLarge();
    }
}
