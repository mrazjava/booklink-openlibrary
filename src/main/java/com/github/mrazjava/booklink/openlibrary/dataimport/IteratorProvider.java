package com.github.mrazjava.booklink.openlibrary.dataimport;

import org.apache.commons.io.LineIterator;

import java.io.Closeable;
import java.util.Iterator;

/**
 * @param <I> type of iterator to provide
 * @param <S> source type based on which iterator is generated
 */
public interface IteratorProvider<I, S> {

    Closeable open(S source);

    Iterator<I> provide(Closeable closeable);
}
