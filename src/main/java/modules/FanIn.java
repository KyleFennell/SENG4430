package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.utils.SourceRoot;

public class FanIn implements ModuleInterface{


    @Override
    public String getName() {
        return "Fan_In";
    }

    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        String fileCount = sourceRoot.getCompilationUnits().toString();
        System.out.println("files found: "+fileCount);
        for (CompilationUnit c : sourceRoot.getCompilationUnits()){
            System.out.println(c.getPrimaryType());
            System.out.println();
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
