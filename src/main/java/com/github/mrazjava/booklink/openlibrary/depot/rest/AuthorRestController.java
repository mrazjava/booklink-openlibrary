package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotAuthor;
import com.github.mrazjava.booklink.openlibrary.depot.service.AuthorService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.BooleanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Api(
        tags = {"Author"}
)
@RestController
@RequestMapping("/rest/v1/depot/author")
@Slf4j
public class AuthorRestController implements DepotSearch<DepotAuthor> {

    @Autowired
    private AuthorService authorService;

    @ApiOperation(value = "Find author matching a specific ID")
    @GetMapping(path = "/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<DepotAuthor> findById(@ApiParam(value = "unique author ID", required = true) @PathVariable("id") String authorId) {
        log.info("id: {}", authorId);
        return ResponseEntity.ok(authorService.findById(authorId));
    }

    @Override
    public ResponseEntity<List<DepotAuthor>> searchText(String searchQuery, Boolean caseSensitive, String languageCode) {

        caseSensitive = BooleanUtils.toBoolean(caseSensitive);
        log.info("searchText[{}], caseSensitive[{}], languageCode[{}]", searchQuery, caseSensitive, languageCode);
        return ResponseEntity.ok(authorService.searchText(searchQuery, languageCode, caseSensitive));
    }

    @Override
    public ResponseEntity<List<DepotAuthor>> randomWithImage(Integer sampleCount) {
        return ResponseEntity.ok(authorService.random(Optional.ofNullable(sampleCount).orElse(1)));
    }
}
