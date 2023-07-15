// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   EmoticonsList.java

package senti.sentistrength.wordsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.utilities.FileOps;
import senti.utilities.Sort;
import senti.sentistrength.classification.ClassificationOptions;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            ClassificationOptions

public class EmoticonsList extends WordsList{
    private String[] sgEmoticon;
    private int[] igEmoticonStrength;
    private int igEmoticonCount;
    private int igEmoticonMax;
    private static final int EmoticonNotFound = 999;
    private static final Logger LOG = LoggerFactory.getLogger(EmoticonsList.class);

    /**
     * EmoticonsList的无参构造函数.
     */
    public EmoticonsList() {
        igEmoticonCount = 0;
        igEmoticonMax = 0;
    }

    /**
     * 获取目标情绪词汇的情感强度.
     *
     * @param emoticon 目标情绪词汇
     * @return 情感强度
     */
    @Override
    public int getEmoticon(final String emoticon) {
        int iEmoticon = Sort.i_FindStringPositionInSortedArray(
                emoticon, sgEmoticon, 1, igEmoticonCount);
        if (iEmoticon >= 0) {
            return igEmoticonStrength[iEmoticon];
        } else {
            return EmoticonNotFound;
        }
    }

    /**
     * 含参的EmoticonsList初始化函数.
     * @param sSourceFile 包含初始化所需数据的文件文件名
     * @param options 读取文件的可选方式，是否使用UTF8编码
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(final String sSourceFile, final ClassificationOptions options) {
        if (igEmoticonCount > 0) {
            return true;
        }
        File f = new File(sSourceFile);
        if (!f.exists()) {
            LOG.error("Could not find file: {}", sSourceFile);
            return false;
        }
        try {
            igEmoticonMax = FileOps.i_CountLinesInTextFile(sSourceFile) + 2;
            igEmoticonCount = 0;
            sgEmoticon = new String[igEmoticonMax];
            igEmoticonStrength = new int[igEmoticonMax];
            BufferedReader rReader;
            if (options.bgForceUTF8) {
                rReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
            } else {
                rReader = new BufferedReader(new FileReader(sSourceFile));
            }
            String sLine;
            while ((sLine = rReader.readLine()) != null) {
                if (!sLine.equals("")) {
                    String[] sData = sLine.split("\t");
                    if (sData.length > 1) {
                        igEmoticonCount++;
                        sgEmoticon[igEmoticonCount] = sData[0];
                        try {
                            igEmoticonStrength[igEmoticonCount] = Integer.parseInt(sData[1].trim());
                        } catch (NumberFormatException e) {
                            LOG.error("Failed to identify integer weight for emoticon! Ignoring emoticon");
                            LOG.error("Line: {}", sLine);
                            igEmoticonCount--;
                        }
                    }
                }
            }
            rReader.close();
        } catch (FileNotFoundException e) {
            LOG.error("Could not find emoticon file: {}", sSourceFile);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found emoticon file but could not read from it: {}", sSourceFile);
            e.printStackTrace();
            return false;
        }
        if (igEmoticonCount > 1) {
            Sort.quickSortStringsWithInt(
                    sgEmoticon, igEmoticonStrength, 1, igEmoticonCount);
        }
        return true;
    }
}
