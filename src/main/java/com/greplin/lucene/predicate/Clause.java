package com.greplin.lucene.predicate;

import com.google.common.base.Objects;
import org.apache.lucene.search.BooleanClause.Occur;

/**
 * A value and an Occur.
 * @param <T> the type of occurrence
 */
public final class Clause<T> {

  /**
   * The occurrence type.
   */
  private final Occur occur;

  /**
   * The value.
   */
  private final T value;


  /**
   * Creates a clause.
   * @param value the value.
   * @param occur the type of occurrence.
   */
  public Clause(final T value, final Occur occur) {
    this.occur = occur;
    this.value = value;
  }


  /**
   * @return the value.
   */
  public T getValue() {
    return this.value;
  }


  /**
   * @return the type of occurrence.
   */
  public Occur getOccur() {
    return this.occur;
  }

  @Override
  public boolean equals(final Object o) {
    if (o == this) {
      return true;
    }
    if (o == null || !(o instanceof Clause)) {
      return false;
    }
    Clause that = (Clause) o;
    return this.occur == that.occur && this.value.equals(that.value);
  }

  @Override
  public int hashCode() {
    return Objects.hashCode(this.value, this.occur);
  }

  @Override
  public String toString() {
    return this.occur.toString() + this.value.toString();
  }

}
