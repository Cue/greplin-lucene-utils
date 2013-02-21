/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;

/**
* Bits provider based on a Filter.
*/
public final class FilterBitsProvider extends BitsProvider {

  /**
   * The filter to provide bits for.
   */
  private final Filter filter;


  /**
   * Creates a bits provider based on the given filter.
   * @param filter the filter.
   */
  public FilterBitsProvider(final Filter filter) {
    this.filter = filter;
  }


  @Override
  public Object getCacheKey() {
    return this.filter;
  }


  @Override
  public Bits get(final IndexReader reader) throws IOException {
    DocIdSet docIdSet = this.filter.getDocIdSet(reader);

    if (docIdSet == null) {
      return new Bits.MatchNoBits(reader.maxDoc());
    } else if (docIdSet instanceof Bits && docIdSet.isCacheable()) {
      return (Bits) docIdSet;
    } else {
      FixedBitSet result = new FixedBitSet(reader.maxDoc());
      DocIdSetIterator iterator = docIdSet.iterator();
      if (iterator != null) {
        result.or(iterator);
      }
      return result;
    }
  }


  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    FilterBitsProvider that = (FilterBitsProvider) other;
    return this.filter.equals(that.filter);
  }


  @Override
  public int hashCode() {
    return this.filter.hashCode();
  }


  @Override
  public String toString() {
    return this.filter.toString();
  }

}
