// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   BoosterWordsList.java

package senti.sentistrength.wordsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.sentistrength.classification.ClassificationOptions;
import senti.utilities.FileOps;
import senti.utilities.Sort;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            ClassificationOptions

public class BoosterWordsList extends WordsListExtend {
    private String[] sgBoosterWords;
    private int[] igBoosterWordStrength;
    private int igBoosterWordsCount;
    private static final Logger LOG = LoggerFactory.getLogger(BoosterWordsList.class);

    /**
     * BoosterWordsList的无参构造函数.
     */
    public BoosterWordsList() {
        igBoosterWordsCount = 0;
    }

    /**
     * 含参的BoosterWordsList初始化函数.
     * @param sFilename 包含初始化所需数据的文件文件名
     * @param options 读取文件的可选方式，是否使用UTF8编码
     * @param iExtraBlankArrayEntriesToInclude 添加的预留额外空白记录行数
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(
            final String sFilename, final ClassificationOptions options,
            final int iExtraBlankArrayEntriesToInclude) {
        int iLinesInFile;
        int iWordStrength;
        if (sFilename.equals("")) {
            LOG.error("No booster words file specified");
            return false;
        }
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find booster words file: {}", sFilename);
            return false;
        }
        iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
        if (iLinesInFile < 1) {
            LOG.error("No booster words specified");
            return false;
        }
        sgBoosterWords = new String[iLinesInFile + 1 + iExtraBlankArrayEntriesToInclude];
        igBoosterWordStrength = new int[iLinesInFile + 1 + iExtraBlankArrayEntriesToInclude];
        igBoosterWordsCount = 0;
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
                                iWordStrength = Integer.parseInt(
                                        sLine.substring(iFirstTabLocation + 1, iSecondTabLocation));
                            } else {
                                iWordStrength = Integer.parseInt(
                                        sLine.substring(iFirstTabLocation + 1).trim());
                            }
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for booster word! Assuming it is zero");
                            LOG.error("Line: {}", sLine);
                            iWordStrength = 0;
                        }
                        sLine = sLine.substring(0, iFirstTabLocation);
                        if (sLine.contains(" ")) {
                            sLine = sLine.trim();
                        }
                        if (!sLine.equals("")) {
                            igBoosterWordsCount++;
                            sgBoosterWords[igBoosterWordsCount] = sLine;
                            igBoosterWordStrength[igBoosterWordsCount] = iWordStrength;
                        }
                    }
                }
            }
            Sort.quickSortStringsWithInt(sgBoosterWords, igBoosterWordStrength, 1, igBoosterWordsCount);
            rReader.close();
        } catch (FileNotFoundException e) {
            LOG.error("Could not find booster words file: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found booster words file but could not read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 在BoosterWordsList中添加额外词汇的函数.
     * @param sText 准备添加的booster词汇
     * @param iWordStrength 所添加booster词汇的强度
     * @param bSortBoosterListAfterAddingTerm 添加完词汇后是否对BoosterWordsList进行排序
     * @return 添加词汇是否成功
     */
    public boolean addExtraTerm(final String sText, final int iWordStrength,
                                final boolean bSortBoosterListAfterAddingTerm) {
        try {
            igBoosterWordsCount++;
            sgBoosterWords[igBoosterWordsCount] = sText;
            igBoosterWordStrength[igBoosterWordsCount] = iWordStrength;
            if (bSortBoosterListAfterAddingTerm) {
                Sort.quickSortStringsWithInt(
                        sgBoosterWords, igBoosterWordStrength, 1, igBoosterWordsCount);
            }
        } catch (Exception e) {
            LOG.error("Could not add extra booster word: {}", sText);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 对BoosterWordsList进行二分法排序的方法.
     */
    public void sortBoosterWordList() {
        Sort.quickSortStringsWithInt(sgBoosterWords,
                igBoosterWordStrength, 1, igBoosterWordsCount);
    }

    /**
     * 在BoosterWordsList中查找目标词汇的强度.
     * @param sWord 目标booster词汇
     * @return booster词汇强度
     */
    @Override
    public int getBoosterStrength(final String sWord) {
        int iWordID = Sort.i_FindStringPositionInSortedArray(
                sWord.toLowerCase(), sgBoosterWords, 1, igBoosterWordsCount);
        if (iWordID >= 0) {
            return igBoosterWordStrength[iWordID];
        } else {
            return 0;
        }
    }
}
