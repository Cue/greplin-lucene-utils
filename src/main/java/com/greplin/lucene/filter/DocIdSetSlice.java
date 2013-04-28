/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.IOException;

/**
* A sub-slice of a DocIdSet.
*/
public class DocIdSetSlice extends DocIdSet {

  /**
   * The underlying DocIdSet.
   */
  private final DocIdSet underlying;

  /**
   * The start of the slice.
   */
  private final int start;

  /**
   * The end of the slice.
   */
  private final int end;


  /**
   * Constructs a DocIdSet that is a slice of another DocIdSet.
   * @param underlying the underlying DocIdSet
   * @param start the start of the slice
   * @param end the end of the slice
   */
  public DocIdSetSlice(
      final DocIdSet underlying, final int start, final int end) {
    assert start >= 0;
    assert end >= start;
    this.underlying = underlying;
    this.start = start;
    this.end = end;
  }


  /**
   * Internal iterator implementation for slices.
   */
  private final class Iterator extends DocIdSetIterator {

    /**
     * The underlying iterator.
     */
    private final DocIdSetIterator underlying;


    /**
     * Constructs an iterator.
     * @throws IOException if IO errors are encountered.
     */
    private Iterator() throws IOException {
      this.underlying = DocIdSetSlice.this.underlying.iterator();
    }


    @Override
    public int docID() {
      int docId = this.underlying.docID();
      return docId < DocIdSetSlice.this.end
          ? docId - DocIdSetSlice.this.start : NO_MORE_DOCS;
    }


    @Override
    public int nextDoc() throws IOException {
      if (this.underlying.docID() == -1) {
        this.underlying.advance(DocIdSetSlice.this.start);
      } else {
        this.underlying.nextDoc();
      }
      return docID();
    }


    @Override
    public int advance(final int target) throws IOException {
      if (target == Integer.MAX_VALUE) {
        this.underlying.advance(target);
        return docID();
      }
      assert DocIdSetSlice.this.start + target >= 0;
      this.underlying.advance(DocIdSetSlice.this.start + target);
      return docID();
    }
  }

  @Override
  public DocIdSetIterator iterator() throws IOException {
    return new Iterator();
  }


  /**
   * @return the index into the underying DocIdSet where this slice begins.
   */
  public int getStart() {
    return this.start;
  }


  /**
   * @return the index into the underying DocIdSet where this slice ends.
   */
  public int getEnd() {
    return this.end;
  }

}
