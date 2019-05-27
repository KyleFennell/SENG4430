package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.SourceRoot;
import utils.Logger;

import java.util.*;

public class FanOut implements ModuleInterface{

    private Map<String, Map<String, Integer>> collatedTotalFanOut = new HashMap<>();
    private Map<String, Map<String, Integer>> collatedUniqueFanOut = new HashMap<>();
    @Override
    public String getName() {
        return "Fan_Out";
    }

    @Override
    public String[] executeModule(SourceRoot sourceRoot) {

        for (CompilationUnit unit : sourceRoot.getCompilationUnits()){
            Logger.debug("PROCESSING FILE: " + unit.getStorage().get().getFileName());
            Map<String, Integer> methodNameToFanOut = new HashMap<>();
            Map<String, Integer> methodNameToUniqueFanOut = new HashMap<>();

            List<MethodDeclaration> methodDeclarations = unit.findAll(MethodDeclaration.class);
            for (MethodDeclaration m : methodDeclarations){
                Logger.debug("PROCESSING METHOD: " + m.getNameAsString());
                if(! m.getBody().isPresent()) {
                    Logger.debug("BODY NOT PRESENT");
                    continue;
                }
                List<String> uniqueCalls = new ArrayList<>();
                BlockStmt body = m.getBody().get();
                List<MethodCallExpr> calls =  body.findAll(MethodCallExpr.class);
                for (MethodCallExpr c : calls){
                    if (!uniqueCalls.contains(c.getName().toString())){
                        uniqueCalls.add(c.getName().toString());
                        Logger.debug("UNIQUE METHOD CALL: " + c.getName().toString());
                    }
                }
                methodNameToFanOut.put(m.getNameAsString(), calls.size());
                methodNameToUniqueFanOut.put(m.getNameAsString(), uniqueCalls.size());
            }
            collatedTotalFanOut.put(unit.getStorage().get().getFileName(), methodNameToFanOut);
            collatedUniqueFanOut.put(unit.getStorage().get().getFileName(), methodNameToUniqueFanOut);
        }
        return new String[0];
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String printMetrics() {
        String output = "";
        output += "TOTAL FAN OUT METRIC: \n";
        for(String fileName : collatedTotalFanOut.keySet()){
            output += "\t" + fileName + "\n";
            for (String methodName : collatedTotalFanOut.get(fileName).keySet()){
                output += "\t\t" + methodName + ": " + collatedTotalFanOut.get(fileName).get(methodName) + "\n";
            }
        }
        output += "UNIQUE FAN OUT METRIC: \n";
        for(String fileName : collatedUniqueFanOut.keySet()){
            output += "\t" + fileName + "\n";
            for (String methodName : collatedUniqueFanOut.get(fileName).keySet()){
                output += "\t\t" + methodName + ": " + collatedUniqueFanOut.get(fileName).get(methodName) + "\n";
            }
        }
        return output;
    }

}

