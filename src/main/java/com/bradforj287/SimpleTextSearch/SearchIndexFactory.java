package com.bradforj287.SimpleTextSearch;

import com.bradforj287.SimpleTextSearch.engine.Corpus;
import com.bradforj287.SimpleTextSearch.engine.DocumentParser;
import com.bradforj287.SimpleTextSearch.engine.InvertedIndex;
import com.bradforj287.SimpleTextSearch.engine.ParsedDocument;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by brad on 6/6/15.
 */
public class SearchIndexFactory {

    private SearchIndexFactory() {

    }
    // 构建解析后的文档列表
    private static List<ParsedDocument> buildParsedDocuments(Collection<Document> docs, DocumentParser parser) {
        // 建立空列表
        List<ParsedDocument> retVal = new ArrayList<>();
        // 遍历文档
        for (Document doc : docs) {
            // 解析文档，加入到列表中
            retVal.add(parser.parseDocument(doc));
        }
        // 返回解析后的文档列表
        return retVal;
    }

    // 并行构建解析后的文档列表 IMPORTANT: LESSON: 并行处理
    private static Collection<ParsedDocument> buildParsedDocumentsParrallel(Collection<Document> theDocs) {
        // 获取处理器核心数
        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        // 构建多线程链列，容纳已解析文档
        final Collection<ParsedDocument> parsedDocuments = new ConcurrentLinkedQueue<>();
        // 文档列表
        List<Document> docsList = new ArrayList<>(theDocs);
        // 线程表
        List<Thread> threads = new ArrayList<>();

        // 文档解析器
        final DocumentParser parser = new DocumentParser(true, true);
        // LESSON: List.partition() 把docList按照线程数分成组，对每一个分区单独操作
        for (final List<Document> partition : Lists.partition(docsList, cores)) {
            // 新建线程，建立 parsedDocuments ，推进 parsedDocument 
            Thread t = new Thread(new Runnable() {
                @Override
                public void run() {
                    parsedDocuments.addAll(buildParsedDocuments(partition, parser));
                }
            });
            // 线程，添加！
            threads.add(t);
            // 启动！
            t.start();
        }

        // 线程加入
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        return parsedDocuments;
    }

    // 构建索引
    public static TextSearchIndex buildIndex(Collection<Document> documents) {
        // 并行进行文档解析
        Collection<ParsedDocument> parsedDocuments = buildParsedDocumentsParrallel(documents);
        // 从解析的文档建立语料库
        Corpus corpus = new Corpus(new ArrayList<>(parsedDocuments));
        // 倒序检索
        InvertedIndex invertedIndex = new InvertedIndex(corpus);

        return invertedIndex;
    }
}
