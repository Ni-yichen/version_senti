package senti.sentistrength.contextProcess;

import senti.sentistrength.classification.ClassificationOptions;
import senti.sentistrength.classification.ClassificationResources;

import java.util.Objects;

public class Term {
   private static final int CONTENT_TYPE_WORD = 1;
   private static final int CONTENT_TYPE_PUNCTUATION = 2;
   private static final int CONTENT_TYPE_EMOTICON = 3;
   private int igContentType = 0; //1word/2punctuation/3emoticon
   private String sgOriginalWord = ""; //原始单词
   private String sgLCaseWord = ""; //全小写的sgTranslatedWord
   private String sgTranslatedWord = "";  //经过处理后的单词
   private String sgWordEmphasis = ""; //单词情绪强化的部分
   private int igWordSentimentID = 0;  //单词在sentimentwords列表里面的位置
   private boolean bgNegatingWord = false;   //是否是消极单词
   private boolean bgNegatingWordCalculated = false;  //是否判断过是否为NegatingWord
   private boolean bgWordSentimentIDCalculated = false;  //是否处理过WordSentimentID
   private boolean bgProperNoun = false; //是否为大写字母开头后跟全部小写的单词
   private boolean bgProperNounCalculated = false; //是否判断过是否为ProperNoun
   private String sgPunctuation = ""; //标点
   private String sgPunctuationEmphasis = ""; //标点字符串情绪强化的部分
   private String sgEmoticon = "";  //表情符
   private static final int NOT_EMOTICON = 999;
   private int igEmoticonStrength = 0;   //表情符情绪得分
   private static final int IGBOOTERWORDSCORE_NOT_SET = 999;
   private int igBoosterWordScore = IGBOOTERWORDSCORE_NOT_SET; //情绪强度助推分
   private ClassificationResources resources; //持有的ClassificationResources
   private ClassificationOptions options; //持有的ClassificationOptions
   private boolean bgAllCapitals = false; //原始单词是否均为大写字母
   private boolean bgAllCaptialsCalculated = false; //是否判断过均为大写字母
   private boolean bgOverrideSentimentScore = false;  //情感得分是否重写过
   private int igOverrideSentimentScore = 0; //重写的情感得分
   private static final int FIVE = 5;
   private static final int THREE = 3;

   /**
    * 提取字符串中的单词、标点和表情符.
    * @param sWordAndPunctuation 原始字符串
    * @param classResources 处理所用资源
    * @param classOptions 处理选项
    * @return 单词或标点的最后一个位置（表情则返回-1）
    */
   public int extractNextWordOrPunctuationOrEmoticon(
           final String sWordAndPunctuation,
           final ClassificationResources classResources,
           final ClassificationOptions classOptions) {
      int iWordCharOrAppostrophe = 1;
      int iPunctuation = 1;
      int iPos = 0;
      int iLastCharType = 0;
      String sChar = "";
      this.resources = classResources; //分类资源对象
      this.options = classOptions; //分类选项对象
      int iTextLength = sWordAndPunctuation.length();
      if (this.codeEmoticon(sWordAndPunctuation)) { //是表情符直接返回
         return -1;
      } else {
         for (; iPos < iTextLength; ++iPos) { //对输入字符进行遍历逐个处理
            sChar = sWordAndPunctuation.substring(iPos, iPos + 1);
            if (!Character.isLetterOrDigit(sWordAndPunctuation.charAt(iPos))
                    && (this.options.bgAlwaysSplitWordsAtApostrophes
                    || !sChar.equals("'") || iPos <= 0
                    || iPos >= iTextLength - 1
                    || !Character.isLetter(sWordAndPunctuation.charAt(iPos + 1)))
                    && !sChar.equals("$") && !sChar.equals("£")
                    && !sChar.equals("@") && !sChar.equals("_")) {
               if (iLastCharType == CONTENT_TYPE_WORD) {
                  this.codeWord(sWordAndPunctuation.substring(0, iPos));
                  return iPos;
               }

               iLastCharType = CONTENT_TYPE_PUNCTUATION;
            } else { //当前字符是单词的一部分，继续
               if (iLastCharType == CONTENT_TYPE_PUNCTUATION) { //和上一字符类型不同，进行标点修正
                  this.codePunctuation(sWordAndPunctuation.substring(0, iPos));
                  return iPos; //返回单词或标点结束位置
               }

               iLastCharType = CONTENT_TYPE_WORD; //和上一字符类型相同，继续
            }
         }

         switch (iLastCharType) { //字符串处理完毕，最后一个字符是字母或数字/标点
            case CONTENT_TYPE_WORD:
               this.codeWord(sWordAndPunctuation);
               break;
            case CONTENT_TYPE_PUNCTUATION:
               this.codePunctuation(sWordAndPunctuation);
               break;
            default:
               break;
         }

         return -1;
      }
   }

