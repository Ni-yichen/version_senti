//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package senti.sentistrength.contextProcess;

import senti.sentistrength.unusedTermsClassificationStrategy.UnusedTermsClassificationStrategy;
import senti.utilities.Sort;
import senti.wkaclass.Arff;
import senti.sentistrength.classification.ClassificationOptions;
import senti.sentistrength.classification.ClassificationResources;
import senti.utilities.StringIndex;


public class Sentence {
    private Term[] term;    // sentence包含的term数组
    private boolean[] bgSpaceAfterTerm; // term数组对应的term后是否有空格
    private int igTermCount = 0;    // 该sentence由多少term组成
    private static final int MAX_COUNT = 100000;
    private static final int DOUBLE_QUOTATION_MARK = 34;
    private static final int MINUS_FIVE = -5;
    private static final int FIVE = 5;
    private static final int THREE = 3;
    private static final double SPELLING_MOOD_EMPHASIS = 0.6D;
    private static final double POS_SENTENCE_COMBINE_METHOD = 0.45D;
    private static final double NEG_SENTENCE_COMBINE_METHOD = 0.55D;


    /**
     * 获得该sentence由多少term组成.
     * @return term数量
     */
    public int getIgTermCount() {
        return igTermCount;
    }

    /**
     * 获得sentence有情感得分的Term数量.
     * @return 有情感得分的term数量
     */
    public int getIgSentiCount() {
        return igSentiCount; }
    private int igSentiCount = 0;   // sentence有情感得分的Term数量
    private int igPositiveSentiment = 0;    // 积极情感得分
    private int igNegativeSentiment = 0;    // 消极情感的分
    private boolean bgNothingToClassify = true; // 是否没有用于情感分类的term
    private ClassificationResources resources;  // 持有的ClassificationResources
    private ClassificationOptions options;  // 持有的ClassificationOptions
    private int[] igSentimentIDList;    // 该sentence包含的每个term对应的sentimentID数组
    private int igSentimentIDListCount = 0; // igSentimentIDList长度
    private boolean bSentimentIDListMade = false;   // 是否创建过igSentimentIDList
    private boolean[] bgIncludeTerm;    // 数组中的每个term是否可用于情感分类
    private boolean bgIdiomsApplied = false;    // 是否从修改短语情感得分角度重写term情感得分
    private boolean bgObjectEvaluationsApplied = false; // 是否使用过评级性词汇列表
    private String sgClassificationRationale = ""; // 分类原理

    /**
     * 无参构造函数.
     */
    public Sentence() {
    }

    /**
     * 将sentence拆分成term，添加到索引列表.
     * @param unusedTermsClassificationIndex 情感词汇表中未使用的term的情感极性索引
     */
    public void addSentenceToIndex(
            final UnusedTermsClassificationStrategy unusedTermsClassificationIndex) {
        for (int i = 1; i <= this.igTermCount; ++i) {
            unusedTermsClassificationIndex.addTermToNewTermIndex(this.term[i].getText());
        }
    }

    /**
     * 将新的字符串添加到字符串索引.
     * @param stringIndex 字符串索引
     * @param textParsingOptions 文本分析选项
     * @param bRecordCount 是否记录出现次数
     * @param bArffIndex 是否ARFF索引
     * @return 检查的Term数量
     */
    public int addToStringIndex(final StringIndex stringIndex,
                                final TextParsingOptions textParsingOptions,
                                final boolean bRecordCount, final boolean bArffIndex) {
        String sEncoded = "";
        int iStringPos = 1;
        int iTermsChecked = 0;
        if (textParsingOptions.bgIncludePunctuation
                && textParsingOptions.igNgramSize == 1
                && !textParsingOptions.bgUseTranslations
                && !textParsingOptions.bgAddEmphasisCode) {
            for (int i = 1; i <= this.igTermCount; ++i) {
                stringIndex.addString(this.term[i].getText(), bRecordCount);
            }

            iTermsChecked = this.igTermCount;
        } else {
            StringBuilder sText = new StringBuilder();
            int iCurrentTerm = 0;
            int iTermCount = 0;

            while (iCurrentTerm < this.igTermCount) {
                ++iCurrentTerm;
                if (textParsingOptions.bgIncludePunctuation
                        || !this.term[iCurrentTerm].isPunctuation()) {
                    ++iTermCount;
                    if (iTermCount > 1) {
                        sText.append(" ");
                    } else {
                        sText = new StringBuilder();
                    }

                    if (textParsingOptions.bgUseTranslations) {
                        sText.append(this.term[iCurrentTerm].getTranslation());
                    } else {
                        sText.append(this.term[iCurrentTerm].getOriginalText());
                    }

                    if (textParsingOptions.bgAddEmphasisCode
                            && this.term[iCurrentTerm].containsEmphasis()) {
                        sText.append("+");
                    }
                }

                if (iTermCount == textParsingOptions.igNgramSize) {
                    if (bArffIndex) {
                        sEncoded = Arff.arffSafeWordEncode(sText.toString()
                                        .toLowerCase(), false);
                        iStringPos = stringIndex.findString(sEncoded);
                        iTermCount = 0;
                        if (iStringPos > -1) {
                            stringIndex.add1ToCount(iStringPos);
                        }
                    } else {
                        stringIndex.addString(sText.toString()
                                .toLowerCase(), bRecordCount);
                        iTermCount = 0;
                    }

                    iCurrentTerm += 1 - textParsingOptions.igNgramSize;
                    ++iTermsChecked;
                }
            }
        }

        return iTermsChecked;
    }

