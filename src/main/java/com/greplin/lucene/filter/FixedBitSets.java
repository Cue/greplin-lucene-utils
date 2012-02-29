/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;

/**
 * Common FixedBitSets.
 */
public final class FixedBitSets {

  /** Not instantiable. */
  private FixedBitSets() { }


  /**
   * Returns a bit set that is set for all docIds with the given term.
   * NOTE: If you'll be calling this frequently, consider re-using a TermDocs
   * object with one of the other methods in this class.
   * @param reader the index reader
   * @param term the term
   * @return a bit set that is set for all docIds with the given term
   * @throws IOException if IO issues occur
   */
  public static FixedBitSet forTerm(final IndexReader reader, final Term term)
      throws IOException {
    TermDocs termDocs = reader.termDocs(term);
    try {
      return forTermDocs(reader.maxDoc(), termDocs);
    } finally {
      termDocs.close();
    }
  }


  /**
   * Returns a bit set that is set for all docIds with the given term.
   * Uses a shared termDocs for efficiency.
   * @param maxDoc the highest numbered doc
   * @param termDocs the shared TermDocs object
   * @param term the term to find
   * @return a bit set that is set for all docIds with the given term
   * @throws IOException if IO issues occur
   */
  public static FixedBitSet forTerm(
      final int maxDoc, final TermDocs termDocs, final Term term)
      throws IOException {
    termDocs.seek(term);
    return forTermDocs(maxDoc, termDocs);
  }


  /**
   * Returns a bit set that is set for all docIds in the given TermDocs enum.
   * Uses a shared termDocs for efficiency.
   * @param maxDoc the highest numbered doc
   * @param termDocs the pre-seeked TermDocs object
   * @return a bit set that is set for all docIds with the given term
   * @throws IOException if IO issues occur
   */
  public static FixedBitSet forTermDocs(
      final int maxDoc, final TermDocs termDocs) throws IOException {
    FixedBitSet result = new FixedBitSet(maxDoc);
    while (termDocs.next()) {
      result.set(termDocs.doc());
    }
    return result;
  }

}
