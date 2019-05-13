package modules;

import java.util.ArrayList;
import java.util.Collections;

import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.CompilationUnit;

import utils.Logger;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfCode
 * Author(s)        : Ben Collins
 * Date Created     : 30/04/19
 * Purpose          : This class is used to calculate the length
 *                    of a project and make some recommendations based on
 *                    the length of files over pre-set numbers.
 */
public class LengthOfCode implements ModuleInterface {

    private ArrayList<String> collectedFiles = new ArrayList<>();

    /**
     * @return the name of the module
     */
    @Override
    public String getName() {
        return "LengthOfCode";
    }

    /**
     * This calculates a number of results for the project:
     * TotalLineCount, NumberOfFiles, SmallestFile, MedianFile, LargestFile.
     *
     * @param sourceRoot AST of Source Files
     * @return           the summary results of the module being run
     * @see              SourceRoot
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        final int LOW_LENGTH = 200;
        final int MED_LENGTH = 500;
        final int HIGH_LENGTH = 1000;

        int lineCount = 0;
        int smallestSize = Integer.MAX_VALUE;
        int largestSize = 0;

        ArrayList<Integer> fileCounts = new ArrayList<>();

        int cuLineCount;
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
            cuLineCount = cu.toString().split(System.getProperty("line.separator")).length;
            fileCounts.add(cuLineCount);

            if (cuLineCount > LOW_LENGTH) {
                String limit = Integer.toString(LOW_LENGTH);
                String messageExtension = "may need reviewing]";
                if (cuLineCount > MED_LENGTH) {
                    limit = Integer.toString(MED_LENGTH);
                    messageExtension = "should be reviewed]";
                }
                if (cuLineCount > HIGH_LENGTH) {
                    limit = Integer.toString(HIGH_LENGTH);
                    messageExtension = "should be refactored]";
                }

                if (cu.getStorage().isPresent()) {
                    StringBuilder pathMessage = new StringBuilder();
                    pathMessage.append(cu.getStorage().get().getPath().toString());
                    pathMessage.append(String.format(": %s lines. [File larger than %s, %s", cuLineCount, limit, messageExtension));
                    collectedFiles.add(pathMessage.toString());
                } else {
                    Logger.error("Storage not present for given CompilationUnit.");
                }
            }

            largestSize = cuLineCount > largestSize ? cuLineCount : largestSize;
            smallestSize = cuLineCount < smallestSize ? cuLineCount : smallestSize;
            lineCount += cuLineCount;
        }

        Collections.sort(fileCounts);
        int medianSize = fileCounts.get(fileCounts.size() / 2);
        int numFilesRead = fileCounts.size();

        return new String[] {getName(), "LineCount:" + lineCount, "FilesRead:" + numFilesRead
                            , "SmallestSize:" + smallestSize, "MedianSize:" + medianSize
                            , "LargestSize:" + largestSize};
    }

    /**
     * @return a description of what the module is testing
     */
    @Override
    public String getDescription() {
        return "This is a measure of the size of a program. Generally, the larger" +
                " the size of the code of a component, the more complex and error-prone" +
                " that component is likely to be. Length of code has been shown to be" +
                " one of the most reliable metrics for predicting error-proneness in components.";
    }

    /**
     * This returns a formatted string that presents the recommendations
     * determined in the executeModule() function.
     *
     * @return a formatted string with recommendations
     */
    @Override
    public String printMetrics() {
        StringBuilder stringBuilder = new StringBuilder();
        for (String file : collectedFiles) {
            stringBuilder.append(file);
            stringBuilder.append(System.getProperty("line.separator"));
        }
        return stringBuilder.toString();
    }
}
