/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.google.common.base.Objects;
import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import java.io.IOException;

/**
 * A filter that computed on a filtered index reader.  Should produce the
 * same results as a PredicateFilter but could be faster especially if the
 * underlying filter is expensive.
 *
 * NOTE: This filter only works if the underlying filter actually operates
 * on the reader's termDocs or termPositions.
 */
public class PrePredicateFilter extends Filter {

  /**
   * The underlying filter.
   */
  private final Filter filter;


  /**
   * The predicate to match against.
   */
  private final BitsProvider predicate;


  /**
   * Creates a predicate filter.
   * @param filter the filter to further filter.
   * @param predicate the predicate.
   */
  public PrePredicateFilter(final Filter filter, final BitsProvider predicate) {
    this.filter = filter;
    this.predicate = predicate;
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    return this.filter.getDocIdSet(
        FilteredIndexReader.wrap(reader, this.predicate));
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PrePredicateFilter that = (PrePredicateFilter) o;
    return this.filter.equals(that.filter)
        && this.predicate.equals(that.predicate);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(this.filter, this.predicate);
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(PrePredicateFilter.class)
        .add("filter", this.filter)
        .add("predicate", this.predicate)
        .toString();
  }

}
