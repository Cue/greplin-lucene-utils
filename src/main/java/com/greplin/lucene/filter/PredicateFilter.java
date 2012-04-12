/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.greplin.lucene.query.DocPredicate;
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
  private final DocPredicate predicate;


  /**
   * Creates a predicate filter.
   * @param filter the filter to further filter.
   * @param predicate the predicate.
   */
  public PredicateFilter(final Filter filter, final DocPredicate predicate) {
    this.filter = filter;
    this.predicate = predicate;
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    return new PredicateDocIdSet(
        this.filter.getDocIdSet(reader),
        this.predicate.get(reader));
  }

}
