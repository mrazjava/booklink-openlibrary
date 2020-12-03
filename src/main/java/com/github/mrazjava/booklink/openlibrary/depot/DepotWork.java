package com.github.mrazjava.booklink.openlibrary.depot;

import static com.github.mrazjava.booklink.openlibrary.SwaggerConfiguration.DEPOT_API_DATE_FORMAT;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.mrazjava.booklink.openlibrary.schema.Series;
import com.github.mrazjava.booklink.openlibrary.schema.WorkSchema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotWork implements DepotRecord {

    private String id;
    private String title;
    private Series series;
    private Set<String> authors;
    private List<String> genres;
    private String firstPublishedDate;
    private String editionCoverId;
    private String description;
    private String firstSentence;
    private String notes;
    private Map<String, String> titleTranslations;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime created;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime modified;

    private DepotPicture imageSmall;
    private DepotPicture imageMedium;
    private DepotPicture imageLarge;

    public DepotWork(WorkSchema schema) {

        id = schema.getId();
        authors = schema.getAuthors();
        title = schema.getTitle();
        series = schema.getSeries();
        genres = schema.getGenres();
        firstPublishedDate = schema.getFirstPublishDate();
        editionCoverId = ofNullable(schema.getCoverEdition()).map(k -> k.getKey()).orElse(null);
        description = schema.getDescription();
        firstSentence = schema.getFirstSentence();
        notes = ofNullable(schema.getNotes()).map(n -> n.getValue()).orElse(null);

        imageSmall = ofNullable(schema.getImageSmall()).map(DepotPicture::new).orElse(null);
        imageMedium = ofNullable(schema.getImageMedium()).map(DepotPicture::new).orElse(null);
        imageLarge = ofNullable(schema.getImageLarge()).map(DepotPicture::new).orElse(null);

        ofNullable(schema.getTranslatedTitles()).ifPresent(l -> {
            titleTranslations = new HashMap<>();
            l.stream().forEach(t -> {
                ofNullable(t.getLanguage()).ifPresent(k -> {
                    titleTranslations.put(k.getKey(), t.getText());
                });
            });
        });
    }
}
