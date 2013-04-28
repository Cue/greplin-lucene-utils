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

import com.google.common.collect.Lists;
import org.apache.lucene.document.Document;

import java.io.IOException;
import java.util.List;

/**
 * Saves all matches it encounters, in no particular order.
 */
public class AllMatchCollector extends UnorderedCollector {
  /**
   * List of documents.
   */
  private List<Document> docs;


  /**
   * Creates a new all match collector.
   */
  public AllMatchCollector() {
    this.docs = Lists.newLinkedList();
  }


  @Override
  public final void collect(final int doc) throws IOException {
    this.docs.add(getCurrentReader().document(doc));
  }


  /**
   * Returns a list of all matching documents.
   * @return list of all matching documents
   */
  public final List<Document> getMatches() {
    return this.docs;
  }

}
