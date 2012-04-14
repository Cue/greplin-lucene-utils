/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;
import org.apache.lucene.index.FilterIndexReader;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;

import java.io.IOException;

/**
 * Creates an index reader that only allows documents matched by
 * the specified filter through.
 *
 * Some parts of the API will return inaccurate information - the
 * term frequencies, document count in the presence of deletions,
 * etc.  Practical experience thus far shows this only affects
 * accuracy of scoring.
 */
public abstract class FilteredIndexReader
    extends FilterIndexReader
    implements Cloneable {

  /**
   * Provider of bits.
   */
  private final BitsProvider bitsProvider;


  /**
   * Cache key for this index reader (see below).
   */
  private final Object cacheKey;


  /**
   * Wraps the given index reader with the appropriate filtered reader type.
   * @param reader the index reader.
   * @param bitsProvider provider of filter bits.
   * @return an index reader filtered by the given bits.
   */
  public static FilteredIndexReader wrap(final IndexReader reader,
                                         final BitsProvider bitsProvider) {
    if (reader.getSequentialSubReaders() == null) {
      return new FilteredSegmentReader(reader, bitsProvider);
    } else {
      return new FilteredMultiReader(reader, bitsProvider);
    }
  }


  /**
   * We need to have the key have the same lifetime as this object, and also be
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
   * Creates a filtered index reader with the given bits provider.
   * @param base the underlying reader.
   * @param bitsProvider the provider of filter bits.
   */
  protected FilteredIndexReader(final IndexReader base,
                                final BitsProvider bitsProvider) {
    super(base);
    this.bitsProvider = bitsProvider;
    this.cacheKey = KEYS.getUnchecked(base.getCoreCacheKey())
        .getUnchecked(bitsProvider.getCacheKey());
  }


  /**
   * Wraps a sub index reader in filtering logic.
   * @param target the target
   * @return the wrapped index reader
   */
  protected abstract IndexReader wrap(final IndexReader target);


  @Override
  public IndexReader reopen() throws IOException {
    return wrap(super.reopen());
  }


  @Override
  public IndexReader reopen(final boolean openReadOnly) throws IOException {
    return wrap(super.reopen(openReadOnly));
  }


  @Override
  public IndexReader reopen(final IndexCommit commit) throws IOException {
    return wrap(super.reopen(commit));
  }


  @Override
  public IndexReader reopen(final IndexWriter writer,
                            final boolean applyAllDeletes) throws IOException {
    return wrap(super.reopen(writer, applyAllDeletes));
  }


  @Override
  public Object clone() {
    return wrap((IndexReader) super.clone());
  }


  @Override
  public IndexReader clone(final boolean openReadOnly) throws IOException {
    return wrap(super.clone(openReadOnly));
  }


  @Override
  public abstract boolean isDeleted(final int n);


  @Override
  public boolean hasDeletions() {
    return true;
  }


  @Override
  public abstract int numDocs();


  @Override
  public abstract TermDocs termDocs(final Term term) throws IOException;


  @Override
  public abstract TermDocs termDocs() throws IOException;


  /**
   * @return the underlying reader being filtered.
   */
  protected IndexReader getUnderlyingReader() {
    return this.in;
  }


  @Override
  public abstract TermPositions termPositions(final Term term)
      throws IOException;


  @Override
  public abstract TermPositions termPositions() throws IOException;


  @Override
  public abstract IndexReader[] getSequentialSubReaders();


  @Override
  public Object getCoreCacheKey() {
    // Ensure the field cache sees this reader as unique.
    return this.cacheKey;
  }


  @Override
  public IndexCommit getIndexCommit() throws IOException {
    return this.in.getIndexCommit();
  }


  /**
   * @return the bits provider.
   */
  protected BitsProvider getBitsProvider() {
    return this.bitsProvider;
  }

}
