// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   Lemmatiser.java

package senti.sentistrength;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.utilities.FileOps;
import senti.utilities.Sort;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class Lemmatiser {
    private String[] sgWord;
    private String[] sgLemma;
    private int igWordLast;
    private static final Logger LOG = LoggerFactory.getLogger(Lemmatiser.class);
    /**
     * Lemmatiser的无参构造函数.
     */
    public Lemmatiser() {
        igWordLast = -1;
    }

    /**
     * Lemmatiser的含参初始化函数.
     * @param sFileName 包含初始化所需数据的文件文件名
     * @param bForceUTF8 是否强制使用UTF8编码解读
     * @return 初始化是否成功
     */
    public boolean initialise(final String sFileName, final boolean bForceUTF8) {
        int iLinesInFile;
        if (sFileName.equals("")) {
            LOG.error("No lemma file specified!");
            return false;
        }
        File f = new File(sFileName);
        if (!f.exists()) {
            LOG.error("Could not find lemma file: {}", sFileName);
            return false;
        }
        iLinesInFile = FileOps.i_CountLinesInTextFile(sFileName);
        if (iLinesInFile < 2) {
            LOG.error("Less than 2 lines in sentiment file: {}", sFileName);
            return false;
        }
        sgWord = new String[iLinesInFile + 1];
        sgLemma = new String[iLinesInFile + 1];
        igWordLast = -1;
        try {
            BufferedReader rReader;
            if (bForceUTF8) {
                rReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(sFileName), StandardCharsets.UTF_8));
            } else {
                rReader = new BufferedReader(new FileReader(sFileName));
            }
            String sLine;
            while ((sLine = rReader.readLine()) != null) {
                if (!sLine.equals("")) {
                    int iFirstTabLocation = sLine.indexOf("\t");
                    if (iFirstTabLocation >= 0) {
                        int iSecondTabLocation = sLine.indexOf("\t", iFirstTabLocation + 1);
                        sgWord[++igWordLast] = sLine.substring(0, iFirstTabLocation);
                        if (iSecondTabLocation > 0) {
                            sgLemma[igWordLast] = sLine.substring(iFirstTabLocation + 1, iSecondTabLocation);
                        } else {
                            sgLemma[igWordLast] = sLine.substring(iFirstTabLocation + 1);
                        }

                        if (sgWord[igWordLast].contains(" ")) {
                            sgWord[igWordLast] = sgWord[igWordLast].trim();
                        }
                        if (sgLemma[igWordLast].contains(" ")) {
                            sgLemma[igWordLast] = sgLemma[igWordLast].trim();
                        }
                    }
                }
            }
            rReader.close();
            Sort.quickSortStringsWithStrings(sgWord, sgLemma, 0, igWordLast);
        } catch (FileNotFoundException e) {
            LOG.error("Couldn't find lemma file: {}", sFileName);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found lemma file but couldn't read from it: {}", sFileName);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 对目标词汇进行归类.
     * @param sWord 待归类词汇
     * @return 目标词汇所属归类
     */
    public String lemmatise(final String sWord) {
        int iLemmaID = Sort.i_FindStringPositionInSortedArray(sWord, sgWord, 0, igWordLast);
        if (iLemmaID >= 0) {
            return sgLemma[iLemmaID];
        } else {
            return sWord;
        }
    }
}
