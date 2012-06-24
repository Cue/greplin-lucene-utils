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

package com.greplin.lucene.filter;

import org.apache.lucene.index.IndexReader;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;

import java.io.IOException;

/**
 * A Filter that just wraps a sub filter and names it in toString().
 */
public class NamedFilter extends Filter {

  /**
   * The underlying filter.
   */
  private final Filter underlying;

  /**
   * The name of the filter, for toString.
   */
  private final String name;


  /**
   * Whether to include the underlying result in toString.
   */
  private final boolean includeUnderlying;

  /**
   * Constructs a filter that adds a name to another filter.
   * @param underlying the sub filter
   * @param name the name of the filter
   * @param includeUnderlying whether to include the underlying result
   *     in toString.
   */
  public NamedFilter(final Filter underlying,
                     final String name,
                     final boolean includeUnderlying) {
    this.underlying = underlying;
    this.name = name;
    this.includeUnderlying = includeUnderlying;
  }

  @Override
  public final String toString() {
    if (this.includeUnderlying) {
      return this.name + '=' + this.underlying.toString();
    } else {
      return this.name;
    }
  }

  @Override
  public final int hashCode() {
    // Only check for filter equality.
    return this.underlying.hashCode();
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
    NamedFilter other = (NamedFilter) obj;
    // Only check for filter equality.
    return this.underlying.equals(other.underlying);
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader)
      throws IOException {
    return this.underlying.getDocIdSet(reader);
  }

}
