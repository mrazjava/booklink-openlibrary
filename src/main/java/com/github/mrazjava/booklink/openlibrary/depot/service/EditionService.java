package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotEdition;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class EditionService extends AbstractMongoSupport<DepotEdition, EditionSchema> {

    @Override
    public List<DepotEdition> searchText(String search, String langIso, boolean caseSensitive) {
        throw new IllegalStateException("not implemented yet");
    }

    @Override
    protected Function<EditionSchema, DepotEdition> mapper() {
        return DepotEdition::new;
    }

    @Override
    protected Class<EditionSchema> getSchemaClass() {
        return EditionSchema.class;
    }
}
