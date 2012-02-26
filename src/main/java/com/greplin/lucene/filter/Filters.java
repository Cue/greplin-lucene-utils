/*
 * Copyright 2011 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;

import javax.annotation.Nullable;

/**
 * Static utility methods for Filters.
 */
public final class Filters {

  /** Not instantiable. */
  private Filters() { }


  /**
   * Returns a filter that allows documents that match every constituent
   * filter.  For convenience, null values are accepted and ignored.  If
   * all values are null, null will be returned.
   * @param filters the filters to combine
   * @return the combined filter
   */
  @Nullable public static Filter and(@Nullable final Filter... filters) {
    final BooleanFilter booleanFilter = new BooleanFilter();

    Filter lastFilter = null;
    int count = 0;
    for (Filter filter : filters) {
      if (filter != null) {
        booleanFilter.add(new FilterClause(filter, BooleanClause.Occur.MUST));
        count += 1;
        lastFilter = filter;
      }
    }

    if (count == 0) {
      return null;
    } else if (count == 1) {
      return lastFilter;
    } else {
      return booleanFilter;
    }
  }


  /**
   * Returns a filter matching the inverse of the given filter.
   * @param filter the filter to return the opposite ofnegate
   * @return the negated filter
   */
  public static Filter not(final Filter filter) {
    final BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(filter, BooleanClause.Occur.MUST_NOT));
    return booleanFilter;
  }

}
