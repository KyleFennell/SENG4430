package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.utils.SourceRoot;
import utils.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Project          : Software Quality Assignment
 * Class name       : StringMatchFanOut
 * Author(s)        : Kyle Fennell
 * Purpose          : This was made in compensation for FanIn being broken
 *                      It lists the number of times each unique method name is called
 *                      giving an idea about what kind of functions are used most
 *                      in a program.
 *                      Class1.get() and Class2.get() will result in - get: 2.
 */

public class StringMatchFanIn implements ModuleInterface{

    private Map<String, Integer> uniqueFanIn = new HashMap<>();

    @Override
    public String getName() {
        return "String_Match_Fan_In";
    }

    @Override
    public String[] executeModule(SourceRoot sourceRoot) {

        // loop through all files
        for (CompilationUnit unit : sourceRoot.getCompilationUnits()){
            Logger.debug("INITIAL PROCESSING FILE: " + unit.getStorage().get().getFileName());

            List<MethodDeclaration> methodDeclarations = unit.findAll(MethodDeclaration.class);
            // for each method declared in the file
            for (MethodDeclaration m : methodDeclarations) {
                Logger.debug("ADDING METHOD: " + m.getNameAsString());
                // if the method has a unique name add it to the the hashmap
                if (!uniqueFanIn.containsKey(m.getNameAsString())) {
                    uniqueFanIn.put(m.getNameAsString(), 0);
                }
            }
        }

        // re-loop through all the files
        for (CompilationUnit unit : sourceRoot.getCompilationUnits()){
            Logger.debug("RE-PROCESSING FILE: " + unit.getStorage().get().getFileName());

            List<MethodDeclaration> methodDeclarations = unit.findAll(MethodDeclaration.class);
            // loop through all method body's finding all method calls
            for (MethodDeclaration m : methodDeclarations){
                Logger.debug("PROCESSING METHOD: " + m.getNameAsString());
//                System.out.println("PROCESSING METHOD: " + m.getNameAsString());
                if(! m.getBody().isPresent()) {
                    Logger.debug("BODY NOT PRESENT");
                    continue;
                }

                BlockStmt body = m.getBody().get();
                List<MethodCallExpr> calls =  body.findAll(MethodCallExpr.class);
                for (MethodCallExpr c : calls){
                    String callName = c.getNameAsString();
                    if (!uniqueFanIn.containsKey(callName)){
                        uniqueFanIn.put(callName, 0);
                    }
                    uniqueFanIn.put(callName, uniqueFanIn.get(callName) + 1);
                }

            }
        }
        return Util.calculateBasicMetrics(uniqueFanIn);
    }

    @Override
    public String getDescription() {

        return "Counts every method call by name. Does not take into account the class " +
                "this method belongs to. i.e. class1.get() == class2.get() in terms of " +
                "counting. This metric shows the 'kinds' of methods that are used most in " +
                "the program.";
    }

    @Override
    public String printMetrics(){
        String output = "String Match Fan In: \n";
        for (String m : uniqueFanIn.keySet()){
            output += m + ": " + uniqueFanIn.get(m) + "\n";
        }

        return output;
    }

}
