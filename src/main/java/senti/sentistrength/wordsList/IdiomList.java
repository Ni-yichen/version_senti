// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   IdiomList.java

package senti.sentistrength.wordsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.sentistrength.classification.ClassificationOptions;
import senti.utilities.FileOps;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            ClassificationOptions

public class IdiomList extends WordsListExtend {
    private String[] sgIdioms;
    private int[] igIdiomStrength;
    private int igIdiomCount;
    private String[][] sgIdiomWords;
    private int[] igIdiomWordCount;
    private static final int IdiomStrengthNotFound = 999;
    private static final int IdiomWordsMaxWord = 10;
    private static final int WordListMaxLength = 9;
    private static final Logger LOG = LoggerFactory.getLogger(IdiomList.class);
    /**
     * IdiomList的无参构造函数.
     */
    public IdiomList() {
        igIdiomCount = 0;
    }

    /**
     * IdiomList的含参初始化函数.
     * @param sFilename 包含初始化所需数据的源文件文件名
     * @param options 读取源文件的可选方式，是否使用UTF8编码
     * @param iExtraBlankArrayEntriesToInclude 额外添加的空行数
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(
            final String sFilename, final ClassificationOptions options,
            final int iExtraBlankArrayEntriesToInclude) {
        int iLinesInFile;
        int iIdiomStrength;
        if (sFilename.equals("")) {
            return false;
        }
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find idiom list file: {}", sFilename);
            return false;
        }
        iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
        sgIdioms = new String[iLinesInFile + 2 + iExtraBlankArrayEntriesToInclude];
        igIdiomStrength = new int[iLinesInFile + 2 + iExtraBlankArrayEntriesToInclude];
        igIdiomCount = 0;
        try {
            BufferedReader rReader;
            if (options.bgForceUTF8) {
                rReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(sFilename), StandardCharsets.UTF_8));
            } else {
                rReader = new BufferedReader(new FileReader(sFilename));
            }
            String sLine;
            while ((sLine = rReader.readLine()) != null) {
                if (!sLine.equals("")) {
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if (iFirstTabLocation >= 0) {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        try {
                            if (iSecondTabLocation > 0) {
                                iIdiomStrength = Integer.parseInt(sLine.substring(
                                        iFirstTabLocation + 1, iSecondTabLocation).trim());
                            } else {
                                iIdiomStrength = Integer.parseInt(
                                        sLine.substring(iFirstTabLocation + 1).trim());
                            }
                            if (iIdiomStrength > 0) {
                                iIdiomStrength--;
                            } else if (iIdiomStrength < 0) {
                                iIdiomStrength++;
                            }
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for idiom! Ignoring idiom");
                            LOG.error("Line: {}", sLine);
                            iIdiomStrength = 0;
                        }
                        sLine = sLine.substring(0, iFirstTabLocation);
                        if (sLine.contains(" ")) {
                            sLine = sLine.trim();
                        }
                        if (sLine.indexOf("  ") > 0) {
                            sLine = sLine.replace("  ", " ");
                        }
                        if (sLine.indexOf("  ") > 0) {
                            sLine = sLine.replace("  ", " ");
                        }
                        if (!sLine.equals("")) {
                            igIdiomCount++;
                            sgIdioms[igIdiomCount] = sLine;
                            igIdiomStrength[igIdiomCount] = iIdiomStrength;
                        }
                    }
                }
            }
            rReader.close();
        } catch (FileNotFoundException e) {
            LOG.error("Could not find idiom list file: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found idiom list file but could not read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        convertIdiomStringsToWordLists();
        return true;
    }

    /**
     * 添加额外idiom的函数.
     * @param sIdiom 待添加的idiom
     * @param iIdiomStrength 该idiom的强度
     * @param bConvertIdiomStringsToWordListsAfterAddingIdiom 是否在添加完新的idiom后，将sgIdioms拆分且更新到sgIdiomWords中
     * @return 添加是否成功
     */
    @Override
    public boolean addExtraIdiom(
            final String sIdiom, int iIdiomStrength,
            final boolean bConvertIdiomStringsToWordListsAfterAddingIdiom) {
        try {
            igIdiomCount++;
            sgIdioms[igIdiomCount] = sIdiom;
            if (iIdiomStrength > 0) {
                iIdiomStrength--;
            } else if (iIdiomStrength < 0) {
                iIdiomStrength++;
            }
            igIdiomStrength[igIdiomCount] = iIdiomStrength;
            if (bConvertIdiomStringsToWordListsAfterAddingIdiom) {
                convertIdiomStringsToWordLists();
            }
        } catch (Exception e) {
            LOG.error("Could not add extra idiom: {}", sIdiom);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 将sgIdioms内的习语字符串按单词拆分，并且按顺序存入sgIdiomWords中对应的字符串数组(限制：习语的单词数不能大于8个).
     */
    @Override
    public void convertIdiomStringsToWordLists() {
        sgIdiomWords = new String[igIdiomCount + 1][IdiomWordsMaxWord];
        igIdiomWordCount = new int[igIdiomCount + 1];
        for (int iIdiom = 1; iIdiom <= igIdiomCount; iIdiom++) {
            String[] sWordList = sgIdioms[iIdiom].split(" ");
            if (sWordList.length >= WordListMaxLength) {
                LOG.error("Ignoring idiom! Too many words in it! (>9): {}", sgIdioms[iIdiom]);
            } else {
                igIdiomWordCount[iIdiom] = sWordList.length;
                System.arraycopy(sWordList, 0, sgIdiomWords[iIdiom], 0, sWordList.length);
            }
        }

    }

    /**
     * 被弃用的得到目标短语的习语强度的函数.
     * @param sPhrase 目标短语
     * @return 短语的强度
     */
    public int getIdiomStrength_oldNotUseful(String sPhrase) {
        sPhrase = sPhrase.toLowerCase();
        for (int i = 1; i <= igIdiomCount; i++) {
            if (sPhrase.contains(sgIdioms[i])) {
                return igIdiomStrength[i];
            }
        }
        return IdiomStrengthNotFound;
    }

    /**
     * 根据给定的idiom编号，查找出对应的idiom.
     * @param iIdiomID 目标idiom的id编号
     * @return 查找到的idiom
     */
    @Override
    public String getIdiom(final int iIdiomID) {
        if (iIdiomID > 0 && iIdiomID < igIdiomCount) {
            return sgIdioms[iIdiomID];
        } else {
            return "";
        }
    }

    /**
     * igIdiomStrength的外部访问方法.
     * @return IdiomList的私有变量igIdiomStrength
     */
    @Override
    public int[] getIgIdiomStrength() {
        return igIdiomStrength;
    }
    /**
     * igIdiomCount的外部访问方法.
     * @return IdiomList的私有变量igIdiomCount
     */
    @Override
    public int getIgIdiomCount() {
        return igIdiomCount;
    }
    /**
     * sgIdiomWords的外部访问方法.
     * @return IdiomList的私有变量sgIdiomWords
     */
    @Override
    public String[][] getSgIdiomWords() {
        return sgIdiomWords;
    }
    /**
     * igIdiomWordCount的外部访问方法.
     * @return IdiomList的私有变量igIdiomWordCount
     */
    @Override
    public int[] getIgIdiomWordCount() {
        return igIdiomWordCount;
    }
}
