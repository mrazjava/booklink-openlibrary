package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotEdition;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class EditionService extends AbstractMongoSupport<DepotEdition, EditionSchema> {

    @Override
    protected Function<EditionSchema, DepotEdition> mapper() {
        return DepotEdition::new;
    }
}
