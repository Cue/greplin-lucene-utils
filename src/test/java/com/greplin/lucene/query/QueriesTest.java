package com.greplin.lucene.query;/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.util.Version;
import org.junit.Assert;
import org.junit.Test;

public class QueriesTest {
  @Test
  public void testPhraseForWithNullStringReturnsNull() throws Exception {
    Assert.assertNull(Queries.phraseFor(new StandardAnalyzer(Version.LUCENE_34), "field", null));
  }
}
