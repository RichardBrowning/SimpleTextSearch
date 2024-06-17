package com.bradforj287.SimpleTextSearch.engine;

/**
 * Created by brad on 6/7/15.
 * 文档“发布”
 */
public class DocumentPosting {

    // 文档术语类，property包括词和词在文档中的位置
    private DocumentTerm documentTerm;
    // 文档类，property包括文档术语列表和文档的唯一标识符
    private ParsedDocument parsedDocument;
    public DocumentPosting(DocumentTerm documentTerm, ParsedDocument document) {
        this.documentTerm = documentTerm;
        this.parsedDocument = document;
    }

    public DocumentTerm getDocumentTerm() {
        return documentTerm;
    }

    public ParsedDocument getParsedDocument() {
        return parsedDocument;
    }

}
