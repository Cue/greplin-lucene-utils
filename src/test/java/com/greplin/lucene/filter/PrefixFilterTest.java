package com.greplin.lucene.filter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.junit.Test;

/**
 * Tests for the prefix filter.
 */
public class PrefixFilterTest extends BaseFilterTest {

  @Test
  public void testBasics() throws Exception {
    IndexWriter w = createWriter();

    Document doc1 = new Document();
    doc1.add(new Field("f", "party", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("f", "partner", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("f", "partridge", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc2);

    IndexReader reader = createReader(w);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "o"), false, false);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "par"), true, true);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "partner"), true, false);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "partridge"), false, true);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "partridges"), false, false);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "parts"), false, false);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "partr"), false, true);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "party"), true, false);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "q"), false, false);
  }

  @Test
  public void testFieldBoundary() throws Exception {
    IndexWriter w = createWriter();

    Document doc1 = new Document();
    doc1.add(new Field("f", "party", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("g", "quiz", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc1);

    IndexReader reader = createReader(w);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "p"), true);
    assertFilterBitsEqual(reader, new PrefixFilter("f", "q"), false);
    assertFilterBitsEqual(reader, new PrefixFilter("g", "p"), false);
    assertFilterBitsEqual(reader, new PrefixFilter("g", "q"), true);
  }

}
