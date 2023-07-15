// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   Corpus.java

package senti.sentistrength;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.sentistrength.classification.ClassificationOptions;
import senti.sentistrength.classification.ClassificationResources;
import senti.sentistrength.classification.ClassificationStatistics;
import senti.sentistrength.contextProcess.Paragraph;
import senti.sentistrength.unusedTermsClassificationStrategy.UnusedTermsClassificationStrategy;
import senti.sentistrength.unusedTermsClassificationStrategy.UnusedTermsContext;
import senti.utilities.FileOps;
import senti.utilities.Sort;
import senti.sentistrength.unusedTermsClassificationStrategy.*;

// Referenced classes of package uk.ac.wlv.sentistrength:
//      ClassificationOptions, ClassificationResources, UnusedTermsClassificationIndex, Paragraph,
//      ClassificationStatistics, SentimentWords

public class Corpus {

    public ClassificationOptions options;
    public ClassificationResources resources;
    private Paragraph[] paragraph;
    private int igParagraphCount; //段落数
    private int[] igPosCorrect;
    private int[] igNegCorrect;
    private int[] igTrinaryCorrect;
    private int[] igScaleCorrect;
    private int[] igPosClass;
    private int[] igNegClass;
    private int[] igTrinaryClass; //三元（积极，消极，中性）类
    private int[] igScaleClass; //五级尺度（即绝对值为1-5的分数评级尺度）类
    private boolean bgCorpusClassified; //语料库是否被分类
    private int[] igSentimentIDList; //情感ID列表
    private int igSentimentIDListCount; //情感ID列表元素数
    private int[] igSentimentIDParagraphCount; //情感ID对应的段落数
    private boolean bSentimentIDListMade; //情感ID列表是否被构造
    UnusedTermsClassificationStrategy unusedTermsClassificationIndex; //未使用术语分类索引
    private boolean[] bgSupcorpusMember; //是否是超语料库成员
    int igSupcorpusMemberCount; //超语料库成员数

    private static final Logger LOG = LoggerFactory.getLogger("Corpus");

    /**
     * 构造函数.
     */
    public Corpus() {
        options = new ClassificationOptions();
        resources = new ClassificationResources();
        igParagraphCount = 0;
        bgCorpusClassified = false;
        igSentimentIDListCount = 0;
        bSentimentIDListMade = false;
        unusedTermsClassificationIndex = null;
    }

    /**
     * 将已分类的语料库进行索引以进行文本分类.
     */
    public void indexClassifiedCorpus() {

        if (options.bgScaleMode) {
            unusedTermsClassificationIndex = UnusedTermsContext.getStrategy("Scale");
            unusedTermsClassificationIndex.initialise(true, false, false, false);
            for (int i = 1; i <= igParagraphCount; i++) {
                paragraph[i].addParagraphToIndexWithScaleValues(unusedTermsClassificationIndex,
                        igScaleCorrect[i], igScaleClass[i]);
            }
        } else if (options.bgTrinaryMode && options.bgBinaryVersionOfTrinaryMode) {
            unusedTermsClassificationIndex = UnusedTermsContext.getStrategy("Binary");
            unusedTermsClassificationIndex.initialise(false, false, true, false);
            for (int i = 1; i <= igParagraphCount; i++) {
                paragraph[i].addParagraphToIndexWithBinaryValues(unusedTermsClassificationIndex,
                        igTrinaryCorrect[i], igTrinaryClass[i]);
            }
        } else if (options.bgTrinaryMode && !options.bgBinaryVersionOfTrinaryMode) {
            unusedTermsClassificationIndex = UnusedTermsContext.getStrategy("Trinary");
            unusedTermsClassificationIndex.initialise(false, false, false, true);
            for (int i = 1; i <= igParagraphCount; i++) {
                paragraph[i].addParagraphToIndexWithTrinaryValues(unusedTermsClassificationIndex,
                        igTrinaryCorrect[i], igTrinaryClass[i]);
            }
        } else {
            unusedTermsClassificationIndex = UnusedTermsContext.getStrategy("PosNeg");
            unusedTermsClassificationIndex.initialise(false, true, false, false);
            for (int i = 1; i <= igParagraphCount; i++) {
                paragraph[i].addParagraphToIndexWithPosNegValues(unusedTermsClassificationIndex,
                        igPosCorrect[i], igPosClass[i], igNegCorrect[i], igNegClass[i]);
            }
        }
    }

    /**
     * 将已分类的语料库中未达到最小使用频率的术语的权重打印出来，并保存到指定的文件中.
     *
     * @param saveFile 保存文件路径
     * @param iMinFreq 术语最小使用频率
     */
    public void printCorpusUnusedTermsClassificationIndex(String saveFile, int iMinFreq) {
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        if (unusedTermsClassificationIndex == null) {
            indexClassifiedCorpus();
        }
        if (options.bgScaleMode) {
            unusedTermsClassificationIndex.printIndex(saveFile, iMinFreq);
        } else if (options.bgTrinaryMode && options.bgBinaryVersionOfTrinaryMode) {
            unusedTermsClassificationIndex.printIndex(saveFile, iMinFreq);
        } else if (options.bgTrinaryMode && !options.bgBinaryVersionOfTrinaryMode) {
            unusedTermsClassificationIndex.printIndex(saveFile, iMinFreq);
        } else {
            unusedTermsClassificationIndex.printIndex(saveFile, iMinFreq);
        }
        System.out.println("Term weights saved to " + saveFile);
    }

    /**
     * 根据标记列表设置超语料库.
     *
     * @param bSubcorpusMember 各段落是否为超语料库的标记列表
     */
    public void setSubcorpus(boolean[] bSubcorpusMember) {
        igSupcorpusMemberCount = 0;
        for (int i = 0; i <= igParagraphCount; i++) {
            if (bSubcorpusMember[i]) {
                bgSupcorpusMember[i] = true;
                igSupcorpusMemberCount++;
            } else {
                bgSupcorpusMember[i] = false;
            }
        }

    }

    /**
     * 将所有段落都设置为超语料库成员，以保证语料库不是任何语料库的子语料库.
     */
    public void useWholeCorpusNotSubcorpus() {
        for (int i = 0; i <= igParagraphCount; i++) {
            bgSupcorpusMember[i] = true;
        }

        igSupcorpusMemberCount = igParagraphCount;
    }

    /**
     * 获取语料库大小.
     *
     * @return 段落数
     */
    public int getCorpusSize() {
        return igParagraphCount;
    }

    /**
     * 将单个文本作为整个语料库进行处理，并进行情感分析.
     *
     * @param sText 待处理文本
     * @param iPosCorrect 正确的正面情感分数
     * @param iNegCorrect 正确的负面情感分数
     * @return boolean值，表示处理是否成功
     */
    public boolean setSingleTextAsCorpus(String sText, int iPosCorrect, int iNegCorrect) {
        if (resources == null) {
            return false;
        }
        igParagraphCount = 2;
        paragraph = new Paragraph[igParagraphCount];
        igPosCorrect = new int[igParagraphCount];
        igNegCorrect = new int[igParagraphCount];
        igTrinaryCorrect = new int[igParagraphCount];
        igScaleCorrect = new int[igParagraphCount];
        bgSupcorpusMember = new boolean[igParagraphCount];
        igParagraphCount = 1;
        paragraph[igParagraphCount] = new Paragraph();
        paragraph[igParagraphCount].setParagraph(sText, resources, options);
        igPosCorrect[igParagraphCount] = iPosCorrect;
        if (iNegCorrect < 0) {
            iNegCorrect *= -1;
        }
        igNegCorrect[igParagraphCount] = iNegCorrect;
        useWholeCorpusNotSubcorpus();
        return true;
    }

