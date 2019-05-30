package modules;

import flowgraph.CyclicFlowGraphBuilder;
import flowgraph.FlowGraph;
import java.util.HashMap;
import java.util.Map;

/** Module to calculate the Cyclomatic Complexity of java code.
 * 
 *  <p>This module is configurable with the configuration file with the name {@code config.properties}.<br>
 *  Possible identifiers are: <br>
 *  CyclomaticComplexity_scope : class | components - print components or classes for the advanced analysis<br>
 *  CyclomaticComplexity_classThreshold : [int] - print only classes which have at least that number of Cyclomatic Complexity<br>
 *  CyclomaticComplexity_componentThreshild : [int] - print only components which have at least that number of Cyclomatic Complexity</p>
 * 
 *  <p>Components are initializers, constructors and methods.</p>
 * 
 * <p>Project          : Software Quality Assignment 1<br>
 *    Date             : 26/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see AdjustableModuleInterface
 * @see CyclicFlowGraphBuilder
 * 
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
        return "Module to calculate the Cyclomatic Complexity of java code.";
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
