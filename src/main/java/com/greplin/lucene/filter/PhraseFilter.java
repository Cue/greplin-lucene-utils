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
   * The terms comprising the phrase.
   */
  private final Term[] terms;


  /**
   * Construct a new phrase filter.
   * @param terms the terms in the phrase
   */
  public PhraseFilter(final Term... terms) {
    this.terms = Arrays.copyOf(terms, terms.length);
  }


  /**
   * Construct a new phrase filter.
   * @param field the field to find phrases in
   * @param terms the terms in the phrase
   */
  public PhraseFilter(final String field, final String... terms) {
    this.terms = new Term[terms.length];
    for (int i = 0; i < terms.length; i++) {
      this.terms[i] = new Term(field, terms[i]);
    }
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    List<IndexReader> subReaders = IndexReaders.gatherSubReaders(reader);
    PhraseFilterMatchList[] results =
        new PhraseFilterMatchList[subReaders.size()];
    int matchCount = 0;
    int readerNumber = 0;

    for (IndexReader subReader : subReaders) {
      SortedSet<TermWithFrequency> termsOrderedByFrequency = Sets.newTreeSet();
      for (int i = 0; i < this.terms.length; i++) {
        Term t = this.terms[i];
        termsOrderedByFrequency.add(
            new TermWithFrequency(t, subReader.docFreq(t), i));
      }

      PhraseFilterMatchList matches = null;
      TermPositions termPositions = subReader.termPositions();
      try {
        for (TermWithFrequency term : termsOrderedByFrequency) {
          if (term.docFreq == 0) {
            break;
          }

          termPositions.seek(term.term);

          if (matches == null) {
            // If this is the first term, collect all matches.
            matches = new PhraseFilterMatchList(term.docFreq);
            while (termPositions.next()) {
              int freq = termPositions.freq();
              PhraseFilterIntList list = new PhraseFilterIntList(freq);
              for (int i = 0; i < freq; i++) {
                list.add(termPositions.nextPosition() - term.offset);
              }
              matches.add(termPositions.doc(), list);
            }
          } else {
            // Otherwise, intersect with the existing matches.
            matches.intersect(termPositions, term.offset);
          }

          if (matches.getCount() == 0) {
            break;
          }
        }
      } finally {
        termPositions.close();
      }

      if (matches != null) {
        results[readerNumber] = matches;
        matchCount += matches.getCount();
      }
      readerNumber++;
    }

    final int bitsPerIntPowerLogTwo = 5; // 2^5 = 32
    if (matchCount > reader.maxDoc() >> bitsPerIntPowerLogTwo) {
      FixedBitSet result = new FixedBitSet(reader.maxDoc());
      int readerOffset = 0;
      for (int readerIndex = 0; readerIndex < results.length; readerIndex++) {
        PhraseFilterMatchList matches = results[readerIndex];
        if (matches != null) {
          int count = matches.getCount();
          int[] docIds = matches.getDocIds();
          for (int i = 0; i < count; i++) {
            result.set(docIds[i] + readerOffset);
          }
        }
        readerOffset += subReaders.get(readerIndex).maxDoc();
      }
      return result;
    } else if (matchCount == 0) {
      return DocIdSets.EMPTY;
    } else {
      int[] result = new int[matchCount];
      int base = 0;
      int readerOffset = 0;
      for (int readerIndex = 0; readerIndex < results.length; readerIndex++) {
        PhraseFilterMatchList matches = results[readerIndex];
        if (matches != null) {
          int count = matches.getCount();
          int[] docIds = matches.getDocIds();
          for (int i = 0; i < count; i++) {
            result[base + i] = docIds[i] + readerOffset;
          }
          base += count;
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
      return this.index < this.ints.length
          ? this.ints[this.index] : NO_MORE_DOCS;
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

}
