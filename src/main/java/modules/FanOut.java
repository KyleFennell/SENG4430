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

/**
 * Project          : Software Quality Assignment
 * Class name       : FanOut
 * Author(s)        : Kyle Fennell
 * Purpose          : This class calculates the fan outs of all methods
 *                      declared in the project. Total Fan Out is the sum
 *                      of all method calls and Unique Fan Out is the sum
 *                      of unique methods called.
 */

public class FanOut implements ModuleInterface{

    private Map<String, Map<String, Integer>> collatedTotalFanOut = new HashMap<>();
    private Map<String, Map<String, Integer>> collatedUniqueFanOut = new HashMap<>();
    @Override
    public String getName() {
        return "FanOut";
    }

    @Override
    public String[] executeModule(SourceRoot sourceRoot) {

        for (CompilationUnit unit : sourceRoot.getCompilationUnits()){
            Logger.debug("PROCESSING FILE: " + unit.getStorage().get().getFileName());

            Map<String, Integer> fileTotalFanOut = new HashMap<>();
            Map<String, Integer> fileUniqueFanOut = new HashMap<>();

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
                fileTotalFanOut.put(m.getNameAsString(), calls.size());
                fileUniqueFanOut.put(m.getNameAsString(), uniqueCalls.size());
            }
            collatedTotalFanOut.put(unit.getStorage().get().getFileName(), fileTotalFanOut);
            collatedUniqueFanOut.put(unit.getStorage().get().getFileName(), fileUniqueFanOut);
        }
        String[] out = concatArrays(new String[] {"TotalFanOut: "},
                Util.calculateBasicMetrics(flattenMap(collatedTotalFanOut)));
        out = concatArrays(out, new String[] {"UniqueFanOut: "});
        out = concatArrays(out, Util.calculateBasicMetrics(flattenMap(collatedUniqueFanOut)));
        return out;
    }

    @Override
    public String getDescription() {
        return "The Fan Out module has two sub-metrics being the Total Fan Out metric and the Unique Fan Out metric." +
                "Total Fan Out represents the total number of method calls made inside each method while the " +
                "Unique Fan Out is the number of unique method calls.";
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

    /**
     * @param arr1 the first array
     * @param arr2 the second array
     * @return the first array concatenated with the second array
     */
    private String[] concatArrays(String[] arr1, String[] arr2){
        String[] out = new String[arr1.length + arr2.length];
        for (int i = 0; i < arr1.length; i++){
            out[i] = arr1[i];
        }
        for (int i = 0; i < arr2.length; i++){
            out[arr1.length+i] = arr2[i];
        }
        return out;
    }

    /** takes a 2 deep map and flattens it to a 1 deep map
     * @param map the map to flatten
     * @return a map whos entries are ("parentMapKey_childMapKey", childMapVal)
     */
    private Map<String, Integer> flattenMap(Map<String, Map<String, Integer>> map){
        Map<String, Integer> out = new HashMap<>();
        for (String file : map.keySet()){
            for (String method : map.get(file).keySet()){
                String flattenedName = file + "_" + method;
                out.put(flattenedName, map.get(file).get(method));
            }
        }
        return out;
    }
}

