package senti.utilities;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.File;

public class XMLUtil {
    /**
     * 该方法用于从XML配置文件中提取图表类型，并返回类型名.
     *
     * @param name
     * @return
     */
    public static String getWordsListType(String name) {
        try {
            //创建文档对象
            DocumentBuilderFactory dFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = dFactory.newDocumentBuilder();
            Document doc;
                doc = builder.parse(new File("src/main/resources/xmls/WordsList.xml"));
            //获取文本节点
            NodeList nl = doc.getElementsByTagName(name);
            Node classNode = nl.item(0).getFirstChild();
            return classNode.getNodeValue().trim();
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}

