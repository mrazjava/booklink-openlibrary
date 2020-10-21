package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AuthorService {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private MongoTemplate mongoTemplate;

    public DepotAuthor findById(String id) {
        return authorRepository.findById(id).map(s -> new DepotAuthor(s)).orElse(new DepotAuthor());
    }

    public List<DepotAuthor> searchText(String search, String langIso, boolean caseSensitive) {

        TextCriteria txtCriteria = StringUtils.isEmpty(langIso) ?
                TextCriteria.forDefaultLanguage() : TextCriteria.forLanguage(langIso);
        txtCriteria.caseSensitive(caseSensitive).matching(search);


        Query query = TextQuery.queryText(txtCriteria.caseSensitive(caseSensitive).matching(search))
                .sortByScore();

        return mongoTemplate.find(query, AuthorSchema.class).stream()
                .map(a -> new DepotAuthor(a)).collect(Collectors.toList());
    }
}
