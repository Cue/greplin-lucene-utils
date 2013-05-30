/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.commons.collections.primitives.ArrayIntList;
import org.apache.commons.collections.primitives.IntList;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.FilteredDocIdSet;
import org.apache.lucene.util.FixedBitSet;

import javax.annotation.Nullable;
import java.io.IOException;

/**
 * Utility functions for dealing with DocIdSets.
 */
public final class DocIdSets {

  /** Not instantiable. */
  private DocIdSets() { }


  /**
   * Returns a cacheable version of the given set of document ids.  May
   * just return the given set if it was already cacheable.
   * @param docIdSet the set to make cacheable.
   * @param reader the index reader the docIdSet references.
   * @return the cacheable version.
   * @throws IOException if IO errors occur.
   */
  public static DocIdSet cacheable(@Nullable final DocIdSet docIdSet,
                                   final IndexReader reader)
      throws IOException {
    if (docIdSet == null) {
      return DocIdSet.EMPTY_DOCIDSET;
    } else if (docIdSet.isCacheable()) {
      return docIdSet;
    } else {
      DocIdSetIterator it = docIdSet.iterator();
      if (it == null) {
        return DocIdSet.EMPTY_DOCIDSET;
      } else {
        FixedBitSet bits = new FixedBitSet(reader.maxDoc());
        bits.or(it);
        return bits;
      }
    }
  }


  /**
   * Returns a cacheable version of the given set of document ids.  Optimized
   * for cases where the target doc id set is sparse, specifically when less
   * than 1/32 of the documents match the input.
   * @param docIdSet the set to make cacheable.
   * @return the cacheable version.
   * @throws IOException if IO errors occur.
   */
  public static DocIdSet cacheableSparse(@Nullable final DocIdSet docIdSet)
      throws IOException {
    if (docIdSet == null) {
      return DocIdSet.EMPTY_DOCIDSET;
    } else if (docIdSet.isCacheable()) {
      return docIdSet;
    } else {
      DocIdSetIterator it = docIdSet.iterator();
      IntList docIds = new ArrayIntList();

      // null is allowed to be returned by iterator(),
      // in this case we wrap with the empty set,
      // which is cacheable.
      if (it == null) {
        return DocIdSet.EMPTY_DOCIDSET;
      } else {
        while (it.nextDoc() != DocIdSetIterator.NO_MORE_DOCS) {
          docIds.add(it.docID());
        }
        return new IntListDocIdSet(docIds);
      }
    }
  }


  /**
   * Check if the given doc id set contains the given doc id.
   * @param docIdSet the doc id set
   * @param docId the doc id
   * @return whether the given doc id set contains the given doc id
   * @throws IOException if IO errors occur
   */
  public static boolean contains(
      @Nullable final DocIdSet docIdSet, final int docId) throws IOException {
    if (docIdSet == null) {
      return false;
    }

    if (docIdSet instanceof FixedBitSet) {
      return ((FixedBitSet) docIdSet).get(docId);
    }

    return docIdSet.iterator().advance(docId) == docId;
  }


  /**
   * Filters another DocIdSet to remove deleted documents.
   */
  public static final class RemoveDeletedDocuments extends FilteredDocIdSet {

    /**
     * The IndexReader the docIdSet is built on.
     */
    private final IndexReader reader;


    /**
     * Creates a new deletion removing doc id set.
     * @param innerSet the set to filter
     * @param reader the reader
     */
    public RemoveDeletedDocuments(final DocIdSet innerSet,
                                  final IndexReader reader) {
      super(innerSet);
      this.reader = reader;
    }


    @Override
    protected boolean match(final int docId) throws IOException {
      return !this.reader.isDeleted(docId);
    }

  }


  /**
   * Empty doc id set iterator.
   */
  private static final DocIdSetIterator EMPTY_ITERATOR =
      new DocIdSetIterator() {
        @Override
        public int docID() {
          return NO_MORE_DOCS;
        }


        @Override
        public int nextDoc() throws IOException {
          return NO_MORE_DOCS;
        }


        @Override
        public int advance(final int target) throws IOException {
          return NO_MORE_DOCS;
        }
      };


  /**
   * Empty doc id set.
   */
  public static final DocIdSet EMPTY = new DocIdSet() {
    @Override
    public DocIdSetIterator iterator() throws IOException {
      return EMPTY_ITERATOR;
    }
  };

}
