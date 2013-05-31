/*
 * Copyright 2013 The greplin-lucene-utils Authors.
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

package com.greplin.lucene.util;

import org.apache.lucene.index.TermDocs;

import java.io.IOException;

/**
 * TermDocs intersection utility.
 */
public interface Intersection {

  /**
   * Move the TermDocs to the next doc that also matches this criteria.
   * @param termDocs the TermDocs object to advance
   * @return false if no more docs match
   * @throws IOException if Lucene encounters IO errors
   */
  boolean advanceToNextIntersection(TermDocs termDocs) throws IOException;

}
