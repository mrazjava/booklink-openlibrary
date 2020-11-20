package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotEdition;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class EditionService extends AbstractDepotService<DepotEdition, EditionSchema> {

    @Override
    protected Function<EditionSchema, DepotEdition> schemaToDepot() {
        return DepotEdition::new;
    }

    @Override
    protected DepotEdition depotFallback() {
        return new DepotEdition();
    }

    @Override
    protected Class<EditionSchema> getSchemaClass() {
        return EditionSchema.class;
    }

    @Override
    protected String getCollectionName() {
        return "editions";
    }
}
