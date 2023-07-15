// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   QuestionWords.java

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

public class QuestionWords extends WordsList{
    private String[] sgQuestionWord;
    private int igQuestionWordCount;
    private int igQuestionWordMax;
    private static final Logger LOG = LoggerFactory.getLogger(QuestionWords.class);
    /**
     * QuestionWords的无参构造函数.
     */
    public QuestionWords() {
        igQuestionWordCount = 0;
        igQuestionWordMax = 0;
    }

    /**
     * QuestionWords的含参初始化函数.
     *
     * @param sFilename 记录初始化所需数据的文件文件名
     * @param options 读取文件方式，是否强制UTF8编码
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(final String sFilename, final ClassificationOptions options) {
        if (igQuestionWordMax > 0) {
            return true;
        }
        File f = new File(sFilename);
        if (!f.exists()) {
            LOG.error("Could not find the question word file: {}", sFilename);
            return false;
        }
        igQuestionWordMax = FileOps.i_CountLinesInTextFile(sFilename) + 2;
        sgQuestionWord = new String[igQuestionWordMax];
        igQuestionWordCount = 0;
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
                    igQuestionWordCount++;
                    sgQuestionWord[igQuestionWordCount] = sLine;
                }
            }
            rReader.close();
            Sort.quickSortStrings(sgQuestionWord, 1, igQuestionWordCount);
        } catch (FileNotFoundException e) {
            LOG.error("Could not find the question word file: {}", sFilename);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found question word file but could not read from it: {}", sFilename);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 判断目标词汇是否属于question word.
     * @param sWord 待判断词汇
     * @return 是否属于question word
     */
    @Override
    public boolean questionWord(final String sWord) {
        return Sort.i_FindStringPositionInSortedArray(
                sWord, sgQuestionWord, 1, igQuestionWordCount) >= 0;
    }
}
