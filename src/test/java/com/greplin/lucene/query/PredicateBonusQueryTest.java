/*
 * Copyright 2012 The greplin-lucene-utils Authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.greplin.lucene.query;

import com.greplin.lucene.document.DocumentBuilder;
import com.greplin.lucene.predicate.Predicates;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.ConstantScoreQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * Tests for the PredicateBonusQuery.
 */
public class PredicateBonusQueryTest {

  private RAMDirectory directory;


  @Before
  public void setUp() throws Exception {
    this.directory = new RAMDirectory();
  }


  @Test
  public void testBasics() throws Exception {
    IndexWriter writer = new IndexWriter(this.directory,
        new IndexWriterConfig(
            Version.LUCENE_35, new WhitespaceAnalyzer(Version.LUCENE_35)));
    writer.addDocument(new DocumentBuilder().add("value", "5").build());
    writer.close();

    IndexReader reader = IndexReader.open(this.directory);
    IndexSearcher searcher = new IndexSearcher(reader);

    Query query = new ConstantScoreQuery(new TermQuery(new Term("value", "5")));
    Assert.assertEquals(1.0, searcher.search(query, 1).getMaxScore(), 0.00001);

    Query noBonus = new PredicateBonusQuery(query, Predicates.NONE, 10.0f);
    Assert.assertEquals(1.0, searcher.search(noBonus, 1).getMaxScore(), 0.00001);

    Query bonus = new PredicateBonusQuery(query, Predicates.ALL, 100.0f);
    Assert.assertEquals(101.0, searcher.search(bonus, 1).getMaxScore(), 0.00001);

    Query noMatch = new TermQuery(new Term("value", "not5"));
    Assert.assertEquals(Double.NaN, searcher.search(noMatch, 1).getMaxScore(), 0.00001);

    Query noMatchNoBonus = new PredicateBonusQuery(noMatch, Predicates.NONE, 10.0f);
    Assert.assertEquals(Double.NaN, searcher.search(noMatchNoBonus, 1).getMaxScore(), 0.00001);

    Query noMatchIgnoresBonus = new PredicateBonusQuery(noMatch, Predicates.ALL, 100.0f);
    Assert.assertEquals(Double.NaN, searcher.search(noMatchIgnoresBonus, 1).getMaxScore(), 0.00001);
  }

}
