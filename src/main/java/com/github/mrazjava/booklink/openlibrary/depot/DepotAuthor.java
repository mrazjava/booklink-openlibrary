package com.github.mrazjava.booklink.openlibrary.depot;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotAuthor {

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
    private List<String> notes;
    private LocalDateTime created;
    private LocalDateTime modified;
    private DepotPicture imageSmall;
    private DepotPicture imageMedium;
    private DepotPicture imageLarge;
}
