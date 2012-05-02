/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.greplin.lucene.predicate.BitsProvider;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.util.Bits;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.OpenBitSet;

import java.io.IOException;

/**
 * Creates an index reader that only lets documents matched by the
 * specified filter through.
 */
public class FilteredSegmentReader extends FilteredIndexReader {

  /**
   * Set of valid documents.
   */
  private final Bits bits;


  /**
   * Creates a filtered index reader with the given bits provider.
   * @param base the underlying reader.
   * @param bitsProvider the provider of filter bits.
   * @param cacheKeyProvider provider of cache keys.
   */
  public FilteredSegmentReader(final IndexReader base,
                               final BitsProvider bitsProvider,
                               final CacheKeyProvider cacheKeyProvider) {
    super(base, bitsProvider, cacheKeyProvider);
    try {
      this.bits = bitsProvider.get(base);
    } catch (IOException ex) {
      throw new RuntimeException("Exception while creating filter bits", ex);
    }
  }


  /**
   * Wraps an index reader in filtering logic.
   * @param target the target
   * @return the wrapped index reader
   */
  protected IndexReader wrap(final IndexReader target) {
    return new FilteredSegmentReader(
        target, this.getBitsProvider(), this.getCacheKeyProvider());
  }


  @Override
  public boolean isDeleted(final int n) {
    return getUnderlyingReader().isDeleted(n) || !this.bits.get(n);
  }


  @Override
  public boolean hasDeletions() {
    return true;
  }


  @Override
  public int numDocs() {
    if (this.bits instanceof FixedBitSet) {
      return ((FixedBitSet) this.bits).cardinality();
    } else if (this.bits instanceof OpenBitSet) {
      return (int) ((OpenBitSet) this.bits).cardinality();
    } else {
      int count = 0;
      for (int i = 0; i < this.bits.length(); i++) {
        if (this.bits.get(i)) {
          count++;
        }
      }
      return count;
    }
  }


  /**
   * TermDocs that are filtered by the set of valid bits.
   */
  private class FilteredTermDocs extends FilterTermDocs implements TermDocs {

    /**
     * TermDocs that are filtered by the set of valid bits.
     * @param termDocs the underlying term docs.
     */
    protected FilteredTermDocs(final TermDocs termDocs) {
      super(termDocs);
    }

    @Override
    public boolean next() throws IOException {
      while (super.next()) {
        if (FilteredSegmentReader.this.bits.get(super.doc())) {
          return true;
        }
      }
      return false;
    }


    @Override
    public int read(final int[] docs, final int[] freqs) throws IOException {
      int read = 0;

      int[] freqTemp = new int[freqs.length];

      while (read < docs.length) {
        int[] docTemp = new int[docs.length - read];

        int got = super.read(docTemp, freqTemp);
        if (got == 0) {
          break;
        }

        for (int i = 0; i < got; i++) {
          if (FilteredSegmentReader.this.bits.get(docTemp[i])) {
            docs[read] = docTemp[i];
            freqs[read] = freqTemp[i];
            read++;
          }
        }
      }

      return read;
    }


    @Override
    public boolean skipTo(final int target) throws IOException {
      return super.skipTo(target)
          && (FilteredSegmentReader.this.bits.get(super.doc()) || next());
    }
  }


  @Override
  public TermDocs termDocs(final Term term) throws IOException {
    return new FilteredTermDocs(getUnderlyingReader().termDocs(term));
  }


  @Override
  public TermDocs termDocs() throws IOException {
    return new FilteredTermDocs(getUnderlyingReader().termDocs());
  }


  /**
   * TermPositions that are filtered by the set of valid bits.
   */
  private final class FilteredTermPositions extends FilteredTermDocs
      implements TermPositions {

    /**
     * The underlying term positions.
     */
    private final TermPositions forward;


    /**
     * TermPositions that are filtered by the set of valid bits.
     * @param forward the underlying term positions.
     */
    private FilteredTermPositions(final TermPositions forward) {
      super(forward);
      this.forward = forward;
    }


    @Override
    public int nextPosition() throws IOException {
      return this.forward.nextPosition();
    }


    @Override
    public int getPayloadLength() {
      return this.forward.getPayloadLength();
    }


    @Override
    public byte[] getPayload(final byte[] data, final int offset)
        throws IOException {
      return this.forward.getPayload(data, offset);
    }


    @Override
    public boolean isPayloadAvailable() {
      return this.forward.isPayloadAvailable();
    }
  }


  @Override
  public TermPositions termPositions(final Term term) throws IOException {
    return new FilteredTermPositions(getUnderlyingReader().termPositions(term));
  }


  @Override
  public TermPositions termPositions() throws IOException {
    return new FilteredTermPositions(getUnderlyingReader().termPositions());
  }


  @Override
  public IndexReader[] getSequentialSubReaders() {
    return null;
  }

}