    /**
     * 对原始sentence进行预处理，将每个单词都标准化处理后存入term数组.
     * @param sSentence 原始sentence
     * @param classResources 处理所用资源
     * @param newClassificationOptions 处理选项
     */
    public void setSentence(final String sSentence,
                            final ClassificationResources classResources,
                            final ClassificationOptions newClassificationOptions) {
        String sTmpSentence = sSentence;
        this.resources = classResources;
        this.options = newClassificationOptions;
        if (this.options.bgAlwaysSplitWordsAtApostrophes
                && sTmpSentence.contains("'")) {
            sTmpSentence = sTmpSentence.replace("'", " ");
        }

        String[] sSegmentList = sTmpSentence.split(" ");
        int iSegmentListLength = sSegmentList.length;
        int iMaxTermListLength = sTmpSentence.length() + 1;
        this.term = new Term[iMaxTermListLength];
        this.bgSpaceAfterTerm = new boolean[iMaxTermListLength];
        int iPos = 0;
        this.igTermCount = 0;

        for (String s : sSegmentList) {
            for (iPos = 0; iPos >= 0 && iPos < s.length();
                 this.bgSpaceAfterTerm[this.igTermCount] = false) {
                this.term[++this.igTermCount] = new Term();
                int iOffset = this.term[this.igTermCount]
                        .extractNextWordOrPunctuationOrEmoticon(s
                                .substring(iPos), this.resources, this.options);
                if (iOffset < 0) {
                    iPos = iOffset;
                } else {
                    iPos += iOffset;
                }
            }

            this.bgSpaceAfterTerm[this.igTermCount] = true;
        }

        this.bgSpaceAfterTerm[this.igTermCount] = false;
    }

    /**
     * 获得该sentennce包含的每个term对应的sentimentID数组.
     * @return 该sentence包含的每个term对应的sentimentID数组
     */
    public int[] getSentimentIDList() {
        if (!this.bSentimentIDListMade) {
            this.makeSentimentIDList();
        }

        return this.igSentimentIDList;
    }

    /**
     * 创建该sentence包含的每个term对应的sentimentID数组.
     */
    public void makeSentimentIDList() {
        int iSentimentIDTemp = 0;
        this.igSentimentIDListCount = 0;

        int i;
        for (i = 1; i <= this.igTermCount; ++i) {
            if (this.term[i].getSentimentID() > 0) {
                ++this.igSentimentIDListCount;
            }
        }

        if (this.igSentimentIDListCount > 0) {
            this.igSentimentIDList = new int[this.igSentimentIDListCount + 1];
            this.igSentimentIDListCount = 0;

            for (i = 1; i <= this.igTermCount; ++i) {
                iSentimentIDTemp = this.term[i].getSentimentID();
                if (iSentimentIDTemp > 0) {
                    for (int j = 1; j <= this.igSentimentIDListCount; ++j) {
                        if (iSentimentIDTemp == this.igSentimentIDList[j]) {
                            iSentimentIDTemp = 0;
                            break;
                        }
                    }

                    if (iSentimentIDTemp > 0) {
                        this.igSentimentIDList[++this.igSentimentIDListCount]
                                = iSentimentIDTemp;
                    }
                }
            }

            Sort.quickSortInt(this.igSentimentIDList,
                    1, this.igSentimentIDListCount);
        }

        this.bSentimentIDListMade = true;
    }

    /**
     * 生成sentence的XML标签字符串.
     * @return sentence的XML标签字符串
     */
    public String getTaggedSentence() {
        StringBuilder sTagged = new StringBuilder();

        for (int i = 1; i <= this.igTermCount; ++i) {
            if (this.bgSpaceAfterTerm[i]) {
                sTagged.append(this.term[i].getTag()).append(" ");
            } else {
                sTagged.append(this.term[i].getTag());
            }
        }

        return sTagged + "<br>";
    }

    /**
     * 获得分类的原理.
     * @return 分类原理
     */
    public String getClassificationRationale() {
        return this.sgClassificationRationale;
    }

    /**
     * 对该sentence中每个term进行修正.
     * @return 修正后的sentence
     */
    public String getTranslatedSentence() {
        StringBuilder sTranslated = new StringBuilder();

        for (int i = 1; i <= this.igTermCount; ++i) {
            if (this.term[i].isWord()) {
                sTranslated.append(this.term[i].getTranslatedWord());
            } else if (this.term[i].isPunctuation()) {
                sTranslated.append(this.term[i].getTranslatedPunctuation());
            } else if (this.term[i].isEmoticon()) {
                sTranslated.append(this.term[i].getEmoticon());
            }

            if (this.bgSpaceAfterTerm[i]) {
                sTranslated.append(" ");
            }
        }

        return sTranslated + "<br>";
    }

