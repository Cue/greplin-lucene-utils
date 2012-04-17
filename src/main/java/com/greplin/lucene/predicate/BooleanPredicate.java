/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.util.Bits;

import java.util.List;

/**
 * Combines BitsProviders with boolean rules.
 */
public class BooleanPredicate implements Bits {

  /**
   * Bits that must match.
   */
  private final List<Bits> must;

  /**
   * Bits that should match.
   */
  private final List<Bits> should;

  /**
   * Bits that must not match.
   */
  private final List<Bits> mustNot;


  /**
   * Creates a new boolean predicate.
   */
  public BooleanPredicate() {
    this.must = Lists.newArrayList();
    this.should = Lists.newArrayList();
    this.mustNot = Lists.newArrayList();
  }


  /**
   * Adds a clause.
   * @param bits the child bits.
   * @param occur the desired occurrence.
   */
  public void add(final Bits bits,
                  final BooleanClause.Occur occur) {
    switch (occur) {
      case MUST:
        this.must.add(bits);
        return;
      case SHOULD:
        this.should.add(bits);
        return;
      default:
        this.mustNot.add(bits);
    }
  }


  @Override
  public boolean get(final int index) {
    for (Bits bits : this.must) {
      if (!bits.get(index)) {
        return false;
      }
    }
    for (Bits bits : this.mustNot) {
      if (bits.get(index)) {
        return false;
      }
    }
    for (Bits bits : this.should) {
      if (bits.get(index)) {
        return true;
      }
    }
    return this.should.isEmpty();
  }


  @Override
  public int length() {
    throw new UnsupportedOperationException();
  }


  @Override
  public boolean equals(final Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    BooleanPredicate that = (BooleanPredicate) o;
    return this.must.equals(that.must)
        && this.mustNot.equals(that.mustNot)
        && this.should.equals(that.should);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(this.must, this.mustNot, this.should);
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this)
        .add("must", this.must)
        .add("mustNot", this.mustNot)
        .add("should", this.should)
        .toString();
  }

}
