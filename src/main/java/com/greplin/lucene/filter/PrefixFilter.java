package com.greplin.lucene.filter;

import com.google.common.base.Objects;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.TermDocs;
import org.apache.lucene.index.TermEnum;
import org.apache.lucene.search.DocIdSet;
import org.apache.lucene.search.Filter;
import org.apache.lucene.util.FixedBitSet;

import java.io.IOException;

/**
 * Filter that matches documents containing a term with the given prefix.
 */
public class PrefixFilter extends Filter {

  /**
   * The field to search.
   */
  private final String field;

  /**
   * The term prefix to match.
   */
  private final String prefix;


  /**
   * Constructs a new prefix filter.
   * @param field the field to search
   * @param prefix the term prefix to match
   */
  public PrefixFilter(final String field, final String prefix) {
    this.field = field.intern();
    this.prefix = prefix;
  }


  @Override
  public DocIdSet getDocIdSet(final IndexReader reader) throws IOException {
    FixedBitSet result = new FixedBitSet(reader.maxDoc());
    Term term = new Term(this.field, this.prefix);
    TermEnum te = reader.terms(term);
    TermDocs td = reader.termDocs();
    try {
      do {
        term = te.term();

        // OK to compare interned strings with !=
        // noinspection StringEquality
        if (term == null
            || this.field != term.field()
            || !term.text().startsWith(this.prefix)) {
          break;
        }

        td.seek(te.term());
        while (td.next()) {
          result.set(td.doc());
        }
      } while (te.next());
    } finally {
      te.close();
      td.close();
    }

    return result;
  }


  @Override
  public boolean equals(final Object other) {
    if (this == other) {
      return true;
    }
    if (other == null || getClass() != other.getClass()) {
      return false;
    }

    PrefixFilter that = (PrefixFilter) other;

    // OK to compare interned strings with ==
    // noinspection StringEquality
    return this.field == that.field && Objects.equal(this.prefix, that.prefix);
  }


  @Override
  public int hashCode() {
    return Objects.hashCode(this.field, this.prefix);
  }


  @Override
  public String toString() {
    return Objects.toStringHelper(this.getClass())
        .add("field", this.field)
        .add("prefix", this.prefix)
        .toString();
  }

}
