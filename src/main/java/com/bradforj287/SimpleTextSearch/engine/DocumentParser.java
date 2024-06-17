package com.bradforj287.SimpleTextSearch.engine;

import com.bradforj287.SimpleTextSearch.Document;
import org.apache.commons.lang3.StringUtils;
import org.jsoup.Jsoup;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by brad on 6/7/15.
 */
public class DocumentParser {
    // 用于存储字符串的池
    private ConcurrentHashMap<String, String> stringPool;
    // 是否使用字符串池
    private boolean useStringPool;
    // 是否解析HTML
    private boolean parseHtml;
    // 建立爬取工具的实习
    TextParseUtils parseUtils = new TextParseUtils();

    // 构造函数 
    public DocumentParser(boolean useStringPool, boolean parseHtml) {
        // 初始化是否使用字符串池，是否解析Draw.io
        this.useStringPool = useStringPool;
        this.parseHtml = parseHtml;
        if (useStringPool) {
            stringPool = new ConcurrentHashMap<>();
        }
    }

    public ParsedDocument parseDocument(Document doc) {
        // 将文档的原始文本转换为文档术语列表
        List<DocumentTerm> documentTerms = rawTextToTermList(doc.getRawText());

        ParsedDocument document = new ParsedDocument(documentTerms, doc.getUniqueIdentifier());
        return document;
    }

    /** IMPORTANT: 从文本爬取字段列表*/
    private List<DocumentTerm> rawTextToTermList(String rawText) {
        // 从参数获取文本
        String text = rawText;
        //如果text是空的，也返回空DocTerm列表
        if (StringUtils.isEmpty(text)) {
            return new ArrayList<DocumentTerm>();
        }

        // 如果解析HTML的flag为真，把HTML中的文本提取出来
        if (parseHtml) {
            // strip HTML
            text = Jsoup.parse(text).text();
        }
        //如果html中提取的文本为空
        if (text == null) {
            text = "";
        }

        // 全部小写
        // to lower case
        text = text.toLowerCase();

        // IMPORTANT: 
        // iterate over parsed terms
        List<String> terms = parseUtils.tokenize(text);

        // retVal: 储存要返还的术语列表
        List<DocumentTerm> retVal = new ArrayList<>();
        int pos = 0;
        // 遍历术语
        for (String str : terms) {
            // 词干化，去除格式
            String stemmedTerm = TextParseUtils.stemWord(str);

            // 去除常见词：is, that, which, etc
            // remove stop words
            if (StopWordHelper.isStopWord(stemmedTerm)) {
                continue;
            }

            // 处理过的词：词根化，去常见词化
            String strToUse = stemmedTerm;

            // 如果使用字符串池（LESSON:提高内存使用效率）
            if (useStringPool) {
                // 如果字符串池不包含处理过的词（两化）
                if (!stringPool.containsKey(stemmedTerm)) {
                    // 加入字符串池
                    stringPool.put(stemmedTerm, stemmedTerm);
                }
                // 从哈希表中取得，此方法提高内存使用效率
                strToUse = stringPool.get(stemmedTerm);
            }
            // 加入返还词表，用DocumentTerm类，有词有词在文档中的位置
            retVal.add(new DocumentTerm(strToUse, pos));
            pos++;
        }
        return retVal;
    }
}
