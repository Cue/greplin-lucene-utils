/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;
import java.util.Set;


/**
 * A query that matches all documents the given filter matches, with
 * a constant score of 1.
 *
 * Note: the bits are retrieved from the filter each time this
 * query is used in a search - use a CachingWrapperFilter to avoid
 * regenerating the bits every time.
 *
 * Based on {@link org.apache.lucene.search.CachingWrapperFilter}.
 */
public class FilterWrapperQuery extends Query {

  /**
   * The filter to emulate.
   */
  private final Filter filter;


  /**
   * Constructs a new query which matches the same documents as
   * the given filter.
   * @param filter the filter
   */
  public FilterWrapperQuery(final Filter filter) {
    this.filter = filter;
  }


  /**
   * @param searcher the searcher to create a weight for
   * @return a Weight that matches the filter's documents
   * @throws IOException if IO issues occur
   */
  @Override
  public Weight createWeight(final Searcher searcher) throws IOException {
    final Similarity similarity = searcher.getSimilarity();

    return new Weight() {
      private float value;


      @Override
      public float getValue() {
        return this.value;
      }


      @Override
      public boolean scoresDocsOutOfOrder() {
        return false;
      }


      public float sumOfSquaredWeights() throws IOException {
        return getBoost() * getBoost(); // boost sub-weight
      }


      @Override
      public void normalize(final float v) {
        this.value = v;
      }


      @Override
      public Explanation explain(final IndexReader ir, final int i)
          throws IOException {
        Filter f = FilterWrapperQuery.this.filter;
        DocIdSet docIdSet = f.getDocIdSet(ir);
        DocIdSetIterator docIdSetIterator = docIdSet == null
            ? DocIdSet.EMPTY_DOCIDSET.iterator() : docIdSet.iterator();
        if (docIdSetIterator == null) {
          docIdSetIterator = DocIdSet.EMPTY_DOCIDSET.iterator();
        }
        if (docIdSetIterator.advance(i) == i) {
          return new Explanation(1.0f, "matched filter: " + f.toString());
        } else {
          return new Explanation(
              0.0f, "failure to match filter: " + f.toString());
        }
      }

      @Override
      public Query getQuery() {
        return FilterWrapperQuery.this;
      }


      @Override
      public Scorer scorer(final IndexReader indexReader,
                           final boolean scoreDocsInOrder,
                           final boolean topScorer)
          throws IOException {
        return FilterWrapperQuery.getFilteredScorer(
            indexReader, similarity, this, FilterWrapperQuery.this.filter);
      }
    };
  }

  /**
   * Creates a scorer that matches documents matched by the filter.
   * @param indexReader the atomic reader
   * @param similarity the Similarity to use (deprecated)
   * @param wrapperWeight the weight object.
   * @param filter the Filter to wrap
   * @return a scorer that matches the documents matched by the filter.
   * @throws IOException on IO error
   */
  private static Scorer getFilteredScorer(final IndexReader indexReader,
                                          final Similarity similarity,
                                          final Weight wrapperWeight,
                                          final Filter filter)
      throws IOException {
    assert filter != null;

    DocIdSet filterDocIdSet = filter.getDocIdSet(indexReader);
    if (filterDocIdSet == null) {
      // this means the filter does not accept any documents.
      return null;
    }

    final DocIdSetIterator filterIter = filterDocIdSet.iterator();
    if (filterIter == null) {
      // this means the filter does not accept any documents.
      return null;
    }

    return new Scorer(similarity, wrapperWeight) {
      private int filterDoc = -1;


      @Override
      public void score(final Collector collector) throws IOException {
        int filterDoc = filterIter.nextDoc();
        collector.setScorer(this);
        while (filterDoc != NO_MORE_DOCS) {
          collector.collect(filterDoc);
          filterDoc = filterIter.nextDoc();
        }
      }


      @Override
      public int nextDoc() throws IOException {
        this.filterDoc = filterIter.nextDoc();
        return this.filterDoc;
      }

      @Override
      public int advance(final int target) throws IOException {
        if (target > this.filterDoc) {
          this.filterDoc = filterIter.advance(target);
        }
        return this.filterDoc;
      }

      @Override
      public int docID() {
        return this.filterDoc;
      }

      @Override
      public float score() throws IOException {
        return 1.0f;
      }
    };
  }


  @Override
  public Query rewrite(final IndexReader reader) throws IOException {
    return this;
  }


  @Override
  public void extractTerms(final Set<Term> terms) {
  }


  @Override
  public String toString(final String s) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("filter(");
    buffer.append(this.filter);
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }


  @Override
  public boolean equals(final Object o) {
    return o instanceof FilterWrapperQuery
        && this.filter.equals(((FilterWrapperQuery) o).filter);
  }


  @Override
  public int hashCode() {
    return this.filter.hashCode() + Float.floatToRawIntBits(getBoost());
  }

}
