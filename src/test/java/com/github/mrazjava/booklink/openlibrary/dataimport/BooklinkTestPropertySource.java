package com.github.mrazjava.booklink.openlibrary.dataimport;

import org.springframework.test.context.TestPropertySource;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@TestPropertySource(properties = {
        "booklink.di.start-from-record-no: 0",
        "booklink.di.frequency-check: 20",
        "booklink.di.persist: false",
        "booklink.di.persist-override: true",
        "booklink.di.image-pull: false",
        "booklink.di.with-mongo-images: false",
        "booklink.di.fetch-original-images: false"
})
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface BooklinkTestPropertySource {
}
