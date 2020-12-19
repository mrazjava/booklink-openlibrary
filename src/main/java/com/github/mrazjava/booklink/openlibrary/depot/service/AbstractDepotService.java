package com.github.mrazjava.booklink.openlibrary.depot.service;

import static com.github.mrazjava.booklink.openlibrary.depot.service.SearchOperator.AND;
import static java.util.Optional.ofNullable;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.match;
import static org.springframework.data.mongodb.core.aggregation.Aggregation.newAggregation;
import static org.springframework.data.mongodb.core.query.Criteria.where;

import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.MatchOperation;
import org.springframework.data.mongodb.core.aggregation.SampleOperation;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Field;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.TextCriteria;
import org.springframework.data.mongodb.core.query.TextQuery;

import com.github.mrazjava.booklink.openlibrary.repository.OpenLibraryMongoRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
public abstract class AbstractDepotService<D, S> {

    @Autowired
    protected MongoTemplate mongoTemplate;

    @Autowired
    protected OpenLibraryMongoRepository<S> repository;


    public D findById(String id) {
        return repository.findById(id).map(schemaToDepot()).orElse(depotFallback());
    }
    
    public D findById(String id, boolean withSmallImg, boolean withMediumImg, boolean withLargeImg) {
    	log.debug("id[{}], withSmallImg[{}], withMediumImg[{}], withLargeImg[{}]", id, withSmallImg, withMediumImg, withLargeImg);
    	Query query = new Query().addCriteria(Criteria.where("_id").is(id));
    	handleImageFields(query, withSmallImg, withMediumImg, withLargeImg);
    	return Optional.ofNullable(mongoTemplate.findOne(query, getSchemaClass()))
    		.map(schemaToDepot())
    		.orElse(depotFallback());
    }
    
    private Field handleImageFields(Query query, boolean imgS, boolean imgM, boolean imgL) {
    	
    	Field queryFields = query.fields();
    	if(!imgS) {
    		queryFields = queryFields.exclude("imageSmall.image");
    	}
    	if(!imgM) {
    		queryFields = queryFields.exclude("imageMedium.image");
    	}
    	if(!imgL) {
    		queryFields = queryFields.exclude("imageLarge.image");
    	}
    	return queryFields;
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
                          Boolean withSmallImg, Boolean withMediumImg, Boolean withLargeImg,
                          SearchOperator operator) {

        List<Criteria> imgCriteria = new LinkedList<>();

        ofNullable(withSmallImg).ifPresent(b -> imgCriteria.add(where("imageSmall").exists(b)));
        ofNullable(withMediumImg).ifPresent(b -> imgCriteria.add(where("imageMedium").exists(b)));
        ofNullable(withLargeImg).ifPresent(b -> imgCriteria.add(where("imageLarge").exists(b)));

        MatchOperation matchOperation = ofNullable(operator)
                .filter(op -> imgCriteria.size() > 0)
                .map(op ->
        {
            Criteria where = new Criteria();
            Criteria[] criteria = imgCriteria.toArray(new Criteria[imgCriteria.size()]);
            return op == AND ? where.andOperator(criteria) : where.orOperator(criteria);
        })
                .map(where -> match(where))
                .orElse(imgCriteria.size() == 1 ? match(imgCriteria.get(0)) : null);

        SampleOperation sampleOp = Aggregation.sample(sampleSize);
        Aggregation aggregation = ofNullable(matchOperation)
                .map(matchOp -> newAggregation(matchOp, sampleOp)).orElse(newAggregation(sampleOp));
        AggregationResults<S> output = mongoTemplate.aggregate(aggregation, getCollectionName(), getSchemaClass());

        return output.getMappedResults().stream().map(schemaToDepot()).collect(Collectors.toList());
    }

    public List<D> findAll(int pageNo, int size, Sort sort, boolean withImgS, boolean withImgM, boolean withImgL) {
    	Pageable pageable = PageRequest.of(pageNo, size, sort);
    	Query query = new Query().with(pageable);
    	handleImageFields(query, withImgS, withImgM, withImgL);
    	return mongoTemplate.find(query, getSchemaClass())
    			.stream().map(schemaToDepot()).collect(Collectors.toList());
    }

    protected abstract Function<S, D> schemaToDepot();

    protected abstract Class<S> getSchemaClass();

    protected abstract String getCollectionName();

    protected abstract D depotFallback();
}
