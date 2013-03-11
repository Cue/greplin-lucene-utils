package com.greplin.lucene.index;

import org.apache.lucene.index.IndexReader;

/**
 * A document position: the sub-reader and the offset within that reader.
 */
public class ReaderAndOffset {

  /**
   * The sub-reader that contains the document.
   */
  private final IndexReader reader;


  /**
   * The offset of the document in the reader.
   */
  private final int offset;


  /**
   * Creates a document position detail.
   * @param reader the reader
   * @param offset the offset in that reader
   */
  public ReaderAndOffset(final IndexReader reader, final int offset) {
    this.reader = reader;
    this.offset = offset;
  }


  /**
   * @return the sub-reader that contains the document
   */
  public IndexReader getReader() {
    return this.reader;
  }


  /**
   * @return the offset of the document in the reader
   */
  public int getOffset() {
    return this.offset;
  }

}
