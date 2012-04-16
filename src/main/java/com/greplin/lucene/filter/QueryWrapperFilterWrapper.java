/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.OpenBitSetDISI;

import java.io.IOException;

/**
 * Lucene blows up when we use a QueryWrapperFilter that returns a null
 * doc id set, in a BooleanFilter. So this wraps a QueryWrapperFilter that
 * returns an empty set if the underylying filter is null.
 */
public class QueryWrapperFilterWrapper extends Filter {

  /**
   * The underlying filter.
   */
  private final Filter underlying;


  /**
   * Creates a wrapper query that ensures the underlying query always returns
   * a valid non-null doc id set.
   * @param underlying the underlying filter.
   */
  public QueryWrapperFilterWrapper(final Filter underlying) {
    this.underlying = underlying;
  }

  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    DocIdSet docIdSet = this.underlying.getDocIdSet(reader);
    if (docIdSet == null || docIdSet.iterator() == null) {
      return OpenBitSetDISI.EMPTY_DOCIDSET;
    } else {
      return docIdSet;
    }
  }

  @Override
  public String toString() {
    return "QueryWrapperFilterWrapper{" + "underlying=" + this.underlying + '}';
  }

}
