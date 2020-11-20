package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.io.File;

import static com.github.mrazjava.booklink.openlibrary.OpenLibraryImportApp.openFile;

@Slf4j
@Component
public class ImportInputValidator {

    @Value("${booklink.di.ol-dump-file}")
    private String dumpFilePath;

    @Value("${booklink.di.handler-class}")
    private String handlerClass;

    @Value("${booklink.di.start-from-record-no}")
    private int startWithRecordNo;

    /**
     * For large files, it may be desirable to every so often perform some processing.
     * For example, if total rows is 1000, setting this value to 100, would instruct
     * the importer to perform some action ten times after each batch of 100 records
     * processed.
     */
    @Value("${booklink.di.frequency-check}")
    private int frequencyCheck;

    @Value("${booklink.di.persist}")
    private Boolean persistData;

    @Value("${booklink.di.persist-override}")
    private Boolean persistDataOverride;

    @Value("${booklink.di.image-pull}")
    private Boolean imagePull;

    @Value("${booklink.di.image-dir}")
    private String imageDir;

    @Value("${booklink.di.with-mongo-images}")
    private Boolean withMongoImages;

    @Value("${booklink.di.fetch-original-images}")
    private Boolean fetchOriginalImages;

    @Value("${booklink.di.author-sample-output-file}")
    private String authorSampleFile;

    @Value("${booklink.di.author-sample-randomize}")
    private String authorSampleRandomization;

    public void validate(File importDumpFile) {

        if(log.isInfoEnabled()) {

            log.info("Booklink-OpenLibrary Import\n\n" +
                            "booklink.di:\n" +
                            " ol-dump-file: {}\n" +
                            " handler-class: {}\n" +
                            " start-from-record-no: {}\n" +
                            " frequency-check: {}\n" +
                            " persist: {}\n" +
                            " persist-override: {}\n" +
                            " image-pull: {}\n" +
                            " image-dir: {}\n" +
                            " with-mongo-images: {}\n" +
                            " fetch-original-images: {}\n" +
                            " author-sample-output-file: {}\n" +
                            " author-sample-randomize: {}\n",
                    importDumpFile.getAbsolutePath(),
                    handlerClass,
                    startWithRecordNo,
                    frequencyCheck,
                    persistData,
                    persistDataOverride,
                    imagePull,
                    StringUtils.isBlank(imageDir) ? "feature DISABLED" : imageDir,
                    withMongoImages,
                    fetchOriginalImages,
                    StringUtils.isBlank(authorSampleFile) ? "feature DISABLED" : openFile(importDumpFile.getParent(), authorSampleFile),
                    authorSampleRandomization
            );
        }
    }

    @ConditionalOnProperty(name = "booklink.di.author-sample-randomize")
    @Bean
    AuthorSampleRandomizer produceAuthorSampleRandomizer() {

        AuthorSampleRandomizer bean = null;

        if(StringUtils.isNotBlank(authorSampleFile)) {
            if(StringUtils.isNotBlank(authorSampleRandomization)) {

                String[] tokens = authorSampleRandomization.split("\\|");

                if(tokens == null || tokens.length != 2 ||
                        !NumberUtils.isCreatable(tokens[0]) || !NumberUtils.isCreatable(tokens[0])) {
                    log.error("booklink.di.author-sample-randomize[{}] - bad format; expecting LONG1|LONG2 where LONG1<LONG2", authorSampleRandomization);
                    throw new OpenLibraryImportException(
                            String.format("INPUT ERROR! booklink.di.author-sample-randomize[%s]", authorSampleRandomization)
                    );
                }

                bean = new AuthorSampleRandomizer(Long.valueOf(tokens[0]), Long.valueOf(tokens[1]));

                if(bean.getSampleCount() < 0 || bean.getTotalCount() < 0 || bean.getSampleCount() >= bean.getTotalCount()) {
                    log.error("booklink.di.author-sample-randomize[{}] - both tokens must be positive numbers and left token must be smaller then right token", authorSampleRandomization);
                    throw new OpenLibraryImportException(
                            String.format("INPUT ERROR! booklink.di.author-sample-randomize[%s] - invalid domain", authorSampleRandomization)
                    );
                }
            }
        }

        return bean;
    }
}