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

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.TermDocs;

import java.io.IOException;

/**
 * The most trivial intersection - just lets everything through.
 */
public final class AllDocsIntersectionProvider implements IntersectionProvider {

  /**
   * Intersection constant that intersects with everything.
   */
  private static final Intersection ALL_DOCS = new Intersection() {
    @Override
    public boolean advanceToNextIntersection(final TermDocs termDocs)
        throws IOException {
      return termDocs.next();
    }
  };

  /**
   * Singleton instance of this class.
   */
  public static final IntersectionProvider INSTANCE =
      new AllDocsIntersectionProvider();


  /** Not instantiable. */
  private AllDocsIntersectionProvider() { }


  @Override
  public Intersection get(final IndexReader reader) {
    return ALL_DOCS;
  }

  @Override
  public String toString() {
    return "AllDocsIntersectionProvider";
  }

}
