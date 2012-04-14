/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package org.apache.lucene.index;

/**
 * Total hack!  Wanted to be able to use the package protected multi term
 * positions implementation so we make it visible here.
 */
public class HackMultiTermPositions extends DirectoryReader.MultiTermPositions {

  /**
   * Creates a TermPositions implementation over the given subreaders.
   * @param topReader the top reader.
   * @param readers the subreaders.
   * @param starts doc indices of subreader starts.
   */
  public HackMultiTermPositions(final IndexReader topReader,
                                final IndexReader[] readers,
                                final int[] starts) {
    super(topReader, readers, starts);
  }

}
