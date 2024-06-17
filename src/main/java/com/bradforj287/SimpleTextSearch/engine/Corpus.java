package com.bradforj287.SimpleTextSearch.engine;

import com.google.common.base.Preconditions;

import java.util.List;

/**
 * Created by brad on 7/11/15.
 * 语料库
 */
public class Corpus {
    // 爬取过的文档列表
    private List<ParsedDocument> parsedDocuments;

    // 构造函数
    public Corpus(List<ParsedDocument> documents) {
        // 确保文档列表不为空
        Preconditions.checkNotNull(documents);
        // 确保文档列表不为空
        Preconditions.checkState(!documents.isEmpty());
        // 赋值
        this.parsedDocuments = documents;
    }

    // 获取文档列表
    public List<ParsedDocument> getParsedDocuments() {
        return this.parsedDocuments;
    }

    // 获取文档数量
    public int size() {
        return parsedDocuments.size();
    }

}
