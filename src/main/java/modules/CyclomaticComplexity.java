package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;
import flowgraph.CyclicFlowGraphBuilder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import utils.Adjustment;

/**
 *
 * @author Nicolas Klenert
 */
public class CyclomaticComplexity implements AdjustableModuleInterface {
    
    Adjustment config;
    private final CyclicFlowGraphBuilder builder;
    ArrayList<ClassInfo> classInfos;
    ArrayList<CodeBlock> blocks;
    
    public CyclomaticComplexity(){
        builder = new CyclicFlowGraphBuilder();
        classInfos = new ArrayList<>();
        blocks = new ArrayList<>();
    }
    
    @Override
    public String getName() {
        return "CyclomaticComplexity";
    }

    /**
     * @param sourceRoot the sourceRoot created by JavaParser
     * @return results of the module being run
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
        //TODO: Test
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
            classInfos.addAll(cu
                    .findAll(ClassOrInterfaceDeclaration.class)
                    .stream()
                    .filter(c -> !c.isInterface())
                    .map(c -> new ClassInfo(c))
                    .collect(Collectors.toList()));
        }
        classInfos.sort((c1,c2) -> c1.sum - c2.sum);
        for(ClassInfo classInfo : classInfos){
            blocks.addAll(classInfo.blocks);
        }
        blocks.sort((CodeBlock c1, CodeBlock c2) -> c1.cyclo - c2.cyclo);
        if(blocks.isEmpty()){
            return new String[] {getName(), "No components were found!"};
        }
        return new String[] {getName(),
            "Most complex class is "+classInfos.get(classInfos.size()-1).name+" with Cyclometic Complexity "+classInfos.get(classInfos.size()-1).sum,
            "Number of Components: "+Integer.toString(blocks.size()),
            "Most Complex Component: "+Integer.toString(blocks.get(blocks.size()-1).cyclo),
            "Median Complexity: "+Integer.toString(blocks.get(blocks.size()/2).cyclo)
        };
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
        StringBuilder stringBuilder = new StringBuilder();
        List<? extends Object> list = ("components".equals(config.getString("scope"))) ? blocks : classInfos;
        for (Object o : list) {
            stringBuilder.append(o.toString());
            stringBuilder.append(System.getProperty("line.separator"));
        }
        return stringBuilder.toString();
    }

    @Override
    public void setAdjustments(Adjustment setting) {
        config = setting;
    }

    @Override
    public Map<String, String> getDefaults() {
        return Map.of(
                "threshold",        "7",
                "scope",            "class"
        );
    }
    
    private class ClassInfo{
        final String name;
        final ArrayList<CodeBlock> blocks;
        final int min;
        final int max;
        final int median;
        final int sum;
        public ClassInfo(ClassOrInterfaceDeclaration classDeclaration){
            name = classDeclaration.getNameAsString();
            blocks = new ArrayList<>();
            //get all methods
            blocks.addAll(classDeclaration.getMethods().stream().map(m -> new Method((MethodDeclaration)m,name)).collect(Collectors.toList()));
            //get all initializers
            blocks.addAll(classDeclaration.findAll(InitializerDeclaration.class).stream().map(i -> new CodeBlock((InitializerDeclaration)i,name)).collect(Collectors.toList()));
            //TODO: only sort if scope is class
            blocks.sort((CodeBlock c1, CodeBlock c2) -> c1.cyclo - c2.cyclo);
            if(!blocks.isEmpty()){
                max = blocks.get(blocks.size()-1).cyclo;
                min = blocks.get(0).cyclo;
                median = blocks.get(blocks.size() / 2).cyclo;
                sum = blocks.stream().mapToInt(c -> c.cyclo).sum();
            }else{
                max = 0;
                min = 0;
                median = 0;
                sum = 0;
            }
        }
        @Override
        public String toString(){
            return "";
        }
    }
    
    private class CodeBlock{
        int cyclo;
        int startLine;
        boolean isStatic;
        String className;
        public CodeBlock(){}
        public CodeBlock(InitializerDeclaration init,String className){
            this.startLine = init.getBegin().isPresent() ? init.getBegin().get().line : -1;
            this.isStatic = init.isStatic();
            this.cyclo = builder.explore(init).getCyclomaticComplexity();
            this.className = className;
        }
        @Override
        public String toString(){
            return "";
        }
        
    }
    
    private class Method extends CodeBlock{
        String name;
        public Method(MethodDeclaration method, String className){
            this.name = method.getNameAsString();
            this.isStatic = method.isStatic();
            this.startLine = method.getBegin().isPresent() ? method.getBegin().get().line : -1;
            this.cyclo = builder.explore(method).getCyclomaticComplexity();
            this.className = className;
        }
        @Override
        public String toString(){
            return "";
        }
    }
    
}
