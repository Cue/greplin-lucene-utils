package com.greplin.lucene.filter;

import com.google.common.collect.Sets;
import com.google.common.primitives.Ints;
import com.greplin.lucene.index.IndexReaders;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.SortedSet;

/**
 * Filters for documents matching a phrase.
 *
 * Differences from PhraseQuery:
 * - faster with less features
 * - supports a single field only
 * - does not compute score
 * - does not support slop
 *
 * Optimization notes:
 * - Using a shared TermPositions with seeking saves about 10% !
 */
public class PhraseFilter extends Filter {

  /**
   * The field to find phrases in.
   */
  private final String field;

  /**
   * The terms comprising the phrase.
   */
  private final String[] terms;


  /**
   * Construct a new phrase filter.
   * @param field the field to find phrases in
   * @param terms the terms in the phrase
   */
  public PhraseFilter(final String field, final String... terms) {
    this.field = field;
    this.terms = Arrays.copyOf(terms, terms.length);
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    List<IndexReader> subReaders = IndexReaders.gatherSubReaders(reader);
    SimpleMatchList[] results = new SimpleMatchList[subReaders.size()];
    int matchCount = 0;
    int readerNumber = 0;

    for (IndexReader subReader : subReaders) {
      SortedSet<TermWithFrequency> termsOrderedByFrequency = Sets.newTreeSet();
      for (int i = 0; i < this.terms.length; i++) {
        Term t = new Term(this.field, this.terms[i]);
        termsOrderedByFrequency.add(
            new TermWithFrequency(t, subReader.docFreq(t), i));
      }

      SimpleMatchList matches = null;
      TermPositions termPositions = subReader.termPositions();
      try {
        for (TermWithFrequency term : termsOrderedByFrequency) {
          if (term.docFreq == 0) {
            break;
          }

          termPositions.seek(term.term);

          if (matches == null) {
            // If this is the first term, collect all matches.
            matches = new SimpleMatchList(term.docFreq);
            while (termPositions.next()) {
              int freq = termPositions.freq();
              SimpleIntList list = new SimpleIntList(freq);
              for (int i = 0; i < freq; i++) {
                list.add(termPositions.nextPosition() - term.offset);
              }
              matches.add(termPositions.doc(), list);
            }
          } else {
            // Otherwise, intersect with the existing matches.
            matches.intersect(termPositions, term.offset);
          }

          if (matches.count == 0) {
            break;
          }
        }
      } finally {
        termPositions.close();
      }

      if (matches != null) {
        results[readerNumber] = matches;
        matchCount += matches.count;
      }
      readerNumber++;
    }

    final int bitsPerIntPowerLogTwo = 5; // 2^5 = 32
    if (matchCount > reader.maxDoc() >> bitsPerIntPowerLogTwo) {
      FixedBitSet result = new FixedBitSet(reader.maxDoc());
      int readerOffset = 0;
      for (int readerIndex = 0; readerIndex < results.length; readerIndex++) {
        SimpleMatchList matches = results[readerIndex];
        if (matches != null) {
          for (int i = 0; i < matches.count; i++) {
            result.set(matches.docIds[i] + readerOffset);
          }
        }
        readerOffset += subReaders.get(readerIndex).maxDoc();
      }
      return result;
    } else {
      int[] result = new int[matchCount];
      int base = 0;
      int readerOffset = 0;
      for (int readerIndex = 0; readerIndex < results.length; readerIndex++) {
        SimpleMatchList matches = results[readerIndex];
        if (matches != null) {
          for (int i = 0; i < matches.count; i++) {
            result[base + i] = matches.docIds[i] + readerOffset;
          }
          base += matches.count;
        }
        readerOffset += subReaders.get(readerIndex).maxDoc();
      }
      return new SortedIntArrayDocIdSet(result);
    }
  }


  /**
   * DocId set based on a sorted array of integers.
   * The integer array is not defensively copied - so don't modify it!
   */
  private static final class SortedIntArrayDocIdSet extends DocIdSet {

    /**
     * The sorted array of integers.
     */
    private final int[] ints;


    /**
     * Constructs a new doc id set.
     * @param ints sorted array of integers
     */
    private SortedIntArrayDocIdSet(final int[] ints) {
      this.ints = ints;
    }


    @Override
    public DocIdSetIterator iterator() throws IOException {
      return new SortedIntArrayDocIdSetIterator(this.ints);
    }


    @Override
    public boolean isCacheable() {
      return true;
    }

  }