   /**
    * 根据Term对象的类型返回对应的XML标签字符串.
    * @return XML标签字符串
    */
   public String getTag() {
      switch (this.igContentType) {
         case CONTENT_TYPE_WORD:
            if (!Objects.equals(this.sgWordEmphasis, "")) {
               return "<w equiv=\"" + this.sgTranslatedWord + "\" em=\""
                       + this.sgWordEmphasis + "\">"
                       + this.sgOriginalWord + "</w>";
            }

            return "<w>" + this.sgOriginalWord + "</w>";
         case CONTENT_TYPE_PUNCTUATION:
            if (!Objects.equals(this.sgPunctuationEmphasis, "")) {
               return "<p equiv=\"" + this.sgPunctuation + "\" em=\""
                       + this.sgPunctuationEmphasis + "\">" + this.sgPunctuation
                       + this.sgPunctuationEmphasis + "</p>";
            }

            return "<p>" + this.sgPunctuation + "</p>";
         case CONTENT_TYPE_EMOTICON:
            if (this.igEmoticonStrength == 0) {
               return "<e>" + this.sgEmoticon + "</e>";
            } else {
               if (this.igEmoticonStrength == 1) {
                  return "<e em=\"+\">" + this.sgEmoticon + "</e>";
               }

               return "<e em=\"-\">" + this.sgEmoticon + "</e>";
         }
         default:
            return "";
      }
   }

   /**
    * 获得单词在sentimentwords列表里面的位置（下标）.
    * @return 单词在sentimentwords列表里面的位置（下标）
    */
   public int getSentimentID() {
      if (!this.bgWordSentimentIDCalculated) {
         this.igWordSentimentID = this.resources.sentimentWords.getSentimentID(this.sgTranslatedWord.toLowerCase());
         this.bgWordSentimentIDCalculated = true;
      }

      return this.igWordSentimentID;
   }

   /**
    * 重写/覆盖情感得分.
    * @param iSentiment 新的情感得分
    */
   public void setSentimentOverrideValue(final int iSentiment) {
      this.bgOverrideSentimentScore = true;
      this.igOverrideSentimentScore = iSentiment;
   }

   /**
    * 获得情感得分.
    * @return 情感得分
    */
   public int getSentimentValue() {
      if (this.bgOverrideSentimentScore) {
         return this.igOverrideSentimentScore;
      } else {
         return this.getSentimentID() < 1 ? 0 : this.resources.sentimentWords.getSentiment(this.igWordSentimentID);
      }
   }

   /**
    * 获得单词强调长度.
    * @return 单词强调长度
    */
   public int getWordEmphasisLength() {
      return this.sgWordEmphasis.length();
   }

   /**
    * 获得单词的强调部分.
    * @return 单词的强调部分
    */
   public String getWordEmphasis() {
      return this.sgWordEmphasis;
   }

   /**
    * 判断是否有情感强度提升（重复字母、重复标点）.
    * @return 是否有情感强度提升
    */
   public boolean containsEmphasis() {
      if (this.igContentType == CONTENT_TYPE_WORD) {
         return this.sgWordEmphasis.length() > 1;
      } else if (this.igContentType == CONTENT_TYPE_PUNCTUATION) {
         return this.sgPunctuationEmphasis.length() > 1;
      } else {
         return false;
      }
   }

