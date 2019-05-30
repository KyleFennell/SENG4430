package modules;

import flowgraph.AcyclicFlowGraphBuilder;
import flowgraph.FlowGraph;
import java.util.HashMap;
import java.util.Map;

/** Calculates the number of different paths of a FlowGraph.
 * 
 *  <p>This module can be used to estimate how many test are needed to fully test the behaviour of a method.
 * Different paths on FlowGraph are paths, which have at least one not in common with each other.</p>
 * 
 *  <p>It is configurable with the configuration file with the name {@code config.properties}.<br>
 *  Possible identifiers are: <br>
 *  NumberOfPaths_scope : class | components - print components or classes for the advanced analysis<br>
 *  NumberOfPaths_classThreshold : [int] - print only classes which have at least that number of different paths<br>
 *  NumberOfPaths_componentThreshild : [int] - print only components which have at least that number of different paths</p>
 * 
 * <p>Components are initializers, constructors and methods.</p>
 * 
 * <p>Project          : Software Quality Assignment 1<br>
 *    Date             : 26/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see AdjustableModuleInterface
 * @see AcyclicFlowGraphBuilder
 * 
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
        return "Calculates the number of different paths of a FlowGraph.";
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

