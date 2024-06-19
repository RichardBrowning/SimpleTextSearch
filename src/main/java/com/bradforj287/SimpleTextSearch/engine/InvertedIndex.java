package com.bradforj287.SimpleTextSearch.engine;

import com.bradforj287.SimpleTextSearch.*;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.MinMaxPriorityQueue;

import java.util.*;
import java.util.concurrent.*;

/**
 * Created by brad on 6/6/15.
 */
public class InvertedIndex implements TextSearchIndex {
    // 线程池大小
    private static int THREAD_POOL_SIZE = Math.max(1, Runtime.getRuntime().availableProcessors());
    // 语料库
    private Corpus corpus;
    // 词 -> 文档列表
    private ImmutableMap<String, DocumentPostingCollection> termToPostings;
    // 文档 -> 文档统计
    private ImmutableMap<ParsedDocument, ParsedDocumentMetrics> docToMetrics;
    // 
    private ExecutorService executorService;
    private DocumentParser searchTermParser;

    public InvertedIndex(Corpus corpus) {
        // 语料库
        this.corpus = corpus;
        // 初始化
        init();
        // 初始化固定线程池（线程数=处理器逻辑核心数）
        executorService = Executors.newFixedThreadPool(THREAD_POOL_SIZE);
        // 初始化文档解析器
        searchTermParser = new DocumentParser(false, false);
    }

    private void init() {
        // build term -> posting map
        Map<String, DocumentPostingCollection> termToPostingsMap = new HashMap<>();
        // 遍历语料库中的文档
        for (ParsedDocument document : corpus.getParsedDocuments()) {
            // 遍历文档中的词
            for (DocumentTerm documentTerm : document.getDocumentTerms()) {
                // 从documentTerm中获取词
                final String word = documentTerm.getWord();
                // 如果termToPostingsMap中不包含该词，则添加
                if (!termToPostingsMap.containsKey(word)) {
                    termToPostingsMap.put(word, new DocumentPostingCollection(word));
                }
                termToPostingsMap.get(word).addPosting(documentTerm, document);
            }
        }
        //词对应的文档列表，词对文档的映射
        termToPostings = ImmutableMap.copyOf(termToPostingsMap);

        //init metrics cache
        Map<ParsedDocument, ParsedDocumentMetrics> metricsMap = new HashMap<>();
        for (ParsedDocument document : corpus.getParsedDocuments()) {
            // 为每个文档建立文档统计
            metricsMap.put(document, new ParsedDocumentMetrics(corpus, document, termToPostings));
        }
        docToMetrics = ImmutableMap.copyOf(metricsMap);
    }

    public int numDocuments() {
        return corpus.size();
    }

    public int termCount() {
        return termToPostings.keySet().size();
    }

    private Set<ParsedDocument> getRelevantDocuments(ParsedDocument searchDoc) {
        // 用于存储相关文档
        Set<ParsedDocument> retVal = new HashSet<>();
        // 遍历搜索文档中的词
        for (String word : searchDoc.getUniqueWords()) {
            // 如果词对应的文档列表中包含该词
            if (termToPostings.containsKey(word)) {
                // 添加文档列表中的文档
                retVal.addAll(termToPostings.get(word).getUniqueDocuments());
            }
        }

        return retVal;
    }

    @Override
    public SearchResultBatch search(String searchTerm, int maxResults) {
        //计时开始
        Stopwatch stopwatch = Stopwatch.createStarted();
        //用DocumentParser来解析搜索关键词文档
        ParsedDocument searchDocument = searchTermParser.parseDocument(new Document(searchTerm, new Object()));
        //获取相关文档
        Set<ParsedDocument> documentsToScanSet = getRelevantDocuments(searchDocument);
        //如果是空的，返还空结果和计时
        if (searchDocument.isEmpty() || documentsToScanSet.isEmpty()) {
            return buildResultBatch(new ArrayList<SearchResult>(), stopwatch, 0);
        }

        // do scan
        final Collection<SearchResult> resultsP = new ConcurrentLinkedQueue<>();
        // 将相关文档转换为列表
        List<ParsedDocument> documentsToScan = new ArrayList<>(documentsToScanSet);
        // 用于存储搜索文档的统计
        final ParsedDocumentMetrics pdm = new ParsedDocumentMetrics(corpus, searchDocument, termToPostings);
        // 用于存储Future,Future是concurrent类中用于储存异步运行结果的类
        List<Future> futures = new ArrayList<>();
        // 根据线程数和文档数分工
        for (final List<ParsedDocument> partition : Lists.partition(documentsToScan, THREAD_POOL_SIZE)) {
            // 提交任务？
            Future future = executorService.submit(new Runnable() {
                @Override
                public void run() {
                    for (ParsedDocument doc : partition) {
                        double cosine = computeCosine(pdm, doc);

                        SearchResult result = new SearchResult();
                        result.setRelevanceScore(cosine);
                        result.setUniqueIdentifier(doc.getUniqueId());
                        resultsP.add(result);
                    }
                }
            });

            futures.add(future);
        }

        for (Future f : futures) {
            try {
                f.get();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }
        }

        // 选择最大的maxResults个结果
        int heapSize = Math.min(resultsP.size(), maxResults);

        // 用于存储最大堆
        MinMaxPriorityQueue<SearchResult> maxHeap = MinMaxPriorityQueue.
                orderedBy(new Comparator<SearchResult>() {
                    @Override
                    public int compare(SearchResult o1, SearchResult o2) {
                        if (o1.getRelevanceScore() <= o2.getRelevanceScore()) {
                            return 1;
                        } else {
                            return -1;
                        }
                    }
                }).
                maximumSize(heapSize).
                expectedSize(heapSize).create(resultsP);


        // return results
        ArrayList<SearchResult> r = new ArrayList<>();
        while (!maxHeap.isEmpty()) {
            SearchResult rs = maxHeap.removeFirst();
            r.add(rs);
        }

        return buildResultBatch(r, stopwatch, documentsToScan.size());
    }

    private SearchResultBatch buildResultBatch(List<SearchResult> results, Stopwatch sw, int numDocumentsSearched) {
        SearchResultBatch retVal = new SearchResultBatch();

        SearchStats stats = new SearchStats();
        stats.setDocumentsSearched(numDocumentsSearched);
        stats.setDurationNanos(sw.elapsed(TimeUnit.NANOSECONDS));

        retVal.setSearchResults(results);
        retVal.setStats(stats);

        return retVal;
    }

    //计算余弦相似度
    private double computeCosine(ParsedDocumentMetrics searchDocMetrics, ParsedDocument d2) {
        double cosine = 0;

        Set<String> wordSet = searchDocMetrics.getDocument().getUniqueWords();
        ParsedDocument otherDocument = d2;
        if (d2.getUniqueWords().size() < wordSet.size()) {
            wordSet = d2.getUniqueWords();
            otherDocument = searchDocMetrics.getDocument();
        }
        for (String word : wordSet) {

            double term = ((searchDocMetrics.getTfidf(word)) / searchDocMetrics.getMagnitude()) *
                    ( (docToMetrics.get(d2).getTfidf(word)) / docToMetrics.get(d2).getMagnitude());
            cosine = cosine + term;
        }
        return cosine;
    }


    public Map<String, DocumentPostingCollection> getTermToPostings() {
        return termToPostings;
    }
}