    /**
     * 从指定文件加载语料库.
     *
     * @param sInFilenameAndPath 文件路径（包含文件名）
     * @return boolean值，表示处理是否成功
     */
    public boolean setCorpus(String sInFilenameAndPath) {
        if (resources == null) {
            return false;
        }
        igParagraphCount = FileOps.i_CountLinesInTextFile(sInFilenameAndPath) + 1;
        if (igParagraphCount <= 2) {
            igParagraphCount = 0;
            return false;
        }
        paragraph = new Paragraph[igParagraphCount];
        igPosCorrect = new int[igParagraphCount];
        igNegCorrect = new int[igParagraphCount];
        igTrinaryCorrect = new int[igParagraphCount];
        igScaleCorrect = new int[igParagraphCount];
        bgSupcorpusMember = new boolean[igParagraphCount];
        igParagraphCount = 0;
        try (BufferedReader rReader = new BufferedReader(new FileReader(sInFilenameAndPath))) {

            String sLine;
            if (rReader.ready()) {
                sLine = rReader.readLine();
            }
            while ((sLine = rReader.readLine()) != null) {
                if (!sLine.equals("")) {
                    paragraph[++igParagraphCount] = new Paragraph();
                    int iLastTabPos = sLine.lastIndexOf("\t");
                    int iFirstTabPos = sLine.indexOf("\t");
                    if (iFirstTabPos < iLastTabPos || iFirstTabPos > 0
                            && (options.bgTrinaryMode || options.bgScaleMode)) {
                        paragraph[igParagraphCount].setParagraph(sLine.substring(iLastTabPos + 1),
                                resources, options);
                        if (options.bgTrinaryMode) {
                            try {
                                igTrinaryCorrect[igParagraphCount] = Integer.parseInt(sLine.substring(0, iFirstTabPos)
                                        .trim());
                            } catch (Exception e) {
                                String error = "Trinary classification could not be read and will be ignored!: "
                                        + sLine;
                                LOG.error(error);
                                igTrinaryCorrect[igParagraphCount] = 999;
                            }
                            if (igTrinaryCorrect[igParagraphCount] > 1
                                    || igTrinaryCorrect[igParagraphCount] < -1) {
                                System.out.println("Trinary classification out of bounds" +
                                        " and will be ignored!: " + sLine);
                                igParagraphCount--;
                            } else if (options.bgBinaryVersionOfTrinaryMode
                                    && igTrinaryCorrect[igParagraphCount] == 0) {
                                System.out.println("Warning, unexpected 0 in binary classification!: " + sLine);
                            }
                        } else if (options.bgScaleMode) {
                            try {
                                igScaleCorrect[igParagraphCount] = Integer.parseInt(sLine.substring(0, iFirstTabPos)
                                        .trim());
                            } catch (Exception e) {
                                String error = "Scale classification could not be read and will be ignored!: "
                                        + sLine;
                                LOG.error(error);
                                igScaleCorrect[igParagraphCount] = 999;
                            }
                            if (igScaleCorrect[igParagraphCount] > 4 || igTrinaryCorrect[igParagraphCount] < -4) {
                                System.out.println("Scale classification out of bounds (-4 to +4) "
                                        + "and will be ignored!: " + sLine);
                                igParagraphCount--;
                            }
                        } else {
                            try {
                                igPosCorrect[igParagraphCount] = Integer.parseInt(sLine.substring(0, iFirstTabPos)
                                        .trim());
                                igNegCorrect[igParagraphCount] = Integer.parseInt(sLine.substring(iFirstTabPos + 1,
                                        iLastTabPos).trim());
                                if (igNegCorrect[igParagraphCount] < 0) {
                                    igNegCorrect[igParagraphCount] = -igNegCorrect[igParagraphCount];
                                }
                            } catch (Exception e) {
                                String error = "Positive or negative classification could not be read "
                                        + "and will be ignored!: " + sLine;
                                LOG.error(error);
                                igPosCorrect[igParagraphCount] = 0;
                            }
                            if (igPosCorrect[igParagraphCount] > 5 || igPosCorrect[igParagraphCount] < 1) {
                                System.out.println("Warning, positive classification out of bounds "
                                        + "and line will be ignored!: " + sLine);
                                igParagraphCount--;
                            } else if (igNegCorrect[igParagraphCount] > 5 || igNegCorrect[igParagraphCount] < 1) {
                                System.out.println("Warning, negative classification out of bounds " +
                                        "(must be 1,2,3,4, or 5, with or without -) and line will be ignored!: "
                                        + sLine);
                                igParagraphCount--;
                            }
                        }
                    } else {
                        if (iFirstTabPos >= 0) {
                            if (options.bgTrinaryMode)
                                igTrinaryCorrect[igParagraphCount] = Integer.parseInt(sLine.substring(0, iFirstTabPos)
                                        .trim());
                            sLine = sLine.substring(iFirstTabPos + 1);
                        } else if (options.bgTrinaryMode)
                            igTrinaryCorrect[igParagraphCount] = 0;
                        paragraph[igParagraphCount].setParagraph(sLine, resources, options);
                        igPosCorrect[igParagraphCount] = 0;
                        igNegCorrect[igParagraphCount] = 0;
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        useWholeCorpusNotSubcorpus();
        System.out.println("Number of texts in corpus: " + igParagraphCount);
        return true;
    }

    /**
     * 初始化分类资源.
     *
     * @return 处理是否成功
     */
    public boolean initialise() {
        return resources.initialise(options);
    }

    /**
     * 重新计算语料库的情感得分.
     */
    public void reCalculateCorpusSentimentScores() {
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                paragraph[i].recalculateParagraphSentimentScores();
            }
        }

        calculateCorpusSentimentScores();
    }

    /**
     * 获取指定的语料库成员的正面情感分数.
     *
     * @param i 指定的语料库成员索引
     * @return 指定的语料库成员正面情感分数
     */
    public int getCorpusMemberPositiveSentimentScore(int i) {
        if (i < 1 || i > igParagraphCount) {
            return 0;
        } else {
            return paragraph[i].getParagraphPositiveSentiment();
        }
    }

    /**
     * 获取指定的语料库成员的负面情感分数.
     *
     * @param i 指定的语料库成员索引
     * @return 指定的语料库成员负面情感分数
     */
    public int getCorpusMemberNegativeSentimentScore(int i) {
        if (i < 1 || i > igParagraphCount) {
            return 0;
        } else {
            return paragraph[i].getParagraphNegativeSentiment();
        }
    }

    /**
     * 计算语料库情感得分.
     */
    public void calculateCorpusSentimentScores() {
        if (igParagraphCount == 0) {
            return;
        }
        if (igPosClass == null || igPosClass.length < igPosCorrect.length) {
            igPosClass = new int[igParagraphCount + 1];
            igNegClass = new int[igParagraphCount + 1];
            igTrinaryClass = new int[igParagraphCount + 1];
            igScaleClass = new int[igParagraphCount + 1];
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
                igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
                if (options.bgTrinaryMode) {
                    igTrinaryClass[i] = paragraph[i].getParagraphTrinarySentiment();
                }
                if (options.bgScaleMode) {
                    igScaleClass[i] = paragraph[i].getParagraphScaleSentiment();
                }
            }
        }

        bgCorpusClassified = true;
    }

    /**
     * 根据情感词汇的变化，将已分类的语料库重新分类.
     *
     * @param iSentimentWordID 情感词汇ID
     * @param iMinParasToContainWord 包含该词的段落数最小阈值
     */
    public void reClassifyClassifiedCorpusForSentimentChange(int iSentimentWordID, int iMinParasToContainWord) {
        if (igParagraphCount == 0) {
            return;
        }
        if (!bSentimentIDListMade) {
            makeSentimentIDListForCompleteCorpusIgnoringSubcorpus();
        }
        int iSentimentWordIDArrayPos = Sort.i_FindIntPositionInSortedArray(iSentimentWordID, igSentimentIDList,
                1, igSentimentIDListCount);
        if (iSentimentWordIDArrayPos == -1 || igSentimentIDParagraphCount[iSentimentWordIDArrayPos]
                < iMinParasToContainWord) {
            return;
        }
        igPosClass = new int[igParagraphCount + 1];
        igNegClass = new int[igParagraphCount + 1];
        if (options.bgTrinaryMode) {
            igTrinaryClass = new int[igParagraphCount + 1];
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                paragraph[i].reClassifyClassifiedParagraphForSentimentChange(iSentimentWordID);
                igPosClass[i] = paragraph[i].getParagraphPositiveSentiment();
                igNegClass[i] = paragraph[i].getParagraphNegativeSentiment();
                if (options.bgTrinaryMode)
                    igTrinaryClass[i] = paragraph[i].getParagraphTrinarySentiment();
                if (options.bgScaleMode)
                    igScaleClass[i] = paragraph[i].getParagraphScaleSentiment();
            }
        }

        bgCorpusClassified = true;
    }

