/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.Bits;

import java.io.IOException;

/**
 * Provider of bits for a given IndexReader.
 */
public abstract class BitsProvider {

  /**
   * Gets the relevant bits for the given IndexReader.
   * @param reader the reader.
   * @return the bits for the given reader.
   * @throws IOException if IO issues occur.
   */
  public abstract Bits get(IndexReader reader) throws IOException;


  /**
   * Get the object whose identity (not hashCode/equals) will be used for
   * keying caches with this bits provider.
   * @return the cache key.
   */
  public Object getCacheKey() {
    return this;
  }

}
