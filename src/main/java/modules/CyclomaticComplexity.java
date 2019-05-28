package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;
import flowgraph.CyclicFlowGraphBuilder;
import java.util.ArrayList;
import java.util.HashMap;
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
        //clear all runs before that one
        classInfos.clear();
        blocks.clear();
        //add and sort
        for (CompilationUnit cu : sourceRoot.getCompilationUnits()) {
            classInfos.addAll(cu
                    .findAll(ClassOrInterfaceDeclaration.class)
                    .stream()
                    .filter(c -> !c.isInterface())
                    .map(c -> new ClassInfo(c,cu.getStorage().isPresent() ?
                            cu.getStorage().get().getPath().toString() :
                            "PATH NOT COMPUTABLE"))
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
        return "";
    }

    /**
     * @return a formatted string that represents the metrics determined
     * by the module
     */
    @Override
    public String printMetrics() {
        StringBuilder stringBuilder = new StringBuilder();
        if("components".equals(config.getString("scope"))){
            int threshold = config.getInt("componentThreshold");
            blocks.stream().filter(c -> c.cyclo >= threshold)
                    .forEach(c -> stringBuilder.append(c.toString()).append(System.getProperty("line.separator")));
        }else{
            int threshold = config.getInt("classThreshold");
            classInfos.stream().filter(c -> c.sum >= threshold)
                    .forEach(c -> stringBuilder.append(c.toString()).append(System.getProperty("line.separator")));
        }
        return stringBuilder.toString();
    }

    @Override
    public void setAdjustments(Adjustment setting) {
        config = setting;
    }

    @Override
    public Map<String, String> getDefaults() {
        Map<String, String> map = new HashMap<>();
        map.put("classThreshold",       "40");
        map.put("componentThreshold",   "10");
        //scope can be either class or components
        map.put("scope",                "class");
        return map;
    }
    
    private class ClassInfo{
        final String filePath;
        final String name;
        final ArrayList<CodeBlock> blocks;
        final int min;
        final int max;
        final int median;
        final int sum;
        public ClassInfo(ClassOrInterfaceDeclaration classDeclaration, String filePath){
            name = classDeclaration.getNameAsString();
            this.filePath = filePath;
            blocks = new ArrayList<>();
            //get all methods
            blocks.addAll(classDeclaration.getMethods().stream().map(m -> new Method(m,name,filePath)).collect(Collectors.toList()));
            //get all initializers
            blocks.addAll(classDeclaration.findAll(InitializerDeclaration.class).stream().map(i -> new CodeBlock(i,name,filePath)).collect(Collectors.toList()));
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
            StringBuilder builder = new StringBuilder();
            builder.append(filePath)
                   .append("; ClassName: ")
                   .append(name)
                   .append("; Number of Components: ")
                   .append(blocks.size())
                   .append("; Complexity: ")
                   .append(sum);
            return builder.toString();
        }
    }
    
    private class CodeBlock{
        int cyclo;
        int startLine;
        boolean isStatic;
        String className;
        String filePath;        
        
        public CodeBlock(){}
        public CodeBlock(InitializerDeclaration init, String className, String filePath){
            this.startLine = init.getBegin().isPresent() ? init.getBegin().get().line : -1;
            this.isStatic = init.isStatic();
            this.cyclo = builder.explore(init).getCyclomaticComplexity();
            this.className = className;
            this.filePath = filePath;
        }
        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            String line = startLine == -1 ? "; Initializer in Class "+className : "; Initializer on Line "+startLine;
            builder.append(filePath)
                   .append(line)
                   .append("; Complexity: ")
                   .append(cyclo);
            return builder.toString();
        }
        
    }
    
    private class Method extends CodeBlock{
        String name;
        public Method(MethodDeclaration method, String className, String filePath){
            this.name = method.getNameAsString();
            this.isStatic = method.isStatic();
            this.startLine = method.getBegin().isPresent() ? method.getBegin().get().line : -1;
            this.cyclo = builder.explore(method).getCyclomaticComplexity();
            this.className = className;
            this.filePath = filePath;
        }
        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            String line = startLine == -1 ? "" : " on Line "+startLine;
            builder.append(filePath)
                   .append("; Method "+name)
                   .append(line)
                   .append("; Complexity: ")
                   .append(cyclo);
            return builder.toString();
        }
    }
    
}
