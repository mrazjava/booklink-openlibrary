package com.github.mrazjava.booklink.openlibrary.dataimport;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.time.DurationFormatUtils;
import org.apache.commons.lang3.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.github.mrazjava.booklink.openlibrary.dataimport.filter.LineExclusionFilter;
import com.github.mrazjava.booklink.openlibrary.schema.BaseSchema;

import java.io.Closeable;
import java.io.File;
import java.util.Iterator;

/**
 * <a href="https://itnext.io/using-java-to-read-really-really-large-files-a6f8a3f44649">Benchmarks</a>
 */
@Slf4j
@Component
public class FileDataImport implements DataImport<File> {

    @Autowired
    private AbstractImportHandler<BaseSchema> importHandler;

    @Autowired
    private IteratorProvider<String, File> iteratorProvider;

    @Autowired
    private LineExclusionFilter lineExclusionFilter;
    
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

        try(Closeable closeable = iteratorProvider.open(jsonFile)) {

            Iterator<String> iterator = iteratorProvider.provide(closeable);
            File workingDirectory = jsonFile.getParentFile();

            log.info("working directory: {}", workingDirectory);

            importHandler.prepare(workingDirectory);
            lineExclusionFilter.load(workingDirectory);

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();

            BaseSchema pojo = null;

            // process 1st record explicitly
            if(iterator.hasNext()) {
                line = iterator.next();
                if(!lineExclusionFilter.exists(++counter)) {                    
                    pojo = importHandler.toRecord(line);
                    log.debug("raw JSON #{}:\n{}", ++counter, line);
                    if(log.isInfoEnabled()) { // always log 1st record
                        if (counter < startWithRecordNo) {
                            log.info("pass through check; raw JSON #{}:\n{}", counter, line);
                        } else {
                            log.info("JSON #{}:\n{}", counter, importHandler.toText(pojo));
                        }
                    }
                    importHandler.handle(pojo, counter);
                }
                else {
                    lineExclusionFilter.logSkipMsg(counter, line);
                }
            }

            while(iterator.hasNext()) {
                line = iterator.next();
                if(counter++ < startWithRecordNo) {
                    if(counter % frequencyCheck == 0) {
                        log.info("pass through check; raw JSON #{}:\n{}", counter, line);
                    }
                    continue;
                }
                if(lineExclusionFilter.exists(counter)) {
                    lineExclusionFilter.logSkipMsg(counter, line);
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

    void setFrequencyCheck(int frequencyCheck) {
        this.frequencyCheck = frequencyCheck;
    }

    void setStartWithRecordNo(Integer startWithRecordNo) {
        this.startWithRecordNo = startWithRecordNo;
    }
}
