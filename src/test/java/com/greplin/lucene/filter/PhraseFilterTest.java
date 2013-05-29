package com.greplin.lucene.filter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.junit.Test;

/**
 * Test for phrase filters.
 */
public class PhraseFilterTest extends BaseFilterTest {

  @Test
  public void testBasics() throws Exception {
    IndexWriter w = createWriter();

    Document doc1 = new Document();
    doc1.add(new Field("f", "i love to say hello world to everyone", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("f", "i hate to say world hello to anyone", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc2);

    Document doc3 = new Document();
    doc3.add(new Field("f", "hello hello world world", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc3);

    IndexReader reader = createReader(w);

    assertFilterBitsEqual(reader, new PhraseFilter("f", "world"), true, true, true);
    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello"), true, true, true);

    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello", "world"), true, false, true);
    assertFilterBitsEqual(reader, new PhraseFilter("f", "to", "say"), true, true, false);
    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello", "hello"), false, false, true);
    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello", "everyone"), false, false, false);
    assertFilterBitsEqual(reader, new PhraseFilter("f", "tomato", "hello"), false, false, false);

    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello", "world", "hello"), false, false, false);
    assertFilterBitsEqual(reader, new PhraseFilter("f", "love", "to", "say"), true, false, false);
  }

}
