package com.github.mrazjava.booklink.openlibrary.depot;

import static com.github.mrazjava.booklink.openlibrary.SwaggerConfiguration.DEPOT_API_DATE_FORMAT;
import static java.util.Optional.ofNullable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.collections4.CollectionUtils;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotAuthor implements DepotRecord {

    private String id;
    private List<String> works; // ids
    private String name;
    private String nameFull;
    private String namePersonal;
    private String bio;
    private String birthDate;
    private String deathDate;
    private String comment;
    private List<String> publishers;
    private String notes;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime created;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime modified;

    private DepotPicture imageSmall;
    private DepotPicture imageMedium;
    private DepotPicture imageLarge;

    public DepotAuthor(AuthorSchema schema) {
        id = schema.getId();
        works = CollectionUtils.emptyIfNull(schema.getWorks()).stream().collect(Collectors.toList());
        name = schema.getName();
        nameFull = schema.getFullName();
        namePersonal = schema.getPersonalName();
        bio = schema.getBio();
        birthDate = schema.getBirthDate();
        deathDate = schema.getDeathDate();
        comment = schema.getComment();
        publishers = schema.getPublishers();
        notes = ofNullable(schema.getNotes()).map(tv -> tv.getValue()).orElse(null);
        created = ofNullable(schema.getCreated()).map(tv -> tv.getValue()).orElse(null);
        modified = ofNullable(schema.getLastModified()).map(tv -> tv.getValue()).orElse(null);
        imageSmall = ofNullable(schema.getImageSmall()).map(DepotPicture::new).orElse(null);
        imageMedium = ofNullable(schema.getImageMedium()).map(DepotPicture::new).orElse(null);
        imageLarge = ofNullable(schema.getImageLarge()).map(DepotPicture::new).orElse(null);
    }
}
