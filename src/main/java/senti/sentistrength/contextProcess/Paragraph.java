package senti.sentistrength.contextProcess;

import java.util.Random;

import senti.sentistrength.unusedTermsClassificationStrategy.UnusedTermsClassificationStrategy;
import senti.utilities.Sort;
import senti.sentistrength.classification.ClassificationOptions;
import senti.sentistrength.classification.ClassificationResources;
import senti.sentistrength.unusedTermsClassificationStrategy.UnusedTermsContext;
import senti.utilities.StringIndex;

public class Paragraph {
   private Sentence[] sentence;  // Paragraph包含的sentence数组
   private int igSentenceCount = 0; // 该Paragraph由多少sentence组成
   private int[] igSentimentIDList; //该Paragraph包含的每个term对应的sentimentID数组
   private int igSentimentIDListCount = 0;   // igSentimentIDList长度
   private boolean bSentimentIDListMade = false;   // igSentimentIDList是否创建过
   private int igPositiveSentiment = 0;   // 积极情感得分
   private int igNegativeSentiment = 0;   // 消极情感得分
   private int igTrinarySentiment = 0; // Trinary情感得分
   private int igScaleSentiment = 0;   // Scale情感得分
   private ClassificationResources resources;   // 持有的ClassificationResources
   private ClassificationOptions options;    // 持有的ClassificationOptions
   private Random generator = new Random();
   private String sgClassificationRationale = "";  // 分类原理
   private static final int THREE = 3;
   private static final double PARAGRAPH_COMBINE_METHOD = 0.5D;

