package com.github.mrazjava.booklink.openlibrary.depot.service;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

abstract class AbstractMongoSupport<D, S> {

    @Autowired
    protected MongoTemplate mongoTemplate;


    protected Query prepareTextQuery(String search, String langIso, boolean caseSensitive) {

        TextCriteria txtCriteria = StringUtils.isEmpty(langIso) ?
                TextCriteria.forDefaultLanguage() : TextCriteria.forLanguage(langIso);
        txtCriteria.caseSensitive(caseSensitive).matching(search);

        return TextQuery.queryText(txtCriteria.caseSensitive(caseSensitive).matching(search))
                .sortByScore();
    }

    protected List<D> iterableToList(Iterable<S> schemas) {
        return StreamSupport.stream(schemas.spliterator(), false).map(mapper()).collect(Collectors.toList());
    }

    protected abstract Function<S, D> mapper();
}
