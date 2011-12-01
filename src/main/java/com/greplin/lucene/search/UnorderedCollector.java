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

import org.apache.lucene.search.Scorer;

import java.io.IOException;

/**
 * Base class for a collectors that collect documents in no specific order.
 */
public abstract class UnorderedCollector extends BaseCollector {
  @Override
  public final void setScorer(final Scorer scorer) throws IOException {
    // Do nothing.
  }

  @Override
  public final boolean acceptsDocsOutOfOrder() {
    return true;
  }


  @Override
  public abstract void collect(final int doc) throws IOException;
}
