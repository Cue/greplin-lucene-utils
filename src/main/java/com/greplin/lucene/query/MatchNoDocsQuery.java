/*
 * Copyright 2011 The greplin-lucene-utils Authors.
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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ComplexExplanation;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Scorer;
import org.apache.lucene.search.Searcher;
import org.apache.lucene.search.Similarity;
import org.apache.lucene.search.Weight;

import java.io.IOException;
import java.util.Set;

/**
 * Query that matches no documents.
 */
public final class MatchNoDocsQuery extends Query {

  /**
   * Since all instances of this class are equal to each other,
   * we have a constant hash code.
   */
  private static final int HASH_CODE = 12345;

  /**
   * Scorer that returns a score of zero for all documents.
   */
  private static class MatchNothingScorer extends Scorer {

    /**
     * Creates a new scorer that scores all documents as 0.
     * @param similarity the similarity implementation
     * @throws IOException when IO errors are encountered
     */
    MatchNothingScorer(final Similarity similarity) throws IOException {
      super(similarity);
    }

    @Override
    public int docID() {
      return -1;
    }

    @Override
    public int nextDoc() throws IOException {
      return NO_MORE_DOCS;
    }

    @Override
    public float score() {
      return 0;
    }

    @Override
    public int advance(final int target) throws IOException {
      return NO_MORE_DOCS;
    }
  }

  /**
   * Weight implementation that matches no documents.
   */
  private class MatchNoDocsWeight extends Weight {
    /**
     * The similarity implementation.
     */
    private final Similarity similarity;


    /**
     * Creates a new weight that matches nothing.
     * @param searcher the search to match for
     */
    public MatchNoDocsWeight(final Searcher searcher) {
      this.similarity = searcher.getSimilarity();
    }

    @Override
    public String toString() {
      return "weight(" + MatchNoDocsQuery.this + ")";
    }

    @Override
    public Query getQuery() {
      return MatchNoDocsQuery.this;
    }

    @Override
    public float getValue() {
      return 0;
    }

    @Override
    public float sumOfSquaredWeights() {
      return 0;
    }

    @Override
    public void normalize(final float queryNorm) {
    }

    @Override
    public Scorer scorer(final IndexReader reader,
                         final boolean scoreDocsInOrder,
                         final boolean topScorer) throws IOException {
      return new MatchNothingScorer(this.similarity);
    }

    @Override
    public Explanation explain(final IndexReader reader,
                               final int doc) {
      return new ComplexExplanation(false, 0, "MatchNoDocs matches nothing");
    }
  }

  @Override
  public Weight createWeight(final Searcher searcher) {
    return new MatchNoDocsWeight(searcher);
  }

  @Override
  public void extractTerms(final Set<Term> terms) {
  }

  @Override
  public String toString(final String field) {
    return "MatchNoDocsQuery";
  }

  @Override
  public boolean equals(final Object o) {
    return o instanceof MatchAllDocsQuery;
  }

  @Override
  public int hashCode() {
    return HASH_CODE;
  }
}
