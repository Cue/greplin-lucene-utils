/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.index;

import com.google.common.collect.ImmutableList;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.List;

/**
 * Test terms for field wrapper.
 */
public class TermsForFieldTest {

  private IndexReader reader;


  @Before
  public void setUp() throws Exception {
    Directory d = new RAMDirectory();
    IndexWriter w = new IndexWriter(
        d, new IndexWriterConfig(Version.LUCENE_32,
            new WhitespaceAnalyzer(Version.LUCENE_32)));

    Document doc1 = new Document();
    doc1.add(new Field("stored", "1", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("stored", "2", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("notStored", "a", Field.Store.NO, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("stored", "3", Field.Store.YES, Field.Index.ANALYZED));
    doc2.add(new Field("notStored", "b", Field.Store.NO, Field.Index.ANALYZED));
    doc2.add(new Field("noIndex", "?", Field.Store.YES, Field.Index.NO));
    w.addDocument(doc2);

    w.close();

    this.reader = IndexReader.open(d);
  }


  @Test
  public void testStoredAndIndexed() throws Exception {
    List<Term> terms = ImmutableList.copyOf(
        new TermsForField(this.reader, "stored"));
    Assert.assertEquals(
        ImmutableList.of(
            new Term("stored", "1"),
            new Term("stored", "2"),
            new Term("stored", "3")),
        terms);
  }


  @Test
  public void testIndexed() throws Exception {
    List<Term> terms = ImmutableList.copyOf(
        new TermsForField(this.reader, "notStored"));
    Assert.assertEquals(
        ImmutableList.of(
            new Term("notStored", "a"),
            new Term("notStored", "b")),
        terms);
  }


  @Test
  public void testUnindexed() throws Exception {
    List<Term> terms = ImmutableList.copyOf(
        new TermsForField(this.reader, "noIndex"));
    Assert.assertEquals(Collections.<Term>emptyList(), terms);
  }


  @Test
  public void testNonexistent() throws Exception {
    List<Term> terms = ImmutableList.copyOf(
        new TermsForField(this.reader, "redherring"));
    Assert.assertEquals(Collections.<Term>emptyList(), terms);
  }
}
