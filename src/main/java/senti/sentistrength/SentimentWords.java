// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   SentimentWords.java

package senti.sentistrength;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.sentistrength.classification.ClassificationOptions;
import senti.utilities.FileOps;
import senti.utilities.Sort;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            Corpus, ClassificationOptions

public class SentimentWords {
    private String[] sgSentimentWords;
    private int[] igSentimentWordsStrengthTake1;
    private int igSentimentWordsCount;
    private String[] sgSentimentWordsWithStarAtStart;
    private int[] igSentimentWordsWithStarAtStartStrengthTake1;
    private int igSentimentWordsWithStarAtStartCount;
    private boolean[] bgSentimentWordsWithStarAtStartHasStarAtEnd;
    private static final int SentimentNotFound = 999;
    private static final Logger LOG = LoggerFactory.getLogger(SentimentWords.class);
    /**
     * SentimentWords的无参构造函数.
     */
    public SentimentWords() {
        igSentimentWordsCount = 0;
        igSentimentWordsWithStarAtStartCount = 0;
    }

    /**
     * 根据给定的词汇下标在SentimentWords的词汇列表内查找对应的词汇.
     * @param iWordID 待查找词汇的对应下标
     * @return 在SentimentWords类中查找到的对应Sentiment word
     */
    public String getSentimentWord(final int iWordID) {
        if (iWordID > 0) {
            if (iWordID <= igSentimentWordsCount) {
                return sgSentimentWords[iWordID];
            }
            if (iWordID <= igSentimentWordsCount + igSentimentWordsWithStarAtStartCount) {
                return sgSentimentWordsWithStarAtStart[iWordID - igSentimentWordsCount];
            }
        }
        return "";
    }

    /**
     * 根据给定词汇查找其对应的sentiment.
     * @param sWord 待查找词汇
     * @return 目标词汇对应的sentiment
     */
    public int getSentiment(final String sWord) {
        int iWordID = Sort.i_FindStringPositionInSortedArrayWithWildcardsInArray(
                sWord.toLowerCase(), sgSentimentWords, 1, igSentimentWordsCount);
        if (iWordID >= 0) {
            return igSentimentWordsStrengthTake1[iWordID];
        }
        int iStarWordID = getMatchingStarAtStartRawWordID(sWord);
        if (iStarWordID >= 0) {
            return igSentimentWordsWithStarAtStartStrengthTake1[iStarWordID];
        } else {
            return SentimentNotFound;
        }
    }

