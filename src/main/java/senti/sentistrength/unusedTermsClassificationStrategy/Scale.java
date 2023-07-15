package senti.sentistrength.unusedTermsClassificationStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Scale extends Strategy{
    /**
     * 无参构造函数.
     */
    public Scale() {
        sgTermList = null;
        igTermListCount = 0;
        igTermListMax = FIFTYTHOUSANDS;
    }


    @Override
    public void addNewIndexToMainIndex(int iCorrectScaleClass, int iEstScaleClass) {
        for (int iTerm = 1; iTerm <= iTermsAddedIdTempCount; iTerm++) {
            int iTermID = iTermsAddedIdTemp[iTerm];
            if (igTermListFreqTemp[iTermID] != 0) {
                try {
                    igTermListScaleCorrectClass[iTermID][iCorrectScaleClass + 4]++;
                    igTermListScaleClassDiff[iTermID] += iCorrectScaleClass - iEstScaleClass;
                    igTermListFreq[iTermID]++;
                    iTermsAddedIdTemp[iTerm] = 0;
                } catch (Exception e) {
                    String message = "Error trying to add scale values to index. " + e.getMessage();
                    LOGGER.error(message);
                }
            }
        }
        iTermsAddedIdTempCount = 0;
    }



    @Override
    public void printIndex(String sOutputFile, int iMinFreq) {
        try (BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile))) {
            wWriter.write("Term\tTermFreq\tScaleClassDiff (correct-estimate)\tScaleClassAvDiff\t");
            for (int i = -4; i <= 4; i++) {
                wWriter.write(CORRECTCLASS + i + "\t");
            }

            wWriter.write("\n");
            for (int iTerm = 1; iTerm <= igTermListCount; iTerm++) {
                if (igTermListFreq[iTerm] > iMinFreq) {
                    wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListScaleClassDiff[iTerm]
                            + "\t" + (float) igTermListScaleClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                    for (int i = 0; i < 9; i++) {
                        wWriter.write(igTermListScaleCorrectClass[iTerm][i] + "\t");
                    }
                    wWriter.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
