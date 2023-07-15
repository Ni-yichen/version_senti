package senti.spider;


import com.huaweicloud.sdk.core.auth.BasicCredentials;
import com.huaweicloud.sdk.core.auth.ICredential;
import com.huaweicloud.sdk.core.exception.ConnectionException;
import com.huaweicloud.sdk.core.exception.RequestTimeoutException;
import com.huaweicloud.sdk.core.exception.ServiceResponseException;
import com.huaweicloud.sdk.nlp.v2.NlpClient;
import com.huaweicloud.sdk.nlp.v2.model.RunTextTranslationRequest;
import com.huaweicloud.sdk.nlp.v2.model.RunTextTranslationResponse;
import com.huaweicloud.sdk.nlp.v2.model.TextTranslationReq;
import com.huaweicloud.sdk.nlp.v2.region.NlpRegion;
import com.opencsv.CSVWriter;
import com.opencsv.CSVWriterBuilder;
import com.opencsv.bean.ColumnPositionMappingStrategy;
import com.opencsv.bean.StatefulBeanToCsv;
import com.opencsv.bean.StatefulBeanToCsvBuilder;
import com.opencsv.exceptions.CsvDataTypeMismatchException;
import com.opencsv.exceptions.CsvRequiredFieldEmptyException;
import org.kohsuke.github.*;
import org.springframework.core.io.InputStreamResource;
import senti.sentistrength.SentiStrength;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 使用前请先在根目录下新建result文件夹，即result和src平级
 * 已完成的任务: 每个issue的comments写入一个csv，输入某apache项目的名字，会获取其所有issue
 * 已完成的任务: (接上)并保存在result下, 表格字段除角色外已经填写完毕，表格具体字段参见Form.java
 * 已完成的任务: (接上)并对内容进行初步清洗：包括去除bot的发言，删除``包裹的代码块
 * 已完成的任务: 判断一个用户在本项目中属于什么角色。
 * 已完成的任务: code清洗
 * 已完成的任务: 翻译
 */
public class Spider {
    /*
        github的个人访问令牌，把重要的权限都关了，应该没有大问题
        "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"
        任选
        "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"
        "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"
    */
    private static final String GITHUB_TOKEN = "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";
    private static final String MERGE_PATH = System.getProperty("user.dir") + "/result/merge/";
    private static final String SINGLE_PATH = System.getProperty("user.dir") + "/result/single/";
    private static final String KEYWORD_PATH = System.getProperty("user.dir") + "/src/data/tag_Data/keywordList.txt";

    private static List<GHRelease> _releases =null;
    private static String _option;
    private static boolean _deleteCode;
    private static boolean _translate;

    /**
     * 爬虫并标注和保存单独文件
     */
    public static void spiderRepo(String name, int beginId, int endId, String option, boolean deleteCode, boolean translate) throws IOException {
        _option = option;
        _deleteCode = deleteCode;
        _translate = translate;

        GitHub gitHub = new GitHubBuilder().withOAuthToken(GITHUB_TOKEN).build();
        GHRepository apache = gitHub.getRepository("apache/" + name);
        _releases = apache.listReleases().toList();

        for (int id = beginId; id <= endId; id++) {
            boolean haveBeenSpidered = false;
            File folder = new File(SINGLE_PATH);
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    String fileName = file.getName();
                    String[] splitFileName = fileName.split("_");
                    String currNameStr = splitFileName[0];
                    String currIdStr = splitFileName[1];
                    int currId = Integer.parseInt(currIdStr);
                    String currOptionStr = splitFileName[2].split("\\.")[0];
                    if (Objects.equals(currNameStr, name) &&currId == id&& Objects.equals(currOptionStr, option)){
                        haveBeenSpidered = true;
                        break;
                    }
                }
            }
            if (haveBeenSpidered){
                System.out.println("already spidered and evaluated in this option" + id + " " + option);
                continue;
            }