    /**
     * 更新给定词汇的sentiment.
     * @param sWord 待更新的词汇
     * @param iNewSentiment 更新后的sentiment
     * @return 更新是否成功
     */
    public boolean setSentiment(String sWord, final int iNewSentiment) {
        int iWordID = Sort.i_FindStringPositionInSortedArrayWithWildcardsInArray(
                sWord.toLowerCase(), sgSentimentWords, 1, igSentimentWordsCount);
        if (iWordID >= 0) {
            if (iNewSentiment > 0) {
                setSentiment(iWordID, iNewSentiment - 1);
            } else {
                setSentiment(iWordID, iNewSentiment + 1);
            }
            return true;
        }
        if (sWord.indexOf("*") == 0) {
            sWord = sWord.substring(1);
            if (sWord.indexOf("*") > 0) {
                sWord.substring(0, sWord.length() - 1);
            }
        }
        if (igSentimentWordsWithStarAtStartCount > 0) {
            for (int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++) {
                if (sWord.equals(sgSentimentWordsWithStarAtStart[i])) {
                    if (iNewSentiment > 0) {
                        setSentiment(igSentimentWordsCount + i, iNewSentiment - 1);
                    } else {
                        setSentiment(igSentimentWordsCount + i, iNewSentiment + 1);
                    }
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 保存当前SentimentWords类内部
     * sgSentimentWords、igSentimentWordsStrengthTake1和
     * sgSentimentWordsWithStarAtStart、igSentimentWordsWithStarAtStartStrengthTake1
     * 这两组word-strength对的数据到指定文件名的文件.
     * @param sFilename 将数据写入此文件名的文件
     * @param c 包含文件写入方式的corpus类，决定是否强制使用UTF8编码
     * @return 保存是否成功
     */
    public boolean saveSentimentList(final String sFilename, final Corpus c) {
        try {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sFilename));
            for (int i = 1; i <= igSentimentWordsCount; i++) {
                int iSentimentStrength = igSentimentWordsStrengthTake1[i];
                if (iSentimentStrength < 0) {
                    iSentimentStrength--;
                } else {
                    iSentimentStrength++;
                }
                String sOutput = sgSentimentWords[i] + "\t" + iSentimentStrength + "\n";
                if (c.options.bgForceUTF8) {
                    sOutput = new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                }
                wWriter.write(sOutput);
            }

            for (int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++) {
                int iSentimentStrength = igSentimentWordsWithStarAtStartStrengthTake1[i];
                if (iSentimentStrength < 0) {
                    iSentimentStrength--;
                } else {
                    iSentimentStrength++;
                }
                String sOutput = "*" + sgSentimentWordsWithStarAtStart[i];
                if (bgSentimentWordsWithStarAtStartHasStarAtEnd[i]) {
                    sOutput = sOutput + "*";
                }
                sOutput = sOutput + "\t" + iSentimentStrength + "\n";
                if (c.options.bgForceUTF8) {
                    sOutput = new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                }
                wWriter.write(sOutput);
            }

            wWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 将igSentimentWordsStrengthTake1和igSentimentWordsWithStarAtStartStrengthTake1中
     * 所有的strength值用tab分隔后作为单独一行顺序写入目标BufferedWriter.
     * @param wWriter 用于写入strength值的BufferedWriter
     * @return 写入是否成功
     */
    public boolean printSentimentValuesInSingleRow(final BufferedWriter wWriter) {
        try {
            for (int i = 1; i <= igSentimentWordsCount; i++) {
                int iSentimentStrength = igSentimentWordsStrengthTake1[i];
                wWriter.write("\t" + iSentimentStrength);
            }

            for (int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++) {
                int iSentimentStrength = igSentimentWordsWithStarAtStartStrengthTake1[i];
                wWriter.write("\t" + iSentimentStrength);
            }

            wWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 将sgSentimentWords和sgSentimentWordsWithStarAtStart中
     * 所有的word字符串用tab分隔后作为单独一行顺序写入目标BufferedWriter.
     * @param wWriter 用于写入word字符串的BufferedWriter
     * @return 写入是否成功
     */
    public boolean printSentimentTermsInSingleHeaderRow(final BufferedWriter wWriter) {
        try {
            for (int i = 1; i <= igSentimentWordsCount; i++) {
                wWriter.write("\t" + sgSentimentWords[i]);
            }

            for (int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++) {
                wWriter.write("\t*" + sgSentimentWordsWithStarAtStart[i]);
                if (bgSentimentWordsWithStarAtStartHasStarAtEnd[i]) {
                    wWriter.write("*");
                }
            }
            wWriter.write("\n");
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 根据给定的词汇下标查找对应的强度.
     * @param iWordID 待查找词汇的下标
     * @return 目标词汇的强度
     */
    public int getSentiment(final int iWordID) {
        if (iWordID > 0) {
            if (iWordID <= igSentimentWordsCount) {
                return igSentimentWordsStrengthTake1[iWordID];
            } else {
                return igSentimentWordsWithStarAtStartStrengthTake1[iWordID - igSentimentWordsCount];
            }
        } else {
            return SentimentNotFound;
        }
    }

    /**
     * 更新给定下标的词汇的sentiment strength.
     * @param iWordID 待更新的词汇下标
     * @param iNewSentiment 待更新sentiment strength值
     */
    public void setSentiment(final int iWordID, final int iNewSentiment) {
        if (iWordID <= igSentimentWordsCount) {
            igSentimentWordsStrengthTake1[iWordID] = iNewSentiment;
        } else {
            igSentimentWordsWithStarAtStartStrengthTake1[iWordID - igSentimentWordsCount] = iNewSentiment;
        }
    }

    /**
     * 查找给定不带星号词汇的下标.
     * @param sWord 待查找的词汇
     * @return 目标词汇的下标
     */
    public int getSentimentID(final String sWord) {
        int iWordID = Sort.i_FindStringPositionInSortedArrayWithWildcardsInArray(
                sWord.toLowerCase(), sgSentimentWords, 1, igSentimentWordsCount);
        if (iWordID >= 0) {
            return iWordID;
        }
        iWordID = getMatchingStarAtStartRawWordID(sWord);
        if (iWordID >= 0) {
            return iWordID + igSentimentWordsCount;
        } else {
            return -1;
        }
    }

    /**
     * 查找给定带星号词汇的下标（可以忽略掉多余的星号）.
     * @param sWord 待查找的带星号词汇
     * @return 目标词汇的下标
     */
    private int getMatchingStarAtStartRawWordID(final String sWord) {
        int iSubStringPos;
        if (igSentimentWordsWithStarAtStartCount > 0) {
            for (int i = 1; i <= igSentimentWordsWithStarAtStartCount; i++) {
                iSubStringPos = sWord.indexOf(sgSentimentWordsWithStarAtStart[i]);
                if (iSubStringPos >= 0 && (bgSentimentWordsWithStarAtStartHasStarAtEnd[i]
                        || iSubStringPos + sgSentimentWordsWithStarAtStart[i].length() == sWord.length())) {
                    return i;
                }
            }
        }
        return -1;
    }

    /**
     * 查询SentimentWordsCount，即不带星号的词汇数量.
     * @return igSentimentWordsCount的值
     */
    public int getSentimentWordCount() {
        return igSentimentWordsCount;
    }

    /**
     * 不带星号的SentimentWords的含参初始化函数.
     * @param sFilename 包含初始化所需数据的文件文件名
     * @param options 读取文件的所选方式，是否强制UTF8编码读取文件内容
     * @param iExtraBlankArrayEntriesToInclude 添加的预留额外空白记录行数
     * @return 初始化是否成功
     */
    public boolean initialise(final String sFilename, final ClassificationOptions options,
                              final int iExtraBlankArrayEntriesToInclude) {
        int iWordStrength;
        int iWordsWithStarAtStart = 0;
        if (sFilename.equals("")) {
            LOG.error("No sentiment file specified");
            return false;
        }
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find sentiment file: {}", sFilename);
            return false;
        }
        int iLinesInFile = FileOps.i_CountLinesInTextFile(sFilename);
        if (iLinesInFile < 2) {
            LOG.error("Less than 2 lines in sentiment file: {}", sFilename);
            return false;
        }
        igSentimentWordsStrengthTake1 = new int[iLinesInFile + 1 + iExtraBlankArrayEntriesToInclude];
        sgSentimentWords = new String[iLinesInFile + 1 + iExtraBlankArrayEntriesToInclude];
        igSentimentWordsCount = 0;
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
                    if (sLine.indexOf("*") == 0) {
                        iWordsWithStarAtStart++;
                    } else {
                        int iFirstTabLocation = sLine.indexOf("\t");
                        if (iFirstTabLocation >= 0) {
                            int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                            try {
                                if (iSecondTabLocation > 0) {
                                    iWordStrength = Integer.parseInt(
                                            sLine.substring(iFirstTabLocation + 1, iSecondTabLocation).trim());
                                } else {
                                    iWordStrength = Integer.parseInt(
                                            sLine.substring(iFirstTabLocation + 1).trim());
                                }
                            } catch (NumberFormatException e) {
                                LOG.error("Failed to identify integer weight for sentiment word! Ignoring word\nLine: {}", sLine);
                                iWordStrength = 0;
                            }
                            sLine = sLine.substring(0, iFirstTabLocation);
                            if (sLine.contains(" ")) {
                                sLine = sLine.trim();
                            }
                            if (!sLine.equals("")) {
                                sgSentimentWords[++igSentimentWordsCount] = sLine;
                                if (iWordStrength > 0) {
                                    iWordStrength--;
                                } else if (iWordStrength < 0) {
                                    iWordStrength++;
                                }
                                igSentimentWordsStrengthTake1[igSentimentWordsCount] = iWordStrength;
                            }
                        }
                    }
                }
            }
            rReader.close();
            Sort.quickSortStringsWithInt(sgSentimentWords, igSentimentWordsStrengthTake1, 1, igSentimentWordsCount);
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't find sentiment file: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found sentiment file but couldn't read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        if (iWordsWithStarAtStart > 0) {
            return initialiseWordsWithStarAtStart(
                    sFilename, options, iWordsWithStarAtStart, iExtraBlankArrayEntriesToInclude);
        } else {
            return true;
        }
    }

    /**
     * 带星号的SentimentWords的含参初始化函数.
     * @param sFilename 包含初始化所需数据的文件文件名
     * @param options 读取文件的所选方式，是否强制UTF8编码读取文件内容
     * @param iWordsWithStarAtStart 初始化所涉及的带星号的词汇数量
     * @param iExtraBlankArrayEntriesToInclude 添加的预留额外空白记录行数
     * @return 初始化是否成功
     */
    public boolean initialiseWordsWithStarAtStart(
            final String sFilename, final ClassificationOptions options,
            final int iWordsWithStarAtStart, final int iExtraBlankArrayEntriesToInclude) {
        int iWordStrength;
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find sentiment file: {}", sFilename);
            return false;
        }
        igSentimentWordsWithStarAtStartStrengthTake1 = new int[
                iWordsWithStarAtStart + 1 + iExtraBlankArrayEntriesToInclude];
        sgSentimentWordsWithStarAtStart = new String[
                iWordsWithStarAtStart + 1 + iExtraBlankArrayEntriesToInclude];
        bgSentimentWordsWithStarAtStartHasStarAtEnd = new boolean[
                iWordsWithStarAtStart + 1 + iExtraBlankArrayEntriesToInclude];
        igSentimentWordsWithStarAtStartCount = 0;
        try {
            BufferedReader rReader;
            if (options.bgForceUTF8) {
                rReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(sFilename), StandardCharsets.UTF_8));
            } else {
                rReader = new BufferedReader(new FileReader(sFilename));
            }
            while (rReader.ready()) {
                String sLine = rReader.readLine();
                if (sLine.indexOf("*") == 0) {
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if (iFirstTabLocation >= 0) {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        try {
                            if (iSecondTabLocation > 0) {
                                iWordStrength = Integer.parseInt(
                                        sLine.substring(iFirstTabLocation + 1, iSecondTabLocation));
                            } else {
                                iWordStrength = Integer.parseInt(
                                        sLine.substring(iFirstTabLocation + 1));
                            }
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for *sentiment* word! Ignoring word\nLine: {}", sLine);
                            iWordStrength = 0;
                        }
                        sLine = sLine.substring(1, iFirstTabLocation);
                        if (sLine.indexOf("*") > 0) {
                            sLine = sLine.substring(0, sLine.indexOf("*"));
                            bgSentimentWordsWithStarAtStartHasStarAtEnd[++igSentimentWordsWithStarAtStartCount] = true;
                        } else {
                            bgSentimentWordsWithStarAtStartHasStarAtEnd[++igSentimentWordsWithStarAtStartCount] = false;
                        }
                        if (sLine.contains(" ")) {
                            sLine = sLine.trim();
                        }
                        if (!sLine.equals("")) {
                            sgSentimentWordsWithStarAtStart[igSentimentWordsWithStarAtStartCount] = sLine;
                            if (iWordStrength > 0) {
                                iWordStrength--;
                            } else if (iWordStrength < 0) {
                                iWordStrength++;
                            }
                            igSentimentWordsWithStarAtStartStrengthTake1[igSentimentWordsWithStarAtStartCount] = iWordStrength;
                        } else {
                            igSentimentWordsWithStarAtStartCount--;
                        }
                    }
                }
            }
            rReader.close();
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't find *sentiment file*: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found *sentiment file* but couldn't read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 添加或者修改给定词汇（不含星号）的强度，以及修改后是否重新排序.
     * @param sTerm 需要添加或者修改的目标词汇
     * @param iTermStrength 该词汇的强度
     * @param bSortSentimentListAfterAddingTerm 是否在更新后对不带星号的词汇列表和强度列表重新排序
     * @return 更新操作是否成功
     */
    public boolean addOrModifySentimentTerm(
            final String sTerm, int iTermStrength,
            final boolean bSortSentimentListAfterAddingTerm) {
        int iTermPosition = getSentimentID(sTerm);
        if (iTermPosition > 0) {
            if (iTermStrength > 0) {
                iTermStrength--;
            } else if (iTermStrength < 0) {
                iTermStrength++;
            }
            igSentimentWordsStrengthTake1[iTermPosition] = iTermStrength;
        } else {
            try {
                sgSentimentWords[++igSentimentWordsCount] = sTerm;
                if (iTermStrength > 0) {
                    iTermStrength--;
                } else if (iTermStrength < 0) {
                    iTermStrength++;
                }
                igSentimentWordsStrengthTake1[igSentimentWordsCount] = iTermStrength;
                if (bSortSentimentListAfterAddingTerm) {
                    Sort.quickSortStringsWithInt(
                            sgSentimentWords, igSentimentWordsStrengthTake1, 1, igSentimentWordsCount);
                }
            } catch (Exception e) {
                LOG.error("Could not add extra sentiment term: {}", sTerm);
                e.printStackTrace();
                return false;
            }
        }
        return true;
    }

    /**
     * 对sgSentimentWords和igSentimentWordsStrengthTake1进行排序.
     */
    public void sortSentimentList() {
        Sort.quickSortStringsWithInt(sgSentimentWords,
                igSentimentWordsStrengthTake1, 1, igSentimentWordsCount);
    }
}
