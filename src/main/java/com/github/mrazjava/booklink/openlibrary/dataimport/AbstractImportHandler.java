package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;

import java.io.File;

abstract class AbstractImportHandler<T> implements ImportHandler<File> {

    @Autowired
    protected ObjectMapper objectMapper;

    @Value("${booklink.di.persist}")
    protected boolean persistData;

    @Value("${booklink.di.persist-override}")
    protected boolean persistDataOverride;


    @Override
    public void processRecord(String line) {

        try {
            T pojo = objectMapper.readValue(line,  getSchemaType());
            handle(pojo);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("problem reading record", e);
        }
    }

    protected abstract void handle(T record);

    protected abstract Class<T> getSchemaType();
}
