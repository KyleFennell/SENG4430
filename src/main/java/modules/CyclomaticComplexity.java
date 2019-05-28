package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;
import java.util.Map;
import utils.Adjustment;

/**
 *
 * @author Nicolas Klenert
 */
public class CyclomaticComplexity implements AdjustableModuleInterface {
    
    Adjustment config;
    
    @Override
    public String getName() {
        return "Cyclometic Complexity";
    }

    /**
     * @param sourceRoot the sourceRoot created by JavaParser
     * @return results of the module being run
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        //TODO: Test
        int numFilesRead = sourceRoot.getCompilationUnits().size();
        int cycloNumber = 0;
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
            //find out the cyclometic complexity of ONE file (Compilation Unit is the root of the ast)
            
        }
        //cu.findAll(ClassOrInterfaceDeclaration.class)
        return new String[] {Integer.toString(cycloNumber), Integer.toString(numFilesRead)};
    }

    /**
     * @return a description of what the module is testing
     */
    @Override
    public String getDescription() {
        return "TODO";
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

    @Override
    public void setAdjustments(Adjustment setting) {
        config = setting;
    }

    @Override
    public Map<String, String> getDefaults() {
        return Map.of(
                "threshold",        "7"
        );
    }
}
