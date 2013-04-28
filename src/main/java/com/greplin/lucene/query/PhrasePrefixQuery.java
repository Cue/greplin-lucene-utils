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

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.util.ToStringUtils;

import java.io.IOException;
import java.util.List;

/**
 * A Query that matches documents containing phrases with a specified prefix.
 */
public class PhrasePrefixQuery extends Query {
  /**
   * The field to match against.
   */
  private final String field;

  /**
   * The terms in the phrase prefix.
   */
  private final List<String> terms;

  /**
   * Constructs a query for phrases starting with <code>prefix</code>.
   * @param field the field to match
   */
  public PhrasePrefixQuery(final String field) {
    this.field = field;
    this.terms = Lists.newArrayList();
  }

  /**
   * Add a word.
   * @param word the word to add
   */
  public final void add(final String word) {
    this.terms.add(word);
  }

  /**
   * Returns the field.
   * @return the field
   */
  public final String getField() {
    return this.field;
  }

  /**
   * Returns the phrase terms of this query.
   * @return the phrase terms
   */
  public final List<String> getTerms() {
    return this.terms;
  }

  @Override
  public final String toString(final String field) {
    StringBuilder buffer = new StringBuilder();
    if (!this.field.equals(field)) {
      buffer.append(this.field);
      buffer.append(":");
    }
    buffer.append('"');
    buffer.append(Joiner.on(' ').join(this.terms));
    buffer.append("*\"");
    buffer.append(ToStringUtils.boost(getBoost()));
    return buffer.toString();
  }

  @Override
  public final int hashCode() {
    return new HashCodeBuilder()
        .append(super.hashCode())
        .append(this.field)
        .append(this.terms)
        .hashCode();
  }

  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    PhrasePrefixQuery other = (PhrasePrefixQuery) obj;
    return new EqualsBuilder()
        .append(this.field, other.field)
        .append(this.terms, other.terms)
        .isEquals();
  }


  /**
   * For the given index reader, gets terms that match the given prefix.
   * @param prefix the prefix
   * @param reader the index reader
   * @return matching terms
   * @throws IOException if IO errors are encountered
   */
  private Term[] getPrefixTerms(final String prefix,
                                final IndexReader reader) throws IOException {
    TermEnum enumerator = reader.terms(new Term(this.field, prefix));
    List<Term> terms = Lists.newArrayList();
    try {
      do {
        Term term = enumerator.term();
        if (term != null
            && term.text().startsWith(prefix)
            && term.field().equals(this.field)) {
          terms.add(term);
        } else {
          break;
        }
      } while (enumerator.next());
    } finally {
      enumerator.close();
    }
    if (terms.size() == 0) {
      return null;
    } else {
      return terms.toArray(new Term[terms.size()]);
    }
  }

  @Override
  public final Query rewrite(final IndexReader reader) throws IOException {
    Term[] prefixTerms = getPrefixTerms(
        this.terms.get(this.terms.size() - 1), reader);
    if (prefixTerms == null) {
      return new MatchNoDocsQuery();
    }

    MultiPhraseQuery query = new MultiPhraseQuery();
    for (int i = 0; i < this.terms.size() - 1; i++) {
      query.add(new Term(this.field, this.terms.get(i)));
    }
    query.add(prefixTerms);
    return query;
  }
}
