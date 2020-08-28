package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.LineIterator;
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

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private EditionRepository editionRepository;


    @Override
    public void runImport(File jsonFile, Class schema) {

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
                counter++;

                Object pojo = importHandler.toRecord(line);

                if(counter % frequencyCheck == 0) {

                    if(log.isDebugEnabled()) {
                        log.debug("raw JSON #{}:\n{}", counter, line);
                    }
                    if(log.isInfoEnabled()) {
                        log.info("JSON #{}:\n{}", counter, importHandler.toText(pojo));
                    }
                }

                importHandler.handle(pojo);

                if(log.isTraceEnabled()) {
                    log.trace("raw JSON #{}:\n{}", counter, line);
                    log.trace("raw JSON #{}:\n{}", counter, importHandler.toText(pojo));
                }
            }
            stopWatch.stop();
            log.info("TOTAL RECORDS: {}, time: {}", counter, stopWatch.getTime());
        } catch (Exception e) {
            log.error("JSON #{} failed:\n{}", counter, line, e);
        }
    }
}
