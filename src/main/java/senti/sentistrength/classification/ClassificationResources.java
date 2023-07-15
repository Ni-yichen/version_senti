// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   ClassificationResources.java

package senti.sentistrength.classification;

import java.io.File;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import senti.sentistrength.wordsList.WordsList;
import senti.sentistrength.wordsList.WordsListExtend;
import senti.sentistrength.wordsList.WordsListFactory;
import senti.utilities.FileOps;
import senti.sentistrength.EvaluativeTerms;
import senti.sentistrength.Lemmatiser;
import senti.sentistrength.SentimentWords;
import senti.sentistrength.wordsList.*;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            EmoticonsList, CorrectSpellingsList, SentimentWords, NegatingWordList, 
//            QuestionWords, BoosterWordsList, IdiomList, EvaluativeTerms, 
//            IronyList, Lemmatiser, ClassificationOptions

public class ClassificationResources {

    private static final Logger LOGGER = LoggerFactory.getLogger("ClassificationResources");
    /**
     * 情感符号列表.
     */
    public WordsList emoticons;
    /**
     * 拼写纠正列表.
     */
    public WordsList correctSpellings;
    /**
     * 情感词列表.
     */
    public SentimentWords sentimentWords;
    /**
     * 否定词列表.
     */
    public WordsList negatingWords;
    /**
     * 疑问词列表.
     */
    public WordsList questionWords;
    /**
     * 增强词列表.
     */
    public WordsListExtend boosterWords;
    /**
     * 成语列表.
     */
    public WordsListExtend idiomList;
    /**
     * 评价性词汇列表.
     */
    public EvaluativeTerms evaluativeTerms;
    /**
     * 反语词汇列表.
     */
    public WordsList ironyList;
    /**
     * 单词词形还原器.
     */
    public Lemmatiser lemmatiser;

    /**
     * SentiStrength 程序所在的目录路径.
     */
    public String sgSentiStrengthFolder;
    /**
     * 情感词表文件名.
     */
    public String sgSentimentWordsFile;
    /**
     * 情感词表文件名2.
     */
    public String sgSentimentWordsFile2;
    /**
     * 情感符号表文件名.
     */
    public String sgEmoticonLookupTable;
    /**
     * 拼写纠正文件名.
     */
    public String sgCorrectSpellingFileName;
    /**
     * 拼写纠正文件名2.
     */
    public String sgCorrectSpellingFileName2;
    /**
     * 俚语表文件名.
     */
    public String sgSlangLookupTable;
    /**
     * 否定词文件名.
     */
    public String sgNegatingWordListFile;
    /**
     * 增强词文件名.
     */
    public String sgBoosterListFile;
    /**
     * 成语表文件名.
     */
    public String sgIdiomLookupTableFile;
    /**
     * 疑问词文件名.
     */
    public String sgQuestionWordListFile;
    /**
     * 反语词汇文件名.
     */
    public String sgIronyWordListFile;
    /**
     * 附加文件名.
     */
    public String sgAdditionalFile;
    /**
     * 单词词形还原表文件名.
     */
    public String sgLemmaFile;