   /**
    * 将Paragraph拆分成Sentence，再拆分成Term，添加到索引表.
    * 将新的索引添加到主索引中，并为每个术语记录正面和负面类别的数量和差异
    * @param unusedTermsClassificationIndex 情感词汇表中未使用的term的情感极性索引
    * @param iCorrectPosClass 正确的正面类别数
    * @param iEstPosClass 预测的正面类别数
    * @param iCorrectNegClass 正确的负面类别数
    * @param iEstNegClass 预测的负面类别数
    */
   public void addParagraphToIndexWithPosNegValues(
           final UnusedTermsClassificationStrategy unusedTermsClassificationIndex,
           final int iCorrectPosClass, final int iEstPosClass,
           final int iCorrectNegClass, final int iEstNegClass) {
      for (int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      UnusedTermsContext.getStrategy("PosNeg").addNewIndexToMainIndex(iCorrectPosClass,
              iEstPosClass, iCorrectNegClass, iEstNegClass);
   }

   /**
    * 将Paragraph拆分成Sentence，再拆分成Term，添加到索引表.
    * 新的索引添加到主索引中，并根据给定的比例值对索引进行修改
    * @param unusedTermsClassificationIndex 情感词汇表中未使用的term的情感极性索引
    * @param iCorrectScaleClass 正确比例类
    * @param iEstScaleClass 估计比例类
    */
   public void addParagraphToIndexWithScaleValues(
           final UnusedTermsClassificationStrategy unusedTermsClassificationIndex,
           final int iCorrectScaleClass, final int iEstScaleClass) {
      for (int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      UnusedTermsContext.getStrategy("Scale").addNewIndexToMainIndex(iCorrectScaleClass, iEstScaleClass);
   }

   /**
    * 将Paragraph拆分成Sentence，再拆分成Term，添加到索引表.
    * 将文本的二元特征（如是否出现某个词）添加到主索引中，并将其正确和预测的二元类别值用于更新索引中的统计数据
    * @param unusedTermsClassificationIndex 情感词汇表中未使用的term的情感极性索引
    * @param iCorrectBinaryClass 正确的二元类别值
    * @param iEstBinaryClass 预测的二元类别值
    */
   public void addParagraphToIndexWithBinaryValues(final UnusedTermsClassificationStrategy unusedTermsClassificationIndex, final int iCorrectBinaryClass, final int iEstBinaryClass) {
      for (int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      UnusedTermsContext.getStrategy("Binary").addNewIndexToMainIndex(iCorrectBinaryClass, iEstBinaryClass);
   }

   /**
    * 将Paragraph拆分成Sentence，将新的字符串添加到字符串索引.
    * @param stringIndex 字符串索引
    * @param textParsingOptions 文本分析选项
    * @param bRecordCount 是否记录出现次数
    * @param bArffIndex 是否ARFF索引
    * @return 检查的Term数量
    */
   public int addToStringIndex(final StringIndex stringIndex,
                               final TextParsingOptions textParsingOptions,
                               final boolean bRecordCount, final boolean bArffIndex) {
      int iTermsChecked = 0;

      for (int i = 1; i <= this.igSentenceCount; ++i) {
         iTermsChecked += this.sentence[i].addToStringIndex(stringIndex, textParsingOptions, bRecordCount, bArffIndex);
      }

      return iTermsChecked;
   }

   /**
    * 将Paragraph拆分成Sentence，再拆分成Term，添加到索引表.
    * 将一个新的词条的三进制分类值添加到主索引中
    * @param unusedTermsClassificationIndex 情感词汇表中未使用的term的情感极性索引
    * @param iCorrectTrinaryClass 正确的三进制分类值
    * @param iEstTrinaryClass 预测的三进制分类值
    */
   public void addParagraphToIndexWithTrinaryValues(final UnusedTermsClassificationStrategy unusedTermsClassificationIndex, final int iCorrectTrinaryClass, final int iEstTrinaryClass) {
      for (int i = 1; i <= this.igSentenceCount; ++i) {
         this.sentence[i].addSentenceToIndex(unusedTermsClassificationIndex);
      }

      UnusedTermsContext.getStrategy("Trinary").addNewIndexToMainIndex(iCorrectTrinaryClass, iEstTrinaryClass);
   }

   /**
    * 对原始Paragraph进行预处理，存入sentence数组.
    * @param sParagraph 原始Paragraph
    * @param classResources 处理所用资源
    * @param newClassificationOptions 处理选项
    */
   public void setParagraph(final String sParagraph, final ClassificationResources classResources, final ClassificationOptions newClassificationOptions) {
      String sTmpParagraph = sParagraph;
      this.resources = classResources;
      this.options = newClassificationOptions;
      if (sTmpParagraph.indexOf("\"") >= 0) {
         sTmpParagraph = sTmpParagraph.replace("\"", "'");
      }

      int iSentenceEnds = 2;
      int iPos = 0;

      while (iPos >= 0 && iPos < sTmpParagraph.length()) {
         iPos = sTmpParagraph.indexOf("<br>", iPos);
         if (iPos >= 0) {
            iPos += THREE;
            ++iSentenceEnds;
         }
      }

      iPos = 0;

      while (iPos >= 0 && iPos < sTmpParagraph.length()) {
         iPos = sTmpParagraph.indexOf(".", iPos);
         if (iPos >= 0) {
            ++iPos;
            ++iSentenceEnds;
         }
      }

      iPos = 0;

      while (iPos >= 0 && iPos < sTmpParagraph.length()) {
         iPos = sTmpParagraph.indexOf("!", iPos);
         if (iPos >= 0) {
            ++iPos;
            ++iSentenceEnds;
         }
      }

      iPos = 0;

      while (iPos >= 0 && iPos < sTmpParagraph.length()) {
         iPos = sTmpParagraph.indexOf("?", iPos);
         if (iPos >= 0) {
            ++iPos;
            ++iSentenceEnds;
         }
      }

      this.sentence = new Sentence[iSentenceEnds];
      this.igSentenceCount = 0;
      int iLastSentenceEnd = -1;
      boolean bPunctuationIndicatesSentenceEnd = false;
      int iNextBr = sTmpParagraph.indexOf("<br>");
      String sNextSentence = "";

      for (iPos = 0; iPos < sTmpParagraph.length(); ++iPos) {
         String sNextChar = sTmpParagraph.substring(iPos, iPos + 1);
         if (iPos == sTmpParagraph.length() - 1) {
            sNextSentence = sTmpParagraph.substring(iLastSentenceEnd + 1);
         } else if (iPos == iNextBr) {
            sNextSentence = sTmpParagraph.substring(iLastSentenceEnd + 1, iPos);
            iLastSentenceEnd = iPos + THREE;
            iNextBr = sTmpParagraph.indexOf("<br>", iNextBr + 2);
         } else if (this.bIsSentenceEndPunctuation(sNextChar)) {
            bPunctuationIndicatesSentenceEnd = true;
         } else if (sNextChar.compareTo(" ") == 0) {
            if (bPunctuationIndicatesSentenceEnd) {
               sNextSentence = sTmpParagraph.substring(iLastSentenceEnd + 1, iPos);
               iLastSentenceEnd = iPos;
            }
         } else if (this.bIsAlphanumeric(sNextChar) && bPunctuationIndicatesSentenceEnd) {
            sNextSentence = sTmpParagraph.substring(iLastSentenceEnd + 1, iPos);
            iLastSentenceEnd = iPos - 1;
         }

         if (!sNextSentence.equals("")) {
            ++this.igSentenceCount;
            this.sentence[this.igSentenceCount] = new Sentence();
            this.sentence[this.igSentenceCount].setSentence(sNextSentence, this.resources, this.options);
            sNextSentence = "";
            bPunctuationIndicatesSentenceEnd = false;
         }
      }

   }

   /**
    * 获得该paragraph包含的每个term对应的sentimentID数组.
    * @return 该paragraph包含的每个term对应的sentimentID数组
    */
   public int[] getSentimentIDList() {
      if (!this.bSentimentIDListMade) {
         this.makeSentimentIDList();
      }

      return this.igSentimentIDList;
   }

   /**
    * 获得分类原理.
    * @return 分类原理
    */
   public String getClassificationRationale() {
      return this.sgClassificationRationale;
   }

   /**
    * 创建该paragraph包含的每个term对应的sentimentID数组.
    */
   public void makeSentimentIDList() {
      boolean bIsDuplicate = false;
      this.igSentimentIDListCount = 0;

      int i;
      for (i = 1; i <= this.igSentenceCount; ++i) {
         if (this.sentence[i].getSentimentIDList() != null) {
            this.igSentimentIDListCount += this.sentence[i]
                    .getSentimentIDList().length;
         }
      }

      if (this.igSentimentIDListCount > 0) {
         this.igSentimentIDList = new int[this.igSentimentIDListCount + 1];
         this.igSentimentIDListCount = 0;

         for (i = 1; i <= this.igSentenceCount; ++i) {
            int[] sentenceIDList = this.sentence[i].getSentimentIDList();
            if (sentenceIDList != null) {
               for (int j = 1; j < sentenceIDList.length; ++j) {
                  if (sentenceIDList[j] != 0) {
                     bIsDuplicate = false;

                     for (int k = 1; k <= this.igSentimentIDListCount; ++k) {
                        if (sentenceIDList[j] == this.igSentimentIDList[k]) {
                           bIsDuplicate = true;
                           break;
                        }
                     }

                     if (!bIsDuplicate) {
                        this.igSentimentIDList[++this.igSentimentIDListCount] = sentenceIDList[j];
                     }
                  }
               }
            }
         }

         Sort.quickSortInt(this.igSentimentIDList, 1, this.igSentimentIDListCount);
      }

      this.bSentimentIDListMade = true;
   }

   /**
    * 生成paragraph的XML标签字符串.
    * @return paragraph的XML标签字符串
    */
   public String getTaggedParagraph() {
      StringBuilder sTagged = new StringBuilder();

      for (int i = 1; i <= this.igSentenceCount; ++i) {
         sTagged.append(this.sentence[i].getTaggedSentence());
      }

      return sTagged.toString();
   }

   /**
    * 对该paragraph中每个term进行修正.
    * @return 修正后的paragraph
    */
   public String getTranslatedParagraph() {
      StringBuilder sTranslated = new StringBuilder();

      for (int i = 1; i <= this.igSentenceCount; ++i) {
         sTranslated.append(this.sentence[i].getTranslatedSentence());
      }

      return sTranslated.toString();
   }

   /**
    * 重新计算paragraph的情感得分.
    */
   public void recalculateParagraphSentimentScores() {
      for (int iSentence = 1; iSentence <= this.igSentenceCount; ++iSentence) {
         this.sentence[iSentence].recalculateSentenceSentimentScore();
      }

      this.calculateParagraphSentimentScores();
   }

   /**
    * 当情感词发生变化，对已分类的paragraph重新进行情感分类.
    * @param iSentimentWordID 情感关键词ID
    */
   public void reClassifyClassifiedParagraphForSentimentChange(final int iSentimentWordID) {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      } else {
         if (!this.bSentimentIDListMade) {
            this.makeSentimentIDList();
         }

         if (this.igSentimentIDListCount != 0) {
            if (Sort.i_FindIntPositionInSortedArray(iSentimentWordID,
                    this.igSentimentIDList, 1,
                    this.igSentimentIDListCount) >= 0) { //找到所在sentence
               for (int iSentence = 1; iSentence <= this.igSentenceCount; ++iSentence) {
                  this.sentence[iSentence]
                          .reClassifyClassifiedSentenceForSentimentChange(iSentimentWordID);
               }

               this.calculateParagraphSentimentScores();
            }

         }
      }
   }

   /**
    * 获取paragraph积极情感得分.
    * @return 该paragraph积极情感得分
    */
   public int getParagraphPositiveSentiment() {
      if (this.igPositiveSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igPositiveSentiment;
   }

   /**
    * 获取paragraph消极情感得分.
    * @return 该paragraph消极情感得分
    */
   public int getParagraphNegativeSentiment() {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igNegativeSentiment;
   }

   /**
    * 获取paragraph的三元情感值.
    * @return 该paragraph的三元情感值
    */
   public int getParagraphTrinarySentiment() {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igTrinarySentiment;
   }

   /**
    * 获取paragraph的情感得分.
    * @return 该paragraph的情感得分
    */
   public int getParagraphScaleSentiment() {
      if (this.igNegativeSentiment == 0) {
         this.calculateParagraphSentimentScores();
      }

      return this.igScaleSentiment;
   }

   /**
    * 判断一个字符是否为sentence结束标点符号.
    * @param sChar 要判断的字符
    * @return 是否为sentence结束标点符号
    */
   private boolean bIsSentenceEndPunctuation(final String sChar) {
      return sChar.compareTo(".") == 0
              || sChar.compareTo("!") == 0
              || sChar.compareTo("?") == 0;
   }

   /**
    * 判断一个字符是否为字母/数字/其他某些指定特殊字符.
    * @param sChar 被比较的字符
    * @return 是否为字母/数字/其他某些指定特殊字符
    */
   private boolean bIsAlphanumeric(final String sChar) {
      return sChar.compareToIgnoreCase("a") >= 0
              && sChar.compareToIgnoreCase("z") <= 0
              || sChar.compareTo("0") >= 0 && sChar.compareTo("9") <= 0
              || sChar.compareTo("$") == 0 || sChar.compareTo("£") == 0
              || sChar.compareTo("'") == 0;
   }

   /**
    * 获得paragraph的情感得分.
    */
   private void calculateParagraphSentimentScores() {
      this.igPositiveSentiment = 1;
      this.igNegativeSentiment = -1;
      this.igTrinarySentiment = 0;
      if (this.options.bgExplainClassification
              && this.sgClassificationRationale.length() > 0) {
         this.sgClassificationRationale = "";
      }

      int iPosTotal = 0;
      int iPosMax = 0;
      int iNegTotal = 0;
      int iNegMax = 0;
      int iPosTemp = 0;
      int iNegTemp = 0;
      int iSentencesUsed = 0;
      int wordNum = 0;
      int sentiNum = 0;
      if (this.igSentenceCount != 0) {
         int iNegTot;
         for (iNegTot = 1; iNegTot <= this.igSentenceCount; ++iNegTot) {
            iNegTemp = this.sentence[iNegTot].getSentenceNegativeSentiment();
            iPosTemp = this.sentence[iNegTot].getSentencePositiveSentiment();
            wordNum += this.sentence[iNegTot].getIgTermCount();
            sentiNum += this.sentence[iNegTot].getIgSentiCount();
            if (iNegTemp != 0 || iPosTemp != 0) {
               iNegTotal += iNegTemp;
               ++iSentencesUsed;
               if (iNegMax > iNegTemp) {
                  iNegMax = iNegTemp;
               }

               iPosTotal += iPosTemp;
               if (iPosMax < iPosTemp) {
                  iPosMax = iPosTemp;
               }
            }

            if (this.options.bgExplainClassification) {
               this.sgClassificationRationale = this.sgClassificationRationale
                       + this.sentence[iNegTot].getClassificationRationale() + " ";
            }
         }
         
         int var10000;
         if (iNegTotal == 0) {
            var10000 = this.options.igEmotionParagraphCombineMethod;
            this.options.getClass();
            if (var10000 != 2) {
               this.igPositiveSentiment = 0;
               this.igNegativeSentiment = 0;
               this.igTrinarySentiment = this.binarySelectionTieBreaker();
               return;
            }
         }

         var10000 = this.options.igEmotionParagraphCombineMethod;
         this.options.getClass();
         if (var10000 == 1) {
            this.igPositiveSentiment = (int) ((double) ((float) iPosTotal / (float) iSentencesUsed)
                    + PARAGRAPH_COMBINE_METHOD);
            this.igNegativeSentiment = (int) ((double) ((float) iNegTotal / (float) iSentencesUsed)
                    - PARAGRAPH_COMBINE_METHOD);
            if (this.options.bgExplainClassification) {
               this.sgClassificationRationale = this.sgClassificationRationale
                       + "[result = average (" + iPosTotal
                       + " and " + iNegTotal + ") of "
                       + iSentencesUsed + " sentences]";
            }
         } else {
            var10000 = this.options.igEmotionParagraphCombineMethod;
            this.options.getClass();
            if (var10000 == 2) {
               this.igPositiveSentiment = iPosTotal;
               this.igNegativeSentiment = iNegTotal;
               if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale = this.sgClassificationRationale
                          + "[result: total positive; total negative]";
               }
            } else {
               this.igPositiveSentiment = iPosMax;
               this.igNegativeSentiment = iNegMax;
               if (this.options.bgExplainClassification) {
                  this.sgClassificationRationale = this.sgClassificationRationale
                          + "[result: max + and - of any sentence]";
               }
            }
         }

         var10000 = this.options.igEmotionParagraphCombineMethod;
         this.options.getClass();
         if (var10000 != 2) {
            if (this.igPositiveSentiment == 0) {
               this.igPositiveSentiment = 1;
            }

            if (this.igNegativeSentiment == 0) {
               this.igNegativeSentiment = -1;
            }
         }

         if (this.options.bgScaleMode) {
            this.igScaleSentiment = this.igPositiveSentiment
                    + this.igNegativeSentiment;
            if (this.options.bgExplainClassification) {
               this.sgClassificationRationale = this.sgClassificationRationale
                       + "[scale result = sum of pos and neg scores]";
            }

         } else {
            var10000 = this.options.igEmotionParagraphCombineMethod;
            this.options.getClass();
            if (var10000 == 2) {
               if (this.igPositiveSentiment == 0
                       && this.igNegativeSentiment == 0) {
                  if (this.options.bgBinaryVersionOfTrinaryMode) {
                     this.igTrinarySentiment = this.options.igDefaultBinaryClassification;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[binary result set to default value]";
                     }
                  } else {
                     this.igTrinarySentiment = 0;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[trinary result 0 as pos=1, neg=-1]";
                     }
                  }
               } else {
                  if ((float) this.igPositiveSentiment
                          > this.options.fgNegativeSentimentMultiplier * (float) (-this.igNegativeSentiment)) {
                     this.igTrinarySentiment = 1;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[overall result 1 as pos > -neg * "
                                + this.options.fgNegativeSentimentMultiplier
                                + "]";
                     }

                     return;
                  }

                  if ((float) this.igPositiveSentiment
                          < this.options.fgNegativeSentimentMultiplier * (float) (-this.igNegativeSentiment)) {
                     this.igTrinarySentiment = -1;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[overall result -1 as pos < -neg * "
                                + this.options.fgNegativeSentimentMultiplier
                                + "]";
                     }

                     return;
                  }

                  if (this.options.bgBinaryVersionOfTrinaryMode) {
                     this.igTrinarySentiment = this.options.igDefaultBinaryClassification;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[binary result = default value as pos = -neg * "
                                + this.options.fgNegativeSentimentMultiplier
                                + "]";
                     }
                  } else {
                     this.igTrinarySentiment = 0;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[trinary result = 0 as pos = -neg * "
                                + this.options.fgNegativeSentimentMultiplier
                                + "]";
                     }
                  }
               }
            } else {
               if (this.igPositiveSentiment == 1
                       && this.igNegativeSentiment == -1) {
                  if (this.options.bgBinaryVersionOfTrinaryMode) {
                     this.igTrinarySentiment = this.binarySelectionTieBreaker();
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[binary result = default value as pos=1 neg=-1]";
                     }
                  } else {
                     this.igTrinarySentiment = 0;
                     if (this.options.bgExplainClassification) {
                        this.sgClassificationRationale = this.sgClassificationRationale
                                + "[trinary result = 0 as pos=1 neg=-1]";
                     }
                  }

                  return;
               }

               if (this.igPositiveSentiment > -this.igNegativeSentiment) {
                  this.igTrinarySentiment = 1;
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale
                             + "[overall result = 1 as pos>-neg]";
                  }

                  return;
               }

               if (this.igPositiveSentiment < -this.igNegativeSentiment) {
                  this.igTrinarySentiment = -1;
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale
                             + "[overall result = -1 as pos<-neg]";
                  }

                  return;
               }

               iNegTot = 0;
               int iPosTot = 0;

               for (int iSentence = 1; iSentence <= this.igSentenceCount; ++iSentence) {
                  iNegTot += this.sentence[iSentence]
                          .getSentenceNegativeSentiment();
                  iPosTot = this.sentence[iSentence]
                          .getSentencePositiveSentiment();
               }

               if (this.options.bgBinaryVersionOfTrinaryMode
                       && iPosTot == -iNegTot) {
                  this.igTrinarySentiment = this.binarySelectionTieBreaker();
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale
                             + "[binary result = default as posSentenceTotal>-negSentenceTotal]";
                  }
               } else {
                  if (this.options.bgExplainClassification) {
                     this.sgClassificationRationale = this.sgClassificationRationale
                             + "[overall result = largest of posSentenceTotal, negSentenceTotal]";
                  }

                  if (iPosTot > -iNegTot) {
                     this.igTrinarySentiment = 1;
                  } else {
                     this.igTrinarySentiment = -1;
                  }
               }
            }

         }
      }
   }

   /**
    * 确定二元分类标签.
    * @return 表示当分类器使用二元模式进行分类时默认的分类结果（1表示正向情感，-1表示负向情感）
    */
   private int binarySelectionTieBreaker() {
      if (this.options.igDefaultBinaryClassification != 1
              && this.options.igDefaultBinaryClassification != -1) {
         if (this.generator.nextDouble() > PARAGRAPH_COMBINE_METHOD) {
            return 1;
         } else {
            return -1;
         }
      } else {
         return this.options.igDefaultBinaryClassification;
//         return this.options.igDefaultBinaryClassification != 1 && this.options.igDefaultBinaryClassification != -1 ? this.options.igDefaultBinaryClassification : this.options.igDefaultBinaryClassification;
      }
   }
}
