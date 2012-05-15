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

package com.greplin.lucene.query;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.Query;

import java.io.IOException;

/**
 * A Query that just wraps a sub query and adds a name to it in toString()
 */
public class NamedQuery extends Query {

  /**
   * The query to match.
   */
  private final Query query;

  /**
   * The name of the query, for toString.
   */
  private final String name;

  /**
   * Constructs a query that adds a name to a sub query.
   * @param query the sub query
   * @param name the name of the query
   */
  public NamedQuery(final Query query, final String name) {
    this.query = query;
    this.name = name;
  }

  @Override
  public final String toString(final String field) {
    return this.name + '=' + this.query.toString(field);
  }

  @Override
  public final int hashCode() {
    // Only check for query equality.
    return this.query.hashCode();
  }

  @Override
  public final boolean equals(final Object obj) {
    if (this == obj) {
      return true;
    }
    if (!super.equals(obj)) {
      return false;
    }
    if (getClass() != obj.getClass()) {
      return false;
    }
    NamedQuery other = (NamedQuery) obj;
    // Only check for query equality.
    return this.query.equals(other.query);
  }


  @Override
  public final Query rewrite(final IndexReader reader) throws IOException {
    return this.query.rewrite(reader);
  }

}
