package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
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

import static com.github.mrazjava.booklink.openlibrary.depot.service.ServiceOperator.OR;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
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

    public List<D> random(int sampleSize,
                          boolean withSmallImg, boolean withMediumImg, boolean withLargeImg,
                          ServiceOperator operator) {

        Criteria whereCriteria = null;

        if(withSmallImg) {
            whereCriteria = where("imageSmall").exists(true);
        }
        if(withMediumImg) {
            Criteria whereMediumImg = where("imageMedium").exists(true);
            whereCriteria = Optional.ofNullable(whereCriteria)
                    .map(w -> operator == OR ? w.orOperator(whereMediumImg) : w.andOperator(whereMediumImg))
                    .orElse(whereMediumImg);
        }
        if(withLargeImg) {
            Criteria whereLargeImg = where("imageLarge").exists(true);
            whereCriteria = Optional.ofNullable(whereCriteria)
                    .map(w -> operator == OR ? w.orOperator(whereLargeImg) : w.andOperator(whereLargeImg))
                    .orElse(whereLargeImg);
        }

        MatchOperation matchOperation = Optional.ofNullable(whereCriteria)
                .map(criteria -> match(criteria)).orElse(null);

        SampleOperation sampleStage = Aggregation.sample(sampleSize);
        Aggregation aggregation = Optional.ofNullable(matchOperation)
                .map(op -> newAggregation(op, sampleStage)).orElse(newAggregation(sampleStage));
        AggregationResults<S> output = mongoTemplate.aggregate(aggregation, getCollectionName(), getSchemaClass());

        return output.getMappedResults().stream().map(schemaToDepot()).collect(Collectors.toList());
    }

    protected abstract Function<S, D> schemaToDepot();

    protected abstract Class<S> getSchemaClass();

    protected abstract String getCollectionName();

    protected abstract D depotFallback();
}
