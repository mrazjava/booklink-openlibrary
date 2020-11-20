package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotWork;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class WorkService extends AbstractDepotService<DepotWork, WorkSchema> {

    @Override
    protected Function<WorkSchema, DepotWork> schemaToDepot() {
        return DepotWork::new;
    }

    @Override
    protected DepotWork depotFallback() {
        return new DepotWork();
    }

    @Override
    protected Class<WorkSchema> getSchemaClass() {
        return WorkSchema.class;
    }

    @Override
    protected String getCollectionName() {
        return WorkRepository.COLLECTION_NAME;
    }
}
