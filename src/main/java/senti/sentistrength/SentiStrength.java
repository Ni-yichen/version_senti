package senti.sentistrength;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.Objects;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import senti.sentistrength.classification.ClassificationOptions;
import senti.sentistrength.contextProcess.Paragraph;
import senti.utilities.FileOps;

public class SentiStrength {
    /**
     * 语料库对象.
     */
    Corpus c;
    private static final Logger LOG = LoggerFactory.getLogger("sentiStrength");
    private static final int LAST_SPACE_POS = 5;
    public String output = "";


    /**
     * SentiStrength的无参构造函数.
     */
    public SentiStrength() {
        this.c = new Corpus();
    }


    /**
     * 主函数.
     *
     * @param args 运行参数
     */
    public static void main(final String[] args) {
        SentiStrength classifier = new SentiStrength();
        classifier.initialiseAndRun(args);
    }


    /**
     * 处理传入的参数，构造持有的corpus对象.
     *
     * @param args 运行参数
     */
    public void initialiseAndRun(final String[] args) {
        Corpus c = this.c;
        // 在文件夹中对列中的/所有的文本进行分类(UC-13/12): 文件路径
        String sInputFile = "";
        // 在文件夹中对列中的/所有的文本进行分类(UC-13/12)：文件夹路径
        String sInputFolder = "";
        //对单个文本进行分类(UC-11)：待分类文本
        String sTextToParse = "";
        // 优化现有情绪术语的情绪强度(UC-27): 对EmotionLookupTable.txt术语权重调整的文件
        String sOptimalTermStrengths = "";
        // 使用sInputFolder时，文件夹中只有匹配本字符串的文件才会被分类
        String sFileSubString = "\t";
        // 输出文件夹的位置(UC-19)，默认为输入文件夹
        String sResultsFolder = "";
        //输出的文件扩展名(UC-20)
        String sResultsFileExtension = "_out.txt";
        boolean[] bArgumentRecognised = new boolean[args.length];
        // UC-29：十折的迭代次数
        int iIterations = 1;
        // UC-29：在训练阶段调整术语权重所需的最少额外正确分类数量
        int iMinImprovement = 2;
        // UC-29: 重复术语强度优化来改变情绪权重, 表现为术语权重n次优化并取平均值
        int iMultiOptimisations = 1;
        // 在端口监听要分类的文本(UC-14): 端口值
        int iListenPort = 0;
        // 在使用文件/文件夹输入时，指定分类列号
        int iTextColForAnnotation = -1;
        // 在使用文件/文件夹输入时，指定id列号
        int iIdCol = -1;
        // 如果数据以多个制表符分隔的列，使用本字段指定需要解析的内容所在的列号，从0开始
        int iTextCol = -1;
        // 机器学习评估(UC-29): 测试上述分类算法参数中列出的所有选项变化，而不是使用默认选项
        boolean bDoAll = false;
        //是否可以重写文件
        boolean bOkToOverwrite = false;
        // 机器学习评估(UC-29): true为启用
        boolean bTrain = false;
        // ? 是否打印不佳分类的新权重
        boolean bReportNewTermWeightsForBadClassifications = false;
        // 处理stdin并发送到stdout(UC-16): true为启用
        boolean bStdIn = false;
        // 从命令行交互运行(UC-15): true为启用
        boolean bCmd = false;
        boolean bWait = false;
        // 机器学习评估(UC-29): 通过正确分类的数量来优化，而不是分类差异的总和
        boolean bUseTotalDifference = true;
        // 使用iListenPort时，本字段必须为true
        boolean bURLEncoded = false;
        // 输入的语言
        String sLanguage = "";

        int i;
        for (i = 0; i < args.length; ++i) {
            bArgumentRecognised[i] = false;
        }

        // 以下是在解析参数，参数含义见上
        for (i = 0; i < args.length; ++i) {
            try {
                if (args[i].equalsIgnoreCase("input")) {
                    sInputFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("inputfolder")) {
                    sInputFolder = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("outputfolder")) {
                    sResultsFolder = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("resultextension")) {
                    sResultsFileExtension = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("filesubstring")) {
                    sFileSubString = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("overwrite")) {
                    bOkToOverwrite = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("text")) {
                    sTextToParse = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("urlencoded")) {
                    bURLEncoded = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("listen")) {
                    iListenPort = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("stdin")) {
                    bStdIn = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("cmd")) {
                    bCmd = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("optimise")) {
                    sOptimalTermStrengths = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("annotatecol")) {
                    iTextColForAnnotation = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("textcol")) {
                    iTextCol = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("idcol")) {
                    iIdCol = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("lang")) {
                    sLanguage = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("train")) {
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("all")) {
                    bDoAll = true;
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("numcorrect")) {
                    bUseTotalDifference = false;
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("iterations")) {
                    iIterations = Integer.parseInt(args[i + 1]);
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("minimprovement")) {
                    iMinImprovement = Integer.parseInt(args[i + 1]);
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("multi")) {
                    iMultiOptimisations = Integer.parseInt(args[i + 1]);
                    bTrain = true;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                }

                if (args[i].equalsIgnoreCase("termWeights")) {
                    bReportNewTermWeightsForBadClassifications = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("wait")) {
                    bWait = true;
                    bArgumentRecognised[i] = true;
                }

                if (args[i].equalsIgnoreCase("help")) {
                    this.printCommandLineOptions();
                    return;
                }
            } catch (NumberFormatException var32) {
                LOG.error("Error in argument for {}. Integer expected!", args[i]);
                return;
            } catch (Exception var33) {
                LOG.error("Error in argument for {}. Argument missing?", args[i]);
                return;
            }
        }
        // 构造语料库对象
        this.parseParametersForCorpusOptions(args, bArgumentRecognised);
        if (sLanguage.length() > 1) {
            Locale l = new Locale(sLanguage);
            Locale.setDefault(l);
        }

        for (i = 0; i < args.length; ++i) {
            if (!bArgumentRecognised[i]) {
                LOG.error("Unrecognised command - wrong spelling or case?: {}", args[i]);
                this.showBriefHelp();
                return;
            }
        }
        if (c.initialise()) {
            if (!Objects.equals(sTextToParse, "")) {
                // UC-11 直接输入待处理文本：
                if (bURLEncoded) {
                    // 以urlEncode形式输入，额外处理
                    try {
                        sTextToParse = URLDecoder.decode(sTextToParse, "UTF-8");
                    } catch (UnsupportedEncodingException var31) {
                        var31.printStackTrace();
                    }
                } else {
                    sTextToParse = sTextToParse.replace("+", " ");
                }
                // 分类
                this.parseOneText(c, sTextToParse, bURLEncoded);
            } else if (iListenPort > 0) {
                // UC-14 监听端口输入待处理文本
                // 分类
                this.listenAtPort(c, iListenPort);
            } else if (bCmd) {
                // UC-15 命令行交互输入的文本
                // 分类
                this.listenForCmdInput(c);
            } else if (bStdIn) {
                // UC-16 stdin输入的文本
                // 分类
                this.listenToStdIn(c, iTextCol);
            } else if (!bWait) {
                if (!Objects.equals(sOptimalTermStrengths, "")) {
                    if (Objects.equals(sInputFile, "")) {
                        LOG.error("Input file must be specified to optimise term weights");
                        return;
                    }
                    // 语料库优化：优化现有情绪术语的情绪强度(UC-27): 对EmotionLookupTable.txt术语权重调整
                    if (c.setCorpus(sInputFile)) {
                        c.optimiseDictionaryWeightingsForCorpus(iMinImprovement, bUseTotalDifference);
                        c.resources.sentimentWords.saveSentimentList(sOptimalTermStrengths, c);
                        LOG.info("Saved optimised term weights to {}", sOptimalTermStrengths);
                    } else {
                        LOG.error("Error: Too few texts in {}", sInputFile);
                    }
                } else if (bReportNewTermWeightsForBadClassifications) {
                    // ？打印不佳分类的新术语权重
                    if (c.setCorpus(sInputFile)) {
                        c.printCorpusUnusedTermsClassificationIndex(FileOps.s_ChopFileNameExtension(sInputFile) + "_unusedTerms.txt", 1);
                    } else {
                        LOG.info("Error: Too few texts in {}", sInputFile);
                    }
                } else if (iTextCol > 0 && iIdCol > 0) {
                    // 在文件夹中对指定列中的文本进行分类(UC-13)
                    this.classifyAndSaveWithID(c, sInputFile, sInputFolder, iTextCol, iIdCol);
                } else if (iTextColForAnnotation > 0) {
                    // 在文件夹中对指定列中的文本进行分类(UC-13): 与上个分支的区别是分类结果写回源文件
                    this.annotationTextCol(c, sInputFile, sInputFolder, sFileSubString, iTextColForAnnotation, bOkToOverwrite);
                } else {
                    if (!Objects.equals(sInputFolder, "")) {
                        LOG.error("Input folder specified but textCol and IDcol or annotateCol needed");
                    }

                    if (Objects.equals(sInputFile, "")) {
                        LOG.error("No action taken because no input file nor text specified");
                        this.showBriefHelp();
                        return;
                    }

                    String sOutputFile = FileOps.getNextAvailableFilename(FileOps.s_ChopFileNameExtension(sInputFile), sResultsFileExtension);
                    if (sResultsFolder.length() > 0) {
                        sOutputFile = sResultsFolder + (new File(sOutputFile)).getName();
                    }

                    if (bTrain) {
                        // 进入机器学习模式
                        this.runMachineLearning(c, sInputFile, bDoAll, iMinImprovement, bUseTotalDifference, iIterations, iMultiOptimisations, sOutputFile);
                    } else {
                        // 否则开始分类，在文件夹中对列中的文本进行分类(UC-13)；与之前的区别是参数没有输入文件夹，直接输入文件
                        --iTextCol;
                        c.classifyAllLinesInInputFile(sInputFile, iTextCol, sOutputFile);
                    }

                    LOG.info("Finished! Results in: {}", sOutputFile);
                }
            }
        } else {
            LOG.error("Failed to initialise!");

            try {
                File f = new File(c.resources.sgSentiStrengthFolder);
                if (!f.exists()) {
                    LOG.info("Folder {} does not exist! ", c.resources.sgSentiStrengthFolder);
                }
            } catch (Exception var30) {
                LOG.info("Folder {} doesn't exist! ", c.resources.sgSentiStrengthFolder);
            }

            this.showBriefHelp();
        }

    }

