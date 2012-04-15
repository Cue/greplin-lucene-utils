/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.store;

import org.apache.lucene.store.IndexInput;

import java.io.IOException;

/**
 * IndexInput that just forwards all calls to a delegate.
 */
public abstract class ForwardingIndexInput extends IndexInput {

  /**
   * The delegate index input.
   */
  private final IndexInput delegate;


  /**
   * Creates a forwarding index input.
   * @param name the name of the resource.
   * @param delegate the delegate index input.
   */
  protected ForwardingIndexInput(final String name, final IndexInput delegate) {
    super(name);
    this.delegate = delegate;
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
  public long length() {
    return this.delegate.length();
  }


  @Override
  public byte readByte() throws IOException {
    return this.delegate.readByte();
  }


  @Override
  public void readBytes(final byte[] b, final int offset, final int len)
      throws IOException {
    this.delegate.readBytes(b, offset, len);
  }

}
