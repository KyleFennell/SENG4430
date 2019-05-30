/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package modules;

import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.InitializerDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.utils.SourceRoot;
import flowgraph.AbstractFlowGraphBuilder;
import flowgraph.FlowGraph;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;
import utils.Adjustment;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : FlowGraphNumberExtractor
 * @author(s)       : Nicolas Klenert
 * Date             : 14/05/19
 * Purpose          : Abstract Module used to calculate some statistic number on FlowGraphs.
 */
abstract public class FlowGraphNumberExtractor  implements AdjustableModuleInterface{
    
    Adjustment config;
    private final AbstractFlowGraphBuilder builder;
    ArrayList<ClassInfo> classInfos;
    ArrayList<CodeBlock> blocks;
    
    public FlowGraphNumberExtractor(AbstractFlowGraphBuilder builder){
        this.builder = builder;
        classInfos = new ArrayList<>();
        blocks = new ArrayList<>();
    }
    
    abstract protected int getFlowGraphNumber(FlowGraph graph);
    abstract protected String getNameOfNumber();
    
    @Override
    abstract public String getName();

    /**
     * @param sourceRoot the sourceRoot created by JavaParser
     * @return results of the module being run
     */
    @Override
    public String[] executeModule(SourceRoot sourceRoot) {
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
        blocks.sort((CodeBlock c1, CodeBlock c2) -> c1.number - c2.number);
        if(blocks.isEmpty()){
            return new String[] {getName(), "No components were found!"};
        }
        return new String[] {getName(),
            "Most complex class is "+classInfos.get(classInfos.size()-1).name+" with "+getNameOfNumber()+" "+classInfos.get(classInfos.size()-1).sum,
            "Number of Components: "+Integer.toString(blocks.size()),
            "Most Complex Component: "+Integer.toString(blocks.get(blocks.size()-1).number),
            "Median Complexity: "+Integer.toString(blocks.get(blocks.size()/2).number)
        };
    }

    /**
     * @return a description of what the module is testing
     */
    @Override
    abstract public String getDescription();

    /**
     * @return a formatted string that represents the metrics determined
     * by the module
     */
    @Override
    public String printMetrics() {
        StringBuilder stringBuilder = new StringBuilder();
        if("components".equals(config.getString("scope"))){
            int threshold = config.getInt("componentThreshold");
            blocks.stream().filter(c -> c.number >= threshold)
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
    
    /** Class and it's children are used to store the information gathered.
     * 
     */
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
            //get all methods -> constructors are not yet involved!
            blocks.addAll(classDeclaration.getMethods()
                    .stream()
                    .map(m -> new Method(m,name,filePath))
                    .collect(Collectors.toList()));
            //get all constructor
            blocks.addAll(classDeclaration.findAll(ConstructorDeclaration.class)
                    .stream()
                    .map(c -> new Constructor(c,name,filePath))
                    .collect(Collectors.toList()));
            //get all initializers
            blocks.addAll(classDeclaration.findAll(InitializerDeclaration.class)
                    .stream()
                    .map(i -> new CodeBlock(i,name,filePath))
                    .collect(Collectors.toList()));
            //TODO: only sort if scope is class
            blocks.sort((CodeBlock c1, CodeBlock c2) -> c1.number - c2.number);
            if(!blocks.isEmpty()){
                max = blocks.get(blocks.size()-1).number;
                min = blocks.get(0).number;
                median = blocks.get(blocks.size() / 2).number;
                sum = blocks.stream().mapToInt(c -> c.number).sum();
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
                   .append("; ")
                   .append(getNameOfNumber())
                   .append(": ")
                   .append(sum);
            return builder.toString();
        }
    }
    
    private class CodeBlock{
        int number;
        int startLine;
        boolean isStatic;
        String className;
        String filePath;        
        
        public CodeBlock(){}
        public CodeBlock(InitializerDeclaration init, String className, String filePath){
            this.startLine = init.getBegin().isPresent() ? init.getBegin().get().line : -1;
            this.isStatic = init.isStatic();
            this.number = getFlowGraphNumber(builder.explore(init));
            this.className = className;
            this.filePath = filePath;
        }
        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            String line = startLine == -1 ? "; Initializer in Class "+className : "; Initializer on Line "+startLine;
            builder.append(filePath)
                   .append(line)
                   .append("; ")
                   .append(getNameOfNumber())
                   .append(": ")
                   .append(number);
            return builder.toString();
        }
        
    }
    
    private class Method extends CodeBlock{
        String name;
        public Method(MethodDeclaration method, String className, String filePath){
            this.name = method.getNameAsString();
            this.isStatic = method.isStatic();
            this.startLine = method.getBegin().isPresent() ? method.getBegin().get().line : -1;
            this.number = builder.explore(method).getCyclomaticComplexity();
            this.className = className;
            this.filePath = filePath;
        }
        @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            String line = startLine == -1 ? "" : " on Line "+startLine;
            builder.append(filePath)
                   .append("; Method ")
                   .append(name)
                   .append(line)
                   .append("; ")
                   .append(getNameOfNumber())
                   .append(": ")
                   .append(number);
            return builder.toString();
        }
    }
    
    private class Constructor extends CodeBlock{
        String declarationName;
        public Constructor(ConstructorDeclaration declaration, String className ,String filePath){
            this.declarationName = declaration.getDeclarationAsString(true, false, true);
            this.isStatic = false;
            this.startLine = declaration.getBegin().isPresent() ? declaration.getBegin().get().line : -1;
            this.number = builder.explore(declaration).getCyclomaticComplexity();
            this.className = className;
            this.filePath = filePath;
        }
         @Override
        public String toString(){
            StringBuilder builder = new StringBuilder();
            String line = startLine == -1 ? "" : " on Line "+startLine;
            builder.append(filePath)
                   .append("; Constructor ")
                   .append(declarationName)
                   .append(line)
                   .append("; ")
                   .append(getNameOfNumber())
                   .append(": ")
                   .append(number);
            return builder.toString();
        }
    }
}
