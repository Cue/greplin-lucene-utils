/*
 * Copyright 2010 Greplin, Inc. All Rights Reserved.
 */

package com.greplin.lucene.query;

import org.apache.lucene.search.DefaultSimilarity;

/**
 * Sets queryNorm to 1.0 in all cases.
 */
public class ConstantQueryNormSimilarity extends DefaultSimilarity {

  @Override
  public float queryNorm(final float sumOfSquaredWeights) {
    return 1.0f;
  }

}
