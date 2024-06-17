package com.bradforj287.SimpleTextSearch.engine;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by brad on 7/11/15.
 */
public class ParsedDocumentMetrics {
    // 语料类
    private Corpus corpus;
    // 解析文档类
    private ParsedDocument document;
    // 词到发布的映射
    private ImmutableMap<String, DocumentPostingCollection> termsToPostings;

    //metrics
    // magnitude？
    private Double magnitude;
    // tfidf缓存
    private ImmutableMap<String, Double> tfidfCache;

    // 构造函数
    public ParsedDocumentMetrics(Corpus corpus, ParsedDocument document, ImmutableMap<String, DocumentPostingCollection> termsToPostings) {
        this.corpus = corpus;
        this.document = document;
        this.termsToPostings = termsToPostings;
        // term frequency metric
        Map<String, Double> tfm = new HashMap<>();

        //init tfidf cache
        // 遍历文档中的唯一词
        for (String word : document.getUniqueWords()) {
            // 将词和对应的tfidf值添加到tfm中
            tfm.put(word, calcTfidf(word));
        }
        this.tfidfCache = ImmutableMap.copyOf(tfm);

        //prime magnitude cache
        getMagnitude();
    }

    public double getTfidf(String word) {
        Double retVal = tfidfCache.get(word);
        if (retVal == null) {
            return 0;
        }

        return retVal;
    }

    public double getMagnitude() {
        if (magnitude == null) {
            double sumOfSquares = 0;
            for (String word : document.getUniqueWords()) {
                double d = getTfidf(word);
                sumOfSquares += d * d;
            }

            magnitude = Math.sqrt(sumOfSquares);
        }

        return magnitude;
    }

    public ParsedDocument getDocument() {
        return this.document;
    }

    // 计算tfidf
    private double calcTfidf(String word) {
        int wordFreq = document.getWordFrequency(word);
        if (wordFreq == 0) {
            return 0;
        }
        // TF-IDF = 词频 * 逆文档频率
        return getInverseDocumentFrequency(word) * document.getWordFrequency(word);
    }

    // 计算逆文档频率
    private double getInverseDocumentFrequency(String word) {
        // 总文档数（语料库里面的parsed Doc数量）
        double totalNumDocuments = corpus.size();
        // 通过词，逆向获取文档数
        double numDocsWithTerm = numDocumentsTermIsIn(word);
        // 逆文档频率？？？
        return Math.log((totalNumDocuments) / (1 + numDocsWithTerm));
    }

    private int numDocumentsTermIsIn(String term) {
        // 如果词到发布的映射中不包含该词，返回0
        if (!termsToPostings.containsKey(term)) {
            return 0;
        }
        //找词->找对应的文档“发布”->获取大小
        return termsToPostings.get(term).getUniqueDocuments().size();
    }

}
