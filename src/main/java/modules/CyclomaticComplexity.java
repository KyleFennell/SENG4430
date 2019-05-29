package modules;

import flowgraph.CyclicFlowGraphBuilder;
import flowgraph.FlowGraph;
import java.util.HashMap;
import java.util.Map;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : CyclomaticComplexity
 * @author(s)       : Nicolas Klenert
 * Date             : 14/05/19
 * Purpose          : Module to calculate the Cyclomatic Complexity of java code.
 */
public class CyclomaticComplexity extends FlowGraphNumberExtractor {
        
    public CyclomaticComplexity(){
        super(new CyclicFlowGraphBuilder());
    }
    
    @Override
    public String getName() {
        return "CyclomaticComplexity";
    }

    /**
     * @return a description of what the module is testing
     */
    @Override
    public String getDescription() {
        return "";
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

    @Override
    protected int getFlowGraphNumber(FlowGraph graph) {
        return graph.getCyclomaticComplexity();
    }

    @Override
    protected String getNameOfNumber() {
        return "Cyclomatic Complexity";
    }   
   
}
