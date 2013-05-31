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

import com.google.common.base.Joiner;
import com.greplin.lucene.query.BooleanQueryBuilder;
import com.greplin.lucene.util.FilterIntersectionProvider;
import org.apache.lucene.analysis.WhitespaceAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FilteredQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MatchAllDocsQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.Version;

import java.io.IOException;
import java.util.Random;

/**
 * Random benchmark for the phrase filter.
 */
public class PhraseFilterBenchmark {

  private static final Random RANDOM = new Random();

  private static final int TOTAL_DOCS = 10000;

  private static final int AVERAGE_WORDS_PER_DOC = 100;

  private static final int WORDS_PER_DOC_DEVIATION = 95;

  private static final int SECOND_FIELD_MATCH_PERCENTAGE = 50;


  private static final int ROUNDS = 4;

  private static final int TOTAL_QUERIES = 5000;

  private static final int[] WORDS_PER_QUERY = {2, 2, 2, 2, 3, 3, 4, 5};


  private static final String FIELD = "f";


  private static final String[] WORDS = (
      "the quick brown fox jumps over the lazy dog"
      + " a stitch in time saves nine"
      + " an apple a day keeps the doctor away"
      + " two wrongs do not make a right"
      + " the pen is mightier than the sword"
      + " the squeaky wheel gets the grease"
      + " no man is an island"
      + " fortune favors the bold"
      + " people who live in glass houses should not throw stones"
      + " better late than never"
      + " hope for the best but prepare for the worst"
      + " birds of a feather flock together"
      + " keep your friends close and your enemies closer"
      + " a picture is worth a thousand words"
      + " there is no such thing as a free lunch"
      + " there is no place like home"
      + " discretion is the greater part of valor"
      + " the early bird catches the worm"
      + " never look a gift horse in the mouth"
      + " you cannot make an omelet without breaking a few eggs"
      + " you cannot always get what you want"
      + " cleanliness is next to godliness"
      + " a watched pot never boils"
      + " beggars cannot be choosers"
      + " actions speak louder than words"
      + " if it is not broke, do not fix it"
      + " practice makes perfect"
      + " too many cooks spoil the broth"
      + " easy come easy go"
      + " do not bite the hand that feeds you"
      + " all good things must come to an end"
      + " if you cannot beat them, join them"
      + " there is no time like the present"
      + " beauty is in the eye of the beholder"
      + " necessity is the mother of invention"
      + " a penny saved is a penny earned"
      + " familiarity breeds contempt"
      + " you cannot judge a book by its cover"
      + " good things come to those who wait"
      + " do not put all your eggs in one basket"
      + " two heads are better than one"
      + " the grass is always greener on the other side of the hill"
      + " do unto others as you would have them do unto you"
      + " a chain is only as strong as its weakest link"
      + " honesty is the best policy"
      + " absence makes the heart grow fonder"
      + " you can lead a horse to water but you cannot make him drink"
      + " do not count your chickens before they hatch"
      + " if you want something done right you have to do it yourself"
  ).split(" ");

  private static final int NUMBER_OF_SEGMENTS = 4;

  private static String[] words(int count) {
    String[] words = new String[count];
    for (int w = 0; w < count; w++) {
      words[w] = WORDS[RANDOM.nextInt(WORDS.length)];
    }
    return words;
  }

  public static void main(String[] argv) {
    Directory directory = new RAMDirectory();
    try {
      IndexWriter writer = new IndexWriter(
          directory, new IndexWriterConfig(Version.LUCENE_32, new WhitespaceAnalyzer(Version.LUCENE_32)));
      int done = 0;
      for (int i = 0; i < NUMBER_OF_SEGMENTS; i++) {
        int remaining = NUMBER_OF_SEGMENTS - i;
        int numberOfDocs;
        if (remaining == 1) {
          numberOfDocs = TOTAL_DOCS - done;
        } else {
          numberOfDocs = RANDOM.nextInt(TOTAL_DOCS - done - remaining) + 1;
        }
        done += numberOfDocs;
        System.out.println("Segment #" + i + " has " + numberOfDocs + " docs");

        for (int d = 0; d < numberOfDocs; d++) {
          int wordCount = RANDOM.nextInt(WORDS_PER_DOC_DEVIATION * 2) + AVERAGE_WORDS_PER_DOC - WORDS_PER_DOC_DEVIATION;
          Document doc = new Document();
          doc.add(new Field("f", Joiner.on(' ').join(words(wordCount)), Field.Store.YES, Field.Index.ANALYZED));
          doc.add(new Field("second", RANDOM.nextInt(100) < SECOND_FIELD_MATCH_PERCENTAGE ? "yes" : "no",
              Field.Store.NO, Field.Index.ANALYZED));
          writer.addDocument(doc);
        }
        writer.commit();
      }
      writer.close();

      IndexReader reader = IndexReader.open(directory);
      IndexSearcher searcher = new IndexSearcher(reader);

      String[][] queries = new String[TOTAL_QUERIES][];
      Term[][] terms = new Term[TOTAL_QUERIES][];

      for (int q = 0; q < TOTAL_QUERIES; q++) {
        queries[q] = words(WORDS_PER_QUERY[RANDOM.nextInt(WORDS_PER_QUERY.length)]);
        terms[q] = new Term[queries[q].length];
        for (int qw = 0; qw < queries[q].length; qw++) {
          terms[q][qw] = new Term(FIELD, queries[q][qw]);
        }
      }

      // Warm up.
      new PhraseFilter(FIELD, queries[0]).getDocIdSet(reader);

      for (int round = 0; round < ROUNDS; round++) {
        System.out.println();
        String name1 = "filter";
        String name2 = "query";

        long ms1 = 0, ms2 = 0;
        for (int step = 0; step < 2; step++) {
          System.gc();
          System.gc();
          System.gc();

          if (step == (round & 1)) {
            long millis = System.currentTimeMillis();
            long hits = 0;
            for (String[] queryWords : queries) {
              PhraseFilter pf = new PhraseFilter(
                  new FilterIntersectionProvider(TermsFilter.from(new Term("second", "yes"))), FIELD, queryWords);
              hits += searcher.search(new FilteredQuery(new MatchAllDocsQuery(), pf), 1).totalHits;
            }
            ms1 = System.currentTimeMillis() - millis;
            System.out.println("Finished " + name1 + " in " + ms1 + "ms with " + hits + " hits");
          } else {
            long millis = System.currentTimeMillis();
            long hits = 0;
            for (Term[] queryTerms : terms) {
              PhraseQuery pq = new PhraseQuery();
              for (Term term : queryTerms) {
                pq.add(term);
              }
              Query query = BooleanQueryBuilder.builder()
                  .must(new TermQuery(new Term("second", "yes")))
                  .must(pq)
                  .build();
              hits += searcher.search(query, 1).totalHits;
            }
            ms2 = System.currentTimeMillis() - millis;
            System.out.println("Finished " + name2 + " in " + ms2 + "ms with " + hits + " hits");
          }
        }
        System.out.println(name1 + " took " + (int) ((100.0 * ms1) / ms2) + "% as much time as " + name2);
      }

    } catch (IOException e) {
      e.printStackTrace();
    }
  }


}
