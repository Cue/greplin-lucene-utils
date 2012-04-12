/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

/**
 * The built-in CachingWrapperFilter will possibly compute the same filter more
 * than once due to a race condition between get and put.  This implementation
 * fixes that.  For simplicity, it only handles the IGNORE delete handling mode.
 */
public class ConcurrentCachingWrapperFilter extends Filter {

  /**
   * The underlying filter.
   */
  private final Filter underlying;


  /**
   * The cache from reader key to doc id set.
   */
  private final Cache<Object, DocIdSet> cache;


  /**
   * Constructs a new wrapper for the given underlying filter.
   * @param underlying the underlying filter
   */
  public ConcurrentCachingWrapperFilter(final Filter underlying) {
    this.underlying = underlying;
    this.cache = CacheBuilder.newBuilder().weakKeys().build();
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    try {
      return this.cache.get(reader.getCoreCacheKey(), new FilterLoader(reader));
    } catch (ExecutionException ex) {
      if (ex.getCause() instanceof IOException) {
        throw (IOException) ex.getCause();
      } else {
        throw (RuntimeException) ex.getCause();
      }
    }
  }


  /**
   * Internal class for loading the cache.
   */
  private final class FilterLoader implements Callable<DocIdSet> {

    /**
     * The reader to load from.
     */
    private final IndexReader reader;


    /**
     * Constructs a filter loader for the given reader.
     * @param reader the reader to load from.
     */
    private FilterLoader(final IndexReader reader) {
      this.reader = reader;
    }


    @Override
    public DocIdSet call() throws IOException {
      return DocIdSets.cacheable(
          ConcurrentCachingWrapperFilter.this.underlying.getDocIdSet(
              this.reader),
          this.reader);
    }

  }

}
