//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package senti.sentistrength.classification;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClassificationOptions {
    private  static final Float ONEPOINTFIVEF = 1.5F;
    private  static final Float ZEROPOINTFIVEF = 0.5F;
    /**
     * 指示SentiStrength在分类中的情感倾向模式.
     */
    public boolean bgTensiStrength = false;
    /**
     * 表示情感分析程序的名称.
     */
    public String sgProgramName = "SentiStrength";
    /**
     * 表示情感分析程序用于测量情感的方法（例如“情感”）.
     */
    public String sgProgramMeasuring = "sentiment";
    /**
     * 表示情感分析程序使用的正向情感术语（例如“积极情感”）.
     */
    public String sgProgramPos = "positive sentiment";
    /**
     * 表示情感分析程序使用的负向情感术语（例如“消极情感”）.
     */
    public String sgProgramNeg = "negative sentiment";
    /**
     * 指示分类器是否应该使用Scale模式.
     */
    public boolean bgScaleMode = false;
    /**
     * 指示分类器是否应该在三元模式（positive/negative/neutral）和二元模式（positive/negative）之间进行切换.
     */
    public boolean bgTrinaryMode = false;
    /**
     * 指示是否应该在三元模式下计算结果并将其转换为二元模式.
     */
    public boolean bgBinaryVersionOfTrinaryMode = false;
    /**
     * 表示当分类器使用二元模式进行分类时默认的分类结果（1表示正向情感，-1表示负向情感）.
     */
    public int igDefaultBinaryClassification = 1;
    /**
     * 指示分类器在将段落中的情感进行合并时使用的方法.
     */
    public int igEmotionParagraphCombineMethod = 0;



    /**
     * 分类器在将句子中的情感进行合并时使用的方法.
     */
    public  int igEmotionSentenceCombineMethod = 0;
    /**
     * 表示负向情感的权重，用于在情感分析中影响结果.
     */
    public float fgNegativeSentimentMultiplier = ONEPOINTFIVEF;
    /**
     * 指示是否应该减少疑问句中的负面情感.
     */
    public boolean bgReduceNegativeEmotionInQuestionSentences = false;
    /**
     * 指示是否应该将缺少的计数视为+2.
     */
    public boolean bgMissCountsAsPlus2 = true;
    /**
     * 指示在否定句中，出现“你”或“你的”时是否应该视为+2.
     */
    public boolean bgYouOrYourIsPlus2UnlessSentenceNegative = false;
    /**
     * 是否应该将中性句子中的感叹号视为+2.
     */
    public boolean bgExclamationInNeutralSentenceCountsAsPlus2 = false;
    /**
     * 表示在句子中出现感叹号时至少需要的标点符号数目，才能改变句子的情感.
     */
    public int igMinPunctuationWithExclamationToChangeSentenceSentiment = 0;
    /**
     * 指示分类器是否应该使用习语查找表进行情感分析.
     */
    public boolean bgUseIdiomLookupTable = true;
    /**
     * 指示分类器是否应该使用对象评估表.
     */
    public boolean bgUseObjectEvaluationTable = false;


    /**
     * 指定将中性情感解释为何种情绪.
     */
    public int igMoodToInterpretNeutralEmphasis = 1;
    /**
     * 用于指定是否允许多个正面词汇增加正面情感.
     */
    public boolean bgAllowMultiplePositiveWordsToIncreasePositiveEmotion = true;
    /**
     * 用于指定是否允许多个负面词汇增加负面情感.
     */
    public boolean bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion = true;
    /**
     * 指定是否忽略否定词后面的助推词.
     */
    public boolean bgIgnoreBoosterWordsAfterNegatives = true;
    /**
     * 指定是否使用字典校正拼写错误.
     */
    public boolean bgCorrectSpellingsUsingDictionary = true;
    /**
     * 指定是否校正额外的字母拼写错误.
     */
    public boolean bgCorrectExtraLetterSpellingErrors = true;
    /**
     * 指定词汇中不允许出现的重复字母.
     */
    public String sgIllegalDoubleLettersInWordMiddle = "ahijkquvxyz";
    /**
     * 指定词汇末尾不允许出现的重复字母.
     */
    public String sgIllegalDoubleLettersAtWordEnd = "achijkmnpqruvwxyz";
    /**
     * 指定是否允许多个相同字母的词汇增强情感.
     */
    public boolean bgMultipleLettersBoostSentiment = true;
    /**
     * 指定助推词是否改变情感.
     */
    public boolean bgBoosterWordsChangeEmotion = true;
    /**
     * 指定是否总是在撇号处分割单词.
     */
    public boolean bgAlwaysSplitWordsAtApostrophes = false;
    /**
     * 指定否定词是否出现在情感词之前.
     */
    public boolean bgNegatingWordsOccurBeforeSentiment = true;
    /**
     * 指定在情感词之前最多有多少个单词可以被否定.
     */
    public int igMaxWordsBeforeSentimentToNegate = 0;
    /**
     * 指定否定词是否出现在情感词之后.
     */
    public boolean bgNegatingWordsOccurAfterSentiment = false;
    /**
     * 指定在情感词之后最多有多少个单词可以被否定.
     */
    public int igMaxWordsAfterSentimentToNegate = 0;
    /**
     * 指定否定词是否反转正面情感.
     */
    public boolean bgNegatingPositiveFlipsEmotion = true;
    public boolean bgNegatingNegativeNeutralisesEmotion = true;
    public boolean bgNegatingWordsFlipEmotion = false;
    public float fgStrengthMultiplierForNegatedWords = ZEROPOINTFIVEF;
    public boolean bgCorrectSpellingsWithRepeatedLetter = true;
    public boolean bgUseEmoticons = true;
    public boolean bgCapitalsBoostTermSentiment = false;
    public int igMinRepeatedLettersForBoost = TWO;
    public String[] sgSentimentKeyWords = null;
    public boolean bgIgnoreSentencesWithoutKeywords = false;
    public int igWordsToIncludeBeforeKeyword = FOUR;
    public int igWordsToIncludeAfterKeyword = FOUR;
    /**
     * 如果为true，则在分析结束时打印详细分类.
     */
    public boolean bgExplainClassification = false;
    /**
     * 如果为true，则在分析结束时打印输入文本.
     */
    public boolean bgEchoText = false;
    /**
     * 如果为true，则将文本强制转换为UTF-8编码.
     */
    public boolean bgForceUTF8 = false;
    /**
     * 如果为true，则使用Lemmatisation算法对单词进行规范化.
     */
    public boolean bgUseLemmatisation = false;
    public int igMinSentencePosForQuotesIrony = TEN;
    public int igMinSentencePosForPunctuationIrony = TEN;
    public int igMinSentencePosForTermsIrony = TEN;

    private static final int TWO = 2;
    private static final int FOUR = 2;
    private static final int TEN = 2;
    private static final Logger LOGGER = LoggerFactory.getLogger("ClassificationOptions");
    /**
     * 将关键字列表字符串按照 "," 分割处理，并将bgIgnoreSentencesWithoutKeywords置为true.
     *
     * @param sKeywordList 关键字列表字符串
     */
    public void parseKeywordList(final String sKeywordList) {
        this.sgSentimentKeyWords = sKeywordList.split(",");
        this.bgIgnoreSentencesWithoutKeywords = true;
    }

    /**
     * 在方法体内，根据一系列成员变量的值将字符串写入输出文件，每个值都用一个制表符分隔开来。
     * 四个参数：wWriter是一个BufferedWriter对象，用于写入输出文件；iMinImprovement是一个整数，表示最小的改进量；
     * bUseTotalDifference是一个布尔值，表示是否使用总差异量；iMultiOptimisations是一个整数，表示多重优化.
     *
     * @param wWriter 输出的目标
     * @param iMinImprovement 必须有多少个单词的情感分类改变才会被认为是改进
     * @param bUseTotalDifference 是否使用总差异量来计算分类分数
     * @param iMultiOptimisations 多个优化中使用了多少个优化
     * @return 写入成功，该方法将返回true，否则将返回false。
     */
    public boolean printClassificationOptions(final BufferedWriter wWriter, final int iMinImprovement, final boolean bUseTotalDifference, final int iMultiOptimisations) {
        try {
            if (this.igEmotionParagraphCombineMethod == 0) {
                wWriter.write("Max");
            } else if (this.igEmotionParagraphCombineMethod == 1) {
                wWriter.write("Av");
            } else {
                wWriter.write("Tot");
            }

            if (this.igEmotionSentenceCombineMethod == 0) {
                wWriter.write("\tMax");
            } else if (this.igEmotionSentenceCombineMethod == 1) {
                wWriter.write("\tAv");
            } else {
                wWriter.write("\tTot");
            }

            if (bUseTotalDifference) {
                wWriter.write("\tTotDiff");
            } else {
                wWriter.write("\tExactCount");
            }

            wWriter.write("\t" + iMultiOptimisations + "\t"
                    + this.bgReduceNegativeEmotionInQuestionSentences + "\t"
                    + this.bgMissCountsAsPlus2 + "\t"
                    + this.bgYouOrYourIsPlus2UnlessSentenceNegative + "\t"
                    + this.bgExclamationInNeutralSentenceCountsAsPlus2 + "\t"
                    + this.bgUseIdiomLookupTable + "\t" + this.igMoodToInterpretNeutralEmphasis + "\t"
                    + this.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion + "\t"
                    + this.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion + "\t"
                    + this.bgIgnoreBoosterWordsAfterNegatives + "\t"
                    + this.bgMultipleLettersBoostSentiment + "\t"
                    + this.bgBoosterWordsChangeEmotion + "\t"
                    + this.bgNegatingWordsFlipEmotion + "\t"
                    + this.bgNegatingPositiveFlipsEmotion + "\t"
                    + this.bgNegatingNegativeNeutralisesEmotion + "\t"
                    + this.bgCorrectSpellingsWithRepeatedLetter + "\t"
                    + this.bgUseEmoticons + "\t"
                    + this.bgCapitalsBoostTermSentiment + "\t"
                    + this.igMinRepeatedLettersForBoost + "\t"
                    + this.igMaxWordsBeforeSentimentToNegate + "\t" + iMinImprovement);
            return true;
        } catch (IOException var6) {
            var6.printStackTrace();
            return false;
        }
    }

    /**
     *它将一些占位符写入到BufferedWriter对象中，以表示没有任何分类选项.
     *
     * @param wWriter 输出的目标
     * @return  返回true表示写入成功。如果写入过程中发生IOException异常，则会捕获该异常并打印堆栈跟踪，最终返回false表示写入失败。
     */
    public boolean printBlankClassificationOptions(final BufferedWriter wWriter) {
        try {
            wWriter.write("~");
            wWriter.write("\t~");
            wWriter.write("\tBaselineMajorityClass");
            wWriter.write("\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~\t~");
            return true;
        } catch (IOException var3) {
            var3.printStackTrace();
            return false;
        }
    }

    /**
     * 打印分类选项的标题，输出的内容包括情感段落合并方法、情感句子合并方法、权重调整方法等等。这些信息用于指导分类器在进行情感分类时如何处理文本数据.
     *
     * @param wWriter 输出的目标
     * @return  打印成功返回true，否则返回false
     */
    public boolean printClassificationOptionsHeadings(final BufferedWriter wWriter) {
        try {
            wWriter.write("EmotionParagraphCombineMethod\tEmotionSentenceCombineMethod\tDifferenceCalculationMethodForTermWeightAdjustments\t"
                    + "MultiOptimisations\tReduceNegativeEmotionInQuestionSentences\tMissCountsAsPlus2\tYouOrYourIsPlus2UnlessSentenceNegative\t"
                    + "ExclamationCountsAsPlus2\tUseIdiomLookupTable\tMoodToInterpretNeutralEmphasis\t"
                    + "AllowMultiplePositiveWordsToIncreasePositiveEmotion\tAllowMultipleNegativeWordsToIncreaseNegativeEmotion\t"
                    + "IgnoreBoosterWordsAfterNegatives\tMultipleLettersBoostSentiment\tBoosterWordsChangeEmotion\tNegatingWordsFlipEmotion\t"
                    + "NegatingPositiveFlipsEmotion\tNegatingNegativeNeutralisesEmotion\tCorrectSpellingsWithRepeatedLetter\tUseEmoticons\t"
                    + "CapitalsBoostTermSentiment\tMinRepeatedLettersForBoost\tWordsBeforeSentimentToNegate\tMinImprovement");
            return true;
        } catch (IOException var3) {
            var3.printStackTrace();
            return false;
        }
    }

    /**
     * 读取一个文件并设置分类选项.
     *
     * @param sFilename 文件名
     * @return 成功设置了分类选项返回true，否则返回false
     */
    public boolean setClassificationOptions(final String sFilename) {
        try (BufferedReader rReader = new BufferedReader(new FileReader(sFilename))) {
            while (rReader.ready()) {
                String sLine = rReader.readLine();
                int iTabPos = sLine.indexOf("\t");
                if (iTabPos > 0) {
                    String[] sData = sLine.split("\t");
                    switch (sData[0]) {
                        case "EmotionParagraphCombineMethod":
                            if (sData[1].contains("Max")) {
                                this.igEmotionParagraphCombineMethod = 0;
                            }

                            if (sData[1].contains("Av")) {
                                this.igEmotionParagraphCombineMethod = 1;
                            }

                            if (sData[1].contains("Tot")) {
                                this.igEmotionParagraphCombineMethod = 2;
                            }
                            break;
                        case "EmotionSentenceCombineMethod":
                            if (sData[1].contains("Max")) {
                                this.igEmotionSentenceCombineMethod = 0;
                            }

                            if (sData[1].contains("Av")) {
                                this.igEmotionSentenceCombineMethod = 1;
                            }

                            if (sData[1].contains("Tot")) {
                                this.igEmotionSentenceCombineMethod = 2;
                            }
                            break;
                        case "IgnoreNegativeEmotionInQuestionSentences":
                            this.bgReduceNegativeEmotionInQuestionSentences = Boolean.parseBoolean(sData[1]);
                            break;
                        case "MissCountsAsPlus2":
                            this.bgMissCountsAsPlus2 = Boolean.parseBoolean(sData[1]);
                            break;
                        case "YouOrYourIsPlus2UnlessSentenceNegative":
                            this.bgYouOrYourIsPlus2UnlessSentenceNegative = Boolean.parseBoolean(sData[1]);
                            break;
                        case "ExclamationCountsAsPlus2":
                            this.bgExclamationInNeutralSentenceCountsAsPlus2 = Boolean.parseBoolean(sData[1]);
                            break;
                        case "UseIdiomLookupTable":
                            this.bgUseIdiomLookupTable = Boolean.parseBoolean(sData[1]);
                            break;
                        case "Mood":
                            this.igMoodToInterpretNeutralEmphasis = Integer.parseInt(sData[1]);
                            break;
                        case "AllowMultiplePositiveWordsToIncreasePositiveEmotion":
                            this.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion = Boolean.parseBoolean(sData[1]);
                            break;
                        case "AllowMultipleNegativeWordsToIncreaseNegativeEmotion":
                            this.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion = Boolean.parseBoolean(sData[1]);
                            break;
                        case "IgnoreBoosterWordsAfterNegatives":
                            this.bgIgnoreBoosterWordsAfterNegatives = Boolean.parseBoolean(sData[1]);
                            break;
                        case "MultipleLettersBoostSentiment":
                            this.bgMultipleLettersBoostSentiment = Boolean.parseBoolean(sData[1]);
                            break;
                        case "BoosterWordsChangeEmotion":
                            this.bgBoosterWordsChangeEmotion = Boolean.parseBoolean(sData[1]);
                            break;
                        case "NegatingWordsFlipEmotion":
                            this.bgNegatingWordsFlipEmotion = Boolean.parseBoolean(sData[1]);
                            break;
                        case "CorrectSpellingsWithRepeatedLetter":
                            this.bgCorrectSpellingsWithRepeatedLetter = Boolean.parseBoolean(sData[1]);
                            break;
                        case "UseEmoticons":
                            this.bgUseEmoticons = Boolean.parseBoolean(sData[1]);
                            break;
                        case "CapitalsAreSentimentBoosters":
                            this.bgCapitalsBoostTermSentiment = Boolean.parseBoolean(sData[1]);
                            break;
                        case "MinRepeatedLettersForBoost":
                            this.igMinRepeatedLettersForBoost = Integer.parseInt(sData[1]);
                            break;
                        case "WordsBeforeSentimentToNegate":
                            this.igMaxWordsBeforeSentimentToNegate = Integer.parseInt(sData[1]);
                            break;
                        case "Trinary":
                            this.bgTrinaryMode = true;
                            break;
                        case "Binary":
                            this.bgTrinaryMode = true;
                            this.bgBinaryVersionOfTrinaryMode = true;
                            break;
                        default:
                            if (!sData[0].equals("Scale")) {
                                return false;
                            }
                            this.bgScaleMode = true;
                            break;
                    }
                }
            }
            return true;
        } catch (FileNotFoundException var7) {
            var7.printStackTrace();
            return false;
        } catch (IOException var8) {
            String message = "Catch Exception -> " + var8.getMessage();
            LOGGER.error(message);
            return false;
        }
    }

    /**
     * 对项目进行命名，根据SentiStrength在分类中的情感倾向模式来决定命名方式.
     *
     * @param bTensiStrength 分类中的情感倾向模式(SENTI/TENSI)
     */
    public void nameProgram(final boolean bTensiStrength) {
        this.bgTensiStrength = bTensiStrength;
        if (bTensiStrength) {
            this.sgProgramName = "TensiStrength";
            this.sgProgramMeasuring = "stress and relaxation";
            this.sgProgramPos = "relaxation";
            this.sgProgramNeg = "stress";
        } else {
            this.sgProgramName = "SentiStrength";
            this.sgProgramMeasuring = "sentiment";
            this.sgProgramPos = "positive sentiment";
            this.sgProgramNeg = "negative sentiment";
        }

    }
}
