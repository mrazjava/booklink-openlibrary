package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

public class CommonsLineIteratorProvider implements IteratorProvider<String, File> {

    @Override
    public Iterator<String> provide(File source) {
        try {
            return FileUtils.lineIterator(source, "UTF-8");
        }
        catch(IOException e) {
            throw new OpenLibraryIntegrationException("file read error", e);
        }
    }
}
