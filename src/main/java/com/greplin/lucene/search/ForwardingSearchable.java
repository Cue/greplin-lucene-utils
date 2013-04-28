/*
 * Copyright 2011 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.search;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.FieldSelector;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.Collector;
import org.apache.lucene.search.Explanation;
import org.apache.lucene.search.Filter;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.Searchable;
import org.apache.lucene.search.Sort;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.search.TopFieldDocs;
import org.apache.lucene.search.Weight;

import java.io.IOException;

/**
 * Convenience class to make tweaking the behavior of a seacher easy.
 */
public class ForwardingSearchable implements Searchable {

  /**
   * The searchable to forward to.
   */
  private final Searchable searcher;


  /**
   * Creates a new forwarding searchable.
   * @param searcher the searchable to forward to
   */
  public ForwardingSearchable(final Searchable searcher) {
    this.searcher = searcher;
  }

  @Override
  public void search(final Weight weight,
                     final Filter filter,
                     final Collector results) throws IOException {
    this.searcher.search(weight, filter, results);
  }


  @Override
  public void close() throws IOException {
    this.searcher.close();
  }


  @Override
  public int docFreq(final Term term) throws IOException {
    return this.searcher.docFreq(term);
  }


  @Override
  public int[] docFreqs(final Term[] terms) throws IOException {
    return this.searcher.docFreqs(terms);
  }


  @Override
  public int maxDoc() throws IOException {
    return this.searcher.maxDoc();
  }


  @Override
  public TopDocs search(final Weight weight,
                        final Filter filter,
                        final int n) throws IOException {
    return this.searcher.search(weight, filter, n);
  }


  @Override
  public Document doc(final int i) throws IOException {
    return this.searcher.doc(i);
  }


  @Override
  public Document doc(final int docid,
                      final FieldSelector fieldSelector) throws IOException {
    return this.searcher.doc(docid, fieldSelector);
  }


  @Override
  public Query rewrite(final Query query) throws IOException {
    return this.searcher.rewrite(query);
  }


  @Override
  public Explanation explain(final Weight weight,
                             final int doc) throws IOException {
    return this.searcher.explain(weight, doc);
  }


  @Override
  public TopFieldDocs search(final Weight weight,
                             final Filter filter,
                             final int n,
                             final Sort sort) throws IOException {
    return this.searcher.search(weight, filter, n, sort);
  }
}
