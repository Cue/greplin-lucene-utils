package com.greplin.lucene.filter;

import junit.framework.Assert;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;

/**
 * Created with IntelliJ IDEA.
 * User: rheyns
 * Date: 2/20/13
 * Time: 6:49 PM
 */
public class FilterBitsProviderTest extends BaseFilterTest {

  private class TestingDocIdSet extends DocIdSet {
    public DocIdSetIterator iterator() {
      return null;
    }
  }

  private class TestFilter extends Filter {
    public DocIdSet getDocIdSet(IndexReader reader) throws IOException {
      return new TestingDocIdSet();
    }
  }

  IndexReader reader;

  @Before
  public void setUp() throws Exception {
    IndexWriter w = createWriter();
    this.reader = createReader(w);
  }

  @Test
  public void testGetShouldHandleNullDocIdSetIterator() throws Exception {
    Filter test = new TestFilter();
    FilterBitsProvider testProvider = new FilterBitsProvider(test);
    try {
      Bits testBits = testProvider.get(this.reader);
    }
    catch (NullPointerException npe) {
      Assert.fail("NPE");
    }
    Assert.assertTrue(true);
  }
}
