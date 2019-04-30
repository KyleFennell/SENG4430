package modules;

import com.github.javaparser.utils.SourceRoot;
import com.github.javaparser.ast.CompilationUnit;

public class LengthOfCode implements ModuleInterface {

    @Override
    public String getName() {
        return "Length of Code";
    }

    /**
     * @return results of the module being run
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        //TODO: Test
        int lineCount = 0;
        int numFilesRead = sourceRoot.getCompilationUnits().size();
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
            lineCount += cu.toString().split(System.getProperty("line.separator")).length;

        }
        //cu.findAll(ClassOrInterfaceDeclaration.class)
        return new String[] {Integer.toString(lineCount), Integer.toString(numFilesRead)};
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
     * @return a formatted string that represents the metrics determined
     * by the module
     */
    @Override
    public String printMetrics() {
        //TODO
        return null;
    }
}
