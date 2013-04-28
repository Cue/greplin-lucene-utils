/*
 * Copyright 2011 The greplin-lucene-utils Authors.
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

package com.greplin.lucene.search;

import org.apache.lucene.index.IndexReader;

import java.io.IOException;

/**
 * Collector that just counts the number of results.
 */
public class CountingCollector extends UnorderedCollector {
  /**
   * The result count.
   */
  private int count = 0;


  /**
   * Returns the result count.
   * @return the result count
   */
  public final int getCount() {
    return this.count;
  }

  @Override
  public final void collect(final int doc) throws IOException {
    this.count++;
  }

  @Override
  public final void setNextReader(final IndexReader reader,
                                  final int docBase) throws IOException {
    // Reader changes don't matter for just getting a count.
  }
}
