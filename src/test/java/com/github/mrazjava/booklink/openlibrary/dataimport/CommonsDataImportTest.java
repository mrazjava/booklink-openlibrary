package com.github.mrazjava.booklink.openlibrary.dataimport;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.junit.jupiter.SpringJUnitConfig;

import java.io.File;

@BooklinkTestPropertySource
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