    /**
     * 解析与 corpus 有关的运行参数，并赋给本类持有的corpus对象 c.
     *
     * @param args                参数
     * @param bArgumentRecognised 记录每一参数是否已解析的数组
     */
    private void parseParametersForCorpusOptions(final String[] args, final boolean[] bArgumentRecognised) {
        for (int i = 0; i < args.length; ++i) {
            ClassificationOptions var10000;
            try {
                if (args[i].equalsIgnoreCase("sentidata")) {
                    // 语言数据文件夹的位置
                    this.c.resources.sgSentiStrengthFolder = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("emotionlookuptable")) {
                    // 情感词权重的位置
                    this.c.resources.sgSentimentWordsFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("additionalfile")) {
                    this.c.resources.sgAdditionalFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("keywords")) {
                    this.c.options.parseKeywordList(args[i + 1].toLowerCase());
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("wordsBeforeKeywords")) {
                    this.c.options.igWordsToIncludeBeforeKeyword = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("wordsAfterKeywords")) {
                    this.c.options.igWordsToIncludeAfterKeyword = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("sentiment")) {
                    this.c.options.nameProgram(false);
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("stress")) {
                    this.c.options.nameProgram(true);
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("trinary")) {
                    // UC-22 采用三元分类 正 负 中性
                    this.c.options.bgTrinaryMode = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("binary")) {
                    // UC-23 采用二元分类 正 负
                    this.c.options.bgBinaryVersionOfTrinaryMode = true;
                    this.c.options.bgTrinaryMode = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("scale")) {
                    // UC-24 采用单一的正负刻度分类 -4到+4
                    this.c.options.bgScaleMode = true;
                    bArgumentRecognised[i] = true;
                    if (this.c.options.bgTrinaryMode) {
                        LOG.info("Must choose binary/trinary OR scale mode");
                        return;
                    }
                } else if (args[i].equalsIgnoreCase("sentenceCombineAv")) {
                    //下文av指: all variations 测试上述分类算法参数中列出的所有选项变化，而不是使用默认选项
                    //下文tot指: 通过正确分类的数量来优化，而不是分类差异的总和
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionSentenceCombineMethod = 1;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("sentenceCombineTot")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionSentenceCombineMethod = 2;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("paragraphCombineAv")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionParagraphCombineMethod = 1;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("paragraphCombineTot")) {
                    var10000 = this.c.options;
                    this.c.options.getClass();
                    var10000.igEmotionParagraphCombineMethod = 2;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("negativeMultiplier")) {
                    // UC-26 设置分类算法参数： 否定时的强度倍增器(默认=0.5)
                    this.c.options.fgNegativeSentimentMultiplier = Float.parseFloat(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("noBoosters")) {
                    // UC-26 设置分类算法参数： 忽略情感助推词，如very
                    this.c.options.bgBoosterWordsChangeEmotion = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noNegatingPositiveFlipsEmotion")) {
                    // UC-26 设置分类算法参数： 不要用否定的词来翻转+ve词
                    this.c.options.bgNegatingPositiveFlipsEmotion = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noNegatingNegativeNeutralisesEmotion")) {
                    // UC-26 设置分类算法参数： 不要用否定的词来中和-ve词
                    this.c.options.bgNegatingNegativeNeutralisesEmotion = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noNegators")) {
                    // ? UC-26 设置分类算法参数： 不要用否定的词来转换情绪
                    this.c.options.bgNegatingWordsFlipEmotion = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noIdioms")) {
                    // UC-26 设置分类算法参数： 忽略习语列表
                    this.c.options.bgUseIdiomLookupTable = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("questionsReduceNeg")) {
                    // UC-26 设置分类算法参数： 疑问句中-ve情绪减少
                    this.c.options.bgReduceNegativeEmotionInQuestionSentences = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noEmoticons")) {
                    // UC-26 设置分类算法参数： 忽略emoticon列表
                    this.c.options.bgUseEmoticons = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("exclamations2")) {
                    // UC-26 设置分类算法参数： 感叹句如果不是-ve句子就算作+2
                    this.c.options.bgExclamationInNeutralSentenceCountsAsPlus2 = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("minPunctuationWithExclamation")) {
                    // ? UC-26 设置分类算法参数： 改变情绪的最小感叹号数量
                    this.c.options.igMinPunctuationWithExclamationToChangeSentenceSentiment = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("mood")) {
                    // UC-26设置分类算法参数: 中性强调的解释(例如miiike;hello!!)
                    // -1表示中性强调,解释为-ve; 1表示解释为+ve; 0表示忽略强调
                    this.c.options.igMoodToInterpretNeutralEmphasis = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("noMultiplePosWords")) {
                    // UC-26 设置分类算法参数： 不允许多个+ve单词增加+ve情绪
                    this.c.options.bgAllowMultiplePositiveWordsToIncreasePositiveEmotion = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noMultipleNegWords")) {
                    // UC-26 设置分类算法参数： 不允许多个-ve单词增加-ve情绪
                    this.c.options.bgAllowMultipleNegativeWordsToIncreaseNegativeEmotion = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noIgnoreBoosterWordsAfterNegatives")) {
                    // UC-26 设置分类算法参数：否定单词后不要忽略助推器
                    this.c.options.bgIgnoreBoosterWordsAfterNegatives = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noDictionary")) {
                    // UC-26 设置分类算法参数：不要试图通过删除未知单词中的重复字母来创建已知单词来使用字典来纠正拼写
                    this.c.options.bgCorrectSpellingsUsingDictionary = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("noDeleteExtraDuplicateLetters")) {
                    // UC-26 设置分类算法参数：不要删除单词中额外的重复字母，即使它们是不可能的，例如，heyyyy)
                    // 与上面的选项相比，此选项不会检查新单词是否合法
                    this.c.options.bgCorrectExtraLetterSpellingErrors = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("illegalDoubleLettersInWordMiddle")) {
                    // UC-26 设置分类算法参数：永远不会在单词中间出现两次的字母列表，通常为 ahijkquvxyz，不要加入w
                    this.c.options.sgIllegalDoubleLettersInWordMiddle = args[i + 1].toLowerCase();
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("illegalDoubleLettersAtWordEnd")) {
                    // UC-26 设置分类算法参数：永远不会在单词结尾出现两次的字母列表，achijkmnpqruvwxyz
                    this.c.options.sgIllegalDoubleLettersAtWordEnd = args[i + 1].toLowerCase();
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("noMultipleLetters")) {
                    // UC-26 设置分类算法参数：不要在一个单词中使用额外的字母来提振情绪
                    this.c.options.bgMultipleLettersBoostSentiment = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("negatedWordStrengthMultiplier")) {
                    // UC-26 设置分类算法参数： 否定时的强度倍增器(默认=0.5)
                    this.c.options.fgStrengthMultiplierForNegatedWords = Float.parseFloat(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("maxWordsBeforeSentimentToNegate")) {
                    // UC-26 设置分类算法参数：否定词和情感词之间的最大单词(默认为0)
                    this.c.options.igMaxWordsBeforeSentimentToNegate = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("negatingWordsDontOccurBeforeSentiment")) {
                    // ? UC-26 设置分类算法参数：否定词不会出现在情绪之前
                    this.c.options.bgNegatingWordsOccurBeforeSentiment = false;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("maxWordsAfterSentimentToNegate")) {
                    // UC-26 设置分类算法参数：否定词和情感词之间的最大单词(默认为0)
                    this.c.options.igMaxWordsAfterSentimentToNegate = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("negatingWordsOccurAfterSentiment")) {
                    // ? UC-26 设置分类算法参数：否定词不会出现在情绪之后
                    this.c.options.bgNegatingWordsOccurAfterSentiment = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("alwaysSplitWordsAtApostrophes")) {
                    // UC-26 设置分类算法参数：当遇到撇号时拆分单词
                    this.c.options.bgAlwaysSplitWordsAtApostrophes = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("capitalsBoostTermSentiment")) {
                    // ? UC-26 设置分类算法参数：大写字母提振情绪
                    this.c.options.bgCapitalsBoostTermSentiment = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("lemmaFile")) {
                    this.c.options.bgUseLemmatisation = true;
                    this.c.resources.sgLemmaFile = args[i + 1];
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("MinSentencePosForQuotesIrony")) {
                    // ? UC-26 设置分类算法参数：引用反语的最小句位
                    this.c.options.igMinSentencePosForQuotesIrony = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("MinSentencePosForPunctuationIrony")) {
                    // ? UC-26 设置分类算法参数：停顿反语的最小句位
                    this.c.options.igMinSentencePosForPunctuationIrony = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("MinSentencePosForTermsIrony")) {
                    // ? UC-26 设置分类算法参数：术语反语的最小句位
                    this.c.options.igMinSentencePosForTermsIrony = Integer.parseInt(args[i + 1]);
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("MinSentencePosForAllIrony")) {
                    // ? UC-26 设置分类算法参数：所有反语的最小句位
                    this.c.options.igMinSentencePosForTermsIrony = Integer.parseInt(args[i + 1]);
                    this.c.options.igMinSentencePosForPunctuationIrony = this.c.options.igMinSentencePosForTermsIrony;
                    this.c.options.igMinSentencePosForQuotesIrony = this.c.options.igMinSentencePosForTermsIrony;
                    bArgumentRecognised[i] = true;
                    bArgumentRecognised[i + 1] = true;
                } else if (args[i].equalsIgnoreCase("explain")) {
                    // UC-25：解释分类，输出对分类的解释
                    this.c.options.bgExplainClassification = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("echo")) {
                    // 输出原句
                    this.c.options.bgEchoText = true;
                    bArgumentRecognised[i] = true;
                } else if (args[i].equalsIgnoreCase("UTF8")) {
                    // 使用utf8编码
                    this.c.options.bgForceUTF8 = true;
                    bArgumentRecognised[i] = true;
                }
//
//                else {
//                    LOG.info("cannot resolve arg: {}. please check your command", args[i]);
//                    LOG.info("program will skip it and keep running···");
//                }
            } catch (NumberFormatException var5) {
                LOG.info("Error in argument for {}. Integer expected!", args[i]);
                return;
            } catch (Exception var6) {
                LOG.info("Error in argument for {}. Argument missing?", args[i]);
                return;
            }
        }

    }

    /**
     * 在指定的数据集上进行机器学习的训练.
     *
     * @param c                   corpus对象
     * @param sInputFile          输入的文件
     * @param bDoAll              是否测试所有分类参数的变化
     * @param iMinImprovement     最小额外正确类数量
     * @param bUseTotalDifference 是否使用通过分类差异的总和优化
     * @param iIterations         迭代次数
     * @param iMultiOptimisations 重复术语强度优化来改变情绪权重次数
     * @param sOutputFile         结果输出路径
     */
    private void runMachineLearning(final Corpus c, final String sInputFile, final boolean bDoAll,
                                    final int iMinImprovement, final boolean bUseTotalDifference,
                                    final int iIterations, final int iMultiOptimisations, final String sOutputFile) {
        if (iMinImprovement < 1) {
            LOG.info("No action taken because min improvement < 1");
            this.showBriefHelp();
        } else {
            c.setCorpus(sInputFile);
            c.calculateCorpusSentimentScores();
            int corpusSize = c.getCorpusSize();
            if (c.options.bgTrinaryMode) {
                if (c.options.bgBinaryVersionOfTrinaryMode) {
                    LOG.info("Before training, binary accuracy: {} {}%",
                            c.getClassificationTrinaryNumberCorrect(),
                            c.getClassificationTrinaryNumberCorrect() * 100.0F / corpusSize
                    );
                } else {
                    LOG.info("Before training, trinary accuracy: {} {}%",
                            c.getClassificationTrinaryNumberCorrect(),
                            c.getClassificationTrinaryNumberCorrect() * 100.0F / corpusSize);
                }
            } else if (c.options.bgScaleMode) {
                LOG.info("Before training, scale accuracy: {} {}% corr {}",
                        c.getClassificationScaleNumberCorrect(),
                        c.getClassificationScaleNumberCorrect() * 100.0F / corpusSize,
                        c.getClassificationScaleCorrelationWholeCorpus()
                );
            } else {
                LOG.info("Before training, positive: {} {}% negative {} {}%",
                        c.getClassificationPositiveNumberCorrect(),
                        c.getClassificationPositiveAccuracyProportion() * 100.0F,
                        c.getClassificationNegativeNumberCorrect(),
                        c.getClassificationNegativeAccuracyProportion() * 100.0F
                );
                LOG.info("   Positive corr: {} negative {}",
                        c.getClassificationPosCorrelationWholeCorpus(),
                        c.getClassificationNegCorrelationWholeCorpus()
                );
            }

            LOG.info(" out of {}", c.getCorpusSize());
            if (bDoAll) {
                LOG.info("Running {} iteration(s) of all options on file {}; results in {}",
                        iIterations, sInputFile, sOutputFile);
                c.run10FoldCrossValidationForAllOptionVariations(
                        iMinImprovement, bUseTotalDifference, iIterations, iMultiOptimisations, sOutputFile);
            } else {
                LOG.info("Running {} iteration(s) for standard or selected options on file {}; results in {}",
                        iIterations, sInputFile, sOutputFile);
                c.run10FoldCrossValidationMultipleTimes(
                        iMinImprovement, bUseTotalDifference, iIterations, iMultiOptimisations, sOutputFile);
            }

        }
    }

    /**
     * UC-13：在 文件 或 文件夹内符合条件的文件 中对指定列进行分类，结果保存在新文件中.
     *
     * @param c            语料库对象
     * @param sInputFile   输入文件路径
     * @param sInputFolder 输入文件夹路径
     * @param iTextCol     待分类文本所在列
     * @param iIdCol       id所在列
     */
    private void classifyAndSaveWithID(Corpus c, final String sInputFile, final String sInputFolder,
                                       final int iTextCol, final int iIdCol) {
        if (!Objects.equals(sInputFile, "")) {
            c.classifyAllLinesAndRecordWithID(sInputFile, iTextCol - 1, iIdCol - 1,
                    FileOps.s_ChopFileNameExtension(sInputFile) + "_classID.txt");
        } else {
            if (Objects.equals(sInputFolder, "")) {
                LOG.info("No annotations done because no input file or folder specfied");
                this.showBriefHelp();
                return;
            }

            File folder = new File(sInputFolder);
            File[] listOfFiles = folder.listFiles();
            if (listOfFiles == null) {
                LOG.info("Incorrect or empty input folder specfied");
                this.showBriefHelp();
                return;
            }

            for (File listOfFile : listOfFiles) {
                if (listOfFile.isFile()) {
                    LOG.info("Classify + save with ID: {}", listOfFile.getName());
                    c.classifyAllLinesAndRecordWithID(sInputFolder + "/" + listOfFile.getName(),
                            iTextCol - 1, iIdCol - 1, sInputFolder + "/"
                                    + FileOps.s_ChopFileNameExtension(listOfFile.getName()) + "_classID.txt");
                }
            }
        }

    }

    /**
     * UC-13 在 文件 或 文件夹内符合条件的文件 中对指定列进行分类，结果以注释形式附在行后.
     *
     * @param c                     语料库对象
     * @param sInputFile            输入文件路径
     * @param sInputFolder          输入文件夹路径
     * @param sFileSubString        文件夹中文件名含有本参数的文件的内容被分类
     * @param iTextColForAnnotation 需要分类的列号
     * @param bOkToOverwrite        是否可以重写
     */
    private void annotationTextCol(final Corpus c, final String sInputFile, final String sInputFolder,
                                   final String sFileSubString, final int iTextColForAnnotation,
                                   final boolean bOkToOverwrite) {
        if (!bOkToOverwrite) {
            LOG.info("Must include parameter overwrite to annotate");
        } else {
            if (!Objects.equals(sInputFile, "")) {
                c.annotateAllLinesInInputFile(sInputFile, iTextColForAnnotation - 1);
            } else {
                if (Objects.equals(sInputFolder, "")) {
                    LOG.error("No annotations done because no input file or folder specfied");
                    this.showBriefHelp();
                    return;
                }
                File folder = new File(sInputFolder);
                File[] listOfFiles = folder.listFiles();
                if (listOfFiles == null || listOfFiles.length == 0) {
                    LOG.error("the folder {} contains no files. program end.", sInputFolder);
                    return;
                }
                for (File listOfFile : listOfFiles) {
                    if (listOfFile.isFile()) {
                        if (!"".equals(sFileSubString) && listOfFile.getName().indexOf(sFileSubString) <= 0) {
                            LOG.info("  Ignoring {}", listOfFile.getName());
                        } else {
                            LOG.info("Annotate: {}", listOfFile.getName());
                            c.annotateAllLinesInInputFile(sInputFolder + "/" + listOfFile.getName(),
                                    iTextColForAnnotation - 1);
                        }
                    }
                }
            }

        }
    }

    /**
     * UC-11：分类单个文本，结果直接输出.
     *
     * @param c                 构建的语料库对象
     * @param sTextToParse      待分类文本
     * @param bURLEncodedOutput 是否使用urlEncoded形式输出
     */
    private void parseOneText(final Corpus c, final String sTextToParse, final boolean bURLEncodedOutput) {
        String sOutput = parseText(sTextToParse, c);//解析文本

        if (bURLEncodedOutput) {
            try {
                LOG.info(URLEncoder.encode(sOutput, "UTF-8"));
            } catch (UnsupportedEncodingException var13) {
                var13.printStackTrace();
            }
        } else if (c.options.bgForceUTF8) {
            LOG.info(new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
        } else {
            output = sOutput;
            LOG.info(sOutput);
        }

    }

    /**
     * UC-16：分类stdin读入的文本，结果stdout.
     *
     * @param c        构建的语料库对象
     * @param iTextCol 待分类文本所在列
     */
    private void listenToStdIn(final Corpus c, final int iTextCol) {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));

        String sTextToParse;
        try {
            while ((sTextToParse = stdin.readLine()) != null) {
                boolean bSuccess;
                if (sTextToParse.contains("#Change_TermWeight")) {
                    String[] sData = sTextToParse.split("\t");
                    bSuccess = c.resources.sentimentWords.setSentiment(sData[1], Integer.parseInt(sData[2]));
                    if (bSuccess) {
                        LOG.info("1");
                    } else {
                        LOG.info("0");
                    }
                } else {
                    int iPos = 1;
                    int iTrinary = 0;
                    int iScale = 0;
                    Paragraph paragraph = new Paragraph();
                    if (iTextCol > -1) {
                        String[] sData = sTextToParse.split("\t");
                        if (sData.length >= iTextCol) {
                            paragraph.setParagraph(sData[iTextCol], c.resources, c.options);
                        }
                    } else {
                        paragraph.setParagraph(sTextToParse, c.resources, c.options);
                    }

                    int iNeg = paragraph.getParagraphNegativeSentiment();
                    iPos = paragraph.getParagraphPositiveSentiment();
                    iTrinary = paragraph.getParagraphTrinarySentiment();
                    iScale = paragraph.getParagraphScaleSentiment();
                    String sRationale = "";
                    String sOutput;
                    if (c.options.bgEchoText) {
                        sOutput = sTextToParse + "\t";
                    } else {
                        sOutput = "";
                    }

                    if (c.options.bgExplainClassification) {
                        sRationale = paragraph.getClassificationRationale();
                    }

                    if (c.options.bgTrinaryMode) {
                        sOutput = sOutput + iPos + "\t" + iNeg + "\t" + iTrinary + "\t" + sRationale;
                    } else if (c.options.bgScaleMode) {
                        sOutput = sOutput + iPos + "\t" + iNeg + "\t" + iScale + "\t" + sRationale;
                    } else {
                        sOutput = sOutput + iPos + "\t" + iNeg + "\t" + sRationale;
                    }

                    if (c.options.bgForceUTF8) {
                        String str = new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        LOG.info(str);
                    } else {
                        LOG.info(sOutput);
                    }
                }
            }
        } catch (IOException var14) {
            LOG.info("Error reading input");
            var14.printStackTrace();
        }

    }

    /**
     * UC-15：分类从命令行交互运行获得的文本，结果直接输出.
     *
     * @param c 构建的语料库对象
     */
    private void listenForCmdInput(final Corpus c) {
        BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
        while (true) {
            try {
                while (true) {
                    String sTextToParse = stdin.readLine();
                    if (sTextToParse.equalsIgnoreCase("@end")) {
                        return;
                    }

                    String sOutput = parseText(sTextToParse, c);

                    if (!c.options.bgForceUTF8) {
                        LOG.info(sOutput);
                    } else {
                        String str = new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8);
                        LOG.info(str);
                    }
                }
            } catch (IOException var13) {
                LOG.info(var13.getMessage());
            }
        }
    }