  /**
   * Iterator for sorted integer array.
   */
  private static final class SortedIntArrayDocIdSetIterator
      extends DocIdSetIterator {

    /**
     * The list of integers.
     */
    private final int[] ints;

    /**
     * The active index.
     */
    private int index = -1;


    /**
     * Constructs an iterator over a sorted integer array.
     * @param ints the array of integers
     */
    private SortedIntArrayDocIdSetIterator(final int[] ints) {
      this.ints = ints;
    }


    @Override
    public int docID() {
      return this.ints[this.index];
    }


    @Override
    public int nextDoc() throws IOException {
      ++this.index;
      return this.index < this.ints.length
          ? this.ints[this.index] : NO_MORE_DOCS;
    }


    @Override
    public int advance(final int target) throws IOException {
      // TODO(robbyw): Consider doing binary search here.
      // Though, in practice, the array is probably small enough that this
      // would actually be slower.
      while (docID() < target) {
        nextDoc();
      }
      return docID();
    }

  }


  /**
   * A term with a frequency and offset.
   */
  private static final class TermWithFrequency
      implements Comparable<TermWithFrequency> {

    /**
     * The term.
     */
    private final Term term;

    /**
     * Its frequency.
     */
    private final int docFreq;

    /**
     * Offset within the phrase.
     */
    private final int offset;


    /**
     * Construct a term with frequency struct.
     * @param term the term
     * @param docFreq its frequency
     * @param offset offset within the phrase
     */
    private TermWithFrequency(
        final Term term, final int docFreq, final int offset) {
      this.term = term;
      this.docFreq = docFreq;
      this.offset = offset;
    }

    @Override
    public int compareTo(final TermWithFrequency o) {
      int first = Ints.compare(this.docFreq, o.docFreq);
      return first == 0 ? Ints.compare(this.offset, o.offset) : first;
    }

  }


  /**
   * Simple list of ints.  Can not grow beyond the initial capacity.
   */
  private static final class SimpleIntList {

    /**
     * The values.
     */
    private final int[] ints;

    /**
     * The number of values.
     */
    private int count;


    /**
     * Construct an empty list of ints with the given capacity.
     * @param capacity the number of ints this list can store
     */
    private SimpleIntList(final int capacity) {
      this.ints = new int[capacity];
      this.count = 0;
    }


    /**
     * Adds an int to the end of the array.
     * @param item the item to add
     */
    private void add(final int item) {
      this.ints[this.count++] = item;
    }


    /**
     * Intersect this int list with the given positions.
     * Modifies this list in place as an optimization.
     * @param termPositions the term positions
     * @param offset the offset of the term within the phrase
     * @return whether this list has any terms remaining
     * @throws IOException if IO problems occur within Lucene
     */
    private boolean intersect(
        final TermPositions termPositions, final int offset)
        throws IOException {
      int otherCount = termPositions.freq();
      int i = 0;
      int j = 0;
      int jValue = termPositions.nextPosition() - offset;
      int resultCount = 0;
      while (i < this.count && j < otherCount) {
        if (this.ints[i] < jValue) {
          i++;
        } else {
          if (this.ints[i] == jValue) {
            this.ints[resultCount++] = this.ints[i];
            i++;
          }
          j++;
          if (j != otherCount) {
            jValue = termPositions.nextPosition() - offset;
          }
        }
      }
      this.count = resultCount;
      return resultCount != 0;
    }

  }


  /**
   * Simple list of matches.
   */
  private static final class SimpleMatchList {

    /**
     * Docs that match.
     */
    private final int[] docIds;

    /**
     * Collated with docIds, the positions that match for that doc.
     */
    private final SimpleIntList[] positions;

    /**
     * The number of matches.
     * MUTABLE: for efficient in-place modification.
     */
    private int count;


    /**
     * Creates a match list with the given capacity.
     * @param capacity the maximum number of matches we might find
     */
    private SimpleMatchList(final int capacity) {
      this.docIds = new int[capacity];
      this.positions = new SimpleIntList[capacity];
      this.count = 0;
    }


    /**
     * Adds a match.
     * @param docId the doc that matched
     * @param positions the positions it matched in
     */
    private void add(final int docId, final SimpleIntList positions) {
      this.docIds[this.count] = docId;
      this.positions[this.count++] = positions;
    }


    /**
     * Intersects all doc/position pairs at the given offset with this match
     * list.  Modifies this list in place as an optimization.
     * @param termPositions the term positions enumerator
     * @param offset the offset of the given term in the phrase
     * @throws IOException if IO problems occur within Lucene
     */
    private void intersect(final TermPositions termPositions, final int offset)
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
          SimpleIntList positions = this.positions[i];
          if (positions.intersect(termPositions, offset)) {
            this.docIds[resultCount] = docId;
            this.positions[resultCount++] = positions;
          }
        }
      }
      this.count = resultCount;
    }

  }

}

