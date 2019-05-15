package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.utils.SourceRoot;
import modules.helpers.LengthHelper;
import utils.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Project          : Software Quality Assignment
 * Class name       : LengthOfConditionalBlocks
 * Author(s)        : Ben Collins
 * Date Created     : 02/05/19
 * Purpose          : This class is used to calculate the length
 *                    of conditional blocks (if&switch statements)
 *                    in a project and make some recommendations based on
 *                    the length of files over pre-set numbers.
 */
public class LengthOfConditionalBlocks implements ModuleInterface {

    private ArrayList<String> collectedBlocks = new ArrayList<>();
    private ArrayList<Integer> blockCounts = new ArrayList<>();

    /**
     * @return the name of the module
     */
    @Override
    public String getName() {
        return "LengthOfConditionalBlocks";
    }

    /**
     * This calculates a number of results for the project:
     * Total Line Count, Number Of Blocks, Smallest Block Size,
     * Median Block Size, Largest Block Size.
     *
     * @param sourceRoot AST of Source Files
     * @return           the summary results of the module being run
     * @see              SourceRoot
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        int lineCount = 0;
        int smallestSize = Integer.MAX_VALUE;
        int largestSize = 0;

        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
            List<IfStmt> ifStmts = cu.findAll(IfStmt.class);
            List<SwitchStmt> switchStmts = cu.findAll(SwitchStmt.class);

            int[] ifBlocks = calculateBlocks(cu, ifStmts);
            int[] switchBlocks = calculateBlocks(cu, switchStmts);

            lineCount += ifBlocks[0] + switchBlocks[0];
            if (smallestSize > ifBlocks[1]) smallestSize = ifBlocks[1];
            if (smallestSize > switchBlocks[1]) smallestSize = switchBlocks[1];
            if (largestSize < ifBlocks[2]) largestSize = ifBlocks[2];
            if (largestSize < switchBlocks[2]) largestSize = switchBlocks[2];
        }

        Collections.sort(blockCounts);
        int medianSize = blockCounts.get(blockCounts.size() / 2);

        return new String[] {getName(), "LineCount:" + lineCount, "NumberOfBlocks:" + blockCounts.size()
                            ,"SmallestSize:" + smallestSize, "MedianSize:" + medianSize
                            ,"LargestSize:" + largestSize};
    }

    /**
     * This function calculates the results of the blocks in a CompilationUnit
     * to be returned to the executeModule function.
     *
     * @param cu         CompilationUnit of class file
     * @param statements List of Statements
     * @return           Results of blocks in CompilationUnit
     */
    private int[] calculateBlocks(CompilationUnit cu, List<? extends Statement> statements) {
        //TODO: Review Numbers
        final int LOW_LENGTH = 20/2;
        final int MED_LENGTH = 30/2;
        final int HIGH_LENGTH = 40/2;

        int culineCount = 0;
        int cuSmallestBlock = Integer.MAX_VALUE;
        int cuLargestBlock = 0;

        int stmtLineCount;
        for (Statement stmt : statements) {
            stmt.removeComment();
            stmtLineCount = stmt.toString().split(System.getProperty("line.separator")).length;
            blockCounts.add(stmtLineCount);

            if (stmtLineCount > LOW_LENGTH) {
                String[] limitAndMessage = LengthHelper.limitMessageExtension(stmtLineCount, LOW_LENGTH, MED_LENGTH, HIGH_LENGTH);
                String limit = limitAndMessage[0];
                String messageExtension = limitAndMessage[1];

                if (cu.getStorage().isPresent()) {
                    StringBuilder pathMessage = new StringBuilder();
                    pathMessage.append(cu.getStorage().get().getPath().toString());
                    if (stmt.getRange().isPresent()) {
                        pathMessage.append("; Line ").append(stmt.getRange().get().begin.line);
                    } else {
                        Logger.error("Range not present for given Statement.");
                    }
                    pathMessage.append(String.format(": %s lines. [Block larger than %s, %s]"
                                      ,stmtLineCount, limit, messageExtension));
                    collectedBlocks.add(pathMessage.toString());
                } else {
                    Logger.error("Storage not present for given CompilationUnit.");
                }
            }

            if (stmtLineCount < cuSmallestBlock) cuSmallestBlock = stmtLineCount;
            if (stmtLineCount > cuLargestBlock) cuLargestBlock = stmtLineCount;
            culineCount += stmtLineCount;
        }
        return new int[] {culineCount, cuSmallestBlock, cuLargestBlock};
    }

    /**
     * @return a description of what the module is testing
     */
    @Override
    public String getDescription() {
        return "This is a measure of the size of conditional blocks in the program." +
                " Generally, the larger the size of a block, the more complex" +
                " and error-prone that block is likely to be.";
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
        for (String file : collectedBlocks) {
            stringBuilder.append(file);
            stringBuilder.append(System.getProperty("line.separator"));
        }
        return stringBuilder.toString();
    }
}
