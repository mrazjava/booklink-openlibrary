package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.springframework.stereotype.Component;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.util.Iterator;

@Slf4j
@Component
public class CommonsLineIteratorProvider implements IteratorProvider<String, File> {

    @Override
    public LineIterator open(File source) {
        try {
            return FileUtils.lineIterator(source, "UTF-8");
        }
        catch(IOException e) {
            throw new OpenLibraryImportException("file read error", e);
        }
    }

    @Override
    public Iterator<String> provide(Closeable closeable) {
        return (LineIterator)closeable;
    }
}
