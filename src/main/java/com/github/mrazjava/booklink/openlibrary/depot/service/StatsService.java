package com.github.mrazjava.booklink.openlibrary.depot.service;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.repository.EditionRepository;
import com.github.mrazjava.booklink.openlibrary.repository.WorkRepository;
import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.FacetOperation;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;

@Slf4j
@Service
public class StatsService {

    static final String KEY_SMALL = "S";
    static final String KEY_MEDIUM = "M";
    static final String KEY_LARGE = "L";
    static final String KEY_ALL = "A";

    @Autowired
    private MongoTemplate mongoTemplate;

    public CountResults countAuthors() {
        return countCollection(AuthorRepository.COLLECTION_NAME);
    }

    public CountResults countWorks() {
        return countCollection(WorkRepository.COLLECTION_NAME);
    }

    public CountResults countEditions() {
        return countCollection(EditionRepository.COLLECTION_NAME);
    }

    private CountResults countCollection(String collectionName) {

        FacetOperation facets = facet(
                match(where("imageSmall").exists(true)),
                count().as(KEY_SMALL)
        ).as("small")
        .and(
                match(where("imageMedium").exists(true)),
                count().as(KEY_MEDIUM)
        ).as("medium")
        .and(
                match(where("imageLarge").exists(true)),
                count().as(KEY_LARGE)
        ).as("large")
        .and(
                count().as(KEY_ALL)
        ).as("total");

        Aggregation fAgg = newAggregation(facets);

        AggregationResults<CountResults> results = mongoTemplate.aggregate(
                fAgg,
                collectionName,
                CountResults.class
        );

        log.debug("raw result:\n{}", results.getRawResults());

        return results.getUniqueMappedResult();
    }

    @ToString
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CountResults {
        private List<Map<String, Integer>> small;
        private List<Map<String, Integer>> medium;
        private List<Map<String, Integer>> large;
        private List<Map<String, Integer>> total;

        public Integer getSmallImgCount() {
            return Optional.ofNullable(small)
                    .map(l -> l.get(0)).stream().findFirst().map(m -> m.get(KEY_SMALL)).orElse(0);
        }

        public Integer getMediumImgCount() {
            return Optional.ofNullable(medium)
                    .map(l -> l.get(0)).stream().findFirst().map(m -> m.get(KEY_MEDIUM)).orElse(0);
        }

        public Integer getLargeImgCount() {
            return Optional.ofNullable(large)
                    .map(l -> l.get(0)).stream().findFirst().map(m -> m.get(KEY_LARGE)).orElse(0);
        }

        public Integer getTotalCount() {
            return Optional.ofNullable(total)
                    .map(l -> l.get(0)).stream().findFirst().map(m -> m.get(KEY_ALL)).orElse(0);
        }
    }
}
