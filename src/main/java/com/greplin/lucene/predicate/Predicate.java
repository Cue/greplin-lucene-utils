/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.predicate;

import org.apache.lucene.util.Bits;

/**
 * Predicate for documents.
 */
public abstract class Predicate implements Bits {

  @Override
  public abstract boolean get(int index);


  @Override
  public int length() {
    throw new UnsupportedOperationException();
  }

}
