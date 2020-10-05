package com.github.mrazjava.booklink.openlibrary.rest;

import io.swagger.annotations.Api;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Api(
        tags = {"OpenLibrary"}
)
@RestController
@RequestMapping("rest/v1/openlibrary")
public class OpenLibraryRestController {
}
