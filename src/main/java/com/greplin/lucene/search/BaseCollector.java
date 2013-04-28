/*
 * Copyright 2011 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.search;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Collector;

import java.io.IOException;

/**
 * Determines if there were *any* matches to a query.
 */
public abstract class BaseCollector extends Collector {
  /**
   * The current reader.
   */
  private IndexReader currentReader;

  /**
   * The current document base.
   */
  private int currentDocBase;


  @Override
  public void setNextReader(final IndexReader reader,
                            final int docBase)
      throws IOException {
    this.currentDocBase = docBase;
    this.currentReader = reader;
  }


  /**
   * Gets the current index reader.
   * @return the current index reader
   */
  protected IndexReader getCurrentReader() {
    return this.currentReader;
  }


  /**
   * Gets the current document offset.
   * @return the current document offset.
   */
  protected int getCurrentDocBase() {
    return this.currentDocBase;
  }
}
