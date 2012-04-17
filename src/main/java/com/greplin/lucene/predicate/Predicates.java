/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import org.apache.lucene.search.BooleanClause;

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

}
