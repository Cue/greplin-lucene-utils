/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.store;

import org.apache.lucene.store.IndexOutput;

import java.io.IOException;

/**
 * IndexOutput that just forwards all calls to a delegate.
 */
public abstract class ForwardingIndexOutput extends IndexOutput {

  /**
   * The delegate index output.
   */
  private final IndexOutput delegate;


  /**
   * Creates a forwarding index output.
   * @param delegate the delegate index output.
   */
  protected ForwardingIndexOutput(final IndexOutput delegate) {
    this.delegate = delegate;
  }


  @Override
  public void flush() throws IOException {
    this.delegate.flush();
  }


  @Override
  public void close() throws IOException {
    this.delegate.close();
  }


  @Override
  public long getFilePointer() {
    return this.delegate.getFilePointer();
  }


  @Override
  public void seek(final long pos) throws IOException {
    this.delegate.seek(pos);
  }


  @Override
  public long length() throws IOException {
    return this.delegate.length();
  }


  @Override
  public void writeByte(final byte b) throws IOException {
    this.delegate.writeByte(b);
  }


  @Override
  public void writeBytes(final byte[] b, final int offset, final int length)
      throws IOException {
    this.delegate.writeBytes(b, offset, length);
  }

}
