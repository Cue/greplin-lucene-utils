/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.index;

import com.google.common.collect.ImmutableList;
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
      List<IndexReader> gathered =
          ((GatheredSubReaders) readers[0]).gatherSubReaders();
      return gathered == null ? ImmutableList.of(readers[0]) : gathered;
    }
    List<IndexReader> result = Lists.newArrayList();
    for (IndexReader reader : readers) {
      if (reader != null) {
        if (reader instanceof GatheredSubReaders) {
          List<IndexReader> gathered =
              ((GatheredSubReaders) reader).gatherSubReaders();
          if (gathered == null) {
            result.add(reader);
          } else {
            result.addAll(gathered);
          }
        } else {
          ReaderUtil.gatherSubReaders(result, reader);
        }
      }
    }
    return result;
  }


  /**
   * Gets the sub-reader and the offset within that reader
   * of the given global document id.
   * @param reader the main reader
   * @param docId offset within that reader
   * @return the sub-reader and offset
   */
  public static ReaderAndOffset getReaderAndOffset(
      final IndexReader reader, final int docId) {
    int pos = 0;
    int index = 0;
    for (IndexReader subReader : gatherSubReaders(reader)) {
      if (pos + subReader.maxDoc() > docId) {
        return new ReaderAndOffset(subReader, index, docId - pos);
      } else {
        pos += subReader.maxDoc();
        index++;
      }
    }
    return null;
  }

}