   /**
    * 获得修正后的单词.
    * @return 修正后的单词
    */
   public String getTranslatedWord() {
      return this.sgTranslatedWord;
   }

   /**
    * 根据类型得到转换结果（单词、标点、表情符、空）.
    * @return 转换后的单词或标点或表情符或空
    */
   public String getTranslation() {
      if (this.igContentType == CONTENT_TYPE_WORD) {
         return this.sgTranslatedWord;
      } else if (this.igContentType == CONTENT_TYPE_PUNCTUATION) {
         return this.sgPunctuation;
      } else {
         return this.igContentType == CONTENT_TYPE_EMOTICON ? this.sgEmoticon : "";
      }
   }

   /**
    * 获得情绪强度助推分.
    * @return  情绪强度助推分
    */
   public int getBoosterWordScore() {
      if (this.igBoosterWordScore == IGBOOTERWORDSCORE_NOT_SET) {
         this.setBoosterWordScore();
      }

      return this.igBoosterWordScore;
   }

   /**
    * 判断是否均为大写字母.
    * @return 是否均为大写字母
    */
   public boolean isAllCapitals() {
      if (!this.bgAllCaptialsCalculated) {
         if (this.sgOriginalWord == this.sgOriginalWord.toUpperCase()) {
            this.bgAllCapitals = true;
         } else {
            this.bgAllCapitals = false;
         }

         this.bgAllCaptialsCalculated = true;
      }

      return this.bgAllCapitals;
   }

   /**
    * 通过单词设置情绪强度助推分（根据BoosterWordList）.
    */
   public void setBoosterWordScore() {
      this.igBoosterWordScore = this.resources.boosterWords.getBoosterStrength(this.sgTranslatedWord);
   }

   /**
    * 判断是否包含某标点 类型不为标点/包含该标点/在重复标点中.
    * @param sPunctuation 某标点
    * @return 是否包含某标点
    */
   public boolean punctuationContains(final String sPunctuation) {
      if (this.igContentType != CONTENT_TYPE_PUNCTUATION) {
         return false;
      } else if (this.sgPunctuation.indexOf(sPunctuation) > -1) {
         return true;
      } else {
         return !this.sgPunctuationEmphasis.equals("") && this.sgPunctuationEmphasis.indexOf(sPunctuation) > -1;
      }
   }

   /**
    * 获得重复标点长度.
    * @return 重复标点长度
    */
   public int getPunctuationEmphasisLength() {
      return this.sgPunctuationEmphasis.length();
   }

   /**
    * 获得标点的情绪强度.
    * @return 标点的情绪强度
    */
   public int getEmoticonSentimentStrength() {
      return this.igEmoticonStrength;
   }

   /**
    * 获取表情符.
    * @return 表情符
    */
   public String getEmoticon() {
      return this.sgEmoticon;
   }

   /**
    * 获得修正后的标点.
    * @return 修正后的标点
    */
   public String getTranslatedPunctuation() {
      return this.sgPunctuation;
   }

   /**
    * 判断类型是否为单词.
    * @return 是否为单词
    */
   public boolean isWord() {
      return this.igContentType == CONTENT_TYPE_WORD;
   }

   /**
    * 判断类型是否为标点.
    * @return 是否为标点
    */
   public boolean isPunctuation() {
      return this.igContentType == CONTENT_TYPE_PUNCTUATION;
   }

   /**
    * 判断是否为大写字母开头后跟全部小写的单词.
    * @return 是否为大写字母开头后跟全部小写的单词
    */
   public boolean isProperNoun() {
      if (this.igContentType != CONTENT_TYPE_WORD) {
         return false;
      } else {
         if (!this.bgProperNounCalculated) {
            if (this.sgOriginalWord.length() > 1) {
               String sFirstLetter = this.sgOriginalWord.substring(0, 1);
               if (!sFirstLetter.toLowerCase().equals(sFirstLetter.toUpperCase())
                       && !this.sgOriginalWord.substring(0, 2).toUpperCase().equals("I'")) {
                  String sWordRemainder = this.sgOriginalWord.substring(1);
                  if (sFirstLetter.equals(sFirstLetter.toUpperCase())
                          && sWordRemainder.equals(sWordRemainder.toLowerCase())) {
                     this.bgProperNoun = true;
                  }
               }
            }

            this.bgProperNounCalculated = true;
         }

         return this.bgProperNoun;
      }
   }

