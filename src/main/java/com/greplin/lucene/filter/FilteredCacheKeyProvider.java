package com.greplin.lucene.filter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;

/**
 * Key provider for filtering providers.
 */
public class FilteredCacheKeyProvider implements CacheKeyProvider {

  /**
   * We need to have the key have the same lifetime as the reader, and also be
   * the same object across all similar instances of FilteredIndexReader.  Not
   * pretty but it's necessary.
   */
  private static final LoadingCache<Object, LoadingCache<Object, Object>>
      KEYS = CacheBuilder.newBuilder().weakKeys().build(
          new CacheLoader<Object, LoadingCache<Object, Object>>() {
            @Override
            public LoadingCache<Object, Object> load(final Object key)
                throws Exception {
              return CacheBuilder.newBuilder().weakKeys().build(
                  new CacheLoader<Object, Object>() {
                    @Override
                    public Object load(final Object key) throws Exception {
                      return new Object();
                    }
                  }
              );
            }
          });

  /**
   * The bits provider to segment cache keys by.
   */
  private final BitsProvider bitsProvider;


  /**
   * Creates a new key provider that is segmented by filter.
   * @param bitsProvider the filter.
   */
  public FilteredCacheKeyProvider(final BitsProvider bitsProvider) {
    this.bitsProvider = bitsProvider;
  }


  @Override
  public Object getCoreCacheKey(final IndexReader reader) {
    return KEYS.getUnchecked(reader.getCoreCacheKey())
        .getUnchecked(this.bitsProvider.getCacheKey());
  }

}
