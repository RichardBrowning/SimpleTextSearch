package com.bradforj287.SimpleTextSearch.engine;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

/**
 * Created by brad on 6/7/15.
 * 文档“发布”集
 */
public class DocumentPostingCollection {
    // 词
    private String word;
    // 文档“发布”列表
    private List<DocumentPosting> postings;
    // 被解析后的文档·集
    private HashSet<ParsedDocument> uniqueDocuments;

    public DocumentPostingCollection(String word) {
        this.word = word;
        this.postings = new ArrayList<>();
        this.uniqueDocuments = new HashSet<>();
    }

    // 添加文档“发布”，把词和对应文档添加进Posting Collection
    public void addPosting(DocumentTerm documentTerm, ParsedDocument doc) {
        postings.add(new DocumentPosting(documentTerm, doc));
        uniqueDocuments.add(doc);
    }

    public String getWord() {
        return word;
    }

    public List<DocumentPosting> getPostings() {
        return postings;
    }

    public HashSet<ParsedDocument> getUniqueDocuments() {
        return uniqueDocuments;
    }
}