    private String parseText(String text, Corpus c) {
        Paragraph paragraph = new Paragraph();
        paragraph.setParagraph(text, c.resources, c.options);
        int iNeg = paragraph.getParagraphNegativeSentiment();
        int iPos = paragraph.getParagraphPositiveSentiment();
        int iTrinary = paragraph.getParagraphTrinarySentiment();
        int iScale = paragraph.getParagraphScaleSentiment();
        String sRationale = "";
        if (c.options.bgEchoText) {
            sRationale = " " + text;
        }

        if (c.options.bgExplainClassification) {
            sRationale = " " + paragraph.getClassificationRationale();
        }

        String sOutput;
        if (c.options.bgTrinaryMode) {
            sOutput = iPos + " " + iNeg + " " + iTrinary + sRationale;
        } else if (c.options.bgScaleMode) {
            sOutput = iPos + " " + iNeg + " " + iScale + sRationale;
        } else {
            sOutput = iPos + " " + iNeg + sRationale;
        }
        return sOutput;
    }

    /**
     * UC-14：分类在指定端口监听获得的文本，结果直接输出.
     *
     * @param c           构建的语料库
     * @param iListenPort 监听的端口
     */
    private void listenAtPort(Corpus c, final int iListenPort) {
        String decodedText = "";
        try (ServerSocket serverSocket = new ServerSocket(iListenPort)) {
            LOG.info("Listening on port: {} IP: {}", iListenPort, serverSocket.getInetAddress());
            while (true) {
                // 读入内容
                try (Socket clientSocket = serverSocket.accept();
                     PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
                     BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
                ) {
                    String inputLine;
                    try {
                        while ((inputLine = in.readLine()) != null) {
                            if (inputLine.indexOf("GET /") == 0) {
                                int lastSpacePos = inputLine.lastIndexOf(" ");
                                if (lastSpacePos < LAST_SPACE_POS) {
                                    lastSpacePos = inputLine.length();
                                }

                                decodedText = URLDecoder.decode(inputLine.substring(LAST_SPACE_POS, lastSpacePos), "UTF-8");
                                LOG.info("Analysis of text: {}", decodedText);
                                break;
                            }

                            if (inputLine.equals("MikeSpecialMessageToEnd.")) {
                                break;
                            }
                        }
                    } catch (IOException var24) {
                        LOG.error("IOException {}", var24.getMessage());
                        var24.printStackTrace();
                        decodedText = "";
                    } catch (Exception var25) {
                        LOG.error("Non-IOException {}", var25.getMessage());
                        decodedText = "";
                    }
                    // 处理内容
                    String sOutput = parseText(decodedText, c);

                    if (c.options.bgForceUTF8) {
                        out.print(new String(sOutput.getBytes(StandardCharsets.UTF_8), StandardCharsets.UTF_8));
                    } else {
                        out.print(sOutput);
                    }
                } catch (IOException var20) {
                    LOG.error(var20.getMessage());
                    LOG.error("Accept failed at port: {}", iListenPort);
                    LOG.error("or IOException clientSocket.getOutputStream");
                    LOG.error("or IOException InputStreamReader");
                    return;
                }
            }
        } catch (IOException var23) {
            LOG.info("Could not listen on port {} because\n {}", iListenPort, var23.getMessage());
        }

    }

