/*
 * Copyright 2012 The greplin-lucene-utils Authors.
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

package com.greplin.lucene.document;

import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;

/**
 * Utility for building documents without local variables.
 */
public class DocumentBuilder {

  /**
   * The document being built.
   */
  private final Document document;


  /**
   * Construct a new document builder.
   */
  public DocumentBuilder() {
    this.document = new Document();
  }


  /**
   * Adds a field.
   * @param field the field name.
   * @param value the field value.
   * @param store how to store the field.
   * @param index how to index the field.
   * @return this, in a builder pattern.
   */
  public DocumentBuilder add(
      final String field,
      final String value,
      final Field.Store store,
      final Field.Index index) {
    this.document.add(new Field(field, value, store, index));
    return this;
  }


  /**
   * Adds a field.
   * @param field the field name.
   * @param value the field value.
   * @return this, in a builder pattern.
   */
  public DocumentBuilder add(final String field, final String value) {
    return add(field, value, Field.Store.YES, Field.Index.ANALYZED);
  }


  /**
   * @return the built document.
   */
  public Document build() {
    return this.document;
  }

}
