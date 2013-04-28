/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.util.FixedBitSet;

/**
 * A mutable sub-slice of a DocIdSet.
 */
public class FixedBitSetSlice extends DocIdSetSlice {

  /**
   * The underlying fixed bit set.
   */
  private final FixedBitSet underlying;


  /**
   * Constructs a DocIdSet that is a slice of a FixedBitSet.
   * @param underlying the underlying FixedBitSet
   * @param start the start of the slice
   * @param end the end of the slice
   */
  public FixedBitSetSlice(
      final FixedBitSet underlying, final int start, final int end) {
    super(underlying, start, end);
    this.underlying = underlying;
  }


  /**
   * @return the underlying FixedBitSet
   */
  public FixedBitSet getUnderlying() {
    return this.underlying;
  }


  /**
   * Gets the index-th bit of this slice, meaning the
   * (start + index)th bit of the underlying bitset.
   * @param index the index into the slice to get
   * @return whether the bit is set
   */
  public boolean get(final int index) {
    return this.underlying.get(index + this.getStart());
  }


  /**
   * Sets the index-th bit of this slice, meaning the
   * (start + index)th bit of the underlying bitset.
   * @param index the index into the slice to set
   */
  public void set(final int index) {
    this.underlying.set(index + this.getStart());
  }


  /**
   * Clears the index-th bit of this slice, meaning the
   * (start + index)th bit of the underlying bitset.
   * @param index the index into the slice to clear
   */
  public void clear(final int index) {
    this.underlying.clear(index + this.getStart());
  }

}
