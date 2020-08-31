package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

@Slf4j
abstract class AbstractImportHandler<R> implements ImportHandler<File, R> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${booklink.di.persist}")
    protected boolean persistData;

    @Value("${booklink.di.persist-override}")
    protected boolean persistDataOverride;

    @Value("${booklink.di.frequency-check}")
    protected int frequencyCheck;

    protected int savedCount = 0;

    @Override
    public R toRecord(String line) {

        try {
            return objectMapper.readValue(line, getSchemaType());
        } catch (JsonProcessingException e) {
            log.warn("failed line:\n{}", line);
            throw new RuntimeException("problem reading record", e);
        }
    }

    @Override
    public String toText(R record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            log.warn("failed record:\n{}", record);
            throw new RuntimeException("problem writing record", e);
        }
    }

    protected abstract Class<R> getSchemaType();
}
