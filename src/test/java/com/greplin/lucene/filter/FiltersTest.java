package com.greplin.lucene.filter;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Filter;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for filter utility methods.
 */
public class FiltersTest extends BaseFilterTest {

  private IndexReader reader;


  @Before
  public void setUp() throws Exception {
    IndexWriter w = createWriter();

    Document doc1 = new Document();
    doc1.add(new Field("f", "dog", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("f", "cat", Field.Store.YES, Field.Index.ANALYZED));
    doc1.add(new Field("f", "rat", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc1);

    Document doc2 = new Document();
    doc2.add(new Field("f", "dog", Field.Store.YES, Field.Index.ANALYZED));
    doc2.add(new Field("f", "monkey", Field.Store.YES, Field.Index.ANALYZED));
    doc2.add(new Field("f", "goose", Field.Store.YES, Field.Index.ANALYZED));
    w.addDocument(doc2);

    this.reader = createReader(w);
  }


  @Test
  public void testNot() throws Exception {
    Filter dog = TermsFilter.from(new Term("f", "dog"));
    assertDocIds(dog.getDocIdSet(this.reader), true, true);
    assertDocIds(Filters.not(dog).getDocIdSet(this.reader), false, false);

    Filter cat = TermsFilter.from(new Term("f", "cat"));
    assertDocIds(cat.getDocIdSet(this.reader), true, false);
    assertDocIds(Filters.not(cat).getDocIdSet(this.reader), false, true);
  }

}
