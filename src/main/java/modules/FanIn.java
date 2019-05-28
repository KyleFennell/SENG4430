package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.SourceRoot;
import utils.Logger;
import utils.Util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FanIn implements ModuleInterface{


    @Override
    public String getName() {
        return "Fan_In";
    }

    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        Map<String, Map<String, Integer>> collatedTotalFanIn = new HashMap<>();
        Map<String, Map<String, Integer>> collatedUniqueFanIn = new HashMap<>();

        for (CompilationUnit unit : sourceRoot.getCompilationUnits()){
            Logger.debug("INITIAL PROCESSING FILE: " + unit.getStorage().get().getFileName());

            Map<String, Integer> fileTotalFanIn = new HashMap<>();
            Map<String, Integer> fileUniqueFanIn = new HashMap<>();

            List<MethodDeclaration> methodDeclarations = unit.findAll(MethodDeclaration.class);
            for (MethodDeclaration m : methodDeclarations){
                Logger.debug("ADDING METHOD: " + m.getNameAsString());
                fileTotalFanIn.put(m.getNameAsString(), 0);
                fileUniqueFanIn.put(m.getNameAsString(), 0);
            }
        }

        // loop through all method body's finding all method calls
        for (CompilationUnit unit : sourceRoot.getCompilationUnits()){
            Logger.debug("RE-PROCESSING FILE: " + unit.getStorage().get().getFileName());

            Map<String, Integer> fileTotalFanIn = new HashMap<>();
            Map<String, Integer> fileUniqueFanIn = new HashMap<>();

            List<MethodDeclaration> methodDeclarations = unit.findAll(MethodDeclaration.class);
            for (MethodDeclaration m : methodDeclarations){
                Logger.debug("PROCESSING METHOD: " + m.getNameAsString());
//                System.out.println("PROCESSING METHOD: " + m.getNameAsString());
                if(! m.getBody().isPresent()) {
                    Logger.debug("BODY NOT PRESENT");
                    continue;
                }
                List<String> uniqueCalls = new ArrayList<>();
                BlockStmt body = m.getBody().get();
                List<MethodCallExpr> calls =  body.findAll(MethodCallExpr.class);
                for (MethodCallExpr c : calls){
                    String callItendifier = c.getNameAsString();
                    if (!uniqueCalls.contains(callItendifier)){
                        uniqueCalls.add(callItendifier);
                        System.out.println("UNIQUE CALL IDENTIFIER: " + callItendifier);
                    }
                }

            }
        }
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String printMetrics() {
        return null;
    }

}
