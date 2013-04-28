/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;

import java.io.IOException;

/**
 * Matches all documents for any reader.
 */
public class AllDocSet extends DocIdSet {

  /**
   * The highest numbered document.
   */
  private final int maxDoc;


  /**
   * Constructs a new doc set that includes all documents.
   * @param maxDoc the largest document number
   */
  public AllDocSet(final int maxDoc) {
    this.maxDoc = maxDoc;
  }

  @Override
  public boolean isCacheable() {
    return true;
  }

  @Override
  public DocIdSetIterator iterator() throws IOException {
    return new AllDocIdSetIterator();
  }


  /**
   * Internal iterator for matching all documents.
   */
  private class AllDocIdSetIterator extends DocIdSetIterator {

    /**
     * The current document.
     */
    private int curDoc = -1;

    @Override
    public int docID() {
      return this.curDoc;
    }

    @Override
    public int nextDoc() throws IOException {
      this.curDoc++;

      if (this.curDoc < AllDocSet.this.maxDoc) {
        return this.curDoc;
      } else {
        return NO_MORE_DOCS;
      }
    }

    @Override
    public int advance(final int target) throws IOException {
      this.curDoc = target;

      if (this.curDoc < AllDocSet.this.maxDoc) {
        return this.curDoc;
      } else {
        return NO_MORE_DOCS;
      }
    }
  }
}
