// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) fieldsfirst
// Source File Name:   IronyList.java

package senti.sentistrength.wordsList;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import senti.sentistrength.classification.ClassificationOptions;
import senti.utilities.FileOps;
import senti.utilities.Sort;

import java.io.*;
import java.nio.charset.StandardCharsets;

// Referenced classes of package uk.ac.wlv.sentistrength:
//            ClassificationOptions

public class IronyList extends WordsList{
    private String[] sgIronyTerm;
    private int igIronyTermCount;
    private int igIronyTermMax;
    private static final Logger LOG = LoggerFactory.getLogger(IronyList.class);
    /**
     * IronyList的无参构造函数.
     */
    public IronyList() {
        igIronyTermCount = 0;
        igIronyTermMax = 0;
    }

    /**
     * 判断目标词汇是否ironic的函数.
     * @param term 目标词汇
     * @return 布尔值的判断结果
     */
    @Override
    public boolean termIsIronic(final String term) {
        int iIronyTermCount = Sort.i_FindStringPositionInSortedArray(
                term, sgIronyTerm, 1, igIronyTermCount);
        return iIronyTermCount >= 0;
    }

    /**
     * 含参的IronyList初始化函数.
     * @param sSourceFile 包含初始化所需信息的文件文件名
     * @param options 读取文件的可选方式，是否使用UTF8
     * @return 初始化是否成功
     */
    @Override
    public boolean initialise(final String sSourceFile, final ClassificationOptions options) {
        if (igIronyTermCount > 0) {
            return true;
        }
        File f = new File(sSourceFile);
        if (!f.exists()) {
            return true;
        }
        try {
            igIronyTermMax = FileOps.i_CountLinesInTextFile(sSourceFile) + 2;
            igIronyTermCount = 0;
            sgIronyTerm = new String[igIronyTermMax];
            BufferedReader rReader;
            if (options.bgForceUTF8) {
                rReader = new BufferedReader(new InputStreamReader(
                        new FileInputStream(sSourceFile), StandardCharsets.UTF_8));
            } else {
                rReader = new BufferedReader(new FileReader(sSourceFile));
            }
            String sLine;
            sLine = rReader.readLine();
            while (sLine != null) {
                if (!sLine.equals("")) {
                    String[] sData = sLine.split("\t");
                    if (sData.length > 0) {
                        sgIronyTerm[++igIronyTermCount] = sData[0];
                    }
                }
                sLine = rReader.readLine();
            }
            rReader.close();
        } catch (FileNotFoundException e) {
            LOG.error("Could not find IronyTerm file: {}", sSourceFile);
            e.printStackTrace();
            return false;
        } catch (IOException e) {
            LOG.error("Found IronyTerm file but could not read from it: {}", sSourceFile);
            e.printStackTrace();
            return false;
        }
        Sort.quickSortStrings(sgIronyTerm, 1, igIronyTermCount);
        return true;
    }
}
