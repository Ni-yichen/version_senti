package senti.sentistrength.wordsList;

import senti.utilities.XMLUtil;

public class WordsListFactory {
    /**
     * 工厂模式获得WordsList实例.
     *
     * @param name
     * @return
     */
    public static Object getInstanceByclassName(String name){
        String className = XMLUtil.getWordsListType(name);
        //创建一个字符串类型的对象
        Class c = null;
        try {
            c = Class.forName(className);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        Object obj = null;
        try {
            obj = c != null ? c.newInstance() : null;
        } catch (InstantiationException | IllegalAccessException e) {
            e.printStackTrace();
        }
        return obj;
    }
}
