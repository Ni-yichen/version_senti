// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst 
// Source File Name:   ClassificationStatistics.java

package senti.sentistrength.classification;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//评估分类模型的性能指标
public class ClassificationStatistics {
    private static final Logger LOGGER = LoggerFactory.getLogger("ClassificationStatistics");
    /**
     * 无参构造函数.
     */
    ClassificationStatistics() {
        //do nothing
    }

    /**
     * 计算两个向量的绝对相关系数.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值的向量
     * @param iCount 向量中元素的数量
     * @return 相关系数的绝对值
     */
    public static double correlationAbs(final int[] iCorrect, final int[] iPredicted, final int iCount) {
        double fMeanC = 0.0D;
        double fMeanP = 0.0D;
        double fProdCP = 0.0D;
        double fSumCSq = 0.0D;
        double fSumPSq = 0.0D;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            fMeanC += Math.abs(iCorrect[iRow]);
            fMeanP += Math.abs(iPredicted[iRow]);
        }

        fMeanC /= iCount;
        fMeanP /= iCount;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            fProdCP += (Math.abs(iCorrect[iRow]) - fMeanC) * (Math.abs(iPredicted[iRow]) - fMeanP);
            fSumPSq += Math.pow(Math.abs(iPredicted[iRow]) - fMeanP, 2D);
            fSumCSq += Math.pow(Math.abs(iCorrect[iRow]) - fMeanC, 2D);
        }

        return fProdCP / (Math.sqrt(fSumPSq) * Math.sqrt(fSumCSq));
    }

    /**
     * 计算两个向量的相关系数，与correlationAbs方法相同，只不过最后得到的是相关系数的有符号值.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值的向量
     * @param iCount 向量中元素的数量
     * @return 相关系数
     */
    public static double correlation(final int[] iCorrect, final int[] iPredicted, final int iCount) {
        double fMeanC = 0.0D;
        double fMeanP = 0.0D;
        double fProdCP = 0.0D;
        double fSumCSq = 0.0D;
        double fSumPSq = 0.0D;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            fMeanC += iCorrect[iRow];
            fMeanP += iPredicted[iRow];
        }

        fMeanC /= iCount;
        fMeanP /= iCount;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            fProdCP += (iCorrect[iRow] - fMeanC) * (iPredicted[iRow] - fMeanP);
            fSumPSq += Math.pow(iPredicted[iRow] - fMeanP, 2D);
            fSumCSq += Math.pow(iCorrect[iRow] - fMeanC, 2D);
        }

        return fProdCP / (Math.sqrt(fSumPSq) * Math.sqrt(fSumCSq));
    }

    /**
     * 计算三分类或二分类混淆矩阵.
     *
     * @param iTrinaryEstimate 预测值向量
     * @param iTrinaryCorrect 正确值的向量
     * @param iDataCount 向量中元素的数量
     * @param estCorr 计算结果矩阵
     */
    public static void TrinaryOrBinaryConfusionTable(final int[] iTrinaryEstimate, final int[] iTrinaryCorrect, final int iDataCount, final int[][] estCorr) {
        for (int i = 0; i <= 2; i++) {
            for (int j = 0; j <= 2; j++) {
                estCorr[i][j] = 0;
            }
        }
        for (int i = 1; i <= iDataCount; i++) {
            if (iTrinaryEstimate[i] > -2 && iTrinaryEstimate[i] < 2 && iTrinaryCorrect[i] > -2 && iTrinaryCorrect[i] < 2) {
                estCorr[iTrinaryEstimate[i] + 1][iTrinaryCorrect[i] + 1]++;
            } else {
                String message = "Estimate or correct value " + i
                        + " out of range -1 to +1 (data count may be wrong): "
                        + iTrinaryEstimate[i] + " "
                        + iTrinaryCorrect[i];
                LOGGER.error(message);
            }
        }
    }

    /**
     * 计算两个向量的绝对相关系数，但只计算选定的元素.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值向量
     * @param bSelected 指示哪些元素被选择
     * @param bInvert 是否选择未选定的元素
     * @param iCount 向量中元素的数量
     * @return 选定元素的绝对相关系数
     */
    public static double correlationAbs(final int[] iCorrect, final int[] iPredicted, final boolean[] bSelected, final boolean bInvert, final int iCount) {
        double fMeanC = 0.0D;
        double fMeanP = 0.0D;
        double fProdCP = 0.0D;
        double fSumCSq = 0.0D;
        double fSumPSq = 0.0D;
        int iDataCount = 0;
        for (int iRow = 1; iRow <= iCount; iRow++) { //?
            if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
                fMeanC += Math.abs(iCorrect[iRow]);
                fMeanP += Math.abs(iPredicted[iRow]);
                iDataCount++;
            }
        }

        fMeanC /= iDataCount;
        fMeanP /= iDataCount;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
                fProdCP += (Math.abs(iCorrect[iRow]) - fMeanC) * (Math.abs(iPredicted[iRow]) - fMeanP);
                fSumPSq += Math.pow(Math.abs(iPredicted[iRow]) - fMeanP, 2D);
                fSumCSq += Math.pow(Math.abs(iCorrect[iRow]) - fMeanC, 2D);
            }
        }

        return fProdCP / (Math.sqrt(fSumPSq) * Math.sqrt(fSumCSq));
    }

    /**
     * 计算分类器的准确性.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值向量
     * @param iCount 向量元素数量
     * @param bChangeSignOfOneArray 否更改向量的符号
     * @return 准确性
     */
    public static int accuracy(final int[] iCorrect, final int[] iPredicted, final int iCount, final boolean bChangeSignOfOneArray) {
        int iCorrectCount = 0;
        if (bChangeSignOfOneArray) {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                if (iCorrect[iRow] == -iPredicted[iRow]) {
                    iCorrectCount++;
                }
            }
        } else {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                if (iCorrect[iRow] == iPredicted[iRow]) {
                    iCorrectCount++;
                }
            }
        }
        return iCorrectCount;
    }

    /**
     * 方法计算在一个分类模型中，正确分类的样本数目.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值向量
     * @param bSelected 指示哪些元素被选择
     * @param bInvert 是否选择未选定的元素
     * @param iCount 向量中元素的数量
     * @return 正确分类的样本数目
     */
    public static int accuracy(final int[] iCorrect, final int[] iPredicted, final boolean[] bSelected, final boolean bInvert, final int iCount) {
        int iCorrectCount = 0;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            if ((bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) && iCorrect[iRow] == iPredicted[iRow]) {
                iCorrectCount++;
            }
        }
        return iCorrectCount;
    }


    /**
     * 方法计算在一个分类模型中，正确分类或者最多偏离 1个单位的样本数目.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值的向量
     * @param iCount 向量中元素的数量
     * @param bChangeSignOfOneArray 是否反转 iCorrect 数组中的元素符号
     * @return 样本数
     */
    public static int accuracyWithin1(final int[] iCorrect, final int[] iPredicted, final int iCount, final boolean bChangeSignOfOneArray) {
        int iCorrectCount = 0;
        if (bChangeSignOfOneArray) {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                if (Math.abs(iCorrect[iRow] + iPredicted[iRow]) <= 1) {
                    iCorrectCount++;
                }
            }
        } else {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                if (Math.abs(iCorrect[iRow] - iPredicted[iRow]) <= 1) {
                    iCorrectCount++;
                }
            }
        }
        return iCorrectCount;
    }

    /**
     * 方法计算在一个回归模型中，预测值与真实值之差的绝对值的平均值.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值的向量
     * @param bSelected 指示哪些元素被选择
     * @param bInvert 是否选择未选定的元素
     * @param iCount 向量中元素的数量
     * @return 平均值
     */
    public static double absoluteMeanPercentageErrorNoDivision(final int[] iCorrect, final int[] iPredicted, final boolean[] bSelected, final boolean bInvert, final int iCount) {
        int iDataCount = 0;
        double fAMeanPE = 0.0D;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
                fAMeanPE += Math.abs(iPredicted[iRow] - iCorrect[iRow]);
                iDataCount++;
            }
        }
        if (iDataCount != 0) {
            return fAMeanPE / iDataCount;
        } else {
            return -1;
        }
    }

    /**
     * 方法计算在一个回归模型中，预测值与真实值之差的绝对值的平均值与真实值之比的绝对值的平均值.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值的向量
     * @param bSelected 指示哪些元素被选择
     * @param bInvert 是否选择未选定的元素
     * @param iCount 向量中元素的数量
     * @return 平均值
     */
    public static double absoluteMeanPercentageError(final int[] iCorrect, final int[] iPredicted, final boolean[] bSelected, final boolean bInvert, final int iCount) {
        int iDataCount = 0;
        double fAMeanPE = 0.0D;
        for (int iRow = 1; iRow <= iCount; iRow++) {
            if (bSelected[iRow] && !bInvert || !bSelected[iRow] && bInvert) {
                fAMeanPE += Math.abs((double) (iPredicted[iRow] - iCorrect[iRow]) / (double) iCorrect[iRow]);
                iDataCount++;
            }
        }
        if (iDataCount != 0) {
            return fAMeanPE / iDataCount;
        } else {
            return -1;
        }
    }

    /**
     * 方法计算在一个回归模型中，预测值与真实值之差的绝对值的平均值.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值的向量
     * @param iCount 向量中元素的数量
     * @param bChangeSignOfOneArray 是否反转 iCorrect 数组中的元素符号
     * @return 平均值
     */
    public static double absoluteMeanPercentageErrorNoDivision(final int[] iCorrect, final int[] iPredicted, final int iCount, final boolean bChangeSignOfOneArray) {
        double fAMeanPE = 0.0D;
        if (bChangeSignOfOneArray) {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                fAMeanPE += Math.abs(iPredicted[iRow] + iCorrect[iRow]);
            }
        } else {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                fAMeanPE += Math.abs(iPredicted[iRow] - iCorrect[iRow]);
            }
        }
        return fAMeanPE / iCount;
    }

    /**
     * 方法计算在一个分类模型中，最多的类别所占比例.
     *
     * @param iCorrect  正确值向量
     * @param iCount 向量中元素的数量
     * @return 占比
     */
    public static double baselineAccuracyMajorityClassProportion(final int[] iCorrect, final int iCount) {
        if (iCount == 0) {
            return 0.0D;
        }
        int[] iClassCount = new int[100];
        int iMinClass = iCorrect[1];
        int iMaxClass = iCorrect[1];
        for (int i = 2; i <= iCount; i++) {
            if (iCorrect[i] < iMinClass) {
                iMinClass = iCorrect[i];
            }
            if (iCorrect[i] > iMaxClass) {
                iMaxClass = iCorrect[i];
            }
        }

        if (iMaxClass - iMinClass >= 100) {
            return 0.0D;
        }

        for (int i = 0; i <= iMaxClass - iMinClass; i++) {
            iClassCount[i] = 0;
        }

        for (int i = 1; i <= iCount; i++) {
            iClassCount[iCorrect[i] - iMinClass]++;
        }

        int iMaxClassCount = 0;
        for (int i = 0; i <= iMaxClass - iMinClass; i++) {
            if (iClassCount[i] > iMaxClassCount) {
                iMaxClassCount = iClassCount[i];
            }
        }

        return (double) iMaxClassCount / (double) iCount;
    }

    /**
     * 方法将预测值设为最多的类别.
     *
     * @param iCorrect 正确值向量
     * @param iPredict 预测值向量
     * @param iCount 向量中元素的数量
     * @param bChangeSign 是否反转 iCorrect 数组中的元素符号
     */
    public static void baselineAccuracyMakeLargestClassPrediction(final int[] iCorrect, final int[] iPredict, final int iCount, final boolean bChangeSign) {
        if (iCount == 0) {
            return;
        }
        int[] iClassCount = new int[100];
        int iMinClass = iCorrect[1];
        int iMaxClass = iCorrect[1];
        for (int i = 2; i <= iCount; i++) {
            if (iCorrect[i] < iMinClass) {
                iMinClass = iCorrect[i];
            }
            if (iCorrect[i] > iMaxClass) {
                iMaxClass = iCorrect[i];
            }
        }

        if (iMaxClass - iMinClass >= 100) {
            return;
        }
        for (int i = 0; i <= iMaxClass - iMinClass; i++) {
            iClassCount[i] = 0;
        }

        for (int i = 1; i <= iCount; i++) {
            iClassCount[iCorrect[i] - iMinClass]++;
        }

        int iMaxClassCount = 0;
        int iLargestClass = 0;
        for (int i = 0; i <= iMaxClass - iMinClass; i++) {
            if (iClassCount[i] > iMaxClassCount) {
                iMaxClassCount = iClassCount[i];
                iLargestClass = i + iMinClass;
            }
        }
        if (bChangeSign) {
            for (int i = 1; i <= iCount; i++) {
                iPredict[i] = -iLargestClass;
            }
        } else {
            for (int i = 1; i <= iCount; i++) {
                iPredict[i] = iLargestClass;
            }
        }
    }

    /**
     * 计算绝对平均百分比误差（Absolute Mean Percentage Error, AMPE）的方法
     * 使用循环计算每个元素的 AMPE 值，然后将所有 AMPE 值加起来，并将其除以数组的大小来计算平均值。最后，方法返回平均值作为结果.
     *
     * @param iCorrect 正确值向量
     * @param iPredicted 预测值向量
     * @param iCount  向量中元素的数量
     * @param bChangeSignOfOneArray 是否反转 iCorrect 数组中的元素符号
     * @return AMPE
     */
    public static double absoluteMeanPercentageError(final int[] iCorrect, final int[] iPredicted, final int iCount, final boolean bChangeSignOfOneArray) {
        double fAMeanPE = 0.0D;
        if (bChangeSignOfOneArray) {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                fAMeanPE += Math.abs((double) (iPredicted[iRow] + iCorrect[iRow]) / (double) iCorrect[iRow]);
            }
        } else {
            for (int iRow = 1; iRow <= iCount; iRow++) {
                fAMeanPE += Math.abs((double) (iPredicted[iRow] - iCorrect[iRow]) / (double) iCorrect[iRow]);
            }
        }
        return fAMeanPE / iCount;
    }
}
