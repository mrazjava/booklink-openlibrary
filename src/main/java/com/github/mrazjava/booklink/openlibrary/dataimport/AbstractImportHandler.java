package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;
import java.util.Set;

@Slf4j
abstract class AbstractImportHandler<R> implements ImportHandler<File, R> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected OpenLibraryUrlProvider urlProvider;

    @Value("${booklink.di.persist}")
    protected boolean persistData;

    @Value("${booklink.di.persist-override}")
    protected boolean persistDataOverride;

    @Value("${booklink.di.image-download}")
    protected Boolean downloadImages;

    @Value("${booklink.di.image-dir}")
    protected String imageDir;

    @Value("${booklink.di.image-mongo}")
    protected Boolean storeImagesInMongo;

    @Value("${booklink.di.frequency-check}")
    protected int frequencyCheck;

    protected int savedCount = 0;

    @Autowired
    protected ImageDownloader imageDownloader;

    protected File imageDirectoryLocation;


    @Override
    public R toRecord(String line) {

        try {
            return objectMapper.readValue(line, getSchemaType());
        } catch (JsonProcessingException e) {
            log.warn("failed line:\n{}", line);
            throw new OpenLibraryIntegrationException("problem reading record", e);
        }
    }

    @Override
    public String toText(R record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            log.warn("failed record:\n{}", record);
            throw new OpenLibraryIntegrationException("problem writing record", e);
        }
    }

    @Override
    public void conclude(File dataSource) {
        if(log.isInfoEnabled()) {
            Set<String> failedDownloads = imageDownloader.getFailedImageDownloads();
            StringBuilder urls = new StringBuilder();
            failedDownloads.stream().forEach(url -> urls.append(url + "\n"));
            log.info("{} failed downloads:\n{}", failedDownloads.size(), urls.toString());
        }
    }

    protected abstract Class<R> getSchemaType();
}
