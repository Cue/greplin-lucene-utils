package com.greplin.lucene.filter;

import org.apache.lucene.analysis.WhitespaceAnalyzer;
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

import java.io.IOException;

/**
 * Base class for filter tests.
 */
public class BaseFilterTest {

  protected IndexWriter createWriter() throws IOException {
    Directory directory = new RAMDirectory();
    return new IndexWriter(directory,
        new IndexWriterConfig(Version.LUCENE_32, new WhitespaceAnalyzer(Version.LUCENE_32)));
  }

  protected IndexReader createReader(IndexWriter writer) throws IOException {
    Directory d = writer.getDirectory();
    writer.close();
    return IndexReader.open(d);
  }

  protected static void assertDocIds(DocIdSet d, boolean... bits) throws IOException {
    FixedBitSet bitSet = new FixedBitSet(bits.length);
    bitSet.or(d.iterator());
    for (int i = 0; i < bits.length; i++) {
      Assert.assertEquals("Should match expectation at index " + i, bits[i], bitSet.get(i));
    }
  }

  protected static void assertFilterBitsEqual(IndexReader reader, Filter filter, boolean... expected)
      throws IOException {
    DocIdSet actual = filter.getDocIdSet(reader);
    FixedBitSet actualBitSet = new FixedBitSet(expected.length);
    actualBitSet.or(actual.iterator());

    for (int i = 0; i < expected.length; i++) {
      Assert.assertEquals("Expected same value at position " + i, expected[i], actualBitSet.get(i));
    }
  }

}
