/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import org.apache.lucene.index.IndexReader;

/**
 * Predicate for documents.
 */
public interface DocPredicate {

  /**
   * Returns a segment predicate for the given segment.
   * @param reader the segment reader.
   * @return the segment predicate.
   */
  SegmentPredicate get(IndexReader reader);

}