    /**
     * 打印语料库情感得分.
     *
     * @param sOutFilenameAndPath 导出的目标文件路径
     * @return boolean值，表示操作是否成功
     */
    public boolean printCorpusSentimentScores(String sOutFilenameAndPath) {
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        try (BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutFilenameAndPath))){
            wWriter.write("Correct+\tCorrect-\tPredict+\tPredict-\tText\n");
            for (int i = 1; i <= igParagraphCount; i++) {
                if (bgSupcorpusMember[i]) {
                    wWriter.write(String.valueOf(igPosCorrect[i]) + "\t" + igNegCorrect[i] + "\t"
                            + igPosClass[i] + "\t" + igNegClass[i] + "\t" + paragraph[i].getTaggedParagraph() + "\n");
                }
            }
        } catch(IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 计算分类器的正面情感分类准确率.
     *
     * @return 准确率
     */
    public float getClassificationPositiveAccuracyProportion() {
        if (igSupcorpusMemberCount == 0) {
            return 0.0F;
        } else {
            return (float) getClassificationPositiveNumberCorrect() / (float) igSupcorpusMemberCount;
        }
    }

    /**
     * 计算分类器的负面情感分类准确率.
     *
     * @return 准确率
     */
    public float getClassificationNegativeAccuracyProportion() {
        if (igSupcorpusMemberCount == 0) {
            return 0.0F;
        } else {
            return (float) getClassificationNegativeNumberCorrect() / (float) igSupcorpusMemberCount;
        }
    }

    /**
     * 计算负面情感分类精确度基准（底线）值.
     *
     * @return 精确度基准值
     */
    public double getBaselineNegativeAccuracyProportion() {
        if (igParagraphCount == 0) {
            return 0.0D;
        } else {
            return ClassificationStatistics.baselineAccuracyMajorityClassProportion(igNegCorrect, igParagraphCount);
        }
    }

    /**
     * 计算正面情感分类精确度基准（底线）值.
     *
     * @return 精确度基准值
     */
    public double getBaselinePositiveAccuracyProportion() {
        if (igParagraphCount == 0) {
            return 0.0D;
        } else {
            return ClassificationStatistics.baselineAccuracyMajorityClassProportion(igPosCorrect, igParagraphCount);
        }
    }

    /**
     * 计算被正确分类为负面情感的段落数量.
     *
     * @return 被正确分类为负面情感的段落数量
     */
    public int getClassificationNegativeNumberCorrect() {
        if (igParagraphCount == 0) {
            return 0;
        }
        int iMatches = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i] && igNegCorrect[i] == -igNegClass[i]) {
                iMatches++;
            }
        }

        return iMatches;
    }

    /**
     * 计算被正确分类为正面情感的段落数量.
     *
     * @return 被正确分类为正面情感的段落数量
     */
    public int getClassificationPositiveNumberCorrect() {
        if (igParagraphCount == 0) {
            return 0;
        }
        int iMatches = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i] && igPosCorrect[i] == igPosClass[i]) {
                iMatches++;
            }
        }

        return iMatches;
    }


    /**
     * 计算分类器在正面情感分类上的平均误差.
     *
     * @return 平均误差
     */
    public double getClassificationPositiveMeanDifference() {
        if (igParagraphCount == 0) {
            return 0.0D;
        }
        double fTotalDiff = 0.0D;
        int iTotal = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                fTotalDiff += Math.abs(igPosCorrect[i] - igPosClass[i]);
                iTotal++;
            }
        }

        if (iTotal > 0) {
            return fTotalDiff / (double) iTotal;
        } else {
            return 0.0D;
        }
    }

    /**
     * 获取分类器在正面情感分类上的总误差.
     *
     * @return 总误差
     */
    public int getClassificationPositiveTotalDifference() {
        if (igParagraphCount == 0) {
            return 0;
        }
        int iTotalDiff = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                iTotalDiff += Math.abs(igPosCorrect[i] - igPosClass[i]);
            }
        }

        return iTotalDiff;
    }

    /**
     * 计算分类器对语料库进行三元（积极、消极、中性）分类的正确数量，即分类器正确分类的文本数.
     *
     * @return 正确分类的文本数
     */
    public int getClassificationTrinaryNumberCorrect() {
        if (igParagraphCount == 0) {
            return 0;
        }
        int iTrinaryCorrect = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i] && igTrinaryCorrect[i] == igTrinaryClass[i]) {
                iTrinaryCorrect++;
            }
        }

        return iTrinaryCorrect;
    }

    /**
     * 计算分类器在全语料库上的尺度评估相关性（正确尺度评估，全部尺度评估与总段落数之间的关系）.
     *
     * @return 相关性
     */
    public float getClassificationScaleCorrelationWholeCorpus() {
        if (igParagraphCount == 0) {
            return 0.0F;
        } else {
            return (float) ClassificationStatistics.correlation(igScaleCorrect, igScaleClass, igParagraphCount);
        }
    }

    /**
     * 计算分类器的尺度评估正确率.
     *
     * @return 正确率
     */
    public float getClassificationScaleAccuracyProportion() {
        if (igSupcorpusMemberCount == 0) {
            return 0.0F;
        } else {
            return (float) getClassificationScaleNumberCorrect() / (float) igSupcorpusMemberCount;
        }
    }

    /**
     * 计算分类器在全语料库上的正面情感分类相关性（正面情感正确分类，正面情感分类与总段落数之间的关系）.
     *
     * @return 相关性
     */
    public float getClassificationPosCorrelationWholeCorpus() {
        if (igParagraphCount == 0) {
            return 0.0F;
        } else {
            return (float) ClassificationStatistics.correlationAbs(igPosCorrect, igPosClass, igParagraphCount);
        }
    }

    /**
     * 计算分类器在全语料库上的负面情感分类相关性（负面情感正确分类，负面情感分类与总段落数之间的关系）.
     *
     * @return 相关性
     */
    public float getClassificationNegCorrelationWholeCorpus() {
        if (igParagraphCount == 0) {
            return 0.0F;
        } else {
            return (float) ClassificationStatistics.correlationAbs(igNegCorrect, igNegClass, igParagraphCount);
        }
    }

    /**
     * 计算分类器的正确尺度评估数.
     *
     * @return 正确尺度数
     */
    public int getClassificationScaleNumberCorrect() {
        if (igParagraphCount == 0) {
            return 0;
        }
        int iScaleCorrect = 0;
        if(!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i] && igScaleCorrect[i] == igScaleClass[i]) {
                iScaleCorrect++;
            }
        }

        return iScaleCorrect;
    }

    /**
     * 计算分类器在负面情感分类上的总误差.
     *
     * @return 总误差
     */
    public int getClassificationNegativeTotalDifference() {
        if (igParagraphCount == 0) {
            return 0;
        }
        int iTotalDiff = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                iTotalDiff += Math.abs(igNegCorrect[i] + igNegClass[i]);
            }
        }

        return iTotalDiff;
    }

    /**
     * 计算分类器在负面情感分类上的平均误差.
     *
     * @return 平均误差
     */
    public double getClassificationNegativeMeanDifference() {
        if (igParagraphCount == 0) {
            return 0.0D;
        }
        double fTotalDiff = 0.0D;
        int iTotal = 0;
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        for (int i = 1; i <= igParagraphCount; i++) {
            if (bgSupcorpusMember[i]) {
                fTotalDiff += Math.abs(igNegCorrect[i] + igNegClass[i]);
                iTotal++;
            }
        }

        if (iTotal > 0) {
            return fTotalDiff / (double) iTotal;
        } else {
            return 0.0D;
        }
    }

    /**
     * 将分类结果汇总并输出到目标目录文件中.
     *
     * @param sOutFilenameAndPath 输出文件目录
     * @return 输出操作是否成功
     */
    public boolean printClassificationResultsSummary_NOT_DONE(String sOutFilenameAndPath) {
        if (!bgCorpusClassified) {
            calculateCorpusSentimentScores();
        }
        try {
            BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutFilenameAndPath));
            for (int i = 1; i <= igParagraphCount; i++) {
                boolean _tmp = bgSupcorpusMember[i];
            }

            wWriter.close();
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 为全语料库（不考虑子语料库）创建情感ID列表.
     */
    public void makeSentimentIDListForCompleteCorpusIgnoringSubcorpus() {
        igSentimentIDListCount = 0;
        for (int i = 1; i <= igParagraphCount; i++) {
            paragraph[i].makeSentimentIDList();
            if (paragraph[i].getSentimentIDList() != null) {
                igSentimentIDListCount += paragraph[i].getSentimentIDList().length;
            }
        }

        if (igSentimentIDListCount > 0) {
            igSentimentIDList = new int[igSentimentIDListCount + 1];
            igSentimentIDParagraphCount = new int[igSentimentIDListCount + 1];
            igSentimentIDListCount = 0;
            for (int i = 1; i <= igParagraphCount; i++) {
                int[] sentenceIDList = paragraph[i].getSentimentIDList();
                if (sentenceIDList != null) {
                    for (int k : sentenceIDList) {
                        if (k != 0) {
                            igSentimentIDList[++igSentimentIDListCount] = k;
                        }
                    }
                }
            }

            Sort.quickSortInt(igSentimentIDList, 1, igSentimentIDListCount);
            for (int i = 1; i <= igParagraphCount; i++) {
                int[] sentenceIDList = paragraph[i].getSentimentIDList();
                if (sentenceIDList != null) {
                    for (int k : sentenceIDList) {
                        if (k != 0) {
                            igSentimentIDParagraphCount[Sort.i_FindIntPositionInSortedArray(k, igSentimentIDList,
                                    1, igSentimentIDListCount)]++;
                        }
                    }
                }
            }

        }
        bSentimentIDListMade = true;
    }

    /**
     * 多次运行10倍交叉验证.
     *
     * @param iMinImprovement 多次迭代中算法的最小改进幅度
     * @param bUseTotalDifference 多次迭代中是否使用总体差异
     * @param iReplications 10倍交叉验证运行次数
     * @param iMultiOptimisations 优化次数
     * @param sWriter 结果写入器
     * @param wTermStrengthWriter 术语强度写入器
     */
    private void run10FoldCrossValidationMultipleTimes(int iMinImprovement, boolean bUseTotalDifference,
                                                       int iReplications, int iMultiOptimisations,
                                                       BufferedWriter sWriter, BufferedWriter wTermStrengthWriter) {
        for (int i = 1; i <= iReplications; i++) {
            run10FoldCrossValidationOnce(iMinImprovement,
                    bUseTotalDifference, iMultiOptimisations, sWriter, wTermStrengthWriter);
        }
        System.out.println("Set of " + iReplications + " 10-fold cross validations finished");
    }

    /**
     * 多次运行10倍交叉验证.
     *
     * @param iMinImprovement 多次迭代中算法的最小改进幅度
     * @param bUseTotalDifference 多次迭代中是否使用总体差异
     * @param iReplications 10倍交叉验证运行次数
     * @param iMultiOptimisations 优化次数
     * @param sOutFileName 输出文件名
     */
    public void run10FoldCrossValidationMultipleTimes(int iMinImprovement, boolean bUseTotalDifference,
                                                      int iReplications, int iMultiOptimisations, String sOutFileName) {
        try (BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutFileName));
             BufferedWriter wTermStrengthWriter = new BufferedWriter(new FileWriter(String.valueOf(FileOps
                .s_ChopFileNameExtension(sOutFileName)) + "_termStrVars.txt"))) {
            options.printClassificationOptionsHeadings(wWriter);
            writeClassificationStatsHeadings(wWriter);
            options.printClassificationOptionsHeadings(wTermStrengthWriter);
            resources.sentimentWords.printSentimentTermsInSingleHeaderRow(wTermStrengthWriter);
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference, iReplications,
                    iMultiOptimisations, wWriter, wTermStrengthWriter);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 对待读取文本进行情感分析并根据ID进行记录.
     *
     * @param sInputFile 输入文件路径
     * @param iTextCol 待分析文本列数
     * @param iIDCol ID列数
     * @param sOutputFile 输出文件路径
     */
    public void classifyAllLinesAndRecordWithID(String sInputFile, int iTextCol, int iIDCol, String sOutputFile) {
        int iPos = 0;
        int iNeg = 0;
        int iTrinary = -3;
        int iScale = -10;
        int iCount1 = 0;
        String sLine = "";
        try (BufferedReader rReader = new BufferedReader(new FileReader(sInputFile));
             BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile))){
            while (rReader.ready()) {
                sLine = rReader.readLine();
                iCount1++;
                if (!Objects.equals(sLine, "")) {
                    String[] sData = sLine.split("\t");
                    if (sData.length > iTextCol && sData.length > iIDCol) {
                        Paragraph paragraph = new Paragraph();
                        paragraph.setParagraph(sData[iTextCol], resources, options);
                        if (options.bgTrinaryMode) {
                            iTrinary = paragraph.getParagraphTrinarySentiment();
                            wWriter.write(String.valueOf(sData[iIDCol]) + "\t" + iTrinary + "\n");
                        } else if (options.bgScaleMode) {
                            iScale = paragraph.getParagraphScaleSentiment();
                            wWriter.write(String.valueOf(sData[iIDCol]) + "\t" + iScale + "\n");
                        } else {
                            iPos = paragraph.getParagraphPositiveSentiment();
                            iNeg = paragraph.getParagraphNegativeSentiment();
                            wWriter.write(String.valueOf(sData[iIDCol]) + "\t" + iPos + "\t" + iNeg + "\n");
                        }
                    }
                }
            }
            Thread.sleep(10L);
            if (rReader.ready()) {
                System.out.println("Reader ready again after pause!");
            }
            int character;
            if ((character = rReader.read()) != -1) {
                System.out.println("Reader returns char after reader.read() false! " + character);
            }
        }
        catch (FileNotFoundException e) {
            String error = "Could not find input file: " + sInputFile;
            LOG.error(error);
            e.printStackTrace();
        }
        catch (IOException e) {
            String error = "Error reading or writing from file: " + sInputFile;
            System.out.println(error);
            e.printStackTrace();
        }
        catch(Exception e) {
            String error = "Error reading from or writing to file: " + sInputFile;
            System.out.println(error);
            e.printStackTrace();
        }
        System.out.println("Processed " + iCount1 + " lines from file: " + sInputFile + ". Last line was:\n" + sLine);
    }

    /**
     * 对待读取文本逐行标注情感分析结果.
     *
     * @param sInputFile 输入文件路径
     * @param iTextCol 待标注文本列数
     */
    public void annotateAllLinesInInputFile(String sInputFile, int iTextCol) {
        int iPos = 0;
        int iNeg = 0;
        int iTrinary = -3;
        int iScale = -10;
        String sTempFile = String.valueOf(sInputFile) + "_temp";
        try (BufferedReader rReader = new BufferedReader(new FileReader(sInputFile));
             BufferedWriter wWriter = new BufferedWriter(new FileWriter(sTempFile))){
            while (rReader.ready()) {
                String sLine = rReader.readLine();
                if(!Objects.equals(sLine, "")) {
                    String[] sData = sLine.split("\t");
                    if(sData.length > iTextCol) {
                        Paragraph paragraph = new Paragraph();
                        paragraph.setParagraph(sData[iTextCol], resources, options);
                        if(options.bgTrinaryMode) {
                            iTrinary = paragraph.getParagraphTrinarySentiment();
                            wWriter.write(String.valueOf(sLine) + "\t" + iTrinary + "\n");
                        } else if(options.bgScaleMode) {
                            iScale = paragraph.getParagraphScaleSentiment();
                            wWriter.write(String.valueOf(sLine) + "\t" + iScale + "\n");
                        } else {
                            iPos = paragraph.getParagraphPositiveSentiment();
                            iNeg = paragraph.getParagraphNegativeSentiment();
                            wWriter.write(String.valueOf(sLine) + "\t" + iPos + "\t" + iNeg + "\n");
                        }
                    } else {
                        wWriter.write(String.valueOf(sLine) + "\n");
                    }
                }
            }
            File original = new File(sInputFile);
            original.delete();
            File newFile = new File(sTempFile);
            newFile.renameTo(new File(sInputFile));
        }
        catch (FileNotFoundException e) {
            String error = "Could not find input file: " + sInputFile;
            LOG.error(error);
            e.printStackTrace();
        }
        catch(IOException e) {
            String error = "Error reading or writing from file: " + sInputFile;
            LOG.error(error);
            e.printStackTrace();
        }
        catch(Exception e) {
            String error = "Error reading from or writing to file: " + sInputFile;
            LOG.error(error);
            e.printStackTrace();
        }
    }

    /**
     * 对待读取文本朱行进行情感分类.
     *
     * @param sInputFile 输入文件路径
     * @param iTextCol 待分析文本列数
     * @param sOutputFile 输出文件路径
     */
    public void classifyAllLinesInInputFile(String sInputFile, int iTextCol, String sOutputFile) throws NumberFormatException {
        int iPos = 0;
        int iNeg = 0;
        int iTrinary = -3;
        int iScale = -10;
        int iFileTrinary = -2;
        int iFileScale = -9;
        int iClassified = 0;
        int iCorrectPosCount = 0;
        int iCorrectNegCount = 0;
        int iCorrectTrinaryCount = 0;
        int iCorrectScaleCount = 0;
        int iPosAbsDiff = 0;
        int iNegAbsDiff = 0;
        int[][] confusion = {
                new int[3], new int[3], new int[3]
        };
        int maxClassifyForCorrelation = 20000;
        int[] iPosClassCorr = new int[maxClassifyForCorrelation];
        int[] iNegClassCorr = new int[maxClassifyForCorrelation];
        int[] iPosClassPred = new int[maxClassifyForCorrelation];
        int[] iNegClassPred = new int[maxClassifyForCorrelation];
        int[] iScaleClassCorr = new int[maxClassifyForCorrelation];
        int[] iScaleClassPred = new int[maxClassifyForCorrelation];
        String sRationale = "";
        String sOutput = "";
        try {
            BufferedReader rReader;
            BufferedWriter wWriter;
            if(options.bgForceUTF8) {
                wWriter = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(sOutputFile),
                        StandardCharsets.UTF_8));
                rReader = new BufferedReader(new InputStreamReader(new FileInputStream(sInputFile),
                        StandardCharsets.UTF_8));
            } else {
                wWriter = new BufferedWriter(new FileWriter(sOutputFile));
                rReader = new BufferedReader(new FileReader(sInputFile));
            }
            if(options.bgTrinaryMode || options.bgScaleMode) {
                wWriter.write("Overall\tText");
            } else if(options.bgTensiStrength) {
                wWriter.write("Relax\tStress\tText");
            } else {
                wWriter.write("Positive\tNegative\tText");
            }
            if (options.bgExplainClassification) {
                wWriter.write("\tExplanation\n");
            } else {
                wWriter.write("\n");
            }
            while (rReader.ready()) {
                String sLine = rReader.readLine();
                if (!Objects.equals(sLine, "")) {
                    int iTabPos = sLine.lastIndexOf("\t");
                    int iFilePos = 0;
                    int iFileNeg = 0;
                    if(iTabPos >= 0) {
                        String[] sData = sLine.split("\t");
                        if(sData.length > 1) {
                            if (iTextCol > -1) {
                                wWriter.write(sLine + "\t");
                                if (iTextCol < sData.length) {
                                    sLine = sData[iTextCol];
                                }
                            } else if (options.bgTrinaryMode) {
                                iFileTrinary = -2;
                                try {
                                    iFileTrinary = Integer.parseInt(sData[0].trim());
                                    if (iFileTrinary > 1 || iFileTrinary < -1) {
                                        System.out.println("Invalid trinary sentiment " + iFileTrinary
                                                + " (expected -1,0,1) at line: " + sLine);
                                        iFileTrinary = 0;
                                    }
                                } catch (NumberFormatException ignored) {

                                }
                            } else if (options.bgScaleMode) {
                                iFileScale = -9;
                                iFileScale = Integer.parseInt(sData[0].trim());
                                if (iFileScale > 4 || iFileScale < -4) {
                                    System.out.println("Invalid overall sentiment " + iFileScale
                                            + " (expected -4 to +4) at line: " + sLine);
                                    iFileScale = 0;
                                }
                            } else {
                                try {
                                    iFilePos = Integer.parseInt(sData[0].trim());
                                    iFileNeg = Integer.parseInt(sData[1].trim());
                                    if (iFileNeg < 0) {
                                        iFileNeg = -iFileNeg;
                                    }
                                } catch (NumberFormatException ignored) {
                                }
                            }
                        }
                        sLine = sLine.substring(iTabPos + 1);
                    }
                    Paragraph paragraphSingleton = new Paragraph();
                    paragraphSingleton.setParagraph(sLine, resources, options);
                    if (options.bgTrinaryMode) {
                        iTrinary = paragraphSingleton.getParagraphTrinarySentiment();
                        if (options.bgExplainClassification) {
                            sRationale = "\t" + paragraphSingleton.getClassificationRationale();
                        }
                        sOutput = String.valueOf(iTrinary) + "\t" + sLine + sRationale + "\n";
                    } else if(options.bgScaleMode) {
                        iScale = paragraphSingleton.getParagraphScaleSentiment();
                        if (options.bgExplainClassification) {
                            sRationale = "\t" + paragraphSingleton.getClassificationRationale();
                        }
                        sOutput = String.valueOf(iScale) + "\t" + sLine + sRationale + "\n";
                    } else {
                        iPos = paragraphSingleton.getParagraphPositiveSentiment();
                        iNeg = paragraphSingleton.getParagraphNegativeSentiment();
                        if (options.bgExplainClassification) {
                            sRationale = "\t" + paragraphSingleton.getClassificationRationale();
                        }
                        sOutput = String.valueOf(iPos) + "\t" + iNeg + "\t" + sLine + sRationale + "\n";
                    }
                    wWriter.write(sOutput);
                    if (options.bgTrinaryMode) {
                        if (iFileTrinary > -2 && iFileTrinary < 2 && iTrinary > -2 && iTrinary < 2) {
                            iClassified++;
                            if (iFileTrinary == iTrinary) {
                                iCorrectTrinaryCount++;
                            }
                            confusion[iTrinary + 1][iFileTrinary + 1]++;
                        }
                    } else if (options.bgScaleMode) {
                        if (iFileScale > -9) {
                            iClassified++;
                            if (iFileScale == iScale) {
                                iCorrectScaleCount++;
                            }
                            if (iClassified < maxClassifyForCorrelation) {
                                iScaleClassCorr[iClassified] = iFileScale;
                            }
                            iScaleClassPred[iClassified] = iScale;
                        }
                    } else if (iFileNeg != 0) {
                        iClassified++;
                        if (iPos == iFilePos) {
                            iCorrectPosCount++;
                        }
                        iPosAbsDiff += Math.abs(iPos - iFilePos);
                        if (iClassified < maxClassifyForCorrelation) {
                            iNegClassCorr[iClassified] = iFileNeg;
                            iPosClassCorr[iClassified] = iFilePos;
                        }
                        iPosClassPred[iClassified] = iPos;
                        if (iNeg == -iFileNeg) {
                            iCorrectNegCount++;
                        }
                        iNegAbsDiff += Math.abs(iNeg + iFileNeg);
                        iNegClassPred[iClassified] = iNeg;
                    }
                }
            }
            rReader.close();
            wWriter.close();
            if (iClassified > 0) {
                if (options.bgTrinaryMode) {
                    System.out.println("Trinary correct: " + iCorrectTrinaryCount + " ("
                            + ((float) iCorrectTrinaryCount / (float) iClassified) * 100F + "%).");
                    System.out.println("Correct -> -1   0   1");
                    System.out.println("Est = -1   " + confusion[0][0] + " " + confusion[0][1] + " " + confusion[0][2]);
                    System.out.println("Est =  0   " + confusion[1][0] + " " + confusion[1][1] + " " + confusion[1][2]);
                    System.out.println("Est =  1   " + confusion[2][0] + " " + confusion[2][1] + " " + confusion[2][2]);
                } else if (options.bgScaleMode) {
                    System.out.println("Scale correct: " + iCorrectScaleCount + " (" + ((float) iCorrectScaleCount
                            / (float) iClassified) * 100F + "%) out of " + iClassified);
                    System.out.println("  Correlation: " + ClassificationStatistics.correlation(iScaleClassCorr,
                            iScaleClassPred, iClassified));
                } else {
                    System.out.print(String.valueOf(options.sgProgramPos) + " correct: " + iCorrectPosCount
                            + " (" + ((float) iCorrectPosCount / (float) iClassified) * 100F + "%).");
                    System.out.println(" Mean abs diff: " + (float) iPosAbsDiff / (float) iClassified);
                    if (iClassified < maxClassifyForCorrelation) {
                        System.out.println(" Correlation: " + ClassificationStatistics.correlationAbs(iPosClassCorr,
                                iPosClassPred, iClassified));
                        int corrWithin1 = ClassificationStatistics.accuracyWithin1(iPosClassCorr, iPosClassPred,
                                iClassified, false);
                        System.out.println(" Correct +/- 1: " + corrWithin1 + " (" + (float) (100 * corrWithin1)
                                / (float) iClassified + "%)");
                    }
                    System.out.print(String.valueOf(options.sgProgramNeg) + " correct: " + iCorrectNegCount
                            + " (" + ((float) iCorrectNegCount / (float) iClassified) * 100F + "%).");
                    System.out.println(" Mean abs diff: " + (float) iNegAbsDiff / (float) iClassified);
                    if (iClassified < maxClassifyForCorrelation) {
                        System.out.println(" Correlation: " + ClassificationStatistics.correlationAbs(iNegClassCorr,
                                iNegClassPred, iClassified));
                        int corrWithin1 = ClassificationStatistics.accuracyWithin1(iNegClassCorr, iNegClassPred,
                                iClassified, true);
                        System.out.println(" Correct +/- 1: " + corrWithin1 + " (" + (float) (100 * corrWithin1)
                                / (float) iClassified + "%)");
                    }
                }
            }
        }
        catch (FileNotFoundException e) {
            String error = "Could not find input file: " + sInputFile;
            LOG.error(error);
            e.printStackTrace();
        }
        catch (IOException e) {
            String error = "Error reading from input file: " + sInputFile + " or writing to output file "
                    + sOutputFile;
            LOG.error(error);
            e.printStackTrace();
        }
    }


    /**
     * 在文本文件中写入分类统计信息的标题行.
     *
     * @param w 待写入文件的输出流
     */
    private void writeClassificationStatsHeadings(BufferedWriter w) throws IOException {
        String sPosOrScale;
        if (options.bgScaleMode) {
            sPosOrScale = "ScaleCorrel";
        } else {
            sPosOrScale = "PosCorrel";
        }
        w.write("\tPosCorrect\tiPosCorrect/Total\tNegCorrect\tNegCorrect/Total" +
                "\tPosWithin1\tPosWithin1/Total\tNegWithin1\tNegWithin1/Total\t" + sPosOrScale +
                "\tNegCorrel" + "\tPosMPE\tNegMPE\tPosMPEnoDiv\tNegMPEnoDiv" + "\tTrinaryOrScaleCorrect" +
                "\tTrinaryOrScaleCorrect/TotalClassified" + "\tTrinaryOrScaleCorrectWithin1" +
                "\tTrinaryOrScaleCorrectWithin1/TotalClassified" + "\test-1corr-1\test-1corr0\test-1corr1" +
                "\test0corr-1\test0corr0\test0corr1" + "\test1corr-1\test1corr0\test1corr1" + "\tTotalClassified\n");
    }

    /**
     * 为所有情感分析类型进行10倍交叉验证.
     *
     * @param iMinImprovement 多次迭代中算法的最小改进幅度
     * @param bUseTotalDifference 多次迭代中是否使用总体差异
     * @param iReplications 10倍交叉验证运行次数
     * @param iMultiOptimisations 优化次数
     * @param sOutFileName 输出文件路径
     */
    public void run10FoldCrossValidationForAllOptionVariations(int iMinImprovement, boolean bUseTotalDifference,
                                                               int iReplications, int iMultiOptimisations,
                                                               String sOutFileName) {
        try {
            BufferedWriter wResultsWriter = new BufferedWriter(new FileWriter(sOutFileName));
            BufferedWriter wTermStrengthWriter = new BufferedWriter(new FileWriter(
                    String.valueOf(FileOps.s_ChopFileNameExtension(sOutFileName)) + "_termStrVars.txt"));
            if (igPosClass == null || igPosClass.length < igPosCorrect.length) {
                igPosClass = new int[igParagraphCount + 1];
                igNegClass = new int[igParagraphCount + 1];
                igTrinaryClass = new int[igParagraphCount + 1];
            }
            options.printClassificationOptionsHeadings(wResultsWriter);
            writeClassificationStatsHeadings(wResultsWriter);
            options.printClassificationOptionsHeadings(wTermStrengthWriter);
            resources.sentimentWords.printSentimentTermsInSingleHeaderRow(wTermStrengthWriter);
            LOG.error("About to start classifications for 20 different option variations");
            if (options.bgTrinaryMode) {
                ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igTrinaryCorrect,
                        igTrinaryClass, igParagraphCount, false);
            } else if (options.bgScaleMode) {
                ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igScaleCorrect,
                        igScaleClass, igParagraphCount, false);
            } else {
                ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igPosCorrect,
                        igPosClass, igParagraphCount, false);
                ClassificationStatistics.baselineAccuracyMakeLargestClassPrediction(igNegCorrect,
                        igNegClass, igParagraphCount, true);
            }
            options.printBlankClassificationOptions(wResultsWriter);
            if (options.bgTrinaryMode) {
                printClassificationResultsRow(igPosClass, igNegClass, igTrinaryClass, wResultsWriter);
            } else {
                printClassificationResultsRow(igPosClass, igNegClass, igScaleClass, wResultsWriter);
            }
            options.printClassificationOptions(wResultsWriter, igParagraphCount, bUseTotalDifference,
                    iMultiOptimisations);
            calculateCorpusSentimentScores();
            if (options.bgTrinaryMode) {
                printClassificationResultsRow(igPosClass, igNegClass, igTrinaryClass, wResultsWriter);
            } else {
                printClassificationResultsRow(igPosClass, igNegClass, igScaleClass, wResultsWriter);
            }
            options.printBlankClassificationOptions(wTermStrengthWriter);
            resources.sentimentWords.printSentimentValuesInSingleRow(wTermStrengthWriter);
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.igEmotionParagraphCombineMethod = 1 - options.igEmotionParagraphCombineMethod;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.igEmotionParagraphCombineMethod = 1 - options.igEmotionParagraphCombineMethod;
            options.igEmotionSentenceCombineMethod = 1 - options.igEmotionSentenceCombineMethod;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.igEmotionSentenceCombineMethod = 1 - options.igEmotionSentenceCombineMethod;
            options.bgReduceNegativeEmotionInQuestionSentences = !options.bgReduceNegativeEmotionInQuestionSentences;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgReduceNegativeEmotionInQuestionSentences = !options.bgReduceNegativeEmotionInQuestionSentences;
            options.bgMissCountsAsPlus2 = !options.bgMissCountsAsPlus2;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgMissCountsAsPlus2 = !options.bgMissCountsAsPlus2;
            options.bgYouOrYourIsPlus2UnlessSentenceNegative = !options.bgYouOrYourIsPlus2UnlessSentenceNegative;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgYouOrYourIsPlus2UnlessSentenceNegative = !options.bgYouOrYourIsPlus2UnlessSentenceNegative;
            options.bgExclamationInNeutralSentenceCountsAsPlus2 = !options.bgExclamationInNeutralSentenceCountsAsPlus2;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgExclamationInNeutralSentenceCountsAsPlus2 = !options.bgExclamationInNeutralSentenceCountsAsPlus2;
            options.bgUseIdiomLookupTable = !options.bgUseIdiomLookupTable;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgUseIdiomLookupTable = !options.bgUseIdiomLookupTable;
            int iTemp = options.igMoodToInterpretNeutralEmphasis;
            options.igMoodToInterpretNeutralEmphasis = -options.igMoodToInterpretNeutralEmphasis;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.igMoodToInterpretNeutralEmphasis = 0;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.igMoodToInterpretNeutralEmphasis = iTemp;
            String error = "About to start 10th option variation classification";
            LOG.error(error);
            options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion =
                    !options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion =
                    !options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion;
            options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion =
                    !options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion =
                    !options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion;
            options.bgIgnoreBoosterWordsAfterNegatives = !options.bgIgnoreBoosterWordsAfterNegatives;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgIgnoreBoosterWordsAfterNegatives = !options.bgIgnoreBoosterWordsAfterNegatives;
            options.bgMultipleLettersBoostSentiment = !options.bgMultipleLettersBoostSentiment;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgMultipleLettersBoostSentiment = !options.bgMultipleLettersBoostSentiment;
            options.bgBoosterWordsChangeEmotion = !options.bgBoosterWordsChangeEmotion;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgBoosterWordsChangeEmotion = !options.bgBoosterWordsChangeEmotion;
            if (options.bgNegatingWordsFlipEmotion) {
                options.bgNegatingWordsFlipEmotion = !options.bgNegatingWordsFlipEmotion;
                run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                        iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
                options.bgNegatingWordsFlipEmotion = !options.bgNegatingWordsFlipEmotion;
            } else {
                options.bgNegatingPositiveFlipsEmotion = !options.bgNegatingPositiveFlipsEmotion;
                run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                        iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
                options.bgNegatingPositiveFlipsEmotion = !options.bgNegatingPositiveFlipsEmotion;
                options.bgNegatingNegativeNeutralisesEmotion = !options.bgNegatingNegativeNeutralisesEmotion;
                run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                        iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
                options.bgNegatingNegativeNeutralisesEmotion = !options.bgNegatingNegativeNeutralisesEmotion;
            }
            options.bgCorrectSpellingsWithRepeatedLetter = !options.bgCorrectSpellingsWithRepeatedLetter;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgCorrectSpellingsWithRepeatedLetter = !options.bgCorrectSpellingsWithRepeatedLetter;
            options.bgUseEmoticons = !options.bgUseEmoticons;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgUseEmoticons = !options.bgUseEmoticons;
            options.bgCapitalsBoostTermSentiment = !options.bgCapitalsBoostTermSentiment;
            run10FoldCrossValidationMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            options.bgCapitalsBoostTermSentiment = !options.bgCapitalsBoostTermSentiment;
            if (iMinImprovement > 1) {
                run10FoldCrossValidationMultipleTimes(iMinImprovement - 1,
                        bUseTotalDifference, iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            }
            run10FoldCrossValidationMultipleTimes(iMinImprovement + 1, bUseTotalDifference,
                    iReplications, iMultiOptimisations, wResultsWriter, wTermStrengthWriter);
            wResultsWriter.close();
            wTermStrengthWriter.close();
            SummariseMultiple10FoldValidations(sOutFileName, sOutFileName + "_sum.txt");
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 进行一次10倍交叉验证.
     *
     * @param iMinImprovement 多次迭代中算法的最小改进幅度
     * @param bUseTotalDifference 多次迭代中是否使用总体差异
     * @param iMultiOptimisations 优化次数
     * @param wWriter 结果写入器
     * @param wTermStrengthWriter 术语强度写入器
     */
    private void run10FoldCrossValidationOnce(int iMinImprovement, boolean bUseTotalDifference, int iMultiOptimisations,
                                              BufferedWriter wWriter, BufferedWriter wTermStrengthWriter) {
        int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
        int[] iParagraphRand = new int[igParagraphCount + 1];
        int[] iPosClassAll = new int[igParagraphCount + 1];
        int[] iNegClassAll = new int[igParagraphCount + 1];
        int[] iTrinaryOrScaleClassAll = new int[igParagraphCount + 1];
        int iTotalClassified = 0;
        Sort.makeRandomOrderList(iParagraphRand);
        int[] iOriginalSentimentStrengths = new int[iTotalSentimentWords + 1];
        for (int i = 1; i < iTotalSentimentWords; i++) {
            iOriginalSentimentStrengths[i] = resources.sentimentWords.getSentiment(i);
        }

        for (int iFold = 1; iFold <= 10; iFold++) {
            selectDecileAsSubcorpus(iParagraphRand, iFold, true);
            reCalculateCorpusSentimentScores();
            optimiseDictionaryWeightingsForCorpusMultipleTimes(iMinImprovement, bUseTotalDifference,
                    iMultiOptimisations);
            options.printClassificationOptions(wTermStrengthWriter, iMinImprovement, bUseTotalDifference,
                    iMultiOptimisations);
            resources.sentimentWords.printSentimentValuesInSingleRow(wTermStrengthWriter);
            selectDecileAsSubcorpus(iParagraphRand, iFold, false);
            reCalculateCorpusSentimentScores();
            for (int i = 1; i <= igParagraphCount; i++) {
                if (bgSupcorpusMember[i]) {
                    iPosClassAll[i] = igPosClass[i];
                    iNegClassAll[i] = igNegClass[i];
                    if (options.bgTrinaryMode) {
                        iTrinaryOrScaleClassAll[i] = igTrinaryClass[i];
                    } else {
                        iTrinaryOrScaleClassAll[i] = igScaleClass[i];
                    }
                }
            }

            iTotalClassified += igSupcorpusMemberCount;
            for (int i = 1; i < iTotalSentimentWords; i++) {
                resources.sentimentWords.setSentiment(i, iOriginalSentimentStrengths[i]);
            }

        }

        useWholeCorpusNotSubcorpus();
        options.printClassificationOptions(wWriter, iMinImprovement, bUseTotalDifference, iMultiOptimisations);
        printClassificationResultsRow(iPosClassAll, iNegClassAll, iTrinaryOrScaleClassAll, wWriter);
    }

    /**
     * 逐行打印分类结果.
     *
     * @param iPosClassAll 全体正面情感
     * @param iNegClassAll 全体负面情感
     * @param iTrinaryOrScaleClassAll 全体二元或三元分类
     * @param wWriter 写入器
     * @return 操作是否成功
     */
    private boolean printClassificationResultsRow (int[] iPosClassAll, int[] iNegClassAll,
                                                  int[] iTrinaryOrScaleClassAll, BufferedWriter wWriter) {
        int iPosCorrect = -1;
        int iNegCorrect = -1;
        int iPosWithin1 = -1;
        int iNegWithin1 = -1;
        int iTrinaryCorrect = -1;
        int iTrinaryCorrectWithin1 = -1;
        double fPosCorrectPoportion = -1D;
        double fNegCorrectPoportion = -1D;
        double fPosWithin1Poportion = -1D;
        double fNegWithin1Poportion = -1D;
        double fTrinaryCorrectPoportion = -1D;
        double fTrinaryCorrectWithin1Poportion = -1D;
        double fPosOrScaleCorr = 9999D;
        double fNegCorr = 9999D;
        double fPosMPE = 9999D;
        double fNegMPE = 9999D;
        double fPosMPEnoDiv = 9999D;
        double fNegMPEnoDiv = 9999D;
        int[][] estCorr = {
                new int[3], new int[3], new int[3]
        };
        try {
            if (options.bgTrinaryMode) {
                iTrinaryCorrect = ClassificationStatistics.accuracy(igTrinaryCorrect, iTrinaryOrScaleClassAll,
                        igParagraphCount, false);
                iTrinaryCorrectWithin1 = ClassificationStatistics.accuracyWithin1(igTrinaryCorrect, iTrinaryOrScaleClassAll,
                        igParagraphCount, false);
                fTrinaryCorrectPoportion = (float)iTrinaryCorrect / (float)igParagraphCount;
                fTrinaryCorrectWithin1Poportion = (float)iTrinaryCorrectWithin1 / (float)igParagraphCount;
                ClassificationStatistics.TrinaryOrBinaryConfusionTable(iTrinaryOrScaleClassAll,
                        igTrinaryCorrect, igParagraphCount, estCorr);
            } else if (options.bgScaleMode) {
                iTrinaryCorrect = ClassificationStatistics.accuracy(igScaleCorrect, iTrinaryOrScaleClassAll,
                        igParagraphCount, false);
                iTrinaryCorrectWithin1 = ClassificationStatistics.accuracyWithin1(igScaleCorrect,
                        iTrinaryOrScaleClassAll,
                        igParagraphCount, false);
                fTrinaryCorrectPoportion = (float)iTrinaryCorrect / (float)igParagraphCount;
                fTrinaryCorrectWithin1Poportion = (float)iTrinaryCorrectWithin1 / (float)igParagraphCount;
                fPosOrScaleCorr = ClassificationStatistics.correlation(igScaleCorrect, iTrinaryOrScaleClassAll,
                        igParagraphCount);
            } else {
                iPosCorrect = ClassificationStatistics.accuracy(igPosCorrect, iPosClassAll,
                        igParagraphCount, false);
                iNegCorrect = ClassificationStatistics.accuracy(igNegCorrect, iNegClassAll,
                        igParagraphCount, true);
                iPosWithin1 = ClassificationStatistics.accuracyWithin1(igPosCorrect, iPosClassAll,
                        igParagraphCount, false);
                iNegWithin1 = ClassificationStatistics.accuracyWithin1(igNegCorrect, iNegClassAll,
                        igParagraphCount, true);
                fPosOrScaleCorr = ClassificationStatistics.correlationAbs(igPosCorrect, iPosClassAll, igParagraphCount);
                fNegCorr = ClassificationStatistics.correlationAbs(igNegCorrect, iNegClassAll, igParagraphCount);
                fPosMPE = ClassificationStatistics.absoluteMeanPercentageError(igPosCorrect, iPosClassAll,
                        igParagraphCount, false);
                fNegMPE = ClassificationStatistics.absoluteMeanPercentageError(igNegCorrect, iNegClassAll,
                        igParagraphCount, true);
                fPosMPEnoDiv = ClassificationStatistics.absoluteMeanPercentageErrorNoDivision(igPosCorrect,
                        iPosClassAll, igParagraphCount, false);
                fNegMPEnoDiv = ClassificationStatistics.absoluteMeanPercentageErrorNoDivision(igNegCorrect,
                        iNegClassAll, igParagraphCount, true);
                fPosCorrectPoportion = (float)iPosCorrect / (float)igParagraphCount;
                fNegCorrectPoportion = (float)iNegCorrect / (float)igParagraphCount;
                fPosWithin1Poportion = (float)iPosWithin1 / (float)igParagraphCount;
                fNegWithin1Poportion = (float)iNegWithin1 / (float)igParagraphCount;
            }
            wWriter.write("\t" + iPosCorrect + "\t" + fPosCorrectPoportion + "\t" + iNegCorrect + "\t"
                    + fNegCorrectPoportion + "\t" + iPosWithin1 + "\t" + fPosWithin1Poportion + "\t" + iNegWithin1
                    + "\t" + fNegWithin1Poportion + "\t" + fPosOrScaleCorr + "\t" + fNegCorr + "\t" + fPosMPE + "\t"
                    + fNegMPE + "\t" + fPosMPEnoDiv + "\t" + fNegMPEnoDiv + "\t" + iTrinaryCorrect + "\t"
                    + fTrinaryCorrectPoportion + "\t" + iTrinaryCorrectWithin1 + "\t" + fTrinaryCorrectWithin1Poportion
                    + "\t" + estCorr[0][0] + "\t" + estCorr[0][1] + "\t" + estCorr[0][2] + "\t" + estCorr[1][0] + "\t"
                    + estCorr[1][1] + "\t" + estCorr[1][2] + "\t" + estCorr[2][0] + "\t" + estCorr[2][1] + "\t"
                    + estCorr[2][2] + "\t" + igParagraphCount + "\n");
        }
        catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * 选择特定十分位范围内的段落作为子语料库.
     *
     * @param iParagraphRand 一组段落的随机排序数组
     * @param iDecile 所选的十分位数
     * @param bInvert 是否对选择结果进行取反
     */
    private void selectDecileAsSubcorpus (int[] iParagraphRand, int iDecile, boolean bInvert) {
        if (igParagraphCount == 0) {
            return;
        }
        int iMin = (int)((igParagraphCount / 10F) * (iDecile - 1)) + 1;
        int iMax = (int)((igParagraphCount / 10F) * iDecile);
        if (iDecile == 10) {
            iMax = igParagraphCount;
        }
        if (iDecile == 0) {
            iMin = 0;
        }
        igSupcorpusMemberCount = 0;
        for (int i = 1; i <= igParagraphCount; i++) {
            if (i >= iMin && i <= iMax) {
                bgSupcorpusMember[iParagraphRand[i]] = !bInvert;
                if (!bInvert) {
                    igSupcorpusMemberCount++;
                }
            } else {
                bgSupcorpusMember[iParagraphRand[i]] = bInvert;
                if (bInvert) {
                    igSupcorpusMemberCount++;
                }
            }
        }
    }

    /**
     * 对字典的权重进行多次优化.
     *
     * @param iMinImprovement 要达到的最小改进值
     * @param bUseTotalDifference 是否使用总差异来计算改进值
     * @param iOptimisationTotal 要对语料库进行多少次优化
     */
    public void optimiseDictionaryWeightingsForCorpusMultipleTimes(int iMinImprovement,
                                                                   boolean bUseTotalDifference,
                                                                   int iOptimisationTotal) {
        if (iOptimisationTotal < 1) {
            return;
        }
        if (iOptimisationTotal == 1) {
            optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
            return;
        }
        int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
        int[] iOriginalSentimentStrengths = new int[iTotalSentimentWords + 1];
        for (int j = 1; j <= iTotalSentimentWords; j++) {
            iOriginalSentimentStrengths[j] = resources.sentimentWords.getSentiment(j);
        }

        int[] iTotalWeight = new int[iTotalSentimentWords + 1];
        for (int j = 1; j <= iTotalSentimentWords; j++) {
            iTotalWeight[j] = 0;
        }

        for (int i = 0; i < iOptimisationTotal; i++) {
            optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
            for (int j = 1; j <= iTotalSentimentWords; j++) {
                iTotalWeight[j] += resources.sentimentWords.getSentiment(j);
            }

            for (int j = 1; j <= iTotalSentimentWords; j++) {
                resources.sentimentWords.setSentiment(j, iOriginalSentimentStrengths[j]);
            }
        }

        for (int j = 1; j <= iTotalSentimentWords; j++) {
            resources.sentimentWords.setSentiment(j, (int) (((float) iTotalWeight[j] /
                    (float) iOptimisationTotal) + 0.5D));
        }
        optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
    }

    /**
     * 对字典的权重进行一次优化.
     *
     * @param iMinImprovement 要达到的最小改进值
     * @param bUseTotalDifference 是否使用总差异来计算改进值
     */
    public void optimiseDictionaryWeightingsForCorpus (int iMinImprovement, boolean bUseTotalDifference) {
        if (options.bgTrinaryMode) {
            optimiseDictionaryWeightingsForCorpusTrinaryOrBinary(iMinImprovement);
        } else if (options.bgScaleMode) {
            optimiseDictionaryWeightingsForCorpusScale(iMinImprovement);
        } else {
            optimiseDictionaryWeightingsForCorpusPosNeg(iMinImprovement, bUseTotalDifference);
        }
    }

    /**
     * 对字典的权重（单一正负刻度）进行一次优化.
     *
     * @param iMinImprovement 要达到的最小改进值
     */
    public void optimiseDictionaryWeightingsForCorpusScale (int iMinImprovement) {
        boolean bFullListChanges = true;
        int iLastScaleNumberCorrect = getClassificationScaleNumberCorrect();
        int iNewScaleNumberCorrect = 0;
        int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
        int[] iWordRand = new int[iTotalSentimentWords + 1];
        while (bFullListChanges) {
            Sort.makeRandomOrderList(iWordRand);
            bFullListChanges = false;
            for (int i = 1; i <= iTotalSentimentWords; i++) {
                int iOldTermSentimentStrength = resources.sentimentWords.getSentiment(iWordRand[i]);
                boolean bCurrentIDChange = false;
                int iAddOneImprovement = 0;
                int iSubtractOneImprovement = 0;
                if (iOldTermSentimentStrength < 4) {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldTermSentimentStrength + 1);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                    iNewScaleNumberCorrect = getClassificationScaleNumberCorrect();
                    iAddOneImprovement = iNewScaleNumberCorrect - iLastScaleNumberCorrect;
                    if (iAddOneImprovement >= iMinImprovement) {
                        bCurrentIDChange = true;
                        iLastScaleNumberCorrect += iAddOneImprovement;
                    }
                }
                if (iOldTermSentimentStrength > -4 && !bCurrentIDChange) {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldTermSentimentStrength - 1);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                    iNewScaleNumberCorrect = getClassificationScaleNumberCorrect();
                    iSubtractOneImprovement = iNewScaleNumberCorrect - iLastScaleNumberCorrect;
                    if (iSubtractOneImprovement >= iMinImprovement) {
                        bCurrentIDChange = true;
                        iLastScaleNumberCorrect += iSubtractOneImprovement;
                    }
                }
                if (bCurrentIDChange) {
                    bFullListChanges = true;
                } else {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldTermSentimentStrength);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                }
            }

        }
    }

    /**
     * 对字典的权重（二元或三元）进行一次优化.
     *
     * @param iMinImprovement 要达到的最小改进值
     */
    public void optimiseDictionaryWeightingsForCorpusTrinaryOrBinary (int iMinImprovement) {
        boolean bFullListChanges = true;
        int iLastTrinaryCorrect = getClassificationTrinaryNumberCorrect();
        int iNewTrinary = 0;
        int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
        int[] iWordRand = new int[iTotalSentimentWords + 1];
        while (bFullListChanges) {
            Sort.makeRandomOrderList(iWordRand);
            bFullListChanges = false;
            for (int i = 1; i <= iTotalSentimentWords; i++) {
                int iOldSentimentStrength = resources.sentimentWords.getSentiment(iWordRand[i]);
                boolean bCurrentIDChange = false;
                int iAddOneImprovement = 0;
                int iSubtractOneImprovement = 0;
                if (iOldSentimentStrength < 4) {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength + 1);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                    iNewTrinary = getClassificationTrinaryNumberCorrect();
                    iAddOneImprovement = iNewTrinary - iLastTrinaryCorrect;
                    if (iAddOneImprovement >= iMinImprovement) {
                        bCurrentIDChange = true;
                        iLastTrinaryCorrect += iAddOneImprovement;
                    }
                }
                if (iOldSentimentStrength > -4 && !bCurrentIDChange) {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength - 1);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                    iNewTrinary = getClassificationTrinaryNumberCorrect();
                    iSubtractOneImprovement = iNewTrinary - iLastTrinaryCorrect;
                    if (iSubtractOneImprovement >= iMinImprovement) {
                        bCurrentIDChange = true;
                        iLastTrinaryCorrect += iSubtractOneImprovement;
                    }
                }
                if (bCurrentIDChange) {
                    bFullListChanges = true;
                } else {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                }
            }

        }
    }

    /**
     * 对字典的正负权重进行一次优化.
     *
     * @param iMinImprovement 要达到的最小改进值
     * @param bUseTotalDifference 是否使用总差异来计算改进值
     */
    public void optimiseDictionaryWeightingsForCorpusPosNeg (int iMinImprovement, boolean bUseTotalDifference) {
        boolean bFullListChanges = true;
        int iLastPos = 0;
        int iLastNeg = 0;
        int iLastPosTotalDiff = 0;
        int iLastNegTotalDiff = 0;
        if (bUseTotalDifference) {
            iLastPosTotalDiff = getClassificationPositiveTotalDifference();
            iLastNegTotalDiff = getClassificationNegativeTotalDifference();
        } else {
            iLastPos = getClassificationPositiveNumberCorrect();
            iLastNeg = getClassificationNegativeNumberCorrect();
        }
        int iNewPos = 0;
        int iNewNeg = 0;
        int iNewPosTotalDiff = 0;
        int iNewNegTotalDiff = 0;
        int iTotalSentimentWords = resources.sentimentWords.getSentimentWordCount();
        int[] iWordRand = new int[iTotalSentimentWords + 1];
        while (bFullListChanges) {
            Sort.makeRandomOrderList(iWordRand);
            bFullListChanges = false;
            for (int i = 1; i <= iTotalSentimentWords; i++) {
                int iOldSentimentStrength = resources.sentimentWords.getSentiment(iWordRand[i]);
                boolean bCurrentIDChange = false;
                if (iOldSentimentStrength < 4) {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength + 1);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                    if (bUseTotalDifference) {
                        iNewPosTotalDiff = getClassificationPositiveTotalDifference();
                        iNewNegTotalDiff = getClassificationNegativeTotalDifference();
                        if (((iNewPosTotalDiff - iLastPosTotalDiff) + iNewNegTotalDiff) -
                                iLastNegTotalDiff <= -iMinImprovement) {
                            bCurrentIDChange = true;
                        }
                    } else {
                        iNewPos = getClassificationPositiveNumberCorrect();
                        iNewNeg = getClassificationNegativeNumberCorrect();
                        if (((iNewPos - iLastPos) + iNewNeg) - iLastNeg >= iMinImprovement) {
                            bCurrentIDChange = true;
                        }
                    }
                }
                if (iOldSentimentStrength > -4 && !bCurrentIDChange) {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength - 1);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                    if (bUseTotalDifference) {
                        iNewPosTotalDiff = getClassificationPositiveTotalDifference();
                        iNewNegTotalDiff = getClassificationNegativeTotalDifference();
                        if (((iNewPosTotalDiff - iLastPosTotalDiff) + iNewNegTotalDiff) -
                                iLastNegTotalDiff <= -iMinImprovement) {
                            bCurrentIDChange = true;
                        }
                    } else {
                        iNewPos = getClassificationPositiveNumberCorrect();
                        iNewNeg = getClassificationNegativeNumberCorrect();
                        if (((iNewPos - iLastPos) + iNewNeg) - iLastNeg >= iMinImprovement) {
                            bCurrentIDChange = true;
                        }
                    }
                }
                if (bCurrentIDChange) {
                    if (bUseTotalDifference) {
                        iLastNegTotalDiff = iNewNegTotalDiff;
                        iLastPosTotalDiff = iNewPosTotalDiff;
                    } else {
                        iLastNeg = iNewNeg;
                        iLastPos = iNewPos;
                    }
                    bFullListChanges = true;
                } else {
                    resources.sentimentWords.setSentiment(iWordRand[i], iOldSentimentStrength);
                    reClassifyClassifiedCorpusForSentimentChange(iWordRand[i], 1);
                }
            }
        }
    }


    /**
     * 进行多次十倍交叉验证.
     *
     * @param sInputFile 输入文件路径
     * @param sOutputFile 输出文件路径
     */
    public void SummariseMultiple10FoldValidations (String sInputFile, String sOutputFile) {
        int iDataRows = 28;
        int iLastOptionCol = 24;
        String sLine = null;
        String[] sPrevData = null;
        String[] sData = null;
        float[] total = new float[iDataRows];
        int iRows = 0;
        int i = 0;
        try (BufferedReader rResults = new BufferedReader(new FileReader(sInputFile));
             BufferedWriter wSummary = new BufferedWriter(new FileWriter(sOutputFile))){

            sLine = rResults.readLine();
            wSummary.write(String.valueOf(sLine) + "\tNumber\n");
            while (rResults.ready()) {
                sLine = rResults.readLine();
                sData = sLine.split("\t");
                boolean bMatching = true;
                if (sPrevData != null) {
                    for (i = 0; i < iLastOptionCol; i++) {
                        if (!sData[i].equals(sPrevData[i])) {
                            bMatching = false;
                        }
                    }
                }
                if (!bMatching) {
                    for (i = 0; i < iLastOptionCol; i++) {
                        wSummary.write(String.valueOf(sPrevData[i]) + "\t");
                    }

                    for (i = 0; i < iDataRows; i++) {
                        wSummary.write(String.valueOf(total[i] / iRows) + "\t");
                    }

                    wSummary.write(String.valueOf(iRows) + "\n");
                    for (i = 0; i < iDataRows; i++) {
                        total[i] = 0.0F;
                    }

                    iRows = 0;
                }
                for (i = iLastOptionCol; i < iLastOptionCol + iDataRows; i++) {
                    try {
                        total[i - iLastOptionCol] += Float.parseFloat(sData[i]);
                    } catch (Exception e) {
                        total[i - iLastOptionCol] += 9999999F;
                    }
                }
                iRows++;
                sPrevData = sLine.split("\t");
            }
            for (i = 0; i < iLastOptionCol; i++) {
                wSummary.write(String.valueOf(Objects.requireNonNull(sPrevData)[i]) + "\t");
            }

            for (i = 0; i < iDataRows; i++) {
                wSummary.write(String.valueOf(total[i] / iRows) + "\t");
            }

            wSummary.write(String.valueOf(iRows) + "\n");
        }
        catch (IOException e) {
            String error = "SummariseMultiple10FoldValidations: File I/O error: " + sInputFile;
            LOG.error(error);
            e.printStackTrace();
        }
        catch (Exception e) {
            String error = "SummariseMultiple10FoldValidations: Error at line: " + sLine;
            LOG.error(error);
            error = "Value of i: " + i;
            LOG.error(error);
            e.printStackTrace();
        }
    }
}