            //还是使用传入id的方式直接获取所需issue比较好，后续可能会改成一个issue_id_list
            GHIssue issue = apache.getIssue(id);
//            List<GHIssue> issues = new ArrayList<>();
//            issues.add(apache.getIssue(id));

//        //实际上应该是这样获取，但速度很慢，可能要考虑它的第二个参数GHMileStone
            //这个获取方式不好，应该被舍去
//        List<GHIssue> issues = apache.getIssues(GHIssueState.ALL);

            String pathName = SINGLE_PATH + name + "_" + id + "_" + option + ".csv";
            List<Form> results = dealWithAIssue(issue);
            writeTOCSV(results, pathName);
//            issues.forEach(item -> {
//                String pathName = BASE_PATH + name + "_" + item.getNumber() + ".csv";
//                List<Form> results = dealWithAIssue(item, option);
//                writeTOCSV(results, pathName);
//            });
        }
    }

    /**
     * 写入csv
     * @param results list of form 将要被写入的数据，格式参考Form
     * @param path 写入的路径，命名格式为 BASE_PATH/[repo_name]_[issue_number].csv，比如 ./result/doris_6202.csv
     */
    public static void writeTOCSV(List<Form> results, String path) {
        File file = new File(path);
        try (OutputStreamWriter writer = new OutputStreamWriter(new FileOutputStream(path), StandardCharsets.UTF_8)) {
            ColumnPositionMappingStrategy<Form> strategy = new ColumnPositionMappingStrategy<>();
            String[] title = {"文本序号", "用户名", "版本号","时间", "成员角色", "text", "issue_topic", "score", "tag"};
            strategy.setColumnMapping(title);
            strategy.setType(Form.class);
            CSVWriter csvWriter = (CSVWriter) new CSVWriterBuilder(writer)
                    .build();
            csvWriter.writeNext(title, false);
            StatefulBeanToCsv<Form> statefulBeanToCsv = new StatefulBeanToCsvBuilder<Form>(writer)
                    .withMappingStrategy(strategy)
                    .withApplyQuotesToAll(false)
                    .build();
            statefulBeanToCsv.write(results);
        } catch (IOException | CsvRequiredFieldEmptyException | CsvDataTypeMismatchException e) {
            e.printStackTrace();
        }

    }

    /**
     * 对每个issue进行处理
     * @param issue issue对象
     * @return 一个Form的list，将要被写入csv的信息
     */
    public static List<Form> dealWithAIssue(GHIssue issue) {
        Date pulish_time = null;
        String version = "";
        try {
            pulish_time = issue.getCreatedAt();
        } catch (IOException e) {
            e.printStackTrace();
        }
        for(GHRelease release: _releases){//版本号判断
            if(release.getPublished_at().before(pulish_time)){
                version = release.getTagName();
                break;
            }
        }
        List<Form> results = new ArrayList<>();
        String topic = issue.getTitle();
        try {
            List<GHIssueComment> comments = issue.getComments();
            String author = issue.getUser().getLogin();
            int id = 0;
            for (GHIssueComment item : comments) {
                if (!item.getUser().getType().equals("Bot")) {// 这里筛选掉了BOT
                    String text = simpleDealWithText(item.getBody());
                    // sentistrength
                    String precessedText = dealWithText(text);
                    String score = sentistrengthProcess(precessedText);
                    String time = dealWithTime(item.getCreatedAt());
                    // 已完成: 对角色的判断
                    String role = item.getAuthorAssociation().toString();
                    String name = item.getUser().getLogin();
                    if(name.equals(author)){
                        if(role.equals("None")){
                            role = "AUTHOR";
                        }else{
                            role = role + " "+ "AUTHOR";
                        }
                    }
                    String tag = dealWithTag(precessedText+" "+topic);
                    results.add(new Form(id, name, version, time, role, text, topic, score, tag));
                    id++;
                }
            }
            return results;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return results;
    }

    /**
     * 获得去重后的tags
     */
    public static String getTags(String fileName){
        String path = MERGE_PATH + fileName + ".csv";
        String tags = "";
        try(BufferedReader br = new BufferedReader(new FileReader(path))){
            String line;
            line = br.readLine();
            while((line = br.readLine()) != null){
                int index = line.lastIndexOf(",");
                String[] currTags = line.substring(index+1).split(";");
                for(String tag: currTags){
                    if(!tags.contains(tag) && !tag.equals("无")){
                        tags = String.join(";", tags, tag);
                    }
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
        if(tags.equals(""))
            tags = "无";
        else
            tags = tags.substring(1);
        return tags;
    }

    /**
     * 处理tag
     */
    public static String dealWithTag(String comment){
        String result = "";
        try (BufferedReader br = new BufferedReader(new FileReader(KEYWORD_PATH))) {
            String keyword;
            while ((keyword = br.readLine()) != null) {
                if (comment.toLowerCase().contains(keyword)){
                    result = String.join(";", result, keyword);
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        if(result.equals(""))
            result = "无";
        else
            result = result.substring(1);
        return result;
    }

    /**
     * 简单处理一下需要返回的原文，只是删除换行符
     */
    public static String simpleDealWithText(String comment){
        // 删除换行符
        String nextLineReg = "[\n\r]*";
        comment = comment.replaceAll(nextLineReg,"");

        // 删除md格式
        String regex = "[#\\-\\>\\*_~]";
        comment = comment.replaceAll(regex, "");

        return comment;
    }

    /**
     * 删掉代码、换行符、引用内容、@内容、网址
     *
     * @param comment 评论原文
     * @return 经过处理的的评论原文
     */
    public static String dealWithText(String comment) {
        // 翻译
        if(_translate)
            comment = translateToEnglish(comment);
        // 深度删除代码
        if(_deleteCode)
            comment = dealWithCode(comment);

        // 删除换行符
        String nextLineReg = "[\n\r]*";
        comment = comment.replaceAll(nextLineReg,"");

        // 删除引用内容（以 > 开头的行）
        String quote = "^>.*(\n|\r\n)";
        Pattern regex = Pattern.compile(quote, Pattern.MULTILINE);
        Matcher matcher = regex.matcher(comment);
        comment = matcher.replaceAll("");

        // 删除 @ 用户名
        String user = "@\\S+\\s";
        Pattern user_regex = Pattern.compile(user, Pattern.MULTILINE);
        Matcher user_matcher = user_regex.matcher(comment);
        comment = user_matcher.replaceAll("");

        // 删除网址
        String website = "(http|https)://\\S+\\s?";
        Pattern web_regex = Pattern.compile(website, Pattern.MULTILINE);
        Matcher web_matcher = web_regex.matcher(comment);
        comment = web_matcher.replaceAll("");

        // 删除md格式
        String mdRegex = "[#\\-\\>\\*_~]";
        comment = comment.replaceAll(mdRegex, "");

        return comment;
    }

    /**
     * 调用 sentistrength 处理
     * @param text 待处理的comment
     * @return 情感得分
     */
    public static String sentistrengthProcess(String text){
        if(text == null || text.equals(""))
            return "0";
        String [] args=new String[3];
        args[0]="text";
        args[1]= text;
        args[2]= _option;
        SentiStrength classifier = new SentiStrength();
        classifier.initialiseAndRun(args);

        String score_ori = classifier.output;
        String [] scores = score_ori.split(" ");
        return scores[2];
    }

    public static String dealWithTime(Date date){
        DateFormat outputDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        return outputDateFormat.format(date);
    }

    /**
     * 调用华为云api处理任何语言为英文
     */
    public static String translateToEnglish(String comment){

        // dont modify
        String ak = "3ELCUADGDWNOKJ9GNPUV";
        String sk = "d4MTCvrmRJCQygvKZNRvY5CBRPmTy36PSvyT6IrM";

        ICredential auth = new BasicCredentials()
                .withAk(ak)
                .withSk(sk);

        NlpClient client = NlpClient.newBuilder()
                .withCredential(auth)
                .withRegion(NlpRegion.valueOf("cn-north-4"))
                .build();
        RunTextTranslationRequest request = new RunTextTranslationRequest();
        TextTranslationReq body = new TextTranslationReq();
        body.withFrom(TextTranslationReq.FromEnum.fromValue("auto"));
        body.withTo(TextTranslationReq.ToEnum.fromValue("en")); // translate to english
        body.withText(comment); // text to translate
        request.withBody(body);
        try {
            RunTextTranslationResponse response = client.runTextTranslation(request);
            return response.getTranslatedText();
        } catch (ConnectionException | RequestTimeoutException | ServiceResponseException e) {
            e.printStackTrace();
        }
        return comment; // english itself
    }

    /**
     * 删除代码
     * @param comment 评论原文
     * @return 处理后的评论
     */
    public static String dealWithCode(String comment){
        // 删除代码块内容（使用反引号 ` 包围的内容）
        String codeReg = "`([^`]*)`";
        comment = comment.replaceAll(codeReg, "");

        comment = dealWithJavaScript(comment);
        comment = dealWithC(comment);
        comment = dealWithCpp(comment);
        comment = dealWithJava(comment);
        comment = dealWithPython(comment);  // python一定要放在最后面


        // 删除返回语句
        String returnReg = "return\\s+.*?;";
        comment = comment.replaceAll(returnReg, "");

        // 删除嵌对象属性访问和方法调用语句（考虑嵌套）
        String codeSnippetReg = "\\w+(\\.\\w+)*(\\([^()]*\\))+";
        while (true) {
            Pattern pattern = Pattern.compile(codeSnippetReg);
            Matcher matcher = pattern.matcher(comment);
            if (matcher.find()) {
                String matchedCode = matcher.group();
                comment = comment.replace(matchedCode, "");
            } else {
                break;
            }
        }

        return comment;
    }

    public static String dealWithJava(String comment){
        // 删除变量声明和赋值语句
        String variableJavaReg = "\\b((?:byte|short|int|long|float|double|boolean|char|this\\.)?\\s*\\w+\\s*=|\\w+\\s*=)\\s*.*?;";
        comment = comment.replaceAll(variableJavaReg, "");

        // 删除条件语句
        String ifReg = "if\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(ifReg, "");

        // 删除循环语句
        String forReg = "for\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(forReg, "");

        String whileReg = "while\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(whileReg, "");

        String doWhileReg = "do\\s*\\{.*?\\}\\s*while\\s*\\(.*?\\);";
        comment = comment.replaceAll(doWhileReg, "");

        // 删除函数定义
        String functionJavaReg = "\\s*(public|private|protected)?\\s*(static)?\\s*\\w+\\s+\\w+\\(.*?\\)\\s*\\{";
        comment = comment.replaceAll(functionJavaReg, "");

        // 删除异常处理语句
        String tryCatchReg = "try\\s*\\{.*?\\}\\s*catch\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(tryCatchReg, "");

        return comment;
    }

    public static String dealWithC(String comment) {
        // 删除变量声明和赋值语句
        String variableCReg = "\\b((?:int|float|double|char|void)\\s+\\w+\\s*=|\\w+\\s*=)\\s*.*?;";
        comment = comment.replaceAll(variableCReg, "");

        // 删除条件语句
        String ifReg = "if\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(ifReg, "");

        // 删除循环语句
        String forReg = "for\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(forReg, "");

        String whileReg = "while\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(whileReg, "");

        // 删除函数定义
        String functionCReg = "\\w+\\s+\\w+\\s*\\([^;]*?\\)\\s*\\{";
        comment = comment.replaceAll(functionCReg, "");

        return comment;
    }

    public static String dealWithCpp(String comment) {
        // 删除变量声明和赋值语句
        String variableCppReg = "\\b((?:int|float|double|char|void|this->)\\s+\\w+\\s*=|\\w+\\s*=)\\s*.*?;";
        comment = comment.replaceAll(variableCppReg, "");

        // 删除条件语句
        String ifReg = "if\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(ifReg, "");

        // 删除循环语句
        String forReg = "for\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(forReg, "");

        String whileReg = "while\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(whileReg, "");

        // 删除函数定义
        String functionCppReg = "\\w+\\s+\\w+\\s*\\([^;]*?\\)\\s*(const)?\\s*\\{";
        comment = comment.replaceAll(functionCppReg, "");

        return comment;
    }

    public static String dealWithPython(String comment) {
        // 删除变量声明和赋值语句
        String variablePythonReg = "\\b((?:let|const|var|self\\.)?\\s*\\w+\\s*=|\\w+\\s*=)\\s*.*?;";
        comment = comment.replaceAll(variablePythonReg, "");

//        // 删除条件语句
//        String ifReg = "if\\s.*?:\\s*.*?(\\n|$)";
//        comment = comment.replaceAll(ifReg, "");

//        // 删除循环语句
//        String forReg = "for\\s.*?:\\s*.*?(\\n|$)";
//        comment = comment.replaceAll(forReg, "");
//
//        String whileReg = "while\\s.*?:\\s*.*?(\\n|$)";
//        comment = comment.replaceAll(whileReg, "");

        // 删除函数定义
        String functionPythonReg = "\\s*def\\s+\\w+\\(.*?\\)\\s*:";
        comment = comment.replaceAll(functionPythonReg, "");

        // 删除返回语句
        String returnReg = "return\\s+.*";
        comment = comment.replaceAll(returnReg, "");

        // 删除箭头函数
        String arrowFunctionReg = "\\(.*?\\)\\s*=>\\s*.*?(\\n|$)";
        comment = comment.replaceAll(arrowFunctionReg, "");

        return comment;
    }

    public static String dealWithJavaScript(String comment) {
        // 删除变量声明和赋值语句
        String variableJSReg = "\\b((?:let|const|var|this\\.)\\s+\\w+\\s*=|\\w+\\s*=)\\s*.*?;";
        comment = comment.replaceAll(variableJSReg, "");

        // 删除条件语句
        String ifReg = "if\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(ifReg, "");

        // 删除循环语句
        String forReg = "for\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(forReg, "");

        String whileReg = "while\\s*\\(.*?\\)\\s*\\{.*?\\}";
        comment = comment.replaceAll(whileReg, "");

        // 删除箭头函数
        String arrowFunctionReg = "\\([^()]*\\)\\s*=>(?:\\s*\\{(?:[^{}]*|\\{[^{}]*\\})*\\})?";
        comment = comment.replaceAll(arrowFunctionReg, "");

        // 删除函数定义
        String functionJSReg = "\\s*function\\s+\\w+\\(.*?\\)\\s*\\{";
        comment = comment.replaceAll(functionJSReg, "");

        return comment;
    }

    /**
     * 返回合并后的csv
     */
    public static InputStreamResource mergeCSVFiles(String name, int beginId, int endId, String option) throws IOException {
        String mergedFileName = name + "_" + beginId + "_" + endId + "_" + option + ".csv";
        File mergedFile = new File(MERGE_PATH + mergedFileName);

        if (mergedFile.exists()) {
            System.out.println(mergedFileName + " already exists");
            return new InputStreamResource(new FileInputStream(mergedFile));
        }

        BufferedWriter writer = new BufferedWriter(new FileWriter(mergedFile));

        boolean isFirstFile = true;
        int sequence = 0;
        for (int id = beginId; id <= endId; id++) {
            String fileName = SINGLE_PATH + name + "_" + id + "_" + option + ".csv";
            File csvFile = new File(fileName);
            if (csvFile.exists()) {
                BufferedReader reader = new BufferedReader(new FileReader(csvFile));
                boolean isFirstLine = true;
                String line;
                while ((line = reader.readLine()) != null) {
                    if(!isFirstFile && isFirstLine){
                        isFirstLine = false;
                        continue;   // 多余的表头删除
                    }
                    if(isFirstFile && isFirstLine){
                        writer.write(line);
                        writer.newLine();
                        isFirstLine = false;
                        continue;
                    }
                    String[] columns = line.split(",");
                    columns[0] = String.valueOf(sequence); // 为第一列重新赋值
                    sequence ++;
                    String newLine = String.join(",", columns);
                    writer.write(newLine);
                    writer.newLine();
                }
                reader.close();
            }
            isFirstFile = false;
        }

        writer.close();

        return new InputStreamResource(new FileInputStream(mergedFile));
    }

}
