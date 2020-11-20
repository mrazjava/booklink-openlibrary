package com.github.mrazjava.booklink.openlibrary.depot.rest;

import com.github.mrazjava.booklink.openlibrary.depot.DepotEdition;
import com.github.mrazjava.booklink.openlibrary.depot.service.EditionService;
import io.swagger.annotations.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.lang3.BooleanUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Optional;

@Api(
        tags = {"Edition"}
)
@RestController
@RequestMapping("/rest/v1/depot/edition")
@Slf4j
public class EditionRestController implements DepotSearch<DepotEdition> {

    @Autowired
    private EditionService editionService;

    @ApiOperation(value = "Find edition matching a specific ID")
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
    public ResponseEntity<DepotEdition> findById(@ApiParam(value = "unique work ID", required = true) @PathVariable("id") String editionId) {
        log.info("id: {}", editionId);
        return ResponseEntity.ok(editionService.findById(editionId));
    }

    @ApiOperation(value = "Find editions matching one or more keys")
    @GetMapping
    @Produces(MediaType.APPLICATION_JSON)
    @ApiResponses(
            {
                    @ApiResponse(
                            message = "ok",
                            code = 200
                    )
            }
    )
    public ResponseEntity<List<DepotEdition>> findByKey(
            @ApiParam(value = "unique author ID for which to fetch edition")
            @RequestParam(value = "authorId", required = false) String authorId) {

        List<DepotEdition> results = null;

        if(StringUtils.isNotBlank(authorId)) {
            log.debug("search editions by authorId[{}]", authorId);
            results = editionService.findByAuthorId(authorId);
            log.info("found {} results", results.size());
        }

        return ResponseEntity.ok(results = ListUtils.emptyIfNull(results));
    }

    @Override
    public ResponseEntity<List<DepotEdition>> searchText(String searchQuery, Boolean caseSensitive, String languageCode) {

        caseSensitive = BooleanUtils.toBoolean(caseSensitive);
        log.info("searchText[{}], caseSensitive[{}], languageCode[{}]", searchQuery, caseSensitive, languageCode);
        return ResponseEntity.ok(editionService.searchText(searchQuery, languageCode, caseSensitive));
    }

    @Override
    public ResponseEntity<List<DepotEdition>> randomWithImage(Integer sampleCount) {
        return ResponseEntity.ok(editionService.random(
                Optional.ofNullable(sampleCount).orElse(1),
                false, false, true, null
                )
        );
    }
}
