/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowgraph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.fail;
import static org.junit.jupiter.api.Assumptions.assumeTrue;

/** Class to provide comparing functionality of two {@code FlowGraphs}
 * 
 * <p>Project       : Software Quality Assignment 1<br>
 * Date             : 15/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see CyclicFlowGraphBuilderTest
 * @see AcyclicFlowGraphBuilderTest
 */
public class TestUtils {
    private static final String DIR = "src/test/resources/flowgraph/graphs/";
    private static final JavaParser PARSER = new JavaParser();
    
    /** Compares different properties of two graphs to assess if they are similar.
     * 
     * @param graph expected graph
     * @param result constructed graph
     */
    public static void compareGraphs(FlowGraph graph, FlowGraph result){
        //assertAll is used such that all differences are displayed and not only the first mismatch!
        assertAll("comparing Graphs",
                () -> assertEquals(graph.getNodeCount(), result.getNodeCount(), "Nodes"),
                () -> assertEquals(graph.getEdgeCount(), result.getEdgeCount(), "Edges"),
                () -> assertEquals(graph.start.inDeg(), result.start.inDeg(), "Indeg of start node"),
                () -> assertEquals(graph.start.outDeg(), result.start.outDeg(), "Outdeg of start node"),
                () -> assertEquals(graph.end.inDeg(), result.end.inDeg(), "Indeg of end node"),
                () -> assertEquals(graph.end.outDeg(), result.end.outDeg(), "Outdeg of end node"),
                () -> assertEquals(graph.getNumberOfPaths(), graph.getNumberOfPaths(), "Number of Paths")
                );
    }
    
    /** Helper function to more easily compare to functions.
     * 
     * @param src file name in the {@code test/resources/flowgraph/graphs} directory
     * @param result expected graph
     * @see compareGraphs
     */
    public static void compareGraphs(String src, FlowGraph result){
        assumeTrue(src != null && !src.isEmpty(),"graph "+src+" not given!");
        compareGraphs(new FlowGraph(DIR+src+".sfg"),result);
    }
    
    /** Creates a {@link FlowGraph} from {@code code} and compares that to an {@code FlowGraph} saved to a file.
     * 
     *  This Method is heavily used for parametrized tests.
     * 
     * @param src the name of the graph in the {@code test/resource/flowgraph/graph} directory from which to compare
     * @param code the string to parse
     * @param builder the builder to use to parse the {@code code}
     * @param toParse the identifier which says what type of code we are looking at.
     *              Can be either "Statement" or "Method".
     * @see compareGraphs
     */
    public static void compareGraphs(String src, String code, AbstractFlowGraphBuilder builder, String toParse){
        ParseResult<? extends Node> parsed = null;
        try{
            switch(toParse){
                case "Statement": parsed = PARSER.parseStatement(code); break;
                case "Method": parsed = PARSER.parseBodyDeclaration(code); break;
                default: fail("Parameter toParse is not correctly set!");
            }
        }catch(ParseProblemException e){
            fail("Code couldn't be parsed!");
        }
        if(parsed != null){
            if(!parsed.getResult().isPresent()){
                fail("Code is not parsable OR is empty!");
            }
            //assumeTrue(parsed.getResult().isPresent(), "Code is not parsable OR is empty!");
            Node node = parsed.getResult().get();
            FlowGraph result = builder.explore(node);
            compareGraphs(src, result);
        }
    }
    
    /** Function is deprecated! Use Parameterized Tests instead! See tests of {@link CyclicFlowGraphBuilderTest}.
     * 
     * @param src names of files used from which to create graphs
     * @param code code snippet to create the other graph
     * @param builder builder used to parse the {@code code}
     * @param toParse the identifier which says what type of code we are looking at.
     *              Can be either "Statement" or "Method".
     * @deprecated
     */
    @Deprecated
    public static void compareGraphs(String[] src, String[] code, AbstractFlowGraphBuilder builder, String toParse){
        for(int i = 0; i < code.length; ++i){
            compareGraphs(src[i],code[i],builder,toParse);
        }
    }
}
