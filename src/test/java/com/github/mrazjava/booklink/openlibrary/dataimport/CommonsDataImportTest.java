package com.github.mrazjava.booklink.openlibrary.dataimport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;

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
        CommonsDataImport.class
})
public class CommonsDataImportTest {

    @Autowired
    private CommonsDataImport dataImport;

    @MockBean
    private IteratorProvider<String, File> iteratorProvider;

    @MockBean
    private ImportHandler handler;


    @Test
    public void shouldRunImport() {
        // TODO: implement me
    }
}
