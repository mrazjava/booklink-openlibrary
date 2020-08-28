package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.File;

@Slf4j
@Component
public class WorkHandler extends AbstractImportHandler<WorkSchema> {

    @Autowired
    private WorkRepository repository;

    @Override
    public void prepare(File dataSource) {
    }

    @Override
    protected void handle(WorkSchema record) {

        WorkSchema saved = null;

        if(persistData) {
            saved = repository.findById(record.getId()).orElse(repository.save(record));
        }
    }

    @Override
    protected Class<WorkSchema> getSchemaType() {
        return WorkSchema.class;
    }
}
