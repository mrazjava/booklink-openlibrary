package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.File;

/**
 * <a href="https://itnext.io/using-java-to-read-really-really-large-files-a6f8a3f44649">Benchmarks</a>
 */
@Slf4j
@Component
public class CommonsLineIterator implements FileImporter {

    @Autowired
    private ImportHandlingResolution handlingResolver;

    @Value("${booklink.di.frequency-check}")
    private int frequencyCheck;

    @Value("${booklink.di.start-from-record-no}")
    private Integer startWithRecordNo;


    @Override
    public void runImport(File jsonFile, Class schema) {

        if(startWithRecordNo < 0) {
            log.warn("invalid value for [{}={}]; forcing 0!", "start-from-record-no", startWithRecordNo);
            startWithRecordNo = 0;
        }

        String line = null;
        long counter = 0;
        try {
            LineIterator iterator = FileUtils.lineIterator(jsonFile, "UTF-8");
            final String workingDir = jsonFile.getParent();

            log.info("workingDir: {}", workingDir);

            File workingDirectory = new File(workingDir);
            ImportHandler importHandler = handlingResolver.resolve(schema);
            importHandler.prepare(workingDirectory);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            while(iterator.hasNext()) {
                line = iterator.next();
                if(counter++ < startWithRecordNo) {
                    if(counter % frequencyCheck == 0) {
                        log.info("pass through check; raw JSON #{}:\n{}", counter, line);
                    }
                    continue;
                }

                Object pojo = importHandler.toRecord(line);

                if(counter % frequencyCheck == 0) {

                    log.debug("raw JSON #{}:\n{}", counter, line);

                    if(log.isInfoEnabled()) {
                        log.info("JSON #{}:\n{}", counter, importHandler.toText(pojo));
                    }
                }

                importHandler.handle(pojo, counter);

                if(log.isTraceEnabled()) {
                    log.trace("raw JSON #{}:\n{}", counter, line);
                    log.trace("parsed JSON #{}:\n{}", counter, importHandler.toText(pojo));
                }
            }
            stopWatch.stop();

            log.info("TOTAL RECORDS: {}, time: {}",
                    counter,
                    DurationFormatUtils.formatDurationHMS(stopWatch.getTime())
            );
        } catch (Exception e) {
            log.error("JSON #{} failed:\n{}", counter, line, e);
        }
    }
}
