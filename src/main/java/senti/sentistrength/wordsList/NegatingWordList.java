// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   NegatingWordList.java

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

public class NegatingWordList extends WordsList{
    private String[] sgNegatingWord;
    private int igNegatingWordCount;
    private int igNegatingWordMax;
    private static final Logger LOG = LoggerFactory.getLogger(NegatingWordList.class);
    /**
     * NegatingWordList的无参构造函数.
     */
    public NegatingWordList() {
        igNegatingWordCount = 0;
        igNegatingWordMax = 0;
    }

    /**
     * NegatingWordList的含参初始化函数.
     * @param sFilename 包含初始化所需数据的文件文件名
     * @param options 读取文件的所选方式，是否使用UTF8编码
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(final String sFilename, final ClassificationOptions options) {
        if (igNegatingWordMax > 0) {
            return true;
        }
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find the negating words file: {}", sFilename);
            return false;
        }
        igNegatingWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
        sgNegatingWord = new String[igNegatingWordMax];
        igNegatingWordCount = 0;
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
                    igNegatingWordCount++;
                    sgNegatingWord[igNegatingWordCount] = sLine;
                }
            }
            rReader.close();
            Sort.quickSortStrings(sgNegatingWord, 1, igNegatingWordCount);
        } catch (FileNotFoundException e) {
            LOG.error("Could not find negating words file: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found negating words file but could not read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断目标词汇是否属于negating word.
     * @param sWord 待判断词汇
     * @return 是否属于negating word
     */
    @Override
    public boolean negatingWord(final String sWord) {
        return Sort.i_FindStringPositionInSortedArray(
                sWord, sgNegatingWord, 1, igNegatingWordCount) >= 0;
    }
}
