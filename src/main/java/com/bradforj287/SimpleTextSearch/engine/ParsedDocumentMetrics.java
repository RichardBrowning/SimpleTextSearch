package com.bradforj287.SimpleTextSearch.engine;

import com.google.common.collect.ImmutableMap;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Created by brad on 7/11/15.
 */
public class ParsedDocumentMetrics {
    // 语料类
    private int corpusSize;
    // 解析文档类
    private ParsedDocument parsedDocument;
    // 词到发布的映射
    //private ImmutableMap<String, DocumentPostingCollection> termsToPostings;
    private ImmutableMap<String, Set<ParsedDocument>> termsToParsedDocuments;

    // metrics
    // magnitude这个程度值是对文档中所有词的tfidf值的平方和的根，可理解为对其进行进一步量化
    private Double magnitude;
    // tfidf缓存
    private ImmutableMap<String, Double> tfidfCache;

    // 构造函数
    public ParsedDocumentMetrics(Corpus corpus, ParsedDocument parsedDocument, /**ImmutableMap<String, DocumentPostingCollection> termsToPostings*/ImmutableMap<String, Set<ParsedDocument>> termsToParsedDocuments ) {
        this.corpusSize = corpus.size();
        this.parsedDocument = parsedDocument;
        this.termsToParsedDocuments = termsToParsedDocuments;
        // term frequency metric
        Map<String, Double> tfm = new HashMap<>();

        //init tfidf cache
        // 遍历文档中的唯一词
        for (String word : parsedDocument.getUniqueWords()) {
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

    // 获取程度值
    public double getMagnitude() {
        // 如果程度值为空
        if (magnitude == null) {
            //存储平方和
            double sumOfSquares = 0;
            //遍历文档中的唯一词
            for (String word : parsedDocument.getUniqueWords()) {
                //获取次的词频-逆文档频率值
                double d = getTfidf(word);
                //平方和
                sumOfSquares += d * d;
            }
            //平方和的根是这个程度值
            magnitude = Math.sqrt(sumOfSquares);
        }

        return magnitude;
    }

    public ParsedDocument getParsedDocument() {
        return this.parsedDocument;
    }

    // 计算tfidf
    private double calcTfidf(String word) {
        int wordFreq = parsedDocument.getWordFrequency(word);
        if (wordFreq == 0) {
            return 0;
        }
        // LESSON: TF-IDF = 词频 * 逆文档频率
        return getInverseDocumentFrequency(word) * parsedDocument.getWordFrequency(word);
    }

    // 计算逆文档频率
    private double getInverseDocumentFrequency(String word) {
        // 总文档数（语料库里面的parsed Doc数量）
        double totalNumDocuments = this.corpusSize;
        // 通过词，逆向获取文档数
        double numDocsWithTerm = numDocumentsTermIsIn(word);
        // 逆文档频率？？？
        return Math.log((totalNumDocuments) / (1 + numDocsWithTerm));
    }

    private int numDocumentsTermIsIn(String term) {
        // 如果词到发布的映射中不包含该词，返回0
        if (!this.termsToParsedDocuments.containsKey(term)) {
            return 0;
        }
        //找词->找对应的文档“发布”->获取大小
        return termsToParsedDocuments.get(term).size();
    }

}
