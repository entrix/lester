package com.volkoval.lucene.fuzziness;

import org.apache.log4j.Logger;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;


/**
 * Created with IntelliJ IDEA.
 * User: Entrix
 * Date: 10/19/13
 * Time: 2:27 PM
 */
public class Indexer {

    private static enum Switcher {
        NEUTRAL, TITLE, BODY
    }

    private static Logger logger = Logger.getLogger(Indexer.class);
    private static IndexWriter indexWriter;


    private static void init(File filePath) throws IOException {
        indexWriter = new IndexWriter(FSDirectory.open(filePath),
                new IndexWriterConfig(Version.LUCENE_45, new StandardAnalyzer(Version.LUCENE_45)));
    }

    private static void close() throws IOException {
        indexWriter.close();
    }

    private static void indexFile(File file) {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            Document doc = new Document();;
            String line;
            StringBuffer buffer = new StringBuffer();
            Switcher switcher = Switcher.NEUTRAL;

            while ((line = reader.readLine()) != null) {
                if (line.startsWith("title:")) {
                    switcher = Switcher.TITLE;
                }
                else if (line.startsWith("body:")) {
                    switcher = Switcher.BODY;
                }
                else if (line.startsWith("---")) {
                    switch (switcher) {
                        case TITLE:
                            doc.add(new StringField("title", buffer.toString(), Field.Store.YES));
                            break;
                        case BODY:
                            doc.add(new TextField("body", buffer.toString(), Field.Store.YES));
                            indexWriter.addDocument(doc);
                            doc = new Document();
                            break;
                    }
                    switcher = Switcher.NEUTRAL;
                    buffer = new StringBuffer();
                }
                else {
                    buffer.append(line);
                }
            }
            indexWriter.commit();
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    public static void main(String[] args) {

        try {
            logger.info("start indexing");
            init(new File("lucene_index"));

            File[] filesToIndex = new File("files_to_index").listFiles();
            if (filesToIndex != null) {
                for (final File fileEntry : filesToIndex) {
                    if (!fileEntry.isDirectory()) {
                        indexFile(fileEntry);
                    }
                }
            }
            else {
                logger.warn("Couldn't open files to index");
            }

            close();
        } catch (IOException e) {
            logger.error("Couldn't open index files", e);
        }
        logger.info("indexing completed successfully");
    }
}
