package com.bradforj287.SimpleTextSearch;

/**
 * Created by brad on 6/6/15.
 * 文档类
 */
public class Document {
    // Property：原始文本
    private String rawText;
    // Property：唯一标识符
    private Object uniqueIdentifier;

    /**
     *
     * @param rawText - the raw text for this document.
     * @param uniqueIdentifier - (optional) a unique ID for this document. Used in search results to refer back
     *                         to original data
     */
    public Document(String rawText, Object uniqueIdentifier) {
        this.rawText = rawText;
        this.uniqueIdentifier = uniqueIdentifier;
    }

    public String getRawText() {
        return rawText;
    }

    public Object getUniqueIdentifier() {
        return uniqueIdentifier;
    }
}
