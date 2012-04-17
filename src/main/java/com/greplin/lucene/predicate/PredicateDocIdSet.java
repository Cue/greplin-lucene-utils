/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.Bits;

import java.io.IOException;

/**
 * A doc id set that is further filtered by a predicate.
 */
public class PredicateDocIdSet extends DocIdSet {

  /**
   * The underlying doc id set.
   */
  private final DocIdSet docIdSet;


  /**
   * The predicate to match against.
   */
  private final Bits predicate;


  /**
   * Creates a predicate filter.
   * @param docIdSet the doc id set to filter.
   * @param predicate the predicate.
   */
  public PredicateDocIdSet(final DocIdSet docIdSet,
                           final Bits predicate) {
    this.docIdSet = docIdSet;
    this.predicate = predicate;
  }


  @Override
  public DocIdSetIterator iterator() throws IOException {
    DocIdSetIterator iterator = this.docIdSet.iterator();
    return iterator == null
        ? null
        :  new PredicateDocIdSetIterator(iterator, this.predicate);
  }


  /**
   * Wraps a doc id set iterator with a predicate.
   */
  private static final class PredicateDocIdSetIterator
      extends DocIdSetIterator {

    /**
     * The iterator to filter.
     */
    private final DocIdSetIterator iterator;


    /**
     * The predicate ot match against.
     */
    private final Bits predicate;


    /**
     * Creates a filtered doc id set iterator.
     * @param iterator the underlying iterator.
     * @param predicate the predicate to filter with.
     */
    private PredicateDocIdSetIterator(final DocIdSetIterator iterator,
                                      final Bits predicate) {
      this.iterator = iterator;
      this.predicate = predicate;
    }


    @Override
    public int docID() {
      return this.iterator.docID();
    }


    @Override
    public int nextDoc() throws IOException {
      for (;;) {
        int next = this.iterator.nextDoc();
        if (next == NO_MORE_DOCS || this.predicate.get(next)) {
          return next;
        }
      }
    }


    @Override
    public int advance(final int target) throws IOException {
      int docId = this.iterator.advance(target);
      if (docId == NO_MORE_DOCS || this.predicate.get(docId)) {
        return docId;
      } else {
        return nextDoc();
      }
    }

  }
}
