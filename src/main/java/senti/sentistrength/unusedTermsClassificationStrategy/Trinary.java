package senti.sentistrength.unusedTermsClassificationStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Trinary extends Strategy{
    /**
     * 无参构造函数.
     */
    public Trinary() {
        sgTermList = null;
        igTermListCount = 0;
        igTermListMax = FIFTYTHOUSANDS;
    }

    @Override
    public void addNewIndexToMainIndex(int iCorrectTrinaryClass, int iEstTrinaryClass) {
        for (int iTerm = 1; iTerm <= iTermsAddedIdTempCount; iTerm++) {
            int iTermId = iTermsAddedIdTemp[iTerm];
            if (igTermListFreqTemp[iTermId] != 0) {
                try {
                    igTermListTrinaryCorrectClass[iTermId][iCorrectTrinaryClass + 1]++;
                    igTermListTrinaryClassDiff[iTermId] += iCorrectTrinaryClass - iEstTrinaryClass;
                    igTermListFreq[iTermId]++;
                    iTermsAddedIdTemp[iTerm] = 0;
                } catch (Exception e) {
                    String message = "Error trying to add trinary values to index. " + e.getMessage();
                    LOGGER.error(message);
                }
            }
        }
        iTermsAddedIdTempCount = 0;
    }

    @Override
    public void printIndex(String sOutputFile, int iMinFreq) {
        try (BufferedWriter wWriter = new BufferedWriter(new FileWriter(sOutputFile))) {
            wWriter.write("Term\tTermFreq\tTrinaryClassDiff (correct-estimate)\tTrinaryClassAvDiff\t");
            for (int i = -1; i <= 1; i++) {
                wWriter.write(CORRECTCLASS + i + "\t");
            }
            wWriter.write("\n");
            for (int iTerm = 1; iTerm <= igTermListCount; iTerm++) {
                if (igTermListFreq[iTerm] > iMinFreq) {
                    wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListTrinaryClassDiff[iTerm]
                            + "\t" + (float) igTermListTrinaryClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                    for (int i = 0; i < 3; i++) {
                        wWriter.write(igTermListTrinaryCorrectClass[iTerm][i] + "\t");
                    }
                    wWriter.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
