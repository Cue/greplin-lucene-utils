/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.util.Bits;

import java.io.IOException;

/**
 * Common predicates.
 */
public final class Predicates {

  /** Not instantiable. */
  private Predicates() { }


  /**
   * Creates a new predicate bits provider that matches documents
   * matched by all of the arguments.
   * @param bitsProviders the sub-predicates.
   * @return a new predicate bits provider that matches documents
   * matched by all of the arguments.
   */
  public static BitsProvider and(final BitsProvider... bitsProviders) {
    BooleanBitsProvider result = new BooleanBitsProvider();
    for (BitsProvider provider : bitsProviders) {
      result.add(provider, BooleanClause.Occur.MUST);
    }
    return result;
  }


  /**
   * Creates a new predicate bits provider that matches documents
   * matched by any of the arguments.
   * @param bitsProviders the sub-predicates.
   * @return a new predicate bits provider that matches documents
   * matched by any of the arguments.
   */
  public static BitsProvider or(final BitsProvider... bitsProviders) {
    BooleanBitsProvider result = new BooleanBitsProvider();
    for (BitsProvider provider : bitsProviders) {
      result.add(provider, BooleanClause.Occur.SHOULD);
    }
    return result;
  }


  /**
   * Creates a new predicate bits provider that matches documents
   * not matched by the argument.
   * @param bitsProvider the predicate to negate.
   * @return a new predicate bits provider that matches documents
   * not matched by the argument.
   */
  public static BitsProvider not(final BitsProvider bitsProvider) {
    return new InverseBitsProvider(bitsProvider);
  }


  /**
   * BitsProvider that matches the opposite set of bits as its
   * argument.
   */
  private static final class InverseBitsProvider extends BitsProvider {

    /**
     * The original bits to negate.
     */
    private final BitsProvider original;


    /**
     * Creates a BitsProvider that negates its argument.
     * @param original the bits to negate.
     */
    private InverseBitsProvider(final BitsProvider original) {
      this.original = original;
    }


    @Override
    public Bits get(final IndexReader reader) throws IOException {
      return new InverseBits(this.original.get(reader));
    }


    @Override
    public String toString() {
      return "-(" + this.original.toString() + ")";
    }

  }


  /**
   * Bits that matches the opposite set of bits as its argument.
   */
  private static final class InverseBits implements Bits {

    /**
     * The original bits to negate.
     */
    private final Bits original;


    /**
     * Creates Bits that negate the argument.
     * @param original the bits to negate.
     */
    private InverseBits(final Bits original) {
      this.original = original;
    }


    @Override
    public boolean get(final int index) {
      return !this.original.get(index);
    }


    @Override
    public int length() {
      return this.original.length();
    }

  }


  /**
   * Predicate that matches no documents.
   */
  public static final BitsProvider NONE = new BitsProvider() {
    @Override
    public Bits get(final IndexReader reader) throws IOException {
      return new Predicate() {
        @Override
        public boolean get(final int index) {
          return false;
        }
      };
    }
  };


  /**
   * Predicate that matches all documents.
   */
  public static final BitsProvider ALL = new BitsProvider() {
    @Override
    public Bits get(final IndexReader reader) throws IOException {
      return new Predicate() {
        @Override
        public boolean get(final int index) {
          return true;
        }
      };
    }
  };

}
