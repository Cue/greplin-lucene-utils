/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.IOException;

/**
 * DocIdSet backed by an int list.
 */
public class IntListDocIdSet extends DocIdSet {

  /**
   * The underlying IntList.
   */
  private final IntList intList;


  /**
   * Creates a new DocIdSet based on the given IntList.
   * @param intList the list of docIds.
   */
  public IntListDocIdSet(final IntList intList) {
    this.intList = intList;
  }


  @Override
  public DocIdSetIterator iterator() throws IOException {
    return this.intList.size() == 0
        ? DocIdSet.EMPTY_DOCIDSET.iterator()
        : new Iterator();
  }


  @Override
  public boolean isCacheable() {
    return true;
  }


  /**
   * DocIdSetIterator over an IntList.
   */
  private class Iterator extends DocIdSetIterator {

    /**
     * The current offset of iteration.
     */
    private int offset = -1;


    @Override
    public int docID() {
      return this.offset == IntListDocIdSet.this.intList.size()
          || this.offset == -1
          ? DocIdSetIterator.NO_MORE_DOCS
          : IntListDocIdSet.this.intList.get(this.offset);
    }


    @Override
    public int nextDoc() throws IOException {
      this.offset++;
      return docID();
    }


    @Override
    public int advance(final int target) throws IOException {
      int docId = docID();
      for (;;) {
        if (docId >= target) {
          return docId;
        }
        docId = nextDoc();
      }
    }

  }

}
