package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.Closeable;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@Slf4j
@BooklinkTestPropertySource
@SpringJUnitConfig
@Import({
        FileDataImport.class
})
public class FileDataImportTest {

    @Autowired
    private FileDataImport importer;

    @MockBean
    private IteratorProvider<String, File> iteratorProvider;

    @MockBean
    private ImportHandler handler;


    @Test
    public void shouldIterateSource() {

        Closeable closeable = mock(Closeable.class);
        Iterator<String> iterator = mock(Iterator.class);
        File jsonSource = mock(File.class);
        Object record = mock(Object.class);
        String[] mockSource = new String[]{"foo", "abc", "bar"};

        when(jsonSource.getParentFile()).thenReturn(jsonSource);
        when(iteratorProvider.open(eq(jsonSource))).thenReturn(closeable);
        when(iteratorProvider.provide(eq(closeable))).thenReturn(iterator);
        when(iterator.hasNext()).thenReturn(true, true, true, false);
        when(iterator.next()).thenReturn(mockSource[0], mockSource[1], mockSource[2]);
        when(handler.toRecord(anyString())).thenReturn(record);
        when(handler.toText(eq(record))).thenReturn("{MOCK}");

        importer.setFrequencyCheck(1);
        importer.runImport(jsonSource);

        ArgumentCaptor<Long> counterCaptor = ArgumentCaptor.forClass(Long.class);
        ArgumentCaptor<String> handledLinesCaptor = ArgumentCaptor.forClass(String.class);

        verify(handler, times(1)).prepare(eq(jsonSource));
        verify(handler, times(3)).toRecord(handledLinesCaptor.capture());
        verify(handler, times(3)).handle(eq(record), counterCaptor.capture());
        verify(handler, times(1)).conclude(eq(jsonSource));

        assertEquals(counterCaptor.getAllValues(), List.of(1L, 2L, 3L));
        assertEquals(handledLinesCaptor.getAllValues(), List.of(mockSource));
    }
}
