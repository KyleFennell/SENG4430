package modules;

import flowgraph.AcyclicFlowGraphBuilder;
import flowgraph.FlowGraph;
import java.util.HashMap;
import java.util.Map;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : NumberOfPaths
 * @author(s)       : Nicolas Klenert
 * Date             : 14/05/19
 * Purpose          : Module to calculate the number of different paths of a FlowGraph.
 * 
 * This module can be used to estimate how many test are needed to fully test the behaviour of a method.
 * Different paths on FlowGraph are paths, which have at least one not in common with each other.
 */
public class NumberOfPaths extends FlowGraphNumberExtractor {
        
    public NumberOfPaths(){
        super(new AcyclicFlowGraphBuilder());
    }
    
    @Override
    public String getName() {
        return "NumberOfPaths";
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
        return graph.getNumberOfPaths();
    }

    @Override
    protected String getNameOfNumber() {
        return "Number of Paths";
    }   
   
}

