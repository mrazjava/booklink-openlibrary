package com.github.mrazjava.booklink.openlibrary.dataimport;

import static java.util.Optional.ofNullable;

import java.io.File;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.mrazjava.booklink.openlibrary.dataimport.filter.AuthorIdInclusionFilter;
import com.github.mrazjava.booklink.openlibrary.dataimport.filter.AuthorImgExclusionFilter;
import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;

import lombok.extern.slf4j.Slf4j;

@Slf4j
abstract class AbstractImportHandler<R extends BaseSchema> implements ImportHandler<File, R> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Autowired
    protected OpenLibraryUrlProvider urlProvider;

    @Value("${booklink.di.persist}")
    protected boolean persistData;

    @Value("${booklink.di.persist-override}")
    protected boolean persistDataOverride;

    @Value("${booklink.di.image-pull}")
    protected Boolean imagePull;

    @Value("${booklink.di.image-dir}")
    protected String imageDir;

    @Value("${booklink.di.with-mongo-images}")
    protected Boolean withMongoImages;

    @Value("${booklink.di.fetch-original-images}")
    protected Boolean fetchOriginalImages;

    @Value("${booklink.di.frequency-check}")
    protected int frequencyCheck;

    protected int savedCount = 0;

    protected int totalSavedCount = 0;

    @Autowired
    protected ImageDownloader imageDownloader;

    protected File imageDirectoryLocation;

    @Autowired
    protected AuthorIdInclusionFilter authorIdFilter;

    @Autowired
    private AuthorImgExclusionFilter authorImgExclusionFilter;

    protected int authorMatchCount = 0;


    @Override
    public void prepare(File workingDirectory) {

        authorIdFilter.load(workingDirectory);
        authorImgExclusionFilter.load(workingDirectory);

        imageDownloader.setThrottleMs(1000);
        imageDownloader.setImageIdFilter(authorImgExclusionFilter);
    }

    @Override
    public R toRecord(String line) {

        try {
            return objectMapper.readValue(line, getSchemaType());
        } catch (JsonProcessingException e) {
            log.warn("failed line:\n{}", line);
            throw new OpenLibraryImportException("problem reading record", e);
        }
    }

    @Override
    public String toText(R record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            log.warn("failed record:\n{}", record);
            throw new OpenLibraryImportException("problem writing record", e);
        }
    }

    @Override
    public void conclude(File workingDirectory) {
        if(log.isInfoEnabled() && imageDownloader.isEnabled()) {
            Set<String> failedDownloads = imageDownloader.getFailedImageDownloads();
            StringBuilder urls = new StringBuilder();
            failedDownloads.stream().forEach(url -> urls.append(url + "\n"));
            log.info("{} failed downloads:\n{}", failedDownloads.size(), urls.toString());
        }
    }

    /**
     * Some images duplicate across sizes. Make sure that if such duplication is detected, one of
     * the images is removed.
     *
     * @param record to check
     */
    protected void checkImages(BaseSchema record) {
        ofNullable(record.getImageMedium()).ifPresent(imgM -> {
            ofNullable(record.getImageLarge()).ifPresent(imgL -> {
                if(imgM.getSizeBytes() == imgL.getSizeBytes()) {
                    record.setImageLarge(null);
                }
            });
        });
    }

    protected abstract Class<R> getSchemaType();
    protected abstract void enhanceData(R record);   
}
