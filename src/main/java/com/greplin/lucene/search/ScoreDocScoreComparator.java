package com.greplin.lucene.search;

import com.google.common.primitives.Floats;
import org.apache.lucene.search.ScoreDoc;

import java.util.Comparator;

/**
 * ScoreDoc comparator based on score.
 */
public class ScoreDocScoreComparator implements Comparator<ScoreDoc> {

  @Override
  public int compare(final ScoreDoc o1, final ScoreDoc o2) {
    return Floats.compare(o1.score, o2.score);
  }

}
