package com.greplin.lucene.filter;

import org.apache.lucene.index.IndexReader;

/**
 * Key provider for filtering providers.
 */
public class PassthroughCacheKeyProvider implements CacheKeyProvider {

  /**
   * Creates a new key provider that just uses the reader's key.
   */
  public PassthroughCacheKeyProvider() { }


  @Override
  public Object getCoreCacheKey(final IndexReader reader) {
    return reader.getCoreCacheKey();
  }

}
