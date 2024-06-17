package com.bradforj287.SimpleTextSearch.engine;

import com.google.common.base.Preconditions;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

/**
 * Created by brad on 6/6/15.
 */
public class DocumentTerm {
    // 词
    private String word;
    // 词在文档中的位置
    private int positionInDoc;

    public DocumentTerm(String word, int positionInDoc) {
        //LESSON: 检查参数是否有问题，checkArgument方法的第一个参数是boolean表达式，如果为false，则抛出IllegalArgumentException异常
        Preconditions.checkArgument(!StringUtils.isEmpty(word));
        this.word = word;
        this.positionInDoc = positionInDoc;
    }

    public String getWord() {
        return word;
    }

    public int getPositionInDoc() {
        return positionInDoc;
    }

    @Override
    // LESSON: 如何入一个对象，经过mapping转换为本对象，然后再进行比较
    public boolean equals(Object o) {
        // 本身相等
        if (this == o) return true;
        // null或者不是同一个类
        if (o == null || getClass() != o.getClass()) return false;
        //转换
        DocumentTerm documentTerm = (DocumentTerm) o;

        return new EqualsBuilder()
                .append(positionInDoc, documentTerm.positionInDoc)
                .append(word, documentTerm.word)
                .isEquals();
    }

    @Override
    // 生成hashCode
    public int hashCode() {
        // initialOddNumber，multiplierOddNumber
        return new HashCodeBuilder(17, 37)
                .append(word)
                .append(positionInDoc)
                .toHashCode();
    }
}
