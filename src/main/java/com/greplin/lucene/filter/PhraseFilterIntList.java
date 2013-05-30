package com.greplin.lucene.filter;

import org.apache.lucene.index.TermPositions;

import java.io.IOException;

/**
 * Simple list of ints.  Can not grow beyond the initial capacity.
 * Only for use by PhraseFilter.
 */
final class PhraseFilterIntList {

  /**
   * The values.
   */
  private final int[] ints;

  /**
   * The number of values.
   */
  private int count;


  /**
   * Construct an empty list of ints with the given capacity.
   * @param capacity the number of ints this list can store
   */
  PhraseFilterIntList(final int capacity) {
    this.ints = new int[capacity];
    this.count = 0;
  }


  /**
   * Adds an int to the end of the array.
   * @param item the item to add
   */
  void add(final int item) {
    this.ints[this.count++] = item;
  }


  /**
   * Intersect this int list with the given positions.
   * Modifies this list in place as an optimization.
   * @param termPositions the term positions
   * @param offset the offset of the term within the phrase
   * @return whether this list has any terms remaining
   * @throws java.io.IOException if IO problems occur within Lucene
   */
  boolean intersect(final TermPositions termPositions, final int offset)
      throws IOException {
    int otherCount = termPositions.freq();
    int i = 0;
    int j = 0;
    int jValue = termPositions.nextPosition() - offset;
    int resultCount = 0;
    while (i < this.count && j < otherCount) {
      if (this.ints[i] < jValue) {
        i++;
      } else {
        if (this.ints[i] == jValue) {
          this.ints[resultCount++] = this.ints[i];
          i++;
        }
        j++;
        if (j != otherCount) {
          jValue = termPositions.nextPosition() - offset;
        }
      }
    }
    this.count = resultCount;
    return resultCount != 0;
  }

}
