package com.bradforj287.SimpleTextSearch.engine;

import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by brad on 6/6/15.
 * 解析后的文档
 */
public class ParsedDocument {
    // 文档中的词
    private ImmutableList<DocumentTerm> documentTerms;
    // 词频（不可修改）
    private ImmutableMap<String, Integer> wordFrequencyMap;
    // 唯一词（不可修改）
    private ImmutableSet<String> uniqueWords;
    // 唯一标识(文档的)
    private Object uniqueId;

    public ParsedDocument(List<DocumentTerm> documentTerms, Object uniqueId) {
        // 确保文档中的词和唯一标识不为空
        Preconditions.checkNotNull(uniqueId);
        // 文档中的子也不可以空
        Preconditions.checkNotNull(documentTerms);
        // 复制文档词和唯一标识
        this.documentTerms = ImmutableList.copyOf(documentTerms);
        this.uniqueId = uniqueId;
        // 初始化词频
        HashMap<String, Integer> wordFrequency = new HashMap<>();
        uniqueWords = null;

        // 计算词频
        // 遍历文档词
        for (DocumentTerm t : documentTerms) {
            // 获取文档词的词本体
            String word = t.getWord();
            // 如果词频中没有这个词，就添加进去
            if (!wordFrequency.containsKey(word)) {
                wordFrequency.put(word, 0);
            }
            
            // 对应的词词频+1
            int count = wordFrequency.get(word);
            // 更新词频
            wordFrequency.put(word, count + 1);
        }
        // 从得出的词频，建立不可修改的副本
        wordFrequencyMap = ImmutableMap.copyOf(wordFrequency);
        // 从文档词中获取唯一词的哈希集合，建立不可修改的副本
        uniqueWords = ImmutableSet.copyOf(getUniqueWordsHashSet());
    }

    /**
     * 从词频表中获取某词词频
     * @param word
     * @return
     */
    public int getWordFrequency(String word) {
        if (!wordFrequencyMap.containsKey(word)) {
            return 0;
        }

        return wordFrequencyMap.get(word);
    }

    public boolean isEmpty() {
        return documentTerms == null || documentTerms.isEmpty();
    }

    public List<DocumentTerm> getDocumentTerms() {
        return documentTerms;
    }

    public Set<String> getUniqueWords() {
        return uniqueWords;
    }

    /**
     * 从文档词中获取唯一词的哈希集合
     * @return
     */
    private HashSet<String> getUniqueWordsHashSet() {
        // 用HashSet存储唯一词
        HashSet<String> w = new HashSet<>();
        // 遍历文档词
        for (DocumentTerm t : documentTerms) {
            // 添加词
            w.add(t.getWord());
        }
        return w;
    }

    public Object getUniqueId() {
        return uniqueId;
    }
}
