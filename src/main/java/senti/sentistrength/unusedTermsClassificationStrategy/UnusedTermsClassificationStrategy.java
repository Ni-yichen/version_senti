package senti.sentistrength.unusedTermsClassificationStrategy;

public interface UnusedTermsClassificationStrategy {
    /**
     *将一个新的术语添加到术语列表中的函数。如果术语列表还没有初始化，就会先初始化。如果传入的术语字符串为空，函数会直接返回.
     *
     * @param sTerm 术语
     */
    public void addTermToNewTermIndex(final String sTerm);

    /**
     *初始化一个搜索引擎中的词项列表及其相关的类别和统计信息.
     *
     * @param bInitialiseScale 初始化比例级别相关的信息，包括 igTermListScaleCorrectClass 和 igTermListScaleClassDiff 数组。
     * @param bInitialisePosNeg 初始化正负级别相关的信息，包括 igTermListNegCorrectClass、igTermListPosCorrectClass、igTermListNegClassDiff 和 igTermListPosClassDiff 数组。
     * @param bInitialiseBinary 初始化二元级别相关的信息，包括 igTermListBinaryCorrectClass 和 igTermListBinaryClassDiff 数组。
     * @param bInitialiseTrinary 初始化三元级别相关的信息，包括 igTermListTrinaryCorrectClass 和 igTermListTrinaryClassDiff 数组。
     */
    public void initialise(final boolean bInitialiseScale, final boolean bInitialisePosNeg,
                           final boolean bInitialiseBinary, final boolean bInitialiseTrinary);

    /**
     * 这个方法用于将新的索引添加到主索引中，并为每个术语记录正面和负面类别的数量和差异.
     *
     * @param iCorrectPosClass 正确的正面类别数
     * @param iEstPosClass 预测的正面类别数
     * @param iCorrectNegClass 正确的负面类别数
     * @param iEstNegClass 预测的负面类别数
     */
    public void addNewIndexToMainIndex(final int iCorrectPosClass, final int iEstPosClass,
                                       final int iCorrectNegClass, final int iEstNegClass);

    /**
     * 将文本的特征（如是否出现某个词）添加到主索引中，并将其正确和预测的类别值用于更新索引中的统计数据.
     *
     * @param iCorrectClass 正确的类别值
     * @param iEstClass 预测的类别值
     */
    public void addNewIndexToMainIndex(int iCorrectClass, final int iEstClass);

    /**
     * 用于打印分类结果的索引.
     *
     * @param sOutputFile 输出文件名
     * @param iMinFreq 打印的最小词频
     */
    public void printIndex(String sOutputFile, int iMinFreq);
}
