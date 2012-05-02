/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.HackMultiTermDocs;
import org.apache.lucene.index.HackMultiTermPositions;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;

import java.io.IOException;

/**
 * Filtered reader that has child readers.
 */
public class FilteredMultiReader extends FilteredIndexReader {

  /**
   * Sub-readers.
   */
  private final FilteredIndexReader[] subReaders;


  /**
   * Indexes where each reader starts.
   */
  private final int[] starts;


  /**
   * Creates a filtered index reader with the given bits provider.
   * @param base         the underlying reader.
   * @param bitsProvider the provider of filter bits.
   * @param cacheKeyProvider provider of cache keys.
   */
  public FilteredMultiReader(final IndexReader base,
                             final BitsProvider bitsProvider,
                             final CacheKeyProvider cacheKeyProvider) {
    super(base, bitsProvider, cacheKeyProvider);

    this.subReaders = new FilteredIndexReader[
        base.getSequentialSubReaders().length];

    this.starts = new int[this.subReaders.length + 1];

    int pos = 0;
    int maxDoc = 0;
    for (IndexReader reader : base.getSequentialSubReaders()) {
      this.starts[pos] = maxDoc;
      this.subReaders[pos] = FilteredIndexReader.wrap(
          reader, bitsProvider, cacheKeyProvider);
      maxDoc += this.subReaders[pos].maxDoc();
      pos++;
    }

    this.starts[this.subReaders.length] = maxDoc;
  }


  @Override
  protected IndexReader wrap(final IndexReader target) {
    return new FilteredMultiReader(
        target, this.getBitsProvider(), this.getCacheKeyProvider());
  }


  @Override
  public boolean isDeleted(final int n) {
    int offset = 0;
    for (IndexReader reader : this.subReaders) {
      if (n < offset + reader.maxDoc()) {
        return reader.isDeleted(n - offset);
      }
      offset += reader.maxDoc();
    }
    throw new IllegalArgumentException("docId out of bounds!");
  }


  @Override
  public int numDocs() {
    int sum = 0;
    for (IndexReader reader : this.subReaders) {
      sum += reader.numDocs();
    }
    return sum;
  }


  @Override
  public TermDocs termDocs(final Term term) throws IOException {
    TermDocs result = termDocs();
    result.seek(term);
    return result;
  }


  @Override
  public TermDocs termDocs() throws IOException {
    return new HackMultiTermDocs(this, this.subReaders, this.starts);
  }


  @Override
  public TermPositions termPositions(final Term term) throws IOException {
    TermPositions result = termPositions();
    result.seek(term);
    return result;
  }


  @Override
  public TermPositions termPositions() throws IOException {
    return new HackMultiTermPositions(this, this.subReaders, this.starts);
  }


  @Override
  public IndexReader[] getSequentialSubReaders() {
    return this.subReaders;
  }

}
