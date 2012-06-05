package com.greplin.lucene.index;

import com.google.common.collect.ImmutableList;
import org.apache.lucene.index.IndexReader;

import javax.annotation.Nullable;

/**
 * Interface for an object that has it's own sub-reader gathering function.
 */
public interface GatheredSubReaders {

  /**
   * @return an immutable list of all descendant readers, in order,
   * or null if this is a segment reader.
   */
  @Nullable
  ImmutableList<IndexReader> gatherSubReaders();

}
