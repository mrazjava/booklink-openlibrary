package com.github.mrazjava.booklink.openlibrary.dataimport;

import com.github.mrazjava.booklink.openlibrary.OpenLibraryIntegrationException;

import java.util.Iterator;

/**
 * @param <I> type of iterator to provide
 * @param <S> source type based on which iterator is generated
 */
public interface IteratorProvider<I, S> {

    Iterator<I> provide(S source);
}
