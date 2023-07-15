package senti.sentistrength.wordsList;

import senti.sentistrength.classification.ClassificationOptions;

public class WordsListExtend {
    private static final int USELESS = -99;
    /**
     * 含参的BoosterWordsList初始化函数.
     *
     * @param sFilename 包含初始化所需数据的文件文件名
     * @param options 读取文件的可选方式，是否使用UTF8编码
     * @param iExtraBlankArrayEntriesToInclude 添加的预留额外空白记录行数
     * @return 初始化是否成功
     */
    public boolean initialise(
            final String sFilename, final ClassificationOptions options,
            final int iExtraBlankArrayEntriesToInclude) {
        return false;
    }
    /**
     * 在BoosterWordsList中查找目标词汇的强度.
     *
     * @param sgTranslatedWord 目标booster词汇
     * @return booster词汇强度
     */
    public int getBoosterStrength(final String sgTranslatedWord) {
        return USELESS;
    }
    /**
     * igIdiomCount的外部访问方法.
     * @return IdiomList的私有变量igIdiomCount
     */
    public int getIgIdiomCount() {
        return -1;
    }
    /**
     * igIdiomWordCount的外部访问方法.
     * @return IdiomList的私有变量igIdiomWordCount
     */
    public int[] getIgIdiomWordCount() {
        return null;
    }
    /**
     * sgIdiomWords的外部访问方法.
     * @return IdiomList的私有变量sgIdiomWords
     */
    public String[][] getSgIdiomWords() {
        return null;
    }
    /**
     * 根据给定的idiom编号，查找出对应的idiom.
     * @param iIdiom 目标idiom的id编号
     * @return 查找到的idiom
     */
    public String getIdiom(final int iIdiom) {
        return "";
    }
    /**
     * igIdiomStrength的外部访问方法.
     * @return IdiomList的私有变量igIdiomStrength
     */
    public int[] getIgIdiomStrength() {
        return null;
    }
    /**
     * 添加额外idiom的函数.
     * @param sDatum 待添加的idiom
     * @param iStrength 该idiom的强度
     * @param b 是否在添加完新的idiom后，将sgIdioms拆分且更新到sgIdiomWords中
     * @return 添加是否成功
     */
    public boolean addExtraIdiom(final String sDatum, final int iStrength, final boolean b) {
        return false;
    }
    /**
     * 将sgIdioms内的习语字符串按单词拆分，并且按顺序存入sgIdiomWords中对应的字符串数组(限制：习语的单词数不能大于8个).
     */
    public void convertIdiomStringsToWordLists() {
    }
}
