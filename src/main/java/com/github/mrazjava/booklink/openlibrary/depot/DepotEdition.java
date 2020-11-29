package com.github.mrazjava.booklink.openlibrary.depot;

import static com.github.mrazjava.booklink.openlibrary.SwaggerConfiguration.DEPOT_API_DATE_FORMAT;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.mrazjava.booklink.openlibrary.schema.Dimensions;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import com.github.mrazjava.booklink.openlibrary.schema.Weight;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotEdition implements DepotRecord {

    private String id;
    private Set<String> authors;
    private Set<String> works;
    private List<String> authorNames;
    private List<String> workTitles;
    private String title;
    private String editionName;
    private String publisher;
    private String copyrightDate;
    private String publishDate;
    private Integer numberOfPages;
    private String description;
    private List<String> isbns;
    private List<String> isbns10;
    private List<String> isbns13;
    private List<String> isbnsInvalid;
    private String originalIsbn;
    private String physicalFormat;
    private Dimensions dimensions;
    private Weight weight;
    private String notes;
    private Integer volumeNumber;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime created;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime modified;

    private DepotPicture imageSmall;
    private DepotPicture imageMedium;
    private DepotPicture imageLarge;


    public DepotEdition(EditionSchema schema) {
        id = schema.getId();
        authors = schema.getAuthors();
        works = schema.getWorks();
        authorNames = schema.getAuthorNames();
        workTitles = schema.getWorkTitles();
        title = schema.getTitle();
        editionName = schema.getEditionName();
        publisher = schema.getPublisher();
        copyrightDate = schema.getCopyrightDate();
        publishDate = schema.getPublishDate();
        numberOfPages = schema.getNumberOfPages();
        description = schema.getDescription();
        isbns = schema.getIsbn();
        isbns10 = schema.getIsbn10();
        isbns13 = schema.getIsbn13();
        isbnsInvalid = schema.getIsbnInvalid();
        originalIsbn = schema.getOriginalIsbn();
        physicalFormat = schema.getPhysicalFormat();
        dimensions = schema.getDimensions();
        weight = schema.getBookWeight();
        notes = schema.getNotes();
        volumeNumber = schema.getVolumeNumber();

        imageSmall = Optional.ofNullable(schema.getImageSmall()).map(DepotPicture::new).orElse(null);
        imageMedium = Optional.ofNullable(schema.getImageMedium()).map(DepotPicture::new).orElse(null);
        imageLarge = Optional.ofNullable(schema.getImageLarge()).map(DepotPicture::new).orElse(null);
    }
}
