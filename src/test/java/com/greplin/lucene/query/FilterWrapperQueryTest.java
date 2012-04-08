/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.Set;

/**
 * Tests for the filter wrapper query.
 */
public class FilterWrapperQueryTest {

  private IndexReader reader;


  @Before
  public void setUp() throws Exception {
    Directory d = new RAMDirectory();
    IndexWriter w = new IndexWriter(
        d, new IndexWriterConfig(Version.LUCENE_35,
            new WhitespaceAnalyzer(Version.LUCENE_35)));

    for (int i = 0; i < 5; i++) {
      Document doc = new Document();
      doc.add(new Field(
          "id", String.valueOf(i), Field.Store.YES, Field.Index.ANALYZED));
      w.addDocument(doc);
    }

    w.close();

    this.reader = IndexReader.open(d);
  }


  private Set<Integer> search(Filter f) throws IOException {
    TopDocs results = new IndexSearcher(this.reader)
        .search(new FilterWrapperQuery(f), 100);
    Set<Integer> result = Sets.newHashSet();
    for (ScoreDoc scoreDoc : results.scoreDocs) {
      Assert.assertEquals(1.0f, scoreDoc.score, 0.00001);
      result.add(scoreDoc.doc);
    }
    return result;
  }

  @Test
  public void testBasics() throws Exception {
    Assert.assertEquals(Collections.<Integer>emptySet(), search(
        new Filter() {
          @Override
          public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            return new FixedBitSet(reader.maxDoc());
          }
        }
    ));
  }

  @Test
  public void testSeveral() throws Exception {
    Assert.assertEquals(ImmutableSet.of(0, 3), search(
        new Filter() {
          @Override
          public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            FixedBitSet result = new FixedBitSet(reader.maxDoc());
            result.set(0);
            result.set(3);
            return result;
          }
        }
    ));
  }

  @Test
  public void testAll() throws Exception {
    Assert.assertEquals(ImmutableSet.of(0, 1, 2, 3, 4), search(
        new Filter() {
          @Override
          public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
            FixedBitSet result = new FixedBitSet(reader.maxDoc());
            result.set(0);
            result.set(1);
            result.set(2);
            result.set(3);
            result.set(4);
            return result;
          }
        }
    ));
  }

}
