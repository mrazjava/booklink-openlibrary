package com.github.mrazjava.booklink.openlibrary.dataimport;

import static com.github.mrazjava.booklink.openlibrary.OpenLibraryImportApp.openFile;
import static org.apache.commons.lang3.StringUtils.rightPad;

import java.io.File;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import lombok.extern.slf4j.Slf4j;

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
    
    @Autowired
    private ImportConfiguration importConfig;

    public void validate(File importDumpFile) {

    	final int PAD = 30;
    	final String LBL_DISABLED = "DISABLED";
    	
        if(log.isInfoEnabled()) {

            log.info("Booklink-OpenLibrary Import\n\n" +
                            "booklink.di:\n" +
                            rightPad(" ol-dump-file:", PAD) + "{}\n" +
                            rightPad(" handler-class:", PAD) + "{}\n" +
                            rightPad(" start-from-record-no:", PAD) + "{}\n" +
                            rightPad(" frequency-check:", PAD) + "{}\n" +
                            rightPad(" persist:", PAD) + "{}\n" +
                            rightPad(" persist-override:", PAD) + "{}\n" +
                            rightPad(" image-pull:", PAD) + "{}\n" +
                            rightPad(" image-dir:", PAD) + "{}\n" +
                            rightPad(" with-mongo-images:", PAD) + "{}\n" +
                            rightPad(" fetch-original-images:", PAD) + "{}\n" +
                            rightPad(" author-sample-output-file:", PAD) + "{}\n" +
                            rightPad(" author-sample-randomize:", PAD) + "{}\n",
                    importDumpFile.getAbsolutePath(),
                    importConfig.getHandlerClass().getCanonicalName(),
                    startWithRecordNo,
                    frequencyCheck,
                    persistData,
                    persistDataOverride,
                    imagePull,
                    StringUtils.isBlank(imageDir) ? LBL_DISABLED : imageDir,
                    withMongoImages,
                    fetchOriginalImages,
                    StringUtils.isBlank(authorSampleFile) ? LBL_DISABLED : openFile(importDumpFile.getParent(), authorSampleFile).getPath(),
                    StringUtils.isBlank(authorSampleRandomization) ? LBL_DISABLED : authorSampleRandomization
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
