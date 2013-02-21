package com.greplin.lucene.filter;

import junit.framework.Assert;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.CorruptIndexException;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.index.TermFreqVector;
import org.apache.lucene.index.TermPositions;
import org.apache.lucene.index.TermVectorMapper;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.DocIdSetIterator;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.Bits;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.Collection;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: rheyns
 * Date: 2/20/13
 * Time: 6:49 PM
 * To change this template use File | Settings | File Templates.
 */
public class FilterBitsProviderTest extends BaseFilterTest {
  private class TestingIndexReader extends IndexReader {
    @Override
    public TermFreqVector[] getTermFreqVectors(int docNumber) throws IOException {
      return new TermFreqVector[0];  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public TermFreqVector getTermFreqVector(int docNumber, String field) throws IOException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void getTermFreqVector(int docNumber, String field, TermVectorMapper mapper) throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void getTermFreqVector(int docNumber, TermVectorMapper mapper) throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public int numDocs() {
      return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public int maxDoc() {
      return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public Document document(int n, FieldSelector fieldSelector) throws CorruptIndexException, IOException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean isDeleted(int n) {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public boolean hasDeletions() {
      return false;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public byte[] norms(String field) throws IOException {
      return new byte[0];  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public void norms(String field, byte[] bytes, int offset) throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    protected void doSetNorm(int doc, String field, byte value) throws CorruptIndexException, IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public TermEnum terms() throws IOException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public TermEnum terms(Term t) throws IOException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public int docFreq(Term t) throws IOException {
      return 0;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public TermDocs termDocs() throws IOException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public TermPositions termPositions() throws IOException {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    protected void doDelete(int docNum) throws CorruptIndexException, IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    protected void doUndeleteAll() throws CorruptIndexException, IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    protected void doCommit(Map<String, String> commitUserData) throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    protected void doClose() throws IOException {
      //To change body of implemented methods use File | Settings | File Templates.
    }


    @Override
    public Collection<String> getFieldNames(FieldOption fldOption) {
      return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
  };

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

  TestingIndexReader reader = new TestingIndexReader();
  @Before
  public void setUp() throws Exception {

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
