/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.index;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermEnum;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.Iterator;

/**
 * Convenience wrapper for getting all Terms from a reader for a field.
 */
public class TermsForField implements Iterable<Term> {

  /**
   * The interned field name to get terms for.
   */
  private final String fieldName;

  /**
   * The IndexReader.
   */
  private final IndexReader indexReader;


  /**
   * Constructs a wrapper that makes matching terms easily iterable.
   * @param reader the index reader
   * @param fieldName the field to iterator over terms for
   */
  public TermsForField(final IndexReader reader, final String fieldName) {
    this.indexReader = reader;
    this.fieldName = fieldName.intern();
  }


  @Override
  public Iterator<Term> iterator() {
    try {
      return new TermsForFieldIterator();
    } catch (IOException e) {
      throw new RuntimeIOException(e);
    }
  }


  /**
   * Determines if the given term matches the field we are iterating over.
   * @param term the term to check
   * @return whether it matches
   */
  private boolean isField(@Nullable final Term term) {
    // It's OK to use == because of interning.
    return term != null && term.field() == this.fieldName;

  }


  /**
   * The actual iterator class.
   */
  private final class TermsForFieldIterator implements Iterator<Term> {

    /**
     * The underlying term enum.
     */
    private final TermEnum terms;

    /**
     * The nextValue to be returned.
     */
    private Term nextValue;


    /**
     * Constructs an iterator.
     * @throws IOException if IO errors occur
     */
    private TermsForFieldIterator() throws IOException {
      this.terms = TermsForField.this.indexReader.terms(
          new Term(TermsForField.this.fieldName));
      this.nextValue = this.terms == null || !isField(this.terms.term())
          ? null : this.terms.term();
    }


    @Override
    public boolean hasNext() {
      return this.nextValue != null;
    }


    @Override
    public Term next() {
      Term result = this.nextValue;
      try {
        if (this.terms.next() && isField(this.terms.term())) {
          this.nextValue = this.terms.term();
        } else {
          this.nextValue = null;
          this.terms.close();
        }
      } catch (IOException e) {
        throw new RuntimeIOException(e);
      }
      return result;
    }


    @Override
    public void remove() {
      throw new UnsupportedOperationException("Remove not supported");
    }
  }


  /**
   * Special RuntimeException that only gets thrown for IOExceptions,
   * and can easily be unwrapped.
   */
  public static class RuntimeIOException extends RuntimeException {

    /**
     * The underlying exception.
     */
    private final IOException ioException;


    /**
     * Creates a new runtime IOException.
     * @param ioException the underlying exception
     */
    public RuntimeIOException(final IOException ioException) {
      super(ioException);
      this.ioException = ioException;
    }


    /**
     * @return the underlying exception
     */
    public IOException getIOException() {
      return this.ioException;
    }

  }

}
