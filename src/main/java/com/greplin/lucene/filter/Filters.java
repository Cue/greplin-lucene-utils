/*
 * Copyright 2011 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.FilterClause;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Arrays;

/**
 * Static utility methods for Filters.
 */
public final class Filters {

  /** Not instantiable. */
  private Filters() { }


  /**
   * Filter that matches all documents.
   */
  public static final Filter MATCH_ALL = new Filter() {
    @Override
    public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
      return new AllDocSet(reader.maxDoc());
    }
  };


  /**
     * Returns a filter that allows documents that match any constituent
     * filter.  For convenience, null values are accepted and ignored.  If
     * all values are null, null will be returned.
     * @param filters the filters to combine
     * @return the combined filter
     */
    @Nullable public static Filter or(final Iterable<Filter> filters) {
      final BooleanFilter booleanFilter = new BooleanFilter();

      Filter lastFilter = null;
      int count = 0;
      for (Filter filter : filters) {
        if (filter != null) {
          booleanFilter.add(new FilterClause(
              filter, BooleanClause.Occur.SHOULD));
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
   * Returns a filter that allows documents that match any constituent
   * filter.  For convenience, null values are accepted and ignored.  If
   * all values are null, null will be returned.
   * @param filters the filters to combine
   * @return the combined filter
   */
  @Nullable public static Filter or(final Filter... filters) {
    return or(Arrays.asList(filters));
  }


  /**
   * Returns a filter that allows documents that match every constituent
   * filter.  For convenience, null values are accepted and ignored.  If
   * all values are null, null will be returned.
   * @param filters the filters to combine
   * @return the combined filter
   */
  @Nullable public static Filter and(final Iterable<Filter> filters) {
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
   * Returns a filter that allows documents that match every constituent
   * filter.  For convenience, null values are accepted and ignored.  If
   * all values are null, null will be returned.
   * @param filters the filters to combine
   * @return the combined filter
   */
  @Nullable public static Filter and(final Filter... filters) {
    return and(Arrays.asList(filters));
  }


  /**
   * Returns a filter matching the inverse of the given filter.
   * @param filter the filter to return the opposite of
   * @return the negated filter
   */
  public static Filter not(final Filter filter) {
    final BooleanFilter booleanFilter = new BooleanFilter();
    booleanFilter.add(new FilterClause(filter, BooleanClause.Occur.MUST_NOT));
    return booleanFilter;
  }


  /**
   * Returns a terms filter matching the given terms.
   * @param terms the terms to match
   * @return the terms filter
   */
  public static TermsFilter terms(final Iterable<Term> terms) {
    TermsFilter result = new TermsFilter();
    for (Term term : terms) {
      result.addTerm(term);
    }
    return result;
  }

  /**
   * Returns a terms filter matching the given terms.
   * @param terms the terms to match
   * @return the terms filter
   */
  public static TermsFilter terms(final Term... terms) {
    return terms(Arrays.asList(terms));
  }

}
