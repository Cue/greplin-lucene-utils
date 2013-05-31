/*
 * Copyright 2013 The greplin-lucene-utils Authors.
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

package com.greplin.lucene.util;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;

import java.io.IOException;

/**
 * Intersection with another filter.
 */
public final class FilterIntersectionProvider implements IntersectionProvider {

  /**
   * Simple Intersection that matches nothing.
   */
  private static final Intersection NO_DOCS = new Intersection() {
    @Override
    public boolean advanceToNextIntersection(final TermDocs termDocs)
        throws IOException {
      return false;
    }
  };


  /**
   * The filter to intersect with.
   */
  private final Filter filter;


  /**
   * Creates a new intersection provider that intersects with the given filter.
   * @param filter the filter to intersect with
   */
  public FilterIntersectionProvider(final Filter filter) {
    this.filter = filter;
  }


  @Override
  public Intersection get(final IndexReader reader) throws IOException {
    DocIdSet docs = this.filter.getDocIdSet(reader);
    return docs == null ? NO_DOCS : new DocIdSetIntersection(docs);
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    FilterIntersectionProvider that = (FilterIntersectionProvider) o;
    return this.filter.equals(that.filter);
  }


  @Override
  public int hashCode() {
    return this.filter.hashCode();
  }


  @Override
  public String toString() {
    return "FilterIntersectionProvider{"
        + "filter=" + this.filter
        + '}';
  }


  /**
   * Intersection implementation that intersects with a DocIdSet.
   */
  private static class DocIdSetIntersection implements Intersection {

    /**
     * Iterator over the doc id set we are intersecting with.
     */
    private final DocIdSetIterator docIdSetIterator;


    /**
     * Constructs a new DocIdSetIntersection object.
     * @param docIdSet the doc id set to intersect with
     * @throws IOException if Lucene encounters an IO error
     */
    public DocIdSetIntersection(final DocIdSet docIdSet) throws IOException {
      this.docIdSetIterator = docIdSet.iterator();
    }


    @Override
    public boolean advanceToNextIntersection(final TermDocs termDocs)
        throws IOException {
      int currentIterator = this.docIdSetIterator.nextDoc();
      if (!termDocs.next()) {
        return false;
      }
      int currentTermDocs = termDocs.doc();
      while (currentIterator != currentTermDocs) {
        if (currentIterator < currentTermDocs) {
          currentIterator = this.docIdSetIterator.advance(currentTermDocs);
          if (currentIterator == DocIdSetIterator.NO_MORE_DOCS) {
            return false;
          }
        } else {
          if (!termDocs.skipTo(currentIterator)) {
            return false;
          }
          currentTermDocs = termDocs.doc();
        }
      }
      return true;
    }

  }

}
