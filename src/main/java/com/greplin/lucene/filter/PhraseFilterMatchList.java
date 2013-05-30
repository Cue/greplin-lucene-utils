package com.greplin.lucene.filter;

import org.apache.lucene.index.TermPositions;

import java.io.IOException;

/**
 * Simple list of matches.
 * Only for use by PhraseFilter.
 */
final class PhraseFilterMatchList {

  /**
   * Docs that match.
   */
  private final int[] docIds;

  /**
   * Collated with docIds, the positions that match for that doc.
   */
  private final PhraseFilterIntList[] positions;

  /**
   * The number of matches.
   * MUTABLE: for efficient in-place modification.
   */
  private int count;


  /**
   * Creates a match list with the given capacity.
   * @param capacity the maximum number of matches we might find
   */
  PhraseFilterMatchList(final int capacity) {
    this.docIds = new int[capacity];
    this.positions = new PhraseFilterIntList[capacity];
    this.count = 0;
  }


  /**
   * Adds a match.
   * @param docId the doc that matched
   * @param positions the positions it matched in
   */
  void add(final int docId, final PhraseFilterIntList positions) {
    this.docIds[this.count] = docId;
    this.positions[this.count++] = positions;
  }


  /**
   * @return array of doc ids
   */
  int[] getDocIds() {
    return this.docIds;
  }


  /**
   * @return the match count
   */
  int getCount() {
    return this.count;
  }


  /**
   * Intersects all doc/position pairs at the given offset with this match
   * list.  Modifies this list in place as an optimization.
   * @param termPositions the term positions enumerator
   * @param offset the offset of the given term in the phrase
   * @throws java.io.IOException if IO problems occur within Lucene
   */
  void intersect(final TermPositions termPositions, final int offset)
      throws IOException {
    int currentDoc = -1;
    int resultCount = 0;
    for (int i = 0; i < this.count; i++) {
      int docId = this.docIds[i];
      while (currentDoc < docId) {
        if (termPositions.next()) {
          currentDoc = termPositions.doc();
        } else {
          this.count = resultCount;
          return;
        }
      }

      if (currentDoc == docId) {
        PhraseFilterIntList positions = this.positions[i];
        if (positions.intersect(termPositions, offset)) {
          this.docIds[resultCount] = docId;
          this.positions[resultCount++] = positions;
        }
      }
    }
    this.count = resultCount;
  }

}
