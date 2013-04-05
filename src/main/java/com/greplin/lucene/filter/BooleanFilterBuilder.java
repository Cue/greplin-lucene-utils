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

package com.greplin.lucene.filter;

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanFilter;
import org.apache.lucene.search.Filter;

/**
 * Builder patterns for boolean filters.
 */
public class BooleanFilterBuilder {

  /**
   * The filter being built.
   */
  private BooleanFilter result;


  /**
   * Constructs a filter builder.
   */
  public BooleanFilterBuilder() {
    this.result = new BooleanFilter();
  }


  /**
   * Static method that returns a new filter builder.
   * @return a new filter builder
   */
  public static BooleanFilterBuilder builder() {
    return new BooleanFilterBuilder();
  }


  /**
   * Builds the filter builder in to a BooleanFilter.
   * @return the built BooleanFilter
   */
  public final BooleanFilter build() {
    return this.result;
  }


  /**
   * Adds a should clause to the filter.
   * @param filter the filter that should match
   * @return this object
   */
  public final BooleanFilterBuilder should(final Filter filter) {
    if (filter != null) {
      this.result.add(filter, BooleanClause.Occur.SHOULD);
    }
    return this;
  }

  /**
   * Adds a must clause to the filter.
   * @param filter the filter that must match
   * @return this object
   */
  public final BooleanFilterBuilder must(final Filter filter) {
    if (filter != null) {
      this.result.add(filter, BooleanClause.Occur.MUST);
    }
    return this;
  }

  /**
   * Adds a must not clause to the filter.
   * @param filter the filter that must not match
   * @return this object
   */
  public final BooleanFilterBuilder mustNot(final Filter filter) {
    if (filter != null) {
      this.result.add(filter, BooleanClause.Occur.MUST_NOT);
    }
    return this;
  }

}
