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

import org.apache.lucene.document.Document;

import java.io.IOException;

/**
 * Determines if there were *any* matches to a query.
 */
public class AnyMatchCollector extends UnorderedCollector {
  /**
   * Stores the first matched document.
   */
  private Document hit = null;


  /**
   * Returns the first matched document, or null if none matched.
   * @return the first matched document, or null if none matched
   */
  public final Document getHit() {
    return this.hit;
  }


  @Override
  public final void collect(final int doc) throws IOException {
    if (this.hit == null) {
      this.hit = getCurrentReader().document(doc);
    }
  }
}
