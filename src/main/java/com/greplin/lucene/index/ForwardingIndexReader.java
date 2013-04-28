/*
 * Copyright 2011 The greplin-lucene-utils Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greplin.lucene.index;

import org.apache.commons.lang.NotImplementedException;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.IndexCommit;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorMapper;
import org.apache.lucene.store.Directory;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Base class for forwarding readers.
 */
public class ForwardingIndexReader extends IndexReader {

  /**
   * The index reader to forward to.
   */
  private final IndexReader delegate;


  /**
   * Creates a new forwarding index reader.
   * @param delegate the index reader to forward to
   */
  public ForwardingIndexReader(final IndexReader delegate) {
    this.delegate = delegate;
  }

  @Override
  public int getRefCount() {
    return this.delegate.getRefCount();
  }


  @Override
  public void incRef() {
    this.delegate.incRef();
  }


  @Override
  public void decRef() throws IOException {
    this.delegate.decRef();
  }


  @Override
  public IndexReader reopen() throws IOException {
    return this.delegate.reopen();
  }


  @Override
  public IndexReader reopen(final boolean openReadOnly) throws IOException {
    return this.delegate.reopen(openReadOnly);
  }


  @Override
  public IndexReader reopen(final IndexCommit commit) throws IOException {
    return this.delegate.reopen(commit);
  }


  @Override
  public Object clone() {
    return this.delegate.clone();
  }


  @Override
  public IndexReader clone(final boolean openReadOnly) throws IOException {
    return this.delegate.clone(openReadOnly);
  }


  @Override
  public Directory directory() {
    return this.delegate.directory();
  }


  @Override
  public long getVersion() {
    return this.delegate.getVersion();
  }


  @Override
  public Map<String, String> getCommitUserData() {
    return this.delegate.getCommitUserData();
  }


  @Override
  public boolean isCurrent() throws IOException {
    return this.delegate.isCurrent();
  }


  @Override
  public boolean isOptimized() {
    return this.delegate.isOptimized();
  }


  @Override
  public TermFreqVector[] getTermFreqVectors(final int docNumber)
      throws IOException {
    return this.delegate.getTermFreqVectors(docNumber);
  }


  @Override
  public TermFreqVector getTermFreqVector(final int docNumber,
                                          final String field)
      throws IOException {
    return this.delegate.getTermFreqVector(docNumber, field);
  }


  @Override
  public void getTermFreqVector(final int docNumber,
                                final String field,
                                final TermVectorMapper mapper)
      throws IOException {
    this.delegate.getTermFreqVector(docNumber, field, mapper);
  }


  @Override
  public void getTermFreqVector(final int docNumber,
                                final TermVectorMapper mapper)
      throws IOException {
    this.delegate.getTermFreqVector(docNumber, mapper);
  }


  @Override
  public int numDocs() {
    return this.delegate.numDocs();
  }


  @Override
  public int maxDoc() {
    return this.delegate.maxDoc();
  }


  @Override
  public int numDeletedDocs() {
    return this.delegate.numDeletedDocs();
  }


  @Override
  public Document document(final int n) throws IOException {
    return this.delegate.document(n);
  }


  @Override
  public Document document(final int n,
                           final FieldSelector fieldSelector)
      throws IOException {
    return this.delegate.document(n, fieldSelector);
  }


  @Override
  public boolean isDeleted(final int n) {
    return this.delegate.isDeleted(n);
  }


  @Override
  public boolean hasDeletions() {
    return this.delegate.hasDeletions();
  }


  @Override
  public boolean hasNorms(final String field) throws IOException {
    return this.delegate.hasNorms(field);
  }


  @Override
  public byte[] norms(final String field) throws IOException {
    return this.delegate.norms(field);
  }


  @Override
  public void norms(final String field,
                    final byte[] bytes,
                    final int offset) throws IOException {
    this.delegate.norms(field, bytes, offset);
  }


  @Override
  public void setNorm(final int doc,
                      final String field,
                      final byte value) throws IOException {
    this.delegate.setNorm(doc, field, value);
  }


  @Override
  protected void doSetNorm(final int doc,
                           final String field,
                           final byte value) throws IOException {
    throw new NotImplementedException(
        "Not implemented since it's protected in the base class.");
  }


  @Override
  public void setNorm(final int doc,
                      final String field,
                      final float value) throws IOException {
    this.delegate.setNorm(doc, field, value);
  }


  @Override
  public TermEnum terms() throws IOException {
    return this.delegate.terms();
  }


  @Override
  public TermEnum terms(final Term t) throws IOException {
    return this.delegate.terms(t);
  }


  @Override
  public int docFreq(final Term t) throws IOException {
    return this.delegate.docFreq(t);
  }


  @Override
  public TermDocs termDocs(final Term term) throws IOException {
    return this.delegate.termDocs(term);
  }


  @Override
  public TermDocs termDocs() throws IOException {
    return this.delegate.termDocs();
  }


  @Override
  public TermPositions termPositions(final Term term) throws IOException {
    return this.delegate.termPositions(term);
  }


  @Override
  public TermPositions termPositions() throws IOException {
    return this.delegate.termPositions();
  }


  @Override
  public void deleteDocument(final int docNum) throws IOException {
    this.delegate.deleteDocument(docNum);
  }


  @Override
  protected void doDelete(final int docNum) throws IOException {
    throw new NotImplementedException(
        "Not implemented since it's protected in the base class.");
  }


  @Override
  public int deleteDocuments(final Term term) throws IOException {
    return this.delegate.deleteDocuments(term);
  }


  @Override
  public void undeleteAll() throws IOException {
    this.delegate.undeleteAll();
  }


  @Override
  protected void doUndeleteAll() throws IOException {
    throw new NotImplementedException(
        "Not implemented since it's protected in the base class.");
  }


  @Override
  protected void acquireWriteLock() throws IOException {
    throw new NotImplementedException(
        "Not implemented since it's protected in the base class.");
  }


  @Override
  protected void doCommit(final Map<String, String> commitUserData)
      throws IOException {
    throw new NotImplementedException(
        "Not implemented since it's protected in the base class.");
  }


  @Override
  protected void doClose() throws IOException {
    throw new NotImplementedException(
        "Not implemented since it's protected in the base class.");
  }


  @Override
  public Collection<String> getFieldNames(final FieldOption fldOption) {
    return this.delegate.getFieldNames(fldOption);
  }


  @Override
  public IndexCommit getIndexCommit() throws IOException {
    return this.delegate.getIndexCommit();
  }


  @Override
  public IndexReader[] getSequentialSubReaders() {
    return this.delegate.getSequentialSubReaders();
  }


  @Override
  public Object getCoreCacheKey() {
    return this.delegate.getCoreCacheKey();
  }


  @Override
  public Object getDeletesCacheKey() {
    return this.delegate.getDeletesCacheKey();
  }


  @Override
  public long getUniqueTermCount() throws IOException {
    return this.delegate.getUniqueTermCount();
  }


  @Override
  public int getTermInfosIndexDivisor() {
    return this.delegate.getTermInfosIndexDivisor();
  }
}
