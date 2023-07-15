// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   TextParsingOptions.java

package senti.sentistrength.contextProcess;

/**
 * 定义一些文本解析选项，开发人员可以使用这些选项来解析文本.
 */
public class TextParsingOptions {

    /**
     * 表示是否包含标点符号.
     */
    public boolean bgIncludePunctuation;
    /**
     * 表示n-gram的大小.
     */
    public int igNgramSize;
    /**
     * 表示是否使用翻译.
     */
    public boolean bgUseTranslations;
    /**
     * 表示是否添加强调代码.
     */
    public boolean bgAddEmphasisCode;

    /**
     * 无参构造函数.
     */
    public TextParsingOptions() {
        bgIncludePunctuation = true;
        igNgramSize = 1;
        bgUseTranslations = true;
        bgAddEmphasisCode = false;
    }
}
