package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.query.Criteria.where;

public abstract class AbstractDepotService<D, S> {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected OpenLibraryMongoRepository<S> repository;


    public D findById(String id) {
        return repository.findById(id).map(schemaToDepot()).orElse(depotFallback());
    }

    public List<D> findById(List<String> ids) {
        return iterableToList(repository.findAllById(ids));
    }

    protected Query prepareTextQuery(String search, String langIso, boolean caseSensitive) {

        TextCriteria txtCriteria = StringUtils.isEmpty(langIso) ?
                TextCriteria.forDefaultLanguage() : TextCriteria.forLanguage(langIso);
        txtCriteria.caseSensitive(caseSensitive).matching(search);

        return TextQuery.queryText(txtCriteria.caseSensitive(caseSensitive).matching(search))
                .sortByScore();
    }

    protected List<D> iterableToList(Iterable<S> schemas) {
        return StreamSupport.stream(schemas.spliterator(), false).map(schemaToDepot()).collect(Collectors.toList());
    }

    public List<D> findByAuthorId(String authorId) {
        return repository.findByAuthors(List.of(authorId)).stream().map(schemaToDepot()).collect(Collectors.toList());
    }

    public List<D> searchText(String search, String langIso, boolean caseSensitive) {
        return mongoTemplate.find(prepareTextQuery(search, langIso, caseSensitive), getSchemaClass())
                .stream()
                .map(schemaToDepot())
                .collect(Collectors.toList());
    }

    public List<D> random(int sampleSize) {

        SampleOperation sampleStage = Aggregation.sample(sampleSize);
        Aggregation aggregation = Aggregation.newAggregation(match(where("imageLarge").exists(true)), sampleStage);
        AggregationResults<S> output = mongoTemplate.aggregate(aggregation, getCollectionName(), getSchemaClass());

        return output.getMappedResults().stream().map(schemaToDepot()).collect(Collectors.toList());
    }

    protected abstract Function<S, D> schemaToDepot();

    protected abstract Class<S> getSchemaClass();

    protected abstract String getCollectionName();

    protected abstract D depotFallback();
}