    /**
     * 重新计算sentence的情感得分.
     */
    public void recalculateSentenceSentimentScore() {
        this.calculateSentenceSentimentScore();
    }

    /**
     * 对sentence重新进行情感分类.
     * @param iSentimentWordID 情感关键词ID
     */
    public void reClassifyClassifiedSentenceForSentimentChange(
            final int iSentimentWordID) {
        if (this.igNegativeSentiment == 0) { // 未经过情感分类，进行情感分数计算
            this.calculateSentenceSentimentScore();
        } else {
            if (!this.bSentimentIDListMade) { // 未曾创建过情感词ID列表，进行创建
                this.makeSentimentIDList();
            }

            if (this.igSentimentIDListCount != 0) {
                if (Sort.i_FindIntPositionInSortedArray(iSentimentWordID,
                        this.igSentimentIDList, 1,
                        this.igSentimentIDListCount) >= 0) { //若曾在列表中出现则需重新计算
                    this.calculateSentenceSentimentScore();
                }

            }
        }
    }

    /**
     * 获得sentence的积极情感得分.
     * @return 极情感得分
     */
    public int getSentencePositiveSentiment() {
        if (this.igPositiveSentiment == 0) {
            this.calculateSentenceSentimentScore();
        }

        return this.igPositiveSentiment;
    }

    /**
     * 获得sentence的消极情感得分.
     * @return 消极情感得分
     */
    public int getSentenceNegativeSentiment() {
        if (this.igNegativeSentiment == 0) {
            this.calculateSentenceSentimentScore();
        }

        return this.igNegativeSentiment;
    }

