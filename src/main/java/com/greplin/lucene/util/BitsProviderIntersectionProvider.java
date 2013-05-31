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

import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.Bits;

import java.io.IOException;

/**
 * Intersection provider based on a bits provider.
 */
public final class BitsProviderIntersectionProvider
    implements IntersectionProvider {

  /**
   * The bits provider to intersect with.
   */
  private final BitsProvider bitsProvider;


  /**
   * Constructs a new intersection provider with the given bits provider.
   * @param bitsProvider the bits provider to intersect with
   */
  public BitsProviderIntersectionProvider(final BitsProvider bitsProvider) {
    this.bitsProvider = bitsProvider;
  }


  @Override
  public Intersection get(final IndexReader reader) throws IOException {
    return new BitsIntersection(this.bitsProvider.get(reader));
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    BitsProviderIntersectionProvider that =
        (BitsProviderIntersectionProvider) o;
    return this.bitsProvider.equals(that.bitsProvider);
  }


  @Override
  public int hashCode() {
    return this.bitsProvider.hashCode();
  }


  @Override
  public String toString() {
    return "BitsProviderIntersectionProvider{"
        + "bitsProvider=" + this.bitsProvider
        + '}';
  }


  /**
   * Intersection with a Bits object.
   */
  private static class BitsIntersection implements Intersection {

    /**
     * The bits to intersect with.
     */
    private final Bits bits;


    /**
     * Constructs a new intersection with the given Bits object.
     * @param bits the bits to intersect with
     */
    public BitsIntersection(final Bits bits) {
      this.bits = bits;
    }


    @Override
    public boolean advanceToNextIntersection(final TermDocs termDocs)
        throws IOException {
      while (termDocs.next()) {
        if (this.bits.get(termDocs.doc())) {
          return true;
        }
      }
      return false;
    }

  }


}
