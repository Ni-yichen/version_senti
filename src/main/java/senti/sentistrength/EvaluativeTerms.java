// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   EvaluativeTerms.java

package senti.sentistrength;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.sentistrength.classification.ClassificationOptions;
import senti.sentistrength.wordsList.WordsListExtend;
import senti.utilities.FileOps;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            ClassificationOptions, IdiomList, SentimentWords

public class EvaluativeTerms {
    private int igObjectEvaluationMax;
    private String[] sgObject;
    private String[] sgObjectEvaluation;
    private int[] igObjectEvaluationStrength;
    private int igObjectEvaluationCount;
    private static final Logger LOG = LoggerFactory.getLogger(EvaluativeTerms.class);
    /**
     * EvaluativeTerms的无参构造函数.
     */
    public EvaluativeTerms() {
        igObjectEvaluationMax = 0;
        igObjectEvaluationCount = 0;
    }

    /**
     * EvaluativeTerms的含参初始化函数.
     * @param sSourceFile 包含初始化所需数据的文件文件名
     * @param options 读取文件的可选方式，是否使用UTF8编码
     * @param idiomList 初始化同时更新的idiomList类
     * @param sentimentWords 初始化同时更新的sentimentWords类
     * @return 初始化是否成功
     */
    public boolean initialise(
            final String sSourceFile, final ClassificationOptions options,
            final WordsListExtend idiomList, final SentimentWords sentimentWords) {
        if (igObjectEvaluationCount > 0) {
            return true;
        }
        File f = new File(sSourceFile);
        if (!f.exists()) {
            LOG.error("Could not find additional (object/evaluation) file: {}", sSourceFile);
            return false;
        }
        int iStrength;
        boolean bIdiomsAdded = false;
        boolean bSentimentWordsAdded = false;
        try {
            igObjectEvaluationMax = FileOps.i_CountLinesInTextFile(sSourceFile) + 2;
            igObjectEvaluationCount = 0;
            sgObject = new String[igObjectEvaluationMax];
            sgObjectEvaluation = new String[igObjectEvaluationMax];
            igObjectEvaluationStrength = new int[igObjectEvaluationMax];
            BufferedReader rReader;
            if (options.bgForceUTF8) {
                rReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
            } else {
                rReader = new BufferedReader(new FileReader(sSourceFile));
            }
            String sLine;
            while ((sLine = rReader.readLine()) != null) {
                if (sLine.indexOf("##") != 0 && sLine.indexOf("\t") > 0) {
                    //sLine != "" && sLine.indexOf("##") != 0 && sLine.indexOf("\t") > 0
                    String[] sData = sLine.split("\t");
                    if (sData.length > 2 && sData[2].indexOf("##") != 0) {
                        sgObject[++igObjectEvaluationCount] = sData[0];
                        sgObjectEvaluation[igObjectEvaluationCount] = sData[1];
                        try {
                            igObjectEvaluationStrength[igObjectEvaluationCount] = Integer.parseInt(sData[2].trim());
                            if (igObjectEvaluationStrength[igObjectEvaluationCount] > 0) {
                                igObjectEvaluationStrength[igObjectEvaluationCount]--;
                            } else if (igObjectEvaluationStrength[igObjectEvaluationCount] < 0) {
                                igObjectEvaluationStrength[igObjectEvaluationCount]++;
                            }
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for object/evaluation! Ignoring object/evaluation");
                            LOG.error("Line: {}", sLine);
                            igObjectEvaluationCount--;
                        }
                    } else if (sData[0].indexOf(" ") > 0) {
                        try {
                            iStrength = Integer.parseInt(sData[1].trim());
                            idiomList.addExtraIdiom(
                                    sData[0], iStrength, false);
                            bIdiomsAdded = true;
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for idiom in additional file! Ignoring it");
                            LOG.error("Line: {}", sLine);
                        }
                    } else {
                        try {
                            iStrength = Integer.parseInt(sData[1].trim());
                            sentimentWords.addOrModifySentimentTerm(
                                    sData[0], iStrength, false);
                            bSentimentWordsAdded = true;
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for sentiment term in additional file! Ignoring it");
                            LOG.error("Line: {}", sLine);
                            igObjectEvaluationCount--;
                        }
                    }
                }
            }
            rReader.close();
            if (igObjectEvaluationCount > 0) {
                options.bgUseObjectEvaluationTable = true;
            }
            if (bSentimentWordsAdded) {
                sentimentWords.sortSentimentList();
            }
            if (bIdiomsAdded) {
                idiomList.convertIdiomStringsToWordLists();
            }
        } catch (FileNotFoundException e) {
            LOG.error("Could not find additional (object/evaluation) file: {}", sSourceFile);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found additional (object/evaluation) file but could not read from it: {}", sSourceFile);
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * sgObject的外部访问方法.
     * @return EvaluativeTerms的私有变量sgObject
     */
    public String[] getSgObject() {
        return sgObject;
    }
    /**
     * sgObjectEvaluation的外部访问方法.
     * @return EvaluativeTerms的私有变量sgObjectEvaluation
     */
    public String[] getSgObjectEvaluation() {
        return sgObjectEvaluation;
    }
    /**
     * igObjectEvaluationStrength的外部访问方法.
     * @return EvaluativeTerms的私有变量igObjectEvaluationStrength
     */
    public int[] getIgObjectEvaluationStrength() {
        return igObjectEvaluationStrength;
    }
    /**
     * igObjectEvaluationCount的外部访问方法.
     * @return EvaluativeTerms的私有变量igObjectEvaluationCount
     */
    public int getIgObjectEvaluationCount() {
        return igObjectEvaluationCount;
    }
}
