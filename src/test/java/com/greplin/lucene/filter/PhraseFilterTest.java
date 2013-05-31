package com.greplin.lucene.filter;

import com.greplin.lucene.predicate.BitsProvider;
import com.greplin.lucene.util.BitsProviderIntersectionProvider;
import com.greplin.lucene.util.FilterIntersectionProvider;
import com.greplin.lucene.util.IntersectionProvider;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.util.Bits;
import org.junit.Test;

import java.io.IOException;

/**
 * Test for phrase filters.
 */
public class PhraseFilterTest extends BaseFilterTest {

  private IndexReader createReaderWithSampleDocuments() throws IOException {
    IndexWriter w = createWriter();

    Document doc1 = new Document();
    doc1.add(new Field("f", "i love to say hello world to everyone", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("name", "one", Field.Store.NO, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("f", "i hate to say world hello to anyone", Field.Store.YES, Field.Index.ANALYZED));
    doc2.add(new Field("name", "two", Field.Store.NO, Field.Index.ANALYZED));
    w.addDocument(doc2);

    Document doc3 = new Document();
    doc3.add(new Field("f", "hello hello world world", Field.Store.YES, Field.Index.ANALYZED));
    doc3.add(new Field("name", "three", Field.Store.NO, Field.Index.ANALYZED));
    w.addDocument(doc3);

    return createReader(w);
  }

  @Test
  public void testBasics() throws Exception {
    IndexReader reader = createReaderWithSampleDocuments();

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

  @Test
  public void testIntersectionWithFilter() throws Exception {
    IndexReader reader = createReaderWithSampleDocuments();
    IntersectionProvider filter = new FilterIntersectionProvider(
        TermsFilter.from(new Term("name", "one"), new Term("name", "two")));

    assertFilterBitsEqual(reader, new PhraseFilter("f", "world"), true, true, true);
    assertFilterBitsEqual(reader, new PhraseFilter(filter, "f", "world"), true, true, false);

    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello", "world"), true, false, true);
    assertFilterBitsEqual(reader, new PhraseFilter(filter, "f", "hello", "world"), true, false, false);
  }

  @Test
  public void testIntersectionWithPredicate() throws Exception {
    IndexReader reader = createReaderWithSampleDocuments();
    IntersectionProvider predicate = new BitsProviderIntersectionProvider(
        new BitsProvider() {
          @Override
          public Bits get(IndexReader reader) throws IOException {
            return new Bits() {
              @Override
              public boolean get(int index) {
                return index != 0;
              }

              @Override
              public int length() {
                return 3;
              }
            };
          }
        });

    assertFilterBitsEqual(reader, new PhraseFilter("f", "world"), true, true, true);
    assertFilterBitsEqual(reader, new PhraseFilter(predicate, "f", "world"), false, true, true);

    assertFilterBitsEqual(reader, new PhraseFilter("f", "hello", "world"), true, false, true);
    assertFilterBitsEqual(reader, new PhraseFilter(predicate, "f", "hello", "world"), false, false, true);
  }

}
