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

import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.Query;

/**
 * Builder patterns for boolean queries.
 */
public class BooleanQueryBuilder {
  /**
   * The query being built.
   */
  private BooleanQuery result;


  /**
   * Constructs a query builder.
   */
  public BooleanQueryBuilder() {
    this(false);
  }


  /**
   * Constructs a query builder, optionally disabling coordination.
   * @param disableCoordination whether to disable score coordination
   */
  public BooleanQueryBuilder(final boolean disableCoordination) {
    this.result = new BooleanQuery(disableCoordination);
  }


  /**
   * Static method that returns a new query builder.
   * @return a new query builder
   */
  public static BooleanQueryBuilder builder() {
    return new BooleanQueryBuilder();
  }


  /**
   * Static method that returns a new query builder.
   * @param disableCoordination whether to disable score coordination
   * @return a new query builder
   */
  public static BooleanQueryBuilder builder(
      final boolean disableCoordination) {
    return new BooleanQueryBuilder(disableCoordination);
  }


  /**
   * Builds the query builder in to a BooleanQuery.
   * @return the built BooleanQuery
   */
  public final BooleanQuery build() {
    return this.result;
  }


  /**
   * Adds a should clause to the query.
   * @param query the query that should match
   * @return this object
   */
  public final BooleanQueryBuilder should(final Query query) {
    if (query != null) {
      this.result.add(query, BooleanClause.Occur.SHOULD);
    }
    return this;
  }

  /**
   * Adds a must clause to the query.
   * @param query the query that must match
   * @return this object
   */
  public final BooleanQueryBuilder must(final Query query) {
    if (query != null) {
      this.result.add(query, BooleanClause.Occur.MUST);
    }
    return this;
  }

  /**
   * Adds a must not clause to the query.
   * @param query the query that must not match
   * @return this object
   */
  public final BooleanQueryBuilder mustNot(final Query query) {
    if (query != null) {
      this.result.add(query, BooleanClause.Occur.MUST_NOT);
    }
    return this;
  }

  /**
   * Creates a query that is the opposite of the given query.
   * @param query the query that must not occur
   * @return a query that matches all documents that do not match
   *     the given query
   */
  public static BooleanQuery not(final Query query) {
    return builder().should(new MatchAllDocsQuery()).mustNot(query).build();
  }
}
