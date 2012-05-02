package com.greplin.lucene.filter;

import org.apache.lucene.index.IndexReader;

/**
 * Provider of cache keys.
 */
public interface CacheKeyProvider {

  /**
   * Returns a cache key for the given reader.
   * @param reader the reader.
   * @return the cache key.
   */
  Object getCoreCacheKey(IndexReader reader);

}
