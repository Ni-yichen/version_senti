package senti.test;


import senti.spider.Spider;

public class SpiderTest {
    public static void main(String[] args) {
        try {
//            Spider.spiderRepo("doris",3930, 3930, "trinary", true,true);
//            Spider.spiderRepo("doris",7502, 7502, "trinary", true,true);
            Spider.spiderRepo("doris",11706, 11706, "trinary", true,true);
//            Spider.spiderRepo("doris",17038, 17038, "trinary", true,true);
//            Spider.spiderRepo("doris",17174, 17174, "trinary", true,true);
//            Spider.spiderRepo("doris",17176, 17176, "trinary", true,true);
//            Spider.spiderRepo("doris",17341, 17341, "trinary", true,true);
//            Spider.spiderRepo("doris",17419, 17419, "trinary", true,true);
//            Spider.spiderRepo("doris",17543, 17543, "trinary", true,true);
//            Spider.spiderRepo("doris",17785, 17785, "trinary", true,true);
//            Spider.spiderRepo("doris",17806, 17806, "trinary", true,true);
//            Spider.spiderRepo("doris",17902, 17902, "trinary", true,true);
//            Spider.spiderRepo("doris",17947, 17947, "trinary", true,true);
//            Spider.spiderRepo("doris",18091, 18091, "trinary", true,true);
//            Spider.spiderRepo("doris",18109, 18109, "trinary", true,true);

            //            String tags = Spider.getTags("echarts_6200_6206_trinary");
//            System.out.println(tags);
        }catch (Exception e){
            System.out.println("wrong");
            e.printStackTrace();
        }
    }

}
