package com.greplin.lucene.filter;

import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;

/**
 * Like a MultiReader for FixedBitSets.  Uses linear search for position,
 * so it's not ideal for cases with a large number of parts.
 */
public class MultiFixedBitSet implements Bits {

  /**
   * The underlying bit sets.
   */
  private final FixedBitSet[] underlying;


  /**
   * The precomputed total length of the bit set.
   */
  private final int length;


  /**
   * Constructs a multi-part fixed bit set.
   * @param underlying the underlying fixed bit sets.
   */
  public MultiFixedBitSet(final FixedBitSet[] underlying) {
    this.underlying = underlying;
    int length = 0;
    for (FixedBitSet fixedBitSet : underlying) {
      length += fixedBitSet.length();
    }
    this.length = length;
  }


  /**
   * Sets the bit at the given position.
   * @param index the position to set.
   */
  public void set(final int index) {
    int i = 0;
    int offset = index;
    while (offset > this.underlying[i].length()) {
      offset -= this.underlying[i].length();
      i++;
    }
    this.underlying[i].set(offset);
  }


  /**
   * Clears the bit at the given position.
   * @param index the position to clear.
   */
  public void clear(final int index) {
    int i = 0;
    int offset = index;
    while (offset >= this.underlying[i].length()) {
      offset -= this.underlying[i].length();
      i++;
    }
    this.underlying[i].clear(offset);
  }


  @Override
  public boolean get(final int index) {
    int i = 0;
    int offset = index;
    while (offset > this.underlying[i].length()) {
      offset -= this.underlying[i].length();
      i++;
    }
    return this.underlying[i].get(offset);
  }


  @Override
  public int length() {
    return this.length;
  }
}