   /**
    * 判断类型是否为表情符.
    * @return 是否为表情符
    */
   public boolean isEmoticon() {
      return this.igContentType == CONTENT_TYPE_EMOTICON;
   }

   /**
    * 根据Term对象类型返回小写修正后单词/标点/表情/空.
    * @return 小写修正后单词/标点/表情/空
    */
   public String getText() {
      if (this.igContentType == CONTENT_TYPE_WORD) {
         return this.sgTranslatedWord.toLowerCase();
      } else if (this.igContentType == CONTENT_TYPE_PUNCTUATION) {
         return this.sgPunctuation;
      } else {
         return this.igContentType == CONTENT_TYPE_EMOTICON ? this.sgEmoticon : "";
      }
   }

   /**
    * 根据Term类型返回未经处理的原始文本.
    * @return 原始单词/原始标点/表情符/空
    */
   public String getOriginalText() {
      if (this.igContentType == CONTENT_TYPE_WORD) {
         return this.sgOriginalWord;
      } else if (this.igContentType == CONTENT_TYPE_PUNCTUATION) {
         return this.sgPunctuation + this.sgPunctuationEmphasis;
      } else {
         return this.igContentType == CONTENT_TYPE_EMOTICON ? this.sgEmoticon : "";
      }
   }

   /**
    * 判断是否是消极单词.
    * @return 是否是消极单词
    */
   public boolean isNegatingWord() {
      if (!this.bgNegatingWordCalculated) {
         if (this.sgLCaseWord.length() == 0) {
            this.sgLCaseWord = this.sgTranslatedWord.toLowerCase();
         }

         this.bgNegatingWord = this.resources.negatingWords.negatingWord(this.sgLCaseWord);
         this.bgNegatingWordCalculated = true;
      }

      return this.bgNegatingWord;
   }

   /**
    * 当*不在字符串末尾时调用此方法判断是否匹配.
    * @param sText 给定字符串
    * @param bConvertToLowerCase 是否转换为小写
    * @return sgTranslatedWord是否与给定字符串sText匹配
    */
   public boolean matchesString(final String sText, final boolean bConvertToLowerCase) {
      if (sText.length() != this.sgTranslatedWord.length()) {
         return false;
      } else {
         if (bConvertToLowerCase) {
            if (this.sgLCaseWord.length() == 0) {
               this.sgLCaseWord = this.sgTranslatedWord.toLowerCase();
            }

            if (sText.equals(this.sgLCaseWord)) {
               return true;
            }
         } else if (sText.equals(this.sgTranslatedWord)) {
            return true;
         }

         return false;
      }
   }

   /**
    * 判断修正后单词（根据参数判断是否转小写）是否和含有通配符的字符串匹配.
    * @param sTextWithWildcard 带通配符的给定字符串
    * @param bConvertToLowerCase 是否转换为小写
    * @return 单词与给定字符串是否匹配
    */
   public boolean matchesStringWithWildcard(final String sTextWithWildcard, final boolean bConvertToLowerCase) {
      String textWithWildcard = sTextWithWildcard;
      int iStarPos = textWithWildcard.lastIndexOf("*");
      if (iStarPos >= 0 && iStarPos == textWithWildcard.length() - 1) { //星号在末尾可以匹配任何以给定字符串开头的单词
         textWithWildcard = textWithWildcard.substring(0, iStarPos);
         if (bConvertToLowerCase) { //转换为小写进行匹配
            if (this.sgLCaseWord.length() == 0) {
               this.sgLCaseWord = this.sgTranslatedWord.toLowerCase();
            }

            if (textWithWildcard.equals(this.sgLCaseWord)) {
               return true;
            }

            if (textWithWildcard.length() >= this.sgLCaseWord.length()) {
               return false;
            }

            if (textWithWildcard.equals(this.sgLCaseWord.substring(0, textWithWildcard.length()))) {
               return true;
            }
         } else { //修正后单词全部或前一部分与含通配符文本相同则返回true，长度不够则直接返回false
            if (textWithWildcard.equals(this.sgTranslatedWord)) {
               return true;
            }

            if (textWithWildcard.length() >= this.sgTranslatedWord.length()) {
               return false;
            }

            if (textWithWildcard.equals(this.sgTranslatedWord.substring(0, textWithWildcard.length()))) {
               return true;
            }
         }

         return false;
      } else {
         return this.matchesString(textWithWildcard, bConvertToLowerCase);
      }
   }