    /**
     * 输入有误时，本方法打印帮助内容.
     */
    private void showBriefHelp() {
        LOG.info("\n");
        LOG.info("===={} Brief Help====", this.c.options.sgProgramName);
        LOG.info("For most operations, a minimum of two parameters must be set");
        LOG.info("1) folder location for the linguistic files");
        LOG.info("   e.g., on Windows: C:/mike/Lexical_Data/");
        LOG.info("   e.g., on Mac/Linux/Unix: /usr/Lexical_Data/");
        if (this.c.options.bgTensiStrength) {
            LOG.info("TensiiStrength_Data can be downloaded from...[not completed yet]");
        } else {
            LOG.info("SentiStrength_Data can be downloaded with the Windows version of SentiStrength from sentistrength.wlv.ac.uk");
        }

        LOG.info("\n");
        LOG.info("2) text to be classified or file name of texts to be classified");
        LOG.info("   e.g., To classify one text: text love+u");
        LOG.info("   e.g., To classify a file of texts: input /bob/data.txt");
        LOG.info("\n");
        LOG.info("Here is an example complete command:");
        if (this.c.options.bgTensiStrength) {
            LOG.info("java -jar TensiStrength.jar sentidata C:/a/Stress_Data/ text am+stressed");
        } else {
            LOG.info("java -jar SentiStrength.jar sentidata C:/a/SentStrength_Data/ text love+u");
        }
        LOG.info("\n");
        if (!this.c.options.bgTensiStrength) {
            LOG.info("To list all commands: java -jar SentiStrength.jar help");
        }
    }

