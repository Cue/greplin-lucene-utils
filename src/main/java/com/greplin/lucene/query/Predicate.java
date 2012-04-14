/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import org.apache.lucene.util.Bits;

/**
 * Predicate for documents.
 */
public abstract class Predicate implements Bits {

  @Override
  public abstract boolean get(int index);


  @Override
  public int length() {
    return 0;
  }

}
