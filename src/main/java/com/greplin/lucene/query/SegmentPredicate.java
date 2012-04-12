/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

/**
 * Predicate for documents.
 */
public interface SegmentPredicate {

  /**
   * Returns whether the given docId should be included in the results.
   * @param docId the docId.
   * @return whether the given docId should be included in the results.
   */
  boolean isIncluded(int docId);

}
