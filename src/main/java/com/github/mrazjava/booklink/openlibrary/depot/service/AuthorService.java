package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
public class AuthorService extends AbstractMongoSupport {

    @Autowired
    private AuthorRepository authorRepository;

    public DepotAuthor findById(String id) {
        return authorRepository.findById(id).map(DepotAuthor::new).orElse(new DepotAuthor());
    }

    public List<DepotAuthor> findById(List<String> ids) {
        return iterableToList(authorRepository.findAllById(ids));
    }

    public List<DepotAuthor> searchText(String search, String langIso, boolean caseSensitive) {

        return mongoTemplate.find(prepareTextQuery(search, langIso, caseSensitive), AuthorSchema.class)
                .stream()
                .map(DepotAuthor::new)
                .collect(Collectors.toList());
    }

    @Override
    protected Function<AuthorSchema, DepotAuthor> mapper() {
        return DepotAuthor::new;
    }
}
