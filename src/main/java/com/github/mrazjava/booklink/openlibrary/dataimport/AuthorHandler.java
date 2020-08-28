package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.repository.AuthorRepository;
import com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Map;
import java.util.Optional;

import static com.github.mrazjava.booklink.openlibrary.schema.AuthorSchema.AUTHOR_PHOTOID_IMG_URL_TEMPLATE;

@Slf4j
@Component
public class AuthorHandler extends AbstractImportHandler<AuthorSchema> {

    @Autowired
    private AuthorRepository authorRepository;

    @Autowired
    private ImageDownloader imageDownloader;

    private File authorImagesDestination;

    @Value("${booklink.di.author-image-dir}")
    private String authorImgDir;

    @Value("${booklink.di.author-image-mongo}")
    private Boolean storeAuthorImgInMongo;


    @Override
    public void prepare(File workingDirectory) {
        if(StringUtils.isNotBlank(authorImgDir)) {
            authorImagesDestination = Path.of(authorImgDir).getParent() == null ?
                    Path.of(workingDirectory.getAbsolutePath() + File.separator + authorImgDir).toFile() :
                    Path.of(authorImgDir).toFile();
            if(!authorImagesDestination.exists()) {
                authorImagesDestination.mkdir();
            }
        }

        log.info("destinationAuthorImg: {}", authorImagesDestination);
    }

    @Override
    public void handle(AuthorSchema record) {

        AuthorSchema saved = null;

        if(persistData) {
            saved = BooleanUtils.isTrue(persistDataOverride) ?
                    authorRepository.save(record) :
                    authorRepository.findById(record.getId()).orElse(authorRepository.save(record));
        }

        try {
            downloadImages(Optional.ofNullable(saved).orElse(record));
        }
        catch(IOException e) {
            log.error("problem downloading author images: {}", e.getMessage());
        }
    }

    @Override
    protected Class<AuthorSchema> getSchemaType() {
        return AuthorSchema.class;
    }

    private void downloadImages(AuthorSchema record) throws IOException {

        if(CollectionUtils.isEmpty(record.getPhotos())) {
            return;
        }

        Integer photoId = record.getPhotos().stream().filter(id -> id > 0).findFirst().orElse(0);

        if(photoId == 0) {
            return;
        }

        Map<ImageSize, File> imgFiles = StringUtils.isNotBlank(authorImgDir) ?
                imageDownloader.downloadImageToFile(
                        authorImagesDestination.getAbsolutePath(),
                        String.valueOf(photoId), AUTHOR_PHOTOID_IMG_URL_TEMPLATE) :
                null;

        if(BooleanUtils.isTrue(storeAuthorImgInMongo)) {
            imageDownloader.downloadImageToBinary(
                    String.valueOf(photoId), AUTHOR_PHOTOID_IMG_URL_TEMPLATE,
                    record,
                    imgFiles == null ? Map.of() : imgFiles
            );
        }
    }
}
