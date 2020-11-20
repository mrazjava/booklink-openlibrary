package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.stereotype.Service;

import java.util.function.Function;

@Service
public class AuthorService extends AbstractDepotService<DepotAuthor, AuthorSchema> {

    @Override
    protected Function<AuthorSchema, DepotAuthor> schemaToDepot() {
        return DepotAuthor::new;
    }

    @Override
    protected DepotAuthor depotFallback() {
        return new DepotAuthor();
    }

    @Override
    protected Class<AuthorSchema> getSchemaClass() {
        return AuthorSchema.class;
    }

    @Override
    protected String getCollectionName() {
        return AuthorRepository.COLLECTION_NAME;
    }
}
