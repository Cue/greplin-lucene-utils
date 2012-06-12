/*
 * Copyright 2012 The greplin-lucene-utils Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greplin.lucene.query;

import com.google.common.base.Objects;
import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.function.CustomScoreProvider;
import org.apache.lucene.search.function.CustomScoreQuery;
import org.apache.lucene.util.Bits;

import java.io.IOException;

/**
 * Query that gives an additive bonus to any document that also matches
 * the given predicate.
 */
public class PredicateBonusQuery extends CustomScoreQuery {

  /**
   * The underlying query.
   */
  private final Query underlyingQuery;

  /**
   * The predicate for bonuses.
   */
  private final BitsProvider predicate;

  /**
   * The bonus to apply.
   */
  private final float bonus;


  /**
   * Constructs a PredicateBonusQuery.
   * @param subQuery the underlying query.
   * @param predicate the predicate that decides if a bonus is awarded.
   * @param bonus the bonus to award.
   */
  public PredicateBonusQuery(
      final Query subQuery, final BitsProvider predicate, final float bonus) {
    super(subQuery);
    this.underlyingQuery = subQuery;
    this.predicate = predicate;
    this.bonus = bonus;
  }


  @Override
  public CustomScoreProvider getCustomScoreProvider(final IndexReader r)
      throws IOException {
    return new PredicateBonusScoreProvider(
        r, this.predicate.get(r), this.bonus);
  }


  @Override
  public String toString() {
    return "(" + this.underlyingQuery
        + " [" + this.bonus + " if " + this.predicate + "])";
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    if (!super.equals(o)) {
      return false;
    }

    PredicateBonusQuery that = (PredicateBonusQuery) o;
    return Objects.equal(this.underlyingQuery, that.underlyingQuery)
        && Objects.equal(this.predicate, that.predicate)
        && this.bonus == that.bonus;
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(
        super.hashCode(), this.underlyingQuery, this.predicate, this.bonus);
  }


  /**
   * Score provider for predicate bonus.
   */
  private static final class PredicateBonusScoreProvider
      extends CustomScoreProvider {

    /**
     * The bits for the segment reader.
     */
    private final Bits bits;


    /**
     * The bonus to add.
     */
    private final float bonus;


    /**
     * Constructs a score provider.
     * @param reader the segment reader.
     * @param bits the bits for this reader.
     * @param bonus the number of points to award for matching the bonus.
     */
    private PredicateBonusScoreProvider(
        final IndexReader reader, final Bits bits, final float bonus) {
      super(reader);
      this.bits = bits;
      this.bonus = bonus;
    }


    @Override
    public float customScore(
        final int doc, final float subQueryScore, final float valSrcScore)
        throws IOException {

      return subQueryScore > 0
          ? subQueryScore + (this.bits.get(doc) ? this.bonus : 0)
          : subQueryScore;
    }


    @Override
    public Explanation customExplain(
        final int doc,
        final Explanation subQueryExplanation,
        final Explanation[] valSrcExplanations)
        throws IOException {
      float bonus = subQueryExplanation.getValue() > 0
          ? (this.bits.get(doc) ? this.bonus : 0)
          : subQueryExplanation.getValue();

      Explanation result = new Explanation(
          subQueryExplanation.getValue() + bonus, "bonus = " + bonus);
      result.addDetail(subQueryExplanation);
      return result;
    }


    @Override
    public Explanation customExplain(
        final int doc,
        final Explanation subQueryExplanation,
        final Explanation valSrcExplanation)
        throws IOException {
      return customExplain(doc, subQueryExplanation, (Explanation[]) null);
    }
  }

}
