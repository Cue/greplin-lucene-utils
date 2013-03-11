package com.greplin.lucene.search;

import com.google.common.primitives.Ints;
import org.apache.lucene.search.ScoreDoc;

import java.util.Comparator;

/**
 * ScoreDoc comparator based on score.
 */
public class ScoreDocDocIdComparator implements Comparator<ScoreDoc> {

  @Override
  public int compare(final ScoreDoc o1, final ScoreDoc o2) {
    return Ints.compare(o1.doc, o2.doc);
  }

}
