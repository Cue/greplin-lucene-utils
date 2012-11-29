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

package com.greplin.lucene.filter;

import com.google.common.base.Objects;
import com.google.common.collect.Sets;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;
import java.util.Set;

/**
 * Constructs a filter for docs matching any of the terms added to this class.
 *
 * Similar to {@link org.apache.lucene.search.TermsFilter} but has a much
 * better toString().
 */
public class TermsFilter extends Filter {

  /**
   * Terms to match.
   */
  private final Set<Term> terms = Sets.newTreeSet();


  /**
   * Creates a TermsFilter composed from multiple terms.
   * @param terms the terms to add to the filter.
   * @return the new TermsFilter.
   */
  public static TermsFilter from(final Term ... terms) {
    TermsFilter filter = new TermsFilter();
    for (Term term : terms) {
      filter.addTerm(term);
    }
    return filter;
  }

  /**
   * Adds a term to the list of acceptable terms.
   * @param term the term to add.
   */
  public void addTerm(final Term term) {
    this.terms.add(term);
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    FixedBitSet result = new FixedBitSet(reader.maxDoc());
    TermDocs td = reader.termDocs();
    try {
      for (Term term : this.terms) {
        td.seek(term);
        while (td.next()) {
          result.set(td.doc());
        }
      }
    } finally {
      td.close();
    }
    return result;
  }

  @Override
  public boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if ((obj == null) || (obj.getClass() != this.getClass())) {
      return false;
    }
    TermsFilter that = (TermsFilter) obj;
    return that.terms.equals(this.terms);
  }

  @Override
  public int hashCode() {
    return this.terms.hashCode();
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(TermsFilter.class)
        .add("terms", this.terms)
        .toString();
  }
}
