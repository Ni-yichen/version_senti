// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   CorrectSpellingsList.java

package senti.sentistrength.wordsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.utilities.FileOps;
import senti.utilities.Sort;
import senti.sentistrength.classification.ClassificationOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            ClassificationOptions

public class CorrectSpellingsList extends WordsList{
    private String[] sgCorrectWord;
    private int igCorrectWordCount;
    private int igCorrectWordMax;
    private static final Logger LOG = LoggerFactory.getLogger(CorrectSpellingsList.class);

    /**
     * CorrectSpellingsList的无参构造函数.
     */
    public CorrectSpellingsList() {
        igCorrectWordCount = 0;
        igCorrectWordMax = 0;
    }

    /**
     * 含参的CorrectSpellingsList初始化函数.
     * @param sFilename 包含初始化所需信息的文件文件名
     * @param options 读取文件的可选方式，是否使用UTF8编码
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(final String sFilename, final ClassificationOptions options) {
        if (igCorrectWordMax > 0) {
            return true;
        }
        if (!options.bgCorrectSpellingsUsingDictionary) {
            return true;
        }
        igCorrectWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
        sgCorrectWord = new String[igCorrectWordMax];
        igCorrectWordCount = 0;
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find the spellings file: {}", sFilename);
            return false;
        }
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
                    igCorrectWordCount++;
                    sgCorrectWord[igCorrectWordCount] = sLine;
                }
            }
            rReader.close();
            Sort.quickSortStrings(sgCorrectWord, 1, igCorrectWordCount);
        } catch (FileNotFoundException e) {
            LOG.error("Could not find the spellings file: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found spellings file but could not read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断目标词汇是否正确拼写的函数.
     * @param sWord 目标词汇
     * @return 词汇拼写是否正确
     */
    @Override
    public boolean correctSpelling(final String sWord) {
        return Sort.i_FindStringPositionInSortedArray(
                sWord, sgCorrectWord, 1, igCorrectWordCount) >= 0;
    }
}
