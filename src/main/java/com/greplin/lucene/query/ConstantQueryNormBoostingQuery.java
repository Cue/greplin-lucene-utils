/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import com.google.common.base.Objects;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;

import java.io.IOException;

/**
 * Boosting query fork that doesn't break norms.
 */
public class ConstantQueryNormBoostingQuery extends Query {

  /**
   * The amount of the boost.
   */
  private final float boost;


  /**
   * The query to match.
   */
  private final Query match;


  /**
   * The query to base boosting on.
   */
  private final Query context;


  /**
   * Creates a new boosting query.
   * @param match the query to match.
   * @param context the query to base boosting on.
   * @param boost the amount of the boost.
   */
  public ConstantQueryNormBoostingQuery(
      final Query match, final Query context, final float boost) {
    this.match = match;
    this.context = (Query) context.clone();
    this.boost = boost;
    this.context.setBoost(0.0f);
  }


  @Override
  public Query rewrite(final IndexReader reader) throws IOException {
    BooleanQuery result = new BooleanQuery() {

      @Override
      public Similarity getSimilarity(final Searcher searcher) {
        final Similarity base = searcher.getSimilarity();

        return new DefaultSimilarity() {

          @Override
          public float queryNorm(final float sumOfSquaredWeights) {
            return base.queryNorm(sumOfSquaredWeights);
          }


          @Override
          public float coord(final int overlap, final int max) {
            switch (overlap) {
              case 1: // matched only one clause
                return 1.0f;

              case 2: // matched both clauses
                return ConstantQueryNormBoostingQuery.this.boost;

              default:
                return 0.0f;
            }
          }

        };
      }
    };

    result.add(this.match, BooleanClause.Occur.MUST);
    result.add(this.context, BooleanClause.Occur.SHOULD);

    return result;
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(this.boost, this.context, this.match);
  }


  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (obj == null) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    ConstantQueryNormBoostingQuery that = (ConstantQueryNormBoostingQuery) obj;
    return Float.floatToIntBits(this.boost) == Float.floatToIntBits(that.boost)
        && Objects.equal(this.context, that.context)
        && Objects.equal(this.match, that.match);
  }


  @Override
  public String toString(final String field) {
    return this.match.toString(field) + "/" + this.context.toString(field);
  }

}
