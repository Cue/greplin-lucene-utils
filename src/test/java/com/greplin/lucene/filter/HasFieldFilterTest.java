/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.FixedBitSet;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test terms for field wrapper.
 */
public class HasFieldFilterTest {

  private IndexReader reader;


  @Before
  public void setUp() throws Exception {
    Directory d = new RAMDirectory();
    IndexWriter w = new IndexWriter(
        d, new IndexWriterConfig(Version.LUCENE_32,
            new WhitespaceAnalyzer(Version.LUCENE_32)));

    Document doc1 = new Document();
    doc1.add(new Field("field1", "1", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("field2", "2", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("field2", "2again", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("field2", "2", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc2);

    w.close();

    reader = IndexReader.open(d);
  }


  @Test
  public void testBasics() throws Exception {
    Filter field1 = new HasFieldFilter("field1");
    Filter field2 = new HasFieldFilter("field2");

    DocIdSet idSet1 = field1.getDocIdSet(reader);
    Assert.assertTrue(idSet1.isCacheable());
    FixedBitSet bitSet1 = new FixedBitSet(reader.maxDoc());
    bitSet1.or(idSet1.iterator());

    DocIdSet idSet2 = field2.getDocIdSet(reader);
    Assert.assertTrue(idSet2.isCacheable());
    FixedBitSet bitSet2 = new FixedBitSet(reader.maxDoc());
    bitSet2.or(idSet2.iterator());

    // First document has both fields.
    Assert.assertTrue(bitSet1.get(0));
    Assert.assertTrue(bitSet2.get(0));

    // Second document only has second field.
    Assert.assertFalse(bitSet1.get(1));
    Assert.assertTrue(bitSet2.get(1));
  }

}

