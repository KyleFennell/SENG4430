/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

/**
 *
 * @author Nicolas Klenert
 */
public class CyclomaticComplexity implements ModuleInterface {
    @Override
    public String getName() {
        return "Cyclometic Complexity";
    }

    /**
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
}
