package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class EditionHandler extends AbstractImportHandler<EditionSchema> {

    @Autowired
    private EditionRepository repository;


    @Override
    public void prepare(File dataSource) {
    }

    @Override
    public void handle(EditionSchema record) {

        EditionSchema saved = null;

        if(persistData) {
            saved = repository.findById(record.getId()).orElse(repository.save(record));
        }
    }

    @Override
    protected Class<EditionSchema> getSchemaType() {
        return EditionSchema.class;
    }
}
