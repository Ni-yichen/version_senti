package senti.sentistrength.unusedTermsClassificationStrategy;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.utilities.Trie;

public class Strategy implements UnusedTermsClassificationStrategy{
    protected static final Logger LOGGER = LoggerFactory.getLogger("UnusedTermsClassificationIndex");
    protected static final String CORRECTCLASS = "CorrectClass";
    protected String[] sgTermList; //文本数据集中所有的单词
    protected int igTermListCount; //已经处理的单词数
    protected int igTermListMax; //文本数据集中可能的最大单词数
    protected int[] igTermListLessPtr; //Trie 树中比当前单词小的单词的索引位置
    protected int[] igTermListMorePtr; //Trie 树中比当前单词大的单词的索引位置
    protected int[]  igTermListFreq; //每个单词在数据集中出现的频率
    protected int[] igTermListFreqTemp; //每个单词在当前处理的数据集中出现的频率
    protected int[] igTermListPosClassDiff; //每个单词在处理数据集时预测类别与实际类别之间的差异，针对二元分类问题中的正类别
    protected int[] iTermsAddedIdTemp; //最近添加到数据集中的单词的索引位置
    protected int[] igTermListNegClassDiff; //每个单词在处理数据集时预测类别与实际类别之间的差异，针对二元分类问题中的负类别
    protected int[] igTermListScaleClassDiff; //每个单词在处理数据集时预测类别与实际类别之间的差异，针对多元分类问题中的标度（scale）类别
    protected int[] igTermListBinaryClassDiff; //每个单词在处理数据集时预测类别与实际类别之间的差异，针对二元分类问题
    protected int[] igTermListTrinaryClassDiff; //每个单词在处理数据集时预测类别与实际类别之间的差异，针对三元分类问题
    protected int iTermsAddedIdTempCount; //最近添加到数据集中的单词数
    protected int[][] igTermListPosCorrectClass; //每个单词在处理数据集时预测为正类别的次数与实际为正类别的次数的交叉表
    protected int[][] igTermListNegCorrectClass; //每个单词在处理数据集时预测为负类别的次数与实际为负类别的次数的交叉表
    protected int[][] igTermListScaleCorrectClass; //每个单词在处理数据集时预测为标度类别的次数与实际为标度类别的次数的交叉表
    protected int[][] igTermListBinaryCorrectClass; //每个单词在处理数据集时预测为二元类别的次数与实际为二元类别的次数的交叉表
    protected int[][] igTermListTrinaryCorrectClass; //每个单词在处理数据集时预测为二元类别的次数与实际为三元类别的次数的交叉表
    protected static final int FIFTYTHOUSANDS = 50000;
    /**
     * 无参构造函数.
     */
    public Strategy() {
        sgTermList = null;
        igTermListCount = 0;
        igTermListMax = FIFTYTHOUSANDS;
    }
    @Override
    public void addTermToNewTermIndex(String sTerm) {
        if (sgTermList == null) {
            initialise(true, true, true, true);
        }
        if (sTerm.equals("")) {
            return;
        }
        boolean bDontAddMoreElements = false;
        if (igTermListCount == igTermListMax) {
            bDontAddMoreElements = true;
        }
        int iTermId = Trie.i_GetTriePositionForString(sTerm, sgTermList, igTermListLessPtr,
                igTermListMorePtr, 1, igTermListCount, bDontAddMoreElements);
        if (iTermId > 0) {
            iTermsAddedIdTemp[++iTermsAddedIdTempCount] = iTermId;
            igTermListFreqTemp[iTermId]++;
            if (iTermId > igTermListCount) {
                igTermListCount = iTermId;
            }
        }
    }
    @Override
    public void initialise(boolean bInitialiseScale, boolean bInitialisePosNeg, boolean bInitialiseBinary, boolean bInitialiseTrinary) {
        igTermListCount = 0;
        igTermListMax = FIFTYTHOUSANDS;
        iTermsAddedIdTempCount = 0;
        sgTermList = new String[igTermListMax];
        igTermListLessPtr = new int[igTermListMax + 1];
        igTermListMorePtr = new int[igTermListMax + 1];
        igTermListFreq = new int[igTermListMax + 1];
        igTermListFreqTemp = new int[igTermListMax + 1];
        iTermsAddedIdTemp = new int[igTermListMax + 1];
        if (bInitialisePosNeg) {
            igTermListNegCorrectClass = new int[igTermListMax + 1][5];
            igTermListPosCorrectClass = new int[igTermListMax + 1][5];
            igTermListNegClassDiff = new int[igTermListMax + 1];
            igTermListPosClassDiff = new int[igTermListMax + 1];
        }
        if (bInitialiseScale) {
            igTermListScaleCorrectClass = new int[igTermListMax + 1][9];
            igTermListScaleClassDiff = new int[igTermListMax + 1];
        }
        if (bInitialiseBinary) {
            igTermListBinaryCorrectClass = new int[igTermListMax + 1][2];
            igTermListBinaryClassDiff = new int[igTermListMax + 1];
        }
        if (bInitialiseTrinary) {
            igTermListTrinaryCorrectClass = new int[igTermListMax + 1][3];
            igTermListTrinaryClassDiff = new int[igTermListMax + 1];
        }
    }

    /**
     * 用于打印分类结果的索引.
     *
     * @param sOutputFile 输出文件名
     * @param iMinFreq 打印的最小词频
     */
    public void printIndex(String sOutputFile, int iMinFreq){}


    /**
     * 这个方法用于将新的索引添加到主索引中，并为每个术语记录正面和负面类别的数量和差异.
     *
     * @param iCorrectPosClass 正确的正面类别数
     * @param iEstPosClass 预测的正面类别数
     * @param iCorrectNegClass 正确的负面类别数
     * @param iEstNegClass 预测的负面类别数
     */
    public void addNewIndexToMainIndex(final int iCorrectPosClass, final int iEstPosClass,
                                       final int iCorrectNegClass, final int iEstNegClass){}

    /**
     * 将文本的特征（如是否出现某个词）添加到主索引中，并将其正确和预测的类别值用于更新索引中的统计数据.
     *
     * @param iCorrectClass 正确的类别值
     * @param iEstClass 预测的类别值
     */
    public void addNewIndexToMainIndex(int iCorrectClass, final int iEstClass){}
}