   /**
    * 对原始字符串进行单词判断和处理.
    * @param sWord 原始字符串
    */
   private void codeWord(final String sWord) {
      StringBuilder sWordNew = new StringBuilder(); //经过处理的单词
      StringBuilder sEm = new StringBuilder(); //单词的强调部分（被处理掉的字母组成的字符串）
      if (this.options.bgCorrectExtraLetterSpellingErrors) { //是否对错误进行修正
         int iSameCount = 0; //连续相同的字母数量
         int iLastCopiedPos = 0; //上一次复制到的在sWord上的位置
         int iWordEnd = sWord.length() - 1;

         int iPos;
         for (iPos = 1; iPos <= iWordEnd; ++iPos) { //遍历每一个字符
            if (sWord.substring(iPos, iPos + 1).compareToIgnoreCase(sWord.substring(iPos - 1, iPos)) == 0) { //若当前字符和前一个字符相同则iSameCount加一
               ++iSameCount;
            } else {
               if (iSameCount > 0
                       && this.options.sgIllegalDoubleLettersInWordMiddle.indexOf(sWord.substring(iPos - 1, iPos)) >= 0) { //之前有相同字符且当前字符和前一个字符构成IllegalDoubleLettersInWordMiddle
                  ++iSameCount;
               }

               if (iSameCount > 1) { //一个以上的连续相同字符，有IllegalDoubleLettersInWordMiddle
                  if (sEm.toString().equals("")) { //首次出现IllegalDoubleLettersInWordMiddle
                     sWordNew = new StringBuilder(sWord.substring(0, iPos - iSameCount + 1));
                     sEm = new StringBuilder(sWord.substring(iPos - iSameCount, iPos - 1));
                     iLastCopiedPos = iPos;
                  } else {
                     sWordNew.append(sWord.substring(iLastCopiedPos, iPos - iSameCount + 1));
                     sEm.append(sWord.substring(iPos - iSameCount, iPos - 1));
                     iLastCopiedPos = iPos;
                  }
               }

               iSameCount = 0; //置为0处理下一个
            }
         }

         //接下来处理单词末尾的IllegalDoubleLettersAtWordEnd情况，代码逻辑与上相同
         if (iSameCount > 0
                 && this.options.sgIllegalDoubleLettersAtWordEnd.indexOf(sWord.substring(iPos - 1, iPos)) >= 0) {
            ++iSameCount;
         }

         if (iSameCount > 1) {
            if (sEm.toString().equals("")) {
               sWordNew = new StringBuilder(sWord.substring(0, iPos - iSameCount + 1));
               sEm = new StringBuilder(sWord.substring(iPos - iSameCount + 1));
            } else {
               sWordNew.append(sWord.substring(iLastCopiedPos, iPos - iSameCount + 1));
               sEm.append(sWord.substring(iPos - iSameCount + 1));
            }
         } else if (!sEm.toString().equals("")) {
            sWordNew.append(sWord.substring(iLastCopiedPos));
         }
      }

      if (sWordNew.toString().equals("")) { //处理后仍为空直接赋值
         sWordNew = new StringBuilder(sWord);
      }

      this.igContentType = CONTENT_TYPE_WORD; //类型设置为单词
      this.sgOriginalWord = sWord;
      this.sgWordEmphasis = sEm.toString();
      this.sgTranslatedWord = sWordNew.toString();
      if (this.sgTranslatedWord.indexOf("@") < 0) { //转化处理后单词不含有@
         if (this.options.bgCorrectSpellingsUsingDictionary) { //是否进行拼写修正
            this.correctSpellingInTranslatedWord();
         }

         if (this.options.bgUseLemmatisation) { //是否进行词形还原
            if (this.sgTranslatedWord.equals("")) {
               sWordNew = new StringBuilder(this.resources.lemmatiser.lemmatise(this.sgOriginalWord));
               if (!sWordNew.toString().equals(this.sgOriginalWord)) {
                  this.sgTranslatedWord = sWordNew.toString();
               }
            } else {
               this.sgTranslatedWord = this.resources.lemmatiser.lemmatise(this.sgTranslatedWord);
            }
         }
      }

   }

