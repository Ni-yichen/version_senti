package senti.sentistrength.unusedTermsClassificationStrategy;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;

public class Binary extends Strategy{
    /**
     * 无参构造函数.
     */
    public Binary() {
        sgTermList = null;
        igTermListCount = 0;
        igTermListMax = FIFTYTHOUSANDS;
    }

    @Override
    public void addNewIndexToMainIndex(int iCorrectBinaryClass, int iEstBinaryClass) {
        for (int iTerm = 1; iTerm <= iTermsAddedIdTempCount; iTerm++) {
            int iTermId = iTermsAddedIdTemp[iTerm];
            if (igTermListFreqTemp[iTermId] != 0) {
                try {
                    igTermListBinaryClassDiff[iTermId] += iCorrectBinaryClass - iEstBinaryClass;
                    if (iCorrectBinaryClass == -1) {
                        iCorrectBinaryClass = 0;
                    }
                    igTermListBinaryCorrectClass[iTermId][iCorrectBinaryClass]++;
                    igTermListFreq[iTermId]++;
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
            wWriter.write("Term\tTermFreq\tBinaryClassDiff (correct-estimate)\tBinaryClassAvDiff\t");
            wWriter.write("CorrectClass-1\tCorrectClass1\t");
            wWriter.write("\n");
            for (int iTerm = 1; iTerm <= igTermListCount; iTerm++) {
                if (igTermListFreq[iTerm] > iMinFreq) {
                    wWriter.write(sgTermList[iTerm] + "\t" + igTermListFreq[iTerm] + "\t" + igTermListBinaryClassDiff[iTerm]
                            + "\t" + (float) igTermListBinaryClassDiff[iTerm] / (float) igTermListFreq[iTerm] + "\t");
                    for (int i = 0; i < 2; i++) {
                        wWriter.write(igTermListBinaryCorrectClass[iTerm][i] + "\t");
                    }
                    wWriter.write("\n");
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
