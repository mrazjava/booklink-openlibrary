package com.github.mrazjava.booklink.openlibrary.depot;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.github.mrazjava.booklink.openlibrary.schema.EditionSchema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.Optional;

import static com.github.mrazjava.booklink.openlibrary.SwaggerConfiguration.DEPOT_API_DATE_FORMAT;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Data
public class DepotEdition {

    private String id;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime created;

    @JsonFormat(pattern = DEPOT_API_DATE_FORMAT)
    private LocalDateTime modified;

    private DepotPicture imageSmall;
    private DepotPicture imageMedium;
    private DepotPicture imageLarge;


    public DepotEdition(EditionSchema schema) {
        id = schema.getId();
        imageSmall = Optional.ofNullable(schema.getImageSmall()).map(DepotPicture::new).orElse(null);
        imageMedium = Optional.ofNullable(schema.getImageMedium()).map(DepotPicture::new).orElse(null);
        imageLarge = Optional.ofNullable(schema.getImageLarge()).map(DepotPicture::new).orElse(null);
    }
}
