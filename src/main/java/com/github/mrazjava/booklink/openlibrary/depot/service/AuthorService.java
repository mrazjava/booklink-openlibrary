package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService extends AbstractMongoSupport {

    @Autowired
    private AuthorRepository authorRepository;

    public DepotAuthor findById(String id) {
        return authorRepository.findById(id).map(DepotAuthor::new).orElse(new DepotAuthor());
    }

    public List<DepotAuthor> findById(List<String> ids) {
        List<DepotAuthor> results = new LinkedList<>();
        authorRepository.findAllById(ids).forEach(a -> results.add(new DepotAuthor(a)));
        return results;
    }

    public List<DepotAuthor> searchText(String search, String langIso, boolean caseSensitive) {

        return mongoTemplate.find(prepareTextQuery(search, langIso, caseSensitive), AuthorSchema.class)
                .stream()
                .map(DepotAuthor::new)
                .collect(Collectors.toList());
    }
}