    /**
     * 标记term是否可以用于情感分类以及记录sentence中是否有可进行情感分类的term.
     */
    private void markTermsValidToClassify() {
        this.bgIncludeTerm = new boolean[this.igTermCount + 1];
        int iTermsSinceValid; // 计数器
        if (this.options
                .bgIgnoreSentencesWithoutKeywords) { // 忽略不包含关键词的sentence
            this.bgNothingToClassify = true;

            int iTerm;
            for (iTermsSinceValid = 1;
                 iTermsSinceValid <= this.igTermCount; ++iTermsSinceValid) {
                this.bgIncludeTerm[iTermsSinceValid] = false;
                if (this.term[iTermsSinceValid].isWord()) { // 如果是单词则遍历所有情感关键词
                    for (iTerm = 0;
                         iTerm < this.options.sgSentimentKeyWords.length;
                         ++iTerm) {
                        if (this.term[iTermsSinceValid]
                                .matchesString(this.options
                                                .sgSentimentKeyWords[iTerm],
                                        true)) {
                            this.bgIncludeTerm[iTermsSinceValid] = true;
                            this.bgNothingToClassify = false;
                        }
                    }
                }
            }

            if (!this.bgNothingToClassify) {
                iTermsSinceValid = MAX_COUNT;

                for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) { // 从前往后遍历
                    if (this.bgIncludeTerm[iTerm]) { // 已被标记为可用于情感分类，重新开始计数
                        iTermsSinceValid = 0;
                    } else if (iTermsSinceValid
                            < this.options.igWordsToIncludeAfterKeyword) {
                        this.bgIncludeTerm[iTerm] = true; // 进行标记
                        if (this.term[iTerm].isWord()) {
                            ++iTermsSinceValid; // 计数器加一
                        }
                    }
                }

                iTermsSinceValid = MAX_COUNT;

                for (iTerm = this.igTermCount; iTerm >= 1; --iTerm) { // 从后往前遍历
                    if (this.bgIncludeTerm[iTerm]) {
                        iTermsSinceValid = 0;
                    } else if (iTermsSinceValid
                            < this.options.igWordsToIncludeBeforeKeyword) {
                        this.bgIncludeTerm[iTerm] = true;
                        if (this.term[iTerm].isWord()) {
                            ++iTermsSinceValid;
                        }
                    }
                }
            }
        } else { // 所有sentence都参与情感分类
            for (iTermsSinceValid = 1; iTermsSinceValid <= this.igTermCount;
                 ++iTermsSinceValid) {
                this.bgIncludeTerm[iTermsSinceValid] = true;
            }

            this.bgNothingToClassify = false;
        }

    }

    /**
     * 计算sentence情感得分.
     */
    private void calculateSentenceSentimentScore() {
        if (this.options.bgExplainClassification
                && this.sgClassificationRationale.length() > 0) {
            this.sgClassificationRationale = "";
        }

        this.igNegativeSentiment = 1;
        this.igPositiveSentiment = 1;
        int iWordTotal = 0;
        int iLastBoosterWordScore = 0;
        int iTemp = 0;
        if (this.igTermCount == 0) {
            this.bgNothingToClassify = true;
            this.igNegativeSentiment = -1;
        } else {
            this.markTermsValidToClassify();
            if (this.bgNothingToClassify) {
                this.igNegativeSentiment = -1;
                this.igPositiveSentiment = 1;
            } else {
                boolean bSentencePunctuationBoost = false;
                int iWordsSinceNegative = this.options
                        .igMaxWordsBeforeSentimentToNegate + 2;
                float[] fSentiment = new float[this.igTermCount + 1];
                if (this.options.bgUseIdiomLookupTable) {
                    this.overrideTermStrengthsWithIdiomStrengths(false);
                }

                if (this.options.bgUseObjectEvaluationTable) {
                    this.overrideTermStrengthsWithObjectEvaluationStrengths(
                            false);
                }

                for (int iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                    if (this.bgIncludeTerm[iTerm]) {
                        int iTermsChecked;
                        if (!this.term[iTerm].isWord()) {
                            if (this.term[iTerm].isEmoticon()) {
                                iTermsChecked = this.term[iTerm]
                                        .getEmoticonSentimentStrength();
                                if (iTermsChecked != 0) {
                                    if (iWordTotal > 0) {
                                        fSentiment[iWordTotal] += (float) this.term[iTerm]
                                                .getEmoticonSentimentStrength();
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + this.term[iTerm].getEmoticon() + " ["
                                                    + this.term[iTerm].getEmoticonSentimentStrength()
                                                    + " emoticon] ";
                                        }
                                    } else {
                                        ++iWordTotal;
                                        fSentiment[iWordTotal] = (float) iTermsChecked;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + this.term[iTerm].getEmoticon()
                                                    + " [" + this.term[iTerm].getEmoticonSentimentStrength()
                                                    + " emoticon]";
                                        }
                                    }
                                }
                            } else if (this.term[iTerm].isPunctuation()) {
                            	if (this.term[iTerm].getPunctuationEmphasisLength()
                                        >= this.options.igMinPunctuationWithExclamationToChangeSentenceSentiment
                                        && this.term[iTerm].punctuationContains("!")
                                        && iWordTotal > 0) {
                                    bSentencePunctuationBoost = true;   // UC-9
                                }
                                if (this.options.bgExplainClassification) {
                                    this.sgClassificationRationale = this.sgClassificationRationale
                                            + this.term[iTerm].getOriginalText();
                                }
                            }
                        } else {
                            ++iWordTotal;
                            if (iTerm == 1 || !this.term[iTerm].isProperNoun()
                                    || this.term[iTerm - 1].getOriginalText().equals(":")
                                    || this.term[iTerm - 1].getOriginalText().length() > THREE
                                    && this.term[iTerm - 1].getOriginalText().charAt(0) == '@') {
                                fSentiment[iWordTotal] = (float) this.term[iTerm].getSentimentValue();
                                
                                if (this.options.bgExplainClassification) {
                                    iTemp = this.term[iTerm].getSentimentValue();
                                    if (iTemp < 0) {
                                        --iTemp;
                                    } else {
                                        ++iTemp;
                                    }

                                    if (iTemp == 1) {
                                        this.sgClassificationRationale = this.sgClassificationRationale
                                                + this.term[iTerm].getOriginalText() + " ";
                                    } else {
                                        this.sgClassificationRationale = this.sgClassificationRationale
                                                + this.term[iTerm].getOriginalText() + "[" + iTemp + "] ";
                                    }
                                }
                            } else if (this.options.bgExplainClassification) {
                                this.sgClassificationRationale = this.sgClassificationRationale
                                        + this.term[iTerm].getOriginalText() + " [proper noun] ";
                            }

                            if (this.options.bgMultipleLettersBoostSentiment
                                    && this.term[iTerm].getWordEmphasisLength() >= this.options.igMinRepeatedLettersForBoost
                                    && (iTerm == 1 || !this.term[iTerm - 1].isPunctuation()
                                    || !this.term[iTerm - 1].getOriginalText().equals("@"))) {
                                String sEmphasis = this.term[iTerm].getWordEmphasis().toLowerCase();
                                if (!sEmphasis.contains("xx") && !sEmphasis.contains("ww") && !sEmphasis.contains("ha")) {
                                    if (fSentiment[iWordTotal] < 0.0F) {
                                        fSentiment[iWordTotal] = (float) ((double) fSentiment[iWordTotal] - SPELLING_MOOD_EMPHASIS);
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[-0.6 spelling emphasis] ";
                                        }
                                    } else if (fSentiment[iWordTotal] > 0.0F) {
                                        fSentiment[iWordTotal] = (float) ((double) fSentiment[iWordTotal] + SPELLING_MOOD_EMPHASIS);
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[+0.6 spelling emphasis] ";
                                        }
                                    } else if (this.options.igMoodToInterpretNeutralEmphasis > 0) {
                                        fSentiment[iWordTotal] = (float) ((double) fSentiment[iWordTotal]
                                                + SPELLING_MOOD_EMPHASIS);
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[+0.6 spelling mood emphasis] ";
                                        }
                                    } else if (this.options.igMoodToInterpretNeutralEmphasis < 0) {
                                        fSentiment[iWordTotal] = (float) ((double) fSentiment[iWordTotal]
                                                - SPELLING_MOOD_EMPHASIS);
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[-0.6 spelling mood emphasis] ";
                                        }
                                    }
                                }
                            }

                            int var10002;
                            if (this.options.bgCapitalsBoostTermSentiment
                                    && fSentiment[iWordTotal] != 0.0F
                                    && this.term[iTerm].isAllCapitals()) {
                                if (fSentiment[iWordTotal] > 0.0F) {
                                    var10002 = (int) fSentiment[iWordTotal]++;
                                    if (this.options.bgExplainClassification) {
                                        this.sgClassificationRationale = this.sgClassificationRationale
                                                + "[+1 CAPITALS] ";
                                    }
                                } else {
                                    var10002 = (int) fSentiment[iWordTotal]--;
                                    if (this.options.bgExplainClassification) {
                                        this.sgClassificationRationale = this.sgClassificationRationale
                                                + "[-1 CAPITALS] ";
                                    }
                                }
                            }

                            if (this.options.bgBoosterWordsChangeEmotion) {
                                if (iLastBoosterWordScore != 0) {
                                    if (fSentiment[iWordTotal] > 0.0F) {
                                        fSentiment[iWordTotal] += (float) iLastBoosterWordScore;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[+" + iLastBoosterWordScore
                                                    + " booster word] ";
                                        }
                                    } else if (fSentiment[iWordTotal] < 0.0F) {
                                        fSentiment[iWordTotal] -= (float) iLastBoosterWordScore;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[-" + iLastBoosterWordScore
                                                    + " booster word] ";
                                        }
                                    }
                                }

                                iLastBoosterWordScore = this.term[iTerm]
                                        .getBoosterWordScore();
                            }

                            if (this.options.bgNegatingWordsOccurBeforeSentiment) {

                                if (this.options.bgNegatingWordsFlipEmotion) {
                                    if (iWordsSinceNegative <= this.options
                                            .igMaxWordsBeforeSentimentToNegate) {
                                        fSentiment[iWordTotal] = -fSentiment[iWordTotal]
                                                * this.options.fgStrengthMultiplierForNegatedWords;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[*-" + this.options.fgStrengthMultiplierForNegatedWords
                                                    + " approx. negated multiplier] ";
                                        }
                                    }
                                } else {
                                    if (this.options.bgNegatingNegativeNeutralisesEmotion
                                            && fSentiment[iWordTotal] < 0.0F
                                            && iWordsSinceNegative <= this.options
                                            .igMaxWordsBeforeSentimentToNegate) {
                                        fSentiment[iWordTotal] = 0.0F;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale
                                                    = this.sgClassificationRationale
                                                    + "[=0 negation] ";
                                        }
                                    }

                                    if (this.options.bgNegatingPositiveFlipsEmotion
                                            && fSentiment[iWordTotal] > 0.0F
                                            && iWordsSinceNegative <= this.options.igMaxWordsBeforeSentimentToNegate) {
                                        fSentiment[iWordTotal] = -fSentiment[iWordTotal]
                                                * this.options.fgStrengthMultiplierForNegatedWords;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale = this.sgClassificationRationale
                                                    + "[*-" + this.options.fgStrengthMultiplierForNegatedWords
                                                    + " approx. negated multiplier] ";
                                        }
                                    }
                                }
                            }

                            if (this.term[iTerm].isNegatingWord()) {
                                iWordsSinceNegative = -1;
                            }

                            if (iLastBoosterWordScore == 0) {
                                ++iWordsSinceNegative;
                            }

                            if (this.term[iTerm].isNegatingWord()
                                    && this.options.bgNegatingWordsOccurAfterSentiment) {
                                iTermsChecked = 0;

                                for (int iPriorWord = iWordTotal - 1; iPriorWord > 0; --iPriorWord) {
                                    if (this.options.bgNegatingWordsFlipEmotion) {
                                        fSentiment[iPriorWord] = -fSentiment[iPriorWord]
                                                * this.options.fgStrengthMultiplierForNegatedWords;
                                        if (this.options.bgExplainClassification) {
                                            this.sgClassificationRationale
                                                    = this.sgClassificationRationale
                                                    + "[*-" + this.options
                                                    .fgStrengthMultiplierForNegatedWords
                                                    + " approx. negated multiplier] ";
                                        }
                                    } else {
                                        if (this.options.bgNegatingNegativeNeutralisesEmotion
                                                && fSentiment[iPriorWord] < 0.0F) {
                                            fSentiment[iPriorWord] = 0.0F;
                                            if (this.options.bgExplainClassification) {
                                                this.sgClassificationRationale
                                                        = this.sgClassificationRationale
                                                        + "[=0 negation] ";
                                            }
                                        }

                                        if (this.options.bgNegatingPositiveFlipsEmotion
                                                && fSentiment[iPriorWord] > 0.0F) {
                                            fSentiment[iPriorWord] = -fSentiment[iPriorWord]
                                                    * this.options
                                                    .fgStrengthMultiplierForNegatedWords;
                                            if (this.options.bgExplainClassification) {
                                                this.sgClassificationRationale
                                                        = this.sgClassificationRationale
                                                        + "[*-" + this.options
                                                        .fgStrengthMultiplierForNegatedWords
                                                        + " approx. negated multiplier] ";
                                            }
                                        }
                                    }

                                    ++iTermsChecked;
                                    if (iTermsChecked
                                            > this.options
                                            .igMaxWordsAfterSentimentToNegate) {
                                        break;
                                    }
                                }
                            }

                            if (this.options
                                    .bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion
                                    && fSentiment[iWordTotal] < -1.0F
                                    && iWordTotal > 1
                                    && fSentiment[iWordTotal - 1] < -1.0F) {
                                var10002 = (int) fSentiment[iWordTotal]--;
                                if (this.options.bgExplainClassification) {
                                    this.sgClassificationRationale
                                            = this.sgClassificationRationale
                                            + "[-1 consecutive negative words] ";
                                }
                            }

                            if (this.options
                                    .bgAllowMultiplePositiveWordsToIncreasePositiveEmotion
                                    && fSentiment[iWordTotal] > 1.0F
                                    && iWordTotal > 1
                                    && fSentiment[iWordTotal - 1] > 1.0F) {
                                var10002 = (int) fSentiment[iWordTotal]++;
                                if (this.options.bgExplainClassification) {
                                    this.sgClassificationRationale
                                            = this.sgClassificationRationale
                                            + "[+1 consecutive positive words] ";
                                }
                            }
                            
                        }
                    }
                }

                float fTotalNeg = 0.0F;
                float fTotalPos = 0.0F;
                float fMaxNeg = 0.0F;
                float fMaxPos = 0.0F;
                int iPosWords = 0;
                int iNegWords = 0;

                int iTerm;
                for (iTerm = 1; iTerm <= iWordTotal; ++iTerm) {
                    if (fSentiment[iTerm] < 0.0F) {
                        fTotalNeg += fSentiment[iTerm];
                        ++iNegWords;
                        if (fMaxNeg > fSentiment[iTerm]) {
                            fMaxNeg = fSentiment[iTerm];
                        }
                    } else if (fSentiment[iTerm] > 0.0F) {
                        fTotalPos += fSentiment[iTerm];
                        ++iPosWords;
                        if (fMaxPos < fSentiment[iTerm]) {
                            fMaxPos = fSentiment[iTerm];
                        }
                    }
                }
                igSentiCount = iNegWords + iPosWords;
                --fMaxNeg;
                ++fMaxPos;
                int var10000 = this.options.igEmotionSentenceCombineMethod;
                this.options.getClass();
                if (var10000 == 1) {
                    if (iPosWords == 0) {
                        this.igPositiveSentiment = 1;
                    } else {
                        this.igPositiveSentiment = (int) Math
                                .round(((double) (fTotalPos + (float) iPosWords) + POS_SENTENCE_COMBINE_METHOD) / (double) iPosWords);
                    }

                    if (iNegWords == 0) {
                        this.igNegativeSentiment = -1;
                    } else {
                        this.igNegativeSentiment = (int) Math
                                .round(((double) (fTotalNeg - (float) iNegWords) + NEG_SENTENCE_COMBINE_METHOD) / (double) iNegWords);
                    }
                } else {
                    var10000 = this.options.igEmotionSentenceCombineMethod;
                    this.options.getClass();
                    if (var10000 == 2) {
                        this.igPositiveSentiment = Math.round(fTotalPos)
                                + iPosWords;
                        this.igNegativeSentiment = Math.round(fTotalNeg)
                                - iNegWords;
                    } else {
                        this.igPositiveSentiment = Math.round(fMaxPos);
                        this.igNegativeSentiment = Math.round(fMaxNeg);
                    }
                }

                if (this.options.bgReduceNegativeEmotionInQuestionSentences
                        && this.igNegativeSentiment < -1) {
                    for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                        if (this.term[iTerm].isWord()) {
                            if (this.resources.questionWords
                                    .questionWord(this.term[iTerm]
                                            .getTranslatedWord()
                                            .toLowerCase())) {
                                ++this.igNegativeSentiment;
                                if (this.options.bgExplainClassification) {
                                    this.sgClassificationRationale
                                            = this.sgClassificationRationale
                                            + "[+1 negative for question word]";
                                }
                                break;
                            }
                        } else if (this.term[iTerm].isPunctuation()
                                && this.term[iTerm].punctuationContains("?")) {
                            ++this.igNegativeSentiment; // UC-10
                            if (this.options.bgExplainClassification) {
                                this.sgClassificationRationale
                                        = this.sgClassificationRationale
                                        + "[+1 negative for question mark ?]";
                            }
                            break;
                        }
                    }
                }

                if (this.igPositiveSentiment == 1
                        && this.options.bgMissCountsAsPlus2) {
                    for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                        if (this.term[iTerm].isWord()
                                && this.term[iTerm].getTranslatedWord()
                                .toLowerCase().compareTo("miss") == 0) {
                            this.igPositiveSentiment = 2;
                            if (this.options.bgExplainClassification) {
                                this.sgClassificationRationale
                                        = this.sgClassificationRationale
                                        + "[pos = 2 for term 'miss']";
                            }
                            break;
                        }
                    }
                }

                if (bSentencePunctuationBoost) {
                    if (this.igPositiveSentiment < -this.igNegativeSentiment) {
                        --this.igNegativeSentiment;
                        if (this.options.bgExplainClassification) {
                            this.sgClassificationRationale
                                    = this.sgClassificationRationale
                                    + "[-1 punctuation emphasis] ";
                        }
                    } else if (this.igPositiveSentiment
                            > -this.igNegativeSentiment) {
                        ++this.igPositiveSentiment;
                        if (this.options.bgExplainClassification) {
                            this.sgClassificationRationale
                                    = this.sgClassificationRationale
                                    + "[+1 punctuation emphasis] ";
                        }
                    } else if (this.options.igMoodToInterpretNeutralEmphasis
                            > 0) {
                        ++this.igPositiveSentiment;
                        if (this.options.bgExplainClassification) {
                            this.sgClassificationRationale
                                    = this.sgClassificationRationale
                                    + "[+1 punctuation mood emphasis] ";
                        }
                    } else if (this.options.igMoodToInterpretNeutralEmphasis
                            < 0) {
                        --this.igNegativeSentiment;
                        if (this.options.bgExplainClassification) {
                            this.sgClassificationRationale
                                    = this.sgClassificationRationale
                                    + "[-1 punctuation mood emphasis] ";
                        }
                    }
                }

                if (this.igPositiveSentiment == 1
                        && this.igNegativeSentiment == -1
                        && this.options
                        .bgExclamationInNeutralSentenceCountsAsPlus2) {
                    for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                        if (this.term[iTerm].isPunctuation()
                                && this.term[iTerm]
                                .punctuationContains("!")) {
                            this.igPositiveSentiment = 2;     // UC-8
                            if (this.options.bgExplainClassification) {
                                this.sgClassificationRationale
                                        = this.sgClassificationRationale
                                        + "[pos = 2 for !]";
                            }
                            break;
                        }
                    }
                }

                if (this.igPositiveSentiment == 1
                        && this.igNegativeSentiment == -1
                        && this.options
                        .bgYouOrYourIsPlus2UnlessSentenceNegative) {
                    for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                        if (this.term[iTerm].isWord()) {
                            String sTranslatedWord = this.term[iTerm]
                                    .getTranslatedWord().toLowerCase();
                            if (sTranslatedWord.compareTo("you") == 0
                                    || sTranslatedWord.compareTo("your") == 0
                                    || sTranslatedWord.compareTo("whats")
                                    == 0) {
                                this.igPositiveSentiment = 2;
                                if (this.options.bgExplainClassification) {
                                    this.sgClassificationRationale
                                            = this.sgClassificationRationale
                                            + "[pos = 2 for you/your/whats]";
                                }
                                break;
                            }
                        }
                    }
                }

                this.adjustSentimentForIrony();
                var10000 = this.options.igEmotionSentenceCombineMethod;
                this.options.getClass();
                if (var10000 != 2) {
                    if (this.igPositiveSentiment > FIVE) {
                        this.igPositiveSentiment = FIVE;
                    }

                    if (this.igNegativeSentiment < MINUS_FIVE) {
                        this.igNegativeSentiment = MINUS_FIVE;
                    }
                }

                if (this.options.bgExplainClassification) {
                    this.sgClassificationRationale
                            = this.sgClassificationRationale
                            + "[sentence: " + this.igPositiveSentiment
                            + "," + this.igNegativeSentiment + "]";
                }

            }
        }
    }

    /**
     * 调整反讽情况下的情感极性得分.
     */
    private void adjustSentimentForIrony() {
        int iTerm;
        if (this.igPositiveSentiment >= this.options
                .igMinSentencePosForQuotesIrony) {
            for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                if (this.term[iTerm].isPunctuation()
                        && this.term[iTerm].getText()
                        .indexOf(DOUBLE_QUOTATION_MARK) >= 0) {
                    if (this.igNegativeSentiment > -this.igPositiveSentiment) {
                        this.igNegativeSentiment = 1 - this.igPositiveSentiment;
                    }

                    this.igPositiveSentiment = 1;
                    this.sgClassificationRationale
                            = this.sgClassificationRationale
                            + "[Irony change: pos = 1, neg = "
                            + this.igNegativeSentiment + "]";
                    return;
                }
            }
        }

        if (this.igPositiveSentiment
                >= this.options.igMinSentencePosForPunctuationIrony) {
            for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                if (this.term[iTerm].isPunctuation()
                        && this.term[iTerm].punctuationContains("!")
                        && this.term[iTerm].getPunctuationEmphasisLength()
                        > 0) {
                    if (this.igNegativeSentiment > -this.igPositiveSentiment) {
                        this.igNegativeSentiment = 1 - this.igPositiveSentiment;
                    }

                    this.igPositiveSentiment = 1;
                    this.sgClassificationRationale
                            = this.sgClassificationRationale
                            + "[Irony change: pos = 1, neg = "
                            + this.igNegativeSentiment + "]";
                    return;
                }
            }
        }

        if (this.igPositiveSentiment
                >= this.options.igMinSentencePosForTermsIrony) {
            for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                if (this.resources.ironyList
                        .termIsIronic(this.term[iTerm].getText())) {
                    if (this.igNegativeSentiment > -this.igPositiveSentiment) {
                        this.igNegativeSentiment = 1 - this.igPositiveSentiment;
                    }

                    this.igPositiveSentiment = 1;
                    this.sgClassificationRationale
                            = this.sgClassificationRationale
                            + "[Irony change: pos = 1, neg = "
                            + this.igNegativeSentiment + "]";
                    return;
                }
            }
        }

    }

    /**
     * 根据评价性词汇列表，覆盖当前文本中词汇的情感强度值，更新分类原理.
     * @param recalculateIfAlreadyDone 是否重新计算情感强度
     */
    public void overrideTermStrengthsWithObjectEvaluationStrengths(
            final boolean recalculateIfAlreadyDone) {
        boolean bMatchingObject = false; // 是否存在匹配的对象
        boolean bMatchingEvaluation = false; // 是否存在匹配
        if (!this.bgObjectEvaluationsApplied || recalculateIfAlreadyDone) {
            for (int iObject = 1;
                 iObject < this.resources.evaluativeTerms
                         .getIgObjectEvaluationCount();
                 ++iObject) {
                bMatchingObject = false;
                bMatchingEvaluation = false;

                int iTerm;
                for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                    if (this.term[iTerm].isWord()
                            && this.term[iTerm]
                            .matchesStringWithWildcard(this.resources
                                    .evaluativeTerms.getSgObject()[iObject], true)) {
                        bMatchingObject = true;
                        break;
                    }
                }

                if (bMatchingObject) {
                    for (iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                        if (this.term[iTerm].isWord()
                                && this.term[iTerm]
                                .matchesStringWithWildcard(this.resources
                                .evaluativeTerms.getSgObjectEvaluation()[iObject],
                                        true)) {
                            bMatchingEvaluation = true;
                            break;
                        }
                    }
                }

                if (bMatchingEvaluation) {
                    if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale
                                = this.sgClassificationRationale
                                + "[term weight changed by object/evaluation]";
                    }

                    this.term[iTerm]
                            .setSentimentOverrideValue(this.resources
                                    .evaluativeTerms
                                    .getIgObjectEvaluationStrength()[iObject]);
                }
            }

            this.bgObjectEvaluationsApplied = true; // 已使用评价性词汇列表
        }

    }

    /**
     * 从修改短语情感得分角度重写term情感得分.
     * @param recalculateIfAlreadyDone 是否重新计算情感强度
     */
    public void overrideTermStrengthsWithIdiomStrengths(
            final boolean recalculateIfAlreadyDone) {
        if (!this.bgIdiomsApplied || recalculateIfAlreadyDone) {
            for (int iTerm = 1; iTerm <= this.igTermCount; ++iTerm) {
                if (this.term[iTerm].isWord()) {
                    for (int iIdiom = 1;
                         iIdiom <= this.resources.idiomList.getIgIdiomCount();
                         ++iIdiom) {
                        if (iTerm + this.resources
                                .idiomList.getIgIdiomWordCount()[iIdiom] - 1
                                <= this.igTermCount) {
                            boolean bMatchingIdiom = true;

                            int iIdiomTerm;
                            for (iIdiomTerm = 0;
                                 iIdiomTerm < this.resources.idiomList
                                         .getIgIdiomWordCount()[iIdiom];
                                 ++iIdiomTerm) {
                                if (!this.term[iTerm + iIdiomTerm]
                                        .matchesStringWithWildcard(this
                                                        .resources.idiomList
                                                        .getSgIdiomWords()[iIdiom][iIdiomTerm],
                                                true)) {
                                    bMatchingIdiom = false;
                                    break;
                                }
                            }

                            if (bMatchingIdiom) {
                                if (this.options.bgExplainClassification) {
                                    this.sgClassificationRationale
                                            = this.sgClassificationRationale
                                            + "[term weight(s) changed by idiom "
                                            + this.resources.idiomList
                                            .getIdiom(iIdiom) + "]";
                                }

                                this.term[iTerm]
                                        .setSentimentOverrideValue(this
                                                .resources.idiomList
                                                .getIgIdiomStrength()[iIdiom]);

                                for (iIdiomTerm = 1;
                                     iIdiomTerm < this.resources
                                             .idiomList
                                             .getIgIdiomWordCount()[iIdiom];
                                     ++iIdiomTerm) {
                                    this.term[iTerm + iIdiomTerm]
                                            .setSentimentOverrideValue(0);
                                }
                            }
                        }
                    }
                }
            }

            this.bgIdiomsApplied = true;
        }

    }
}