    /**
     * 命令行输入help时，本方法打印帮助内容.
     */
    private void printCommandLineOptions() {
        LOG.info("===={} Command Line Options====", this.c.options.sgProgramName);
        LOG.info("=Source of data to be classified=");
        LOG.info(" text [text to process] OR");
        LOG.info(" input [filename] (each line of the file is classified SEPARATELY");
        LOG.info("        May have +ve 1st col., -ve 2nd col. in evaluation mode) OR");
        LOG.info(" annotateCol [col # 1..] (classify text in col, result at line end) OR");
        LOG.info(" textCol, idCol [col # 1..] (classify text in col, result & ID in new file) OR");
        LOG.info(" inputFolder  [foldername] (all files in folder will be *annotated*)");
        LOG.info(" outputFolder [foldername where to put the output (default: folder of input)]");
        LOG.info(" resultsExtension [file-extension for output (default _out.txt)]");
        LOG.info("  fileSubstring [text] (string must be present in files to annotate)");
        LOG.info("  Ok to overwrite files [overwrite]");
        LOG.info(" listen [port number to listen at - call http://127.0.0.1:81/text]");
        LOG.info(" cmd (wait for stdin input, write to stdout, terminate on input: @end");
        LOG.info(" stdin (read from stdin input, write to stdout, terminate when stdin finished)");
        LOG.info(" wait (just initialise; allow calls to public String computeSentimentScores)");
        LOG.info("=Linguistic data source=");
        LOG.info(" sentidata [folder for {} data (end in slash, no spaces)]", this.c.options.sgProgramName);
        LOG.info("=Options=");
        LOG.info(" keywords [comma-separated list - {} only classified close to these]", this.c.options.sgProgramMeasuring);
        LOG.info("   wordsBeforeKeywords [words to classify before keyword (default 4)]");
        LOG.info("   wordsAfterKeywords [words to classify after keyword (default 4)]");
        LOG.info(" trinary (report positive-negative-neutral classifcation instead)");
        LOG.info(" binary (report positive-negative classifcation instead)");
        LOG.info(" scale (report single -4 to +4 classifcation instead)");
        LOG.info(" emotionLookupTable [filename (default: EmotionLookupTable.txt)]");
        LOG.info(" additionalFile [filename] (domain-specific terms and evaluations)");
        LOG.info(" lemmaFile [filename] (word tab lemma list for lemmatisation)");
        LOG.info("=Classification algorithm parameters=");
        LOG.info(" noBoosters (ignore sentiment booster words (e.g., very))");
        LOG.info(" noNegators (don't use negating words (e.g., not) to flip sentiment) -OR-");
        LOG.info(" noNegatingPositiveFlipsEmotion (don't use negating words to flip +ve words)");
        LOG.info(" bgNegatingNegativeNeutralisesEmotion (negating words don't neuter -ve words)");
        LOG.info(" negatedWordStrengthMultiplier (strength multiplier when negated (default=0.5))");
        LOG.info(" negatingWordsOccurAfterSentiment (negate {} occurring before negatives)", this.c.options.sgProgramMeasuring);
        LOG.info("  maxWordsAfterSentimentToNegate (max words {} to negator (default 0))", this.c.options.sgProgramMeasuring);
        LOG.info(" negatingWordsDontOccurBeforeSentiment (don't negate {} after negatives)", this.c.options.sgProgramMeasuring);
        LOG.info("   maxWordsBeforeSentimentToNegate (max from negator to {} (default 0))", this.c.options.sgProgramMeasuring);
        LOG.info(" noIdioms (ignore idiom list)");
        LOG.info(" questionsReduceNeg (-ve sentiment reduced in questions)");
        LOG.info(" noEmoticons (ignore emoticon list)");
        LOG.info(" exclamations2 (sentence with ! counts as +2 if otherwise neutral)");
        LOG.info(" minPunctuationWithExclamation (min punctuation with ! to boost term {} )", this.c.options.sgProgramMeasuring);
        LOG.info(" mood [-1,0,1] (default 1: -1 assume neutral emphasis is neg, 1, assume is pos");
        LOG.info(" noMultiplePosWords (multiple +ve words don't increase {} )", this.c.options.sgProgramPos);
        LOG.info(" noMultipleNegWords (multiple -ve words don't increase {} )", this.c.options.sgProgramNeg);
        LOG.info(" noIgnoreBoosterWordsAfterNegatives (don't ignore boosters after negating words)");
        LOG.info(" noDictionary (don't try to correct spellings using the dictionary)");
        LOG.info(" noMultipleLetters (don't use additional letters in a word to boost {} )", this.c.options.sgProgramMeasuring);
        LOG.info(" noDeleteExtraDuplicateLetters (don't delete extra duplicate letters in words)");
        LOG.info(" illegalDoubleLettersInWordMiddle [letters never duplicate in word middles]");
        LOG.info("    default for English: ahijkquvxyz (specify list without spaces)");
        LOG.info(" illegalDoubleLettersAtWordEnd [letters never duplicate at word ends]");
        LOG.info("    default for English: achijkmnpqruvwxyz (specify list without spaces)");
        LOG.info(" sentenceCombineAv (average {} strength of terms in each sentence) OR", this.c.options.sgProgramMeasuring);
        LOG.info(" sentenceCombineTot (total the {} strength of terms in each sentence)", this.c.options.sgProgramMeasuring);
        LOG.info(" paragraphCombineAv (average {} strength of sentences in each text) OR", this.c.options.sgProgramMeasuring);
        LOG.info(" paragraphCombineTot (total the {} strength of sentences in each text)", this.c.options.sgProgramMeasuring);
        LOG.info("  *the default for the above 4 options is the maximum, not the total or average");
        LOG.info(" negativeMultiplier [negative total strength polarity multiplier, default 1.5]");
        LOG.info(" capitalsBoostTermSentiment ( {} words in CAPITALS are stronger)", this.c.options.sgProgramMeasuring);
        LOG.info(" alwaysSplitWordsAtApostrophes (e.g., t'aime -> t ' aime)");
        LOG.info(" MinSentencePosForQuotesIrony [integer] quotes in +ve sentences indicate irony");
        LOG.info(" MinSentencePosForPunctuationIrony [integer] +ve ending in !!+ indicates irony");
        LOG.info(" MinSentencePosForTermsIrony [integer] irony terms in +ve sent. indicate irony");
        LOG.info(" MinSentencePosForAllIrony [integer] all of the above irony terms");
        LOG.info(" lang [ISO-639 lower-case two-letter langauge code] set processing language");
        LOG.info("=Input and Output=");
        LOG.info(" explain (explain classification after results)");
        LOG.info(" echo (echo original text after results [for pipeline processes])");
        LOG.info(" UTF8 (force all processing to be in UTF-8 format)");
        LOG.info(" urlencoded (input and output text is URL encoded)");
        LOG.info("=Advanced - machine learning [1st input line ignored]=");
        LOG.info(" termWeights (list terms in badly classified texts; must specify inputFile)");
        LOG.info(" optimise [Filename for optimal term strengths (eg. EmotionLookupTable2.txt)]");
        LOG.info(" train (evaluate {} by training term strengths on results in file)", this.c.options.sgProgramName);
        LOG.info("   all (test all option variations rather than use default)");
        LOG.info("   numCorrect (optimise by # correct - not total classification difference)");
        LOG.info("   iterations [number of 10-fold iterations] (default 1)");
        LOG.info("   minImprovement [min. accuracy improvement to change {} weights (default 1)]", this.c.options.sgProgramMeasuring);
        LOG.info("   multi [# duplicate term strength optimisations to change {} weights (default 1)]", this.c.options.sgProgramMeasuring);
    }

    /**
     * 得到使用的corpus对象.
     *
     * @return 当前使用的corpus
     */
    public Corpus getCorpus() {
        return this.c;
    }
}
