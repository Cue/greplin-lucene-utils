/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.index;

import com.google.common.collect.Lists;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.util.ReaderUtil;

import java.util.List;

/**
 * Utilities for IndexReaders.
 */
public final class IndexReaders {

  /** Not instantiable. */
  private IndexReaders() { }


  /**
   * Constructs a list and gathers subreaders into it.  Subreaders will
   * be added to the list in doc id order.
   * @param readers the primary readers to gather from
   * @return a list of subreaders
   */
  public static List<IndexReader> gatherSubReaders(
      final IndexReader... readers) {
    if (readers.length == 1 && readers[0] instanceof GatheredSubReaders) {
      return ((GatheredSubReaders) readers[0]).gatherSubReaders();
    }
    List<IndexReader> result = Lists.newArrayList();
    for (IndexReader reader : readers) {
      if (reader != null) {
        if (reader instanceof GatheredSubReaders) {
          result.addAll(((GatheredSubReaders) reader).gatherSubReaders());
        } else {
          ReaderUtil.gatherSubReaders(result, reader);
        }
      }
    }
    return result;
  }

}
