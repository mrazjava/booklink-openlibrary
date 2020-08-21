package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
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

    private ObjectMapper objectMapper;

    @Value("${booklink.data-importer.frequency-check}")
    private int frequencyCheck;

    @Value("${booklink.data-importer.persist}")
    private boolean persistData;

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private WorkRepository workRepository;

    @Autowired
    private EditionRepository editionRepository;


    public CommonsLineIterator() {

        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public void runImport(File jsonFile, Class schema) {

        String line = null;
        long counter = 0;
        try {
            LineIterator iterator = FileUtils.lineIterator(jsonFile, "UTF-8");

            StopWatch stopWatch = new StopWatch();
            stopWatch.start();
            while(iterator.hasNext()) {
                line = iterator.next();
                counter++;
                Object pojo = objectMapper.readValue(line, schema);
                if(counter % frequencyCheck == 0) {
                    //log.debug("raw JSON #{}:\n{}", counter, line);
                    log.info("JSON #{}:\n{}", counter, objectMapper.writeValueAsString(pojo));
                }
                processLine(line, schema);
                //log.info("raw JSON #{}:\n{}", counter, line);
                //log.info("raw JSON #{}:\n{}", counter, objectMapper.writeValueAsString(pojo));
            }
            stopWatch.stop();
            log.info("TOTAL RECORDS: {}, time: {}", counter, stopWatch.getTime());
        } catch (Exception e) {
            log.error("JSON #{} failed:\n{}\n\nERROR: {}", counter, line, e.getMessage());
        }
    }

    private void processLine(String line, Class schema) throws JsonProcessingException {

        Object pojo = objectMapper.readValue(line, schema);

        if(AuthorSchema.class.equals(schema)) {
            processAuthor((AuthorSchema)pojo);
        }
        else if(WorkSchema.class.equals(schema)) {
            processWork((WorkSchema)pojo);
        }
        else if(EditionSchema.class.equals(schema)) {
            processEdition((EditionSchema)pojo);
        }
        else {
            log.warn("unsupported schema type: {}", schema);
        }
    }

    private void processAuthor(AuthorSchema author) {
        if(persistData) {
            authorRepository.save(author);
        }
    }

    private void processWork(WorkSchema work) {
        if(persistData) {
            workRepository.save(work);
        }
    }

    private void processEdition(EditionSchema edition) {
        if(persistData) {
            editionRepository.save(edition);
        }
    }
}
