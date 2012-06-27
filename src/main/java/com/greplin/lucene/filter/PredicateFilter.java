/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.google.common.base.Objects;
import com.greplin.lucene.predicate.BitsProvider;
import com.greplin.lucene.predicate.PredicateDocIdSet;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import java.io.IOException;

/**
 * A filter that is further filtered by a predicate.
 */
public class PredicateFilter extends Filter {

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
  public PredicateFilter(final Filter filter, final BitsProvider predicate) {
    assert filter != null;
    assert predicate != null;
    this.filter = filter;
    this.predicate = predicate;
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    return new PredicateDocIdSet(
        this.filter.getDocIdSet(reader),
        this.predicate.get(reader));
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PredicateFilter that = (PredicateFilter) o;
    return this.filter.equals(that.filter)
        && this.predicate.equals(that.predicate);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(this.filter, this.predicate);
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(PredicateFilter.class)
        .add("filter", this.filter)
        .add("predicate", this.predicate)
        .toString();
  }

}