    /**
     * 无参构造函数，初始化上述成员变量.
     */
    public ClassificationResources() {
        emoticons = (WordsList) WordsListFactory.getInstanceByclassName("EmoticonsList");
        correctSpellings = (WordsList) WordsListFactory.getInstanceByclassName("CorrectSpellingsList");
        sentimentWords = new SentimentWords();
        negatingWords = (WordsList) WordsListFactory.getInstanceByclassName("NegatingWordList");
        questionWords = (WordsList) WordsListFactory.getInstanceByclassName("QuestionWords");
        boosterWords = (WordsListExtend) WordsListFactory.getInstanceByclassName("BoosterWordsList");
        idiomList = (WordsListExtend) WordsListFactory.getInstanceByclassName("IdiomList");
        evaluativeTerms = new EvaluativeTerms();
        ironyList =(WordsList) WordsListFactory.getInstanceByclassName("IronyList");
        lemmatiser = new Lemmatiser();
        sgSentiStrengthFolder = System.getProperty("user.dir") + "/src/data/SentStrength_Data/";
        sgSentimentWordsFile = "EmotionLookupTable.txt";
        sgSentimentWordsFile2 = "SentimentLookupTable.txt";
        sgEmoticonLookupTable = "EmoticonLookupTable.txt";
        sgCorrectSpellingFileName = "Dictionary.txt";
        sgCorrectSpellingFileName2 = "EnglishWordList.txt";
        sgSlangLookupTable = "SlangLookupTable_NOT_USED.txt";
        sgNegatingWordListFile = "NegatingWordList.txt";
        sgBoosterListFile = "BoosterWordList.txt";
        sgIdiomLookupTableFile = "IdiomLookupTable.txt";
        sgQuestionWordListFile = "QuestionWords.txt";
        sgIronyWordListFile = "IronyTerms.txt";
        sgAdditionalFile = "";
        sgLemmaFile = "";
    }

    /**
     * 传递了一些分类选项后，根据这些选项来初始化和训练分类器。
     * 在执行训练之前，该方法会根据选项配置创建一个ClassifierTrainer对象。
     * 然后，它将使用此ClassifierTrainer对象来训练分类器。在训练完成后，该方法将分类器存储在classifier成员变量中，以供后续使用.
     *
     * @param options 情感分类选项
     * @return 初始化是否成功
     */
    public boolean initialise(final ClassificationOptions options) {
        int iExtraLinesToReserve = 0;
        if (sgAdditionalFile.compareTo("") != 0) {
            iExtraLinesToReserve = FileOps.i_CountLinesInTextFile(sgSentiStrengthFolder + sgAdditionalFile);
            if (iExtraLinesToReserve < 0) {
                String message = "No lines found in additional file! Ignoring " + sgAdditionalFile;
                LOGGER.error(message);
                return false;
            }
        }
        if (options.bgUseLemmatisation && !lemmatiser.initialise(sgSentiStrengthFolder + sgLemmaFile, false)) {
            String message = "Can't load lemma file! " + sgLemmaFile;
            LOGGER.error(message);
            return false;
        }
        File f = new File(sgSentiStrengthFolder + sgSentimentWordsFile);
        if (!f.exists() || f.isDirectory()) {
            sgSentimentWordsFile = sgSentimentWordsFile2;
        }
        File f2 = new File(sgSentiStrengthFolder + sgCorrectSpellingFileName);
        if (!f2.exists() || f2.isDirectory()) {
            sgCorrectSpellingFileName = sgCorrectSpellingFileName2;
        }
        if (emoticons.initialise(sgSentiStrengthFolder + sgEmoticonLookupTable, options)
                && correctSpellings.initialise(sgSentiStrengthFolder + sgCorrectSpellingFileName, options)
                && sentimentWords.initialise(sgSentiStrengthFolder + sgSentimentWordsFile, options, iExtraLinesToReserve)
                && negatingWords.initialise(sgSentiStrengthFolder + sgNegatingWordListFile, options)
                && questionWords.initialise(sgSentiStrengthFolder + sgQuestionWordListFile, options)
                && ironyList.initialise(sgSentiStrengthFolder + sgIronyWordListFile, options)
                && boosterWords.initialise(sgSentiStrengthFolder + sgBoosterListFile, options, iExtraLinesToReserve)
                && idiomList.initialise(sgSentiStrengthFolder + sgIdiomLookupTableFile, options, iExtraLinesToReserve)) {
            if (iExtraLinesToReserve > 0) {
                return evaluativeTerms.initialise(sgSentiStrengthFolder + sgAdditionalFile, options, idiomList, sentimentWords);
            } else {
                return true;
            }
        } else {
            return false;
        }
    }
}
