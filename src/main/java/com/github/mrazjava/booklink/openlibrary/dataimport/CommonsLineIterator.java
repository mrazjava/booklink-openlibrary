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
    private ImportHandler importHandler;

    @Value("${booklink.di.frequency-check}")
    private int frequencyCheck;

    @Value("${booklink.di.start-from-record-no}")
    private Integer startWithRecordNo;


    @Override
    public void runImport(File jsonFile) {

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
            importHandler.prepare(workingDirectory);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            Object pojo = null;

            // process 1st record explicitly
            if(iterator.hasNext()) {
                line = iterator.next();
                pojo = importHandler.toRecord(line);
                if(log.isInfoEnabled() && frequencyCheck != 1) { // always log 1st record
                    if (counter++ < startWithRecordNo) {
                        log.info("pass through check; raw JSON #{}:\n{}", counter, line);
                    } else {
                        log.info("JSON #{}:\n{}", counter, importHandler.toText(pojo));
                    }
                }
                importHandler.handle(pojo, counter);
            }

            while(iterator.hasNext()) {
                line = iterator.next();
                if(counter++ < startWithRecordNo) {
                    if(counter % frequencyCheck == 0) {
                        log.info("pass through check; raw JSON #{}:\n{}", counter, line);
                    }
                    continue;
                }

                pojo = importHandler.toRecord(line);

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

            if(log.isInfoEnabled() && frequencyCheck != 1) { // always log last record
                if(counter < startWithRecordNo) {
                    log.info("pass through check; raw JSON #{} (last record):\n{}", counter, line);
                } else {
                    log.info("JSON #{} (last record):\n{}", counter, importHandler.toText(pojo));
                }
            }

            stopWatch.stop();

            importHandler.conclude(workingDirectory);

            log.info("TOTAL RECORDS: {}, time: {}",
                    counter,
                    DurationFormatUtils.formatDurationHMS(stopWatch.getTime())
            );
        } catch (Exception e) {
            log.error("JSON #{} failed:\n{}", counter, line, e);
        }
    }
}
