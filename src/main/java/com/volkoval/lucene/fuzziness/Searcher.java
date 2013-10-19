package com.volkoval.lucene.fuzziness;

import org.apache.log4j.Logger;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static java.lang.System.out;

/**
 * Created with IntelliJ IDEA.
 * User: Entrix
 * Date: 10/19/13
 * Time: 2:51 PM
 */
public class Searcher {

    private static Logger logger = Logger.getLogger(Searcher.class);
    private static IndexReader indexReader;
    private static IndexSearcher searcher;


    private static void init() throws IOException {
        indexReader = DirectoryReader.open(FSDirectory.open(new File("lucene_index")));
        searcher    = new IndexSearcher(indexReader);
    }

    private static void close() throws IOException {
        indexReader.close();
    }

    private static List<Document> search(Query query) {
        List<Document> docs = new ArrayList<>();

        try {
            TopDocs topDocs = searcher.search(query, 10);

            if (topDocs.scoreDocs.length > 0) {
                for (ScoreDoc scoredoc: topDocs.scoreDocs) {
                    docs.add(indexReader.document(scoredoc.doc));
                }
            }
        } catch (IOException e) {
            logger.error(e);
        }

        return docs;
    }

    private static void printResults(Query searchQuery, List<Document> results) {
        int num = 0;

        out.println("search query: " + searchQuery);
        for (Document document : results) {
            out.println("title: " + document.getField("title"));
            num++;
        }
        out.println(num + " results");
        out.println();
    }

    public static void main(String[] args) {
        Query searchQuery;

        try {
            init();

            // search by full match: at the index phase we were using StringAnalyzer
            // and were indexing whole string (none tokenization)
            searchQuery = new TermQuery(new Term("title", "Killing Jesus"));
            printResults(searchQuery, search(searchQuery));

            // search by full match: none results
            searchQuery = new TermQuery(new Term("title", "Killing"));
            printResults(searchQuery, search(searchQuery));

            // search by fuzzy match: one missing character
            // compensated by fuzzy search
            searchQuery = new FuzzyQuery(new Term("title", "Kiling Jess"));
            printResults(searchQuery, search(searchQuery));

            close();
        } catch (IOException e) {
            logger.error("Couldn't open lucene index:", e);
        }
    }
}
