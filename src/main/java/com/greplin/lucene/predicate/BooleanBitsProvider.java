/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.util.Bits;

import java.io.IOException;
import java.util.List;

/**
 * Combines BitsProviders with boolean rules.
 */
public class BooleanBitsProvider extends BitsProvider {

  /**
   * Clauses.
   */
  private final List<Clause<BitsProvider>> clauses;


  /**
   * Creates a new BooleanBitsProvider.
   */
  public BooleanBitsProvider() {
    this.clauses = Lists.newArrayList();
  }


  /**
   * Adds a clause.
   * @param bitsProvider the child bits provider.
   * @param occur the type of desired occurrence.
   */
  public void add(final BitsProvider bitsProvider,
                  final BooleanClause.Occur occur) {
    this.clauses.add(new Clause<BitsProvider>(bitsProvider, occur));
  }


  @Override
  public Bits get(final IndexReader reader) throws IOException {
    BooleanPredicate result = new BooleanPredicate();
    for (Clause<BitsProvider> clause : this.clauses) {
      result.add(clause.getValue().get(reader), clause.getOccur());
    }
    return result;
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BooleanBitsProvider that = (BooleanBitsProvider) o;
    return this.clauses.equals(that.clauses);
  }


  @Override
  public int hashCode() {
    return this.clauses.hashCode();
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this).add("clauses", this.clauses).toString();
  }

}
