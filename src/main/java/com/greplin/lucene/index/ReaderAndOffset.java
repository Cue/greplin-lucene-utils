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
   * The index in to the sub reader array where reader occurs.
   */
  private final int readerOffset;


  /**
   * The offset of the document in the reader.
   */
  private final int offset;


  /**
   * Creates a document position detail.
   * @param reader the reader
   * @param readerOffset the index in to the sub readers where reader occurs
   * @param offset the offset in that reader
   */
  public ReaderAndOffset(
      final IndexReader reader, final int readerOffset, final int offset) {
    this.reader = reader;
    this.readerOffset = readerOffset;
    this.offset = offset;
  }


  /**
   * @return the sub-reader that contains the document
   */
  public IndexReader getReader() {
    return this.reader;
  }


  /**
   * @return the index in to the sub reader array where reader occurs
   */
  public int getReaderOffset() {
    return this.readerOffset;
  }


  /**
   * @return the offset of the document in the reader
   */
  public int getOffset() {
    return this.offset;
  }

}
