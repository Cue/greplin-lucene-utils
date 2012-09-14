/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import com.google.common.base.Objects;
import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;
import java.util.Set;

/**
 * Query that is filtered through a simple predicate function.
 *
 * Based on {@link org.apache.lucene.search.CachingWrapperFilter}.
 */
public class PredicateQuery extends Query {

  /**
   * Double the standard max-clause count for BooleanQueries (1024) since it is
   * often too low for large rewrites.
   */
  private static final int MAX_CLAUSE_COUNT = 2048;

  static {
    BooleanQuery.setMaxClauseCount(MAX_CLAUSE_COUNT);
  }

  /**
   * The underlying query.
   */
  private final Query query;


  /**
   * The predicate to match against.
   */
  private final BitsProvider predicate;


  /**
   * Constructs a new query which applies a predicate to filter the results
   * of the original query.
   * @param query Query to be filtered.
   * @param predicate Provider of predicates to apply to the query.
   */
  public PredicateQuery(final Query query, final BitsProvider predicate) {
    this.query = query;
    this.predicate = predicate;
  }

  /**
   * Returns a Weight that applies the predicate to the enclosed query's Weight.
   * This is accomplished by overriding the Scorer returned by the Weight.
   * @param searcher the searcher to create a weight for.
   * @return a Weight that applies the predicate to the query.
   * @throws IOException if IO issues occur.
   */
  @Override
  public Weight createWeight(final Searcher searcher) throws IOException {
    final Weight weight = this.query.createWeight(searcher);
    final Similarity similarity = this.query.getSimilarity(searcher);

    return new Weight() {
      private float value;

      // pass these methods through to enclosed query's weight
      @Override
      public float getValue() {
        return this.value;
      }

      @Override
      public boolean scoresDocsOutOfOrder() {
        return false;
      }

      public float sumOfSquaredWeights() throws IOException {
        return weight.sumOfSquaredWeights() * getBoost() * getBoost();
      }

      @Override
      public void normalize(final float v) {
        weight.normalize(v * getBoost()); // incorporate boost
        this.value = weight.getValue();
      }

      @Override
      public Explanation explain(final IndexReader reader, final int i)
          throws IOException {
        Explanation inner = weight.explain(reader, i);
        Bits predicate = PredicateQuery.this.predicate.get(reader);
        if (predicate.get(i)) {
          return inner;
        } else {
          Explanation result = new Explanation(0.0f,
              "failure to match predicate: " + predicate.toString());
          result.addDetail(inner);
          return result;
        }
      }

      @Override
      public Query getQuery() {
        return PredicateQuery.this;
      }

      @Override
      public Scorer scorer(final IndexReader reader,
                           final boolean scoreDocsInOrder,
                           final boolean topScorer)
          throws IOException {
        Bits predicate = PredicateQuery.this.predicate.get(reader);
        return PredicateQuery.getScorer(
            reader, similarity, weight, this, predicate);
      }
    };
  }


  /**
   * Creates a scorer that matches documents matched by both the query and
   * the predicate.
   * @param indexReader the atomic reader
   * @param similarity the Similarity to use
   * @param weight the weight object of the underlying query
   * @param wrapperWeight the weight object.
   * @param predicate the Bits to use.
   * @return a scorer that matches the documents matched by the filter.
   * @throws IOException on IO error
   */
  private static Scorer getScorer(
      final IndexReader indexReader, final Similarity similarity,
      final Weight weight, final Weight wrapperWeight,
      final Bits predicate) throws IOException {
    // We will advance() this scorer, so we set inorder=true/toplevel=false.
    final Scorer scorer = weight.scorer(indexReader, true, false);
    return (scorer == null) ? null : new Scorer(similarity, wrapperWeight) {

      @Override
      public int nextDoc() throws IOException {
        for (;;) {
          int docId = scorer.nextDoc();
          if (docId == DocIdSetIterator.NO_MORE_DOCS
              || predicate.get(scorer.docID())) {
            return docId;
          }
        }
      }

      @Override
      public int advance(final int target) throws IOException {
        scorer.advance(target);
        if (scorer.docID() == DocIdSetIterator.NO_MORE_DOCS
            || (scorer.docID() >= 0 && predicate.get(scorer.docID()))) {
          return scorer.docID();
        }
        return nextDoc();
      }

      @Override
      public int docID() {
        return scorer.docID();
      }

      @Override
      public float score() throws IOException {
        return scorer.score();
      }
    };
  }


  /**
   * Rewrites the wrapped query.
   * @param reader the reader to rewrite for.
   * @return the rewritten query.
   * @throws IOException if IO issues occur.
   */
  @Override
  public Query rewrite(final IndexReader reader) throws IOException {
    Query rewritten = this.query.rewrite(reader);
    if (rewritten != this.query) {
      return new PredicateQuery(rewritten, this.predicate);
    } else {
      return this;
    }
  }


  /**
   * @return the underlying query.
   */
  public Query getQuery() {
    return this.query;
  }


  /**
   * @return the predicate.
   */
  public BitsProvider getPredicate() {
    return this.predicate;
  }


  @Override
  public void extractTerms(final Set<Term> terms) {
    getQuery().extractTerms(terms);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(this.query, this.predicate);
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    PredicateQuery that = (PredicateQuery) o;
    return Objects.equal(this.query, that.query)
        && Objects.equal(this.predicate, that.predicate);
  }


  /**
   * Prints a user-readable version of this query.
   * @param s the name of the field.
   * @return a user-readable version of this query.
   */
  @Override
  public String toString(final String s) {
    StringBuilder buffer = new StringBuilder();
    buffer.append("predicate(");
    buffer.append(this.query.toString(s));
    buffer.append(")->");
    buffer.append(this.predicate);
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

}
