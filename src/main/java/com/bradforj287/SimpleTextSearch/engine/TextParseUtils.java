package com.bradforj287.SimpleTextSearch.engine;

import java.io.IOException;
import org.apache.commons.lang3.StringUtils;
import org.tartarus.snowball.ext.EnglishStemmer;

import java.util.ArrayList;
import java.util.List;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;

/**
 * Created by brad on 6/6/15.
 * IMPORTANT: 从文本爬取字段列表，对于中文无效，ACTION_REQUIRED: 需要修改为ansj中的ToAnalysis
 */
public class TextParseUtils {
    Analyzer analyzer = new StandardAnalyzer();

    TextParseUtils() {
    }

    // 入一个词，返还分离的词根/基础形式
    public static String stemWord(String word) {
        EnglishStemmer stemmer = new EnglishStemmer();
        stemmer.setCurrent(word);
        stemmer.stem();
        return stemmer.getCurrent();
    }

    public List<String> tokenize(String rawText) {

        List<String> retVal = new ArrayList<>();
        if (StringUtils.isEmpty(rawText)) {
            return retVal;
        }

        try (TokenStream ts = analyzer.tokenStream(null,rawText)) {
            CharTermAttribute term = ts.getAttribute(CharTermAttribute.class);
            ts.reset();
            while (ts.incrementToken()) {
                String str = term.toString();
                if (str == null) {
                    continue;
                }

                str = str.replaceAll("[^a-zA-Z ]", "");

                if (str.isEmpty()) {
                    continue;
                }

                retVal.add(str);
            }
            ts.end();
        }
        catch (IOException ex) {}


        return retVal;
    }

}
