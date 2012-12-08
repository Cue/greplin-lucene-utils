/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import com.google.common.collect.Lists;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;
import java.util.List;

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


  /**
   * Converts a fixed bit set to a list.
   * @param bitSet the bit set to convert.
   * @return the list of matching document ids.
   */
  public static List<Integer> asList(final FixedBitSet bitSet) {
    DocIdSetIterator it = bitSet.iterator();
    List<Integer> ints = Lists.newArrayList();
    try {
      while (it.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
        ints.add(it.docID());
      }
    } catch (IOException e) {
      throw new RuntimeException(
          "Impossible IOException when iterating in memory");
    }
    return ints;
  }


  /**
   * Creates a bitset that includes all docs from the given reader.
   * @param reader the index
   * @return a bitset that includes all docs
   */
  public static FixedBitSet all(final IndexReader reader) {
    FixedBitSet result = new FixedBitSet(reader.maxDoc());
    result.set(0, reader.maxDoc());
    return result;
  }


  /**
   * Gets the bit at the given index, or returns defaultValue if the index is
   * outside the bounds of the given bitset.
   * @param bitSet the bit set to get from
   * @param index the index in the bit set
   * @param defaultValue the value to return if the index is outside the
   *     bounds of the bitset
   * @return the bit at the given index, or defaultValue if the index is
   *     outside the bounds of the bitset
   */
  public static boolean unboundedGet(
      final FixedBitSet bitSet, final int index, final boolean defaultValue) {
    return (index < bitSet.length() && index >= 0)
        ? bitSet.get(index) : defaultValue;
  }

}