   /**
    * 对单词中的拼写错误修正，更新sgTranslatedWord和sgWordEmphasis.
    */
   private void correctSpellingInTranslatedWord() {
      if (!this.resources.correctSpellings.correctSpelling(this.sgTranslatedWord.toLowerCase())) {
         int iLastChar = this.sgTranslatedWord.length() - 1;

         for (int iPos = 1; iPos <= iLastChar; ++iPos) {
            if (this.sgTranslatedWord.substring(iPos, iPos + 1).compareTo(this.sgTranslatedWord.substring(iPos - 1, iPos)) == 0) {
               String sReplaceWord = this.sgTranslatedWord.substring(0, iPos)
                       + this.sgTranslatedWord.substring(iPos + 1); //相邻字母相同只保留一个
               if (this.resources.correctSpellings.correctSpelling(sReplaceWord.toLowerCase())) {
                  this.sgWordEmphasis = this.sgWordEmphasis
                          + this.sgTranslatedWord.substring(iPos, iPos + 1); //若删去一个后拼写正确则添加到sgWordEmphasis
                  this.sgTranslatedWord = sReplaceWord;
                  return;
               }
            }
         }
         if (iLastChar > FIVE) { //对包含haha/hehe的部分进行纠正
            if (this.sgTranslatedWord.indexOf("haha") > 0) {
               this.sgWordEmphasis = this.sgWordEmphasis
                       + this.sgTranslatedWord.substring(THREE, this.sgTranslatedWord.indexOf("haha") + 2);
               this.sgTranslatedWord = "haha";
               return;
            }

            if (this.sgTranslatedWord.indexOf("hehe") > 0) {
               this.sgWordEmphasis = this.sgWordEmphasis
                       + this.sgTranslatedWord.substring(THREE, this.sgTranslatedWord.indexOf("hehe") + 2);
               this.sgTranslatedWord = "hehe";
            }
         }

      }
   }

   /**
    * 对原始字符串进行表情判断和处理.
    * @param sPossibleEmoticon 原始字符串
    * @return 是否为表情符
    */
   private boolean codeEmoticon(final String sPossibleEmoticon) {
      int iEmoticonStrength = this.resources.emoticons.getEmoticon(sPossibleEmoticon);
      if (iEmoticonStrength != NOT_EMOTICON) {
         this.igContentType = CONTENT_TYPE_EMOTICON;
         this.sgEmoticon = sPossibleEmoticon;
         this.igEmoticonStrength = iEmoticonStrength;
         return true;
      } else {
         return false;
      }
   }

   /**
    * 对原始字符串进行标点符号判断和处理.
    * @param sPunctuation 原始字符串
    */
   private void codePunctuation(final String sPunctuation) {
      if (sPunctuation.length() > 1) {
         this.sgPunctuation = sPunctuation.substring(0, 1);
         this.sgPunctuationEmphasis = sPunctuation.substring(1);
      } else {
         this.sgPunctuation = sPunctuation;
         this.sgPunctuationEmphasis = "";
      }

      this.igContentType = CONTENT_TYPE_PUNCTUATION;
   }
}