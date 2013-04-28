/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.search;

import java.io.IOException;

/**
 * Determines if there were *any* matches to a query, returning the doc id.
 */
public class AnyDocIdCollector extends UnorderedCollector {
  /**
   * Stores the first matched document id.
   */
  private Integer hit = null;


  /**
   * @return the first matched document id, or null if none matched
   */
  public final Integer getHit() {
    return this.hit;
  }


  @Override
  public final void collect(final int doc) throws IOException {
    if (this.hit == null) {
      this.hit = getCurrentDocBase() + doc;
    }
  }
}
