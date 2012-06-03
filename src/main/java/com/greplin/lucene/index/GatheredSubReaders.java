package com.greplin.lucene.index;

import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.IndexReader;

/**
 * Interface for an object that has it's own sub-reader gathering function.
 */
public interface GatheredSubReaders {

  /**
   * @return an immutable list of all descendant readers, in order.
   */
  ImmutableList<IndexReader> gatherSubReaders();

}
