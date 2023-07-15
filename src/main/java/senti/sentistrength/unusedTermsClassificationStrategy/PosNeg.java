package senti.sentistrength.unusedTermsClassificationStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class PosNeg extends Strategy{
    /**
     * 无参构造函数.
     */
    public PosNeg() {
        sgTermList = null;
        igTermListCount = 0;
        igTermListMax = FIFTYTHOUSANDS;
    }

    @Override
    public void addNewIndexToMainIndex(int iCorrectPosClass, int iEstPosClass, int iCorrectNegClass, int iEstNegClass) {
        if (iCorrectNegClass > 0 && iCorrectPosClass > 0) {
            for (int iTerm = 1; iTerm <= iTermsAddedIdTempCount; iTerm++) {
                int iTermId = iTermsAddedIdTemp[iTerm];
                if (igTermListFreqTemp[iTermId] != 0) {
                    try {
                        igTermListNegCorrectClass[iTermId][iCorrectNegClass - 1]++;
                        igTermListPosCorrectClass[iTermId][iCorrectPosClass - 1]++;
                        igTermListPosClassDiff[iTermId] += iCorrectPosClass - iEstPosClass;
                        igTermListNegClassDiff[iTermId] += iCorrectNegClass + iEstNegClass;
                        igTermListFreq[iTermId]++;
                        iTermsAddedIdTemp[iTerm] = 0;
                    } catch (Exception e) {
                        String message = "[UnusedTermsClassificationIndex] Error trying to add Pos + Neg to index. " + e.getMessage();
                        LOGGER.error(message);
                    }
                }
            }
        }
        iTermsAddedIdTempCount = 0;
    }

    @Override
    public void printIndex(String sOutputFile, int iMinFreq) {
        try (BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile))) {
            wWriter.write("Term\tTermFreq >= " + iMinFreq + "\t"
                    + "PosClassDiff (correct-estimate)\t" + "NegClassDiff\t" + "PosClassAvDiff\t" + "NegClassAvDiff\t");
            for (int i = 1; i <= 5; i++) {
                wWriter.write(CORRECTCLASS + i + "pos\t");
            }

            for (int i = 1; i <= 5; i++) {
                wWriter.write(CORRECTCLASS + i + "neg\t");
            }

            wWriter.write("\n");
            if (igTermListCount > 0) {
                for (int iTerm = 1; iTerm <= igTermListCount; iTerm++) {
                    if (igTermListFreq[iTerm] >= iMinFreq) {
                        wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListPosClassDiff[iTerm] + "\t"
                                + igTermListNegClassDiff[iTerm] + "\t" + (float) igTermListPosClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t"
                                + (float) igTermListNegClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                        for (int i = 0; i < 5; i++) {
                            wWriter.write(igTermListPosCorrectClass[iTerm][i] + "\t");
                        }
                        for (int i = 0; i < 5; i++) {
                            wWriter.write(igTermListNegCorrectClass[iTerm][i] + "\t");
                        }
                        wWriter.write("\n");
                    }
                }
            } else {
                wWriter.write("No terms found in corpus!\n");
            }
        } catch (IOException e) {
            String message = "Error printing index to " + sOutputFile;
            LOGGER.error(message);
            e.printStackTrace();
        }
    }
}
