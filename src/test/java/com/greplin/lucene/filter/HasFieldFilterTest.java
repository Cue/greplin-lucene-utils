/*
 * Copyright 2012 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.filter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Test terms for field wrapper.
 */
public class HasFieldFilterTest extends BaseFilterTest {

  private IndexReader reader;


  @Before
  public void setUp() throws Exception {
    IndexWriter w = createWriter();

    Document doc1 = new Document();
    doc1.add(new Field("field1", "1", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("field2", "2", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("field2", "2again", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("field2", "2", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc2);

    this.reader = createReader(w);
  }


  @Test
  public void testBasics() throws Exception {
    Filter field1 = new HasFieldFilter("field1");
    Filter field2 = new HasFieldFilter("field2");

    DocIdSet hasField1 = field1.getDocIdSet(this.reader);
    Assert.assertTrue(hasField1.isCacheable());

    DocIdSet hasField2 = field2.getDocIdSet(this.reader);
    Assert.assertTrue(hasField2.isCacheable());

    assertDocIds(hasField1, true, false);
    assertDocIds(hasField2, true, true);
  }

}

