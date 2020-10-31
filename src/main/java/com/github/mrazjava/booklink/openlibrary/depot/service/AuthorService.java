package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.function.Function;

@Service
public class AuthorService extends AbstractMongoSupport<DepotAuthor, AuthorSchema> {

    @Autowired
    private AuthorRepository authorRepository;

    public DepotAuthor findById(String id) {
        return authorRepository.findById(id).map(DepotAuthor::new).orElse(new DepotAuthor());
    }

    public List<DepotAuthor> findById(List<String> ids) {
        return iterableToList(authorRepository.findAllById(ids));
    }

    @Override
    protected Function<AuthorSchema, DepotAuthor> mapper() {
        return DepotAuthor::new;
    }

    @Override
    protected Class<AuthorSchema> getSchemaClass() {
        return AuthorSchema.class;
    }
}
