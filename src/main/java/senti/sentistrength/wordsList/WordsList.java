package senti.sentistrength.wordsList;

import senti.sentistrength.classification.ClassificationOptions;

public class WordsList {
    /**
     * QuestionWords的含参初始化函数.
     *
     * @param sFilename 记录初始化所需数据的文件文件名
     * @param options 读取文件方式，是否强制UTF8编码
     * @return 初始化是否成功
     */
    public boolean initialise(final String sFilename, final ClassificationOptions options) {
        return false;
    }
    /**
     * 判断目标词汇是否属于negating word.
     *
     * @param sgLCaseWord 待判断词汇
     * @return 是否属于negating word
     */
    public boolean negatingWord(final String sgLCaseWord) {
        return false;
    }
    /**
     * 判断目标词汇是否正确拼写的函数.
     *
     * @param toLowerCase 目标词汇
     * @return 词汇拼写是否正确
     */
    public boolean correctSpelling(final String toLowerCase) {
        return false;
    }
    /**
     * 获取目标情绪词汇的情感强度.
     *
     * @param sPossibleEmoticon 目标情绪词汇
     * @return 情感强度
     */
    public int getEmoticon(final String sPossibleEmoticon) {
        return -1;
    }
    /**
     * 判断目标词汇是否属于question word.
     *
     * @param toLowerCase 待判断词汇
     * @return 是否属于question word
     */
    public boolean questionWord(final String toLowerCase) {
        return false;
    }
    /**
     * 判断目标词汇是否ironic的函数.
     *
     * @param text 目标词汇
     * @return 布尔值的判断结果
     */
    public boolean termIsIronic(final String text) {
        return false;
    }
}
