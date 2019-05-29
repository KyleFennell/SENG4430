/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowgraph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.stmt.Statement;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertAll;

/**
 *
 * @author Nicolas Klenert
 */
public class TestUtils {
    private static final String DIR = "src/test/resources/flowgraph/graphs/";
    private static final JavaParser PARSER = new JavaParser();
    
    public static void compareGraphs(FlowGraph graph, FlowGraph result){
        assertAll("comparing Graphs",
                () -> assertEquals(graph.getNodeCount(), result.getNodeCount()),
                () -> assertEquals(graph.getEdgeCount(), result.getEdgeCount()),
                () -> assertEquals(graph.start.inDeg(), result.start.inDeg()),
                () -> assertEquals(graph.start.outDeg(), result.start.outDeg()),
                () -> assertEquals(graph.end.inDeg(), result.end.inDeg()),
                () -> assertEquals(graph.end.outDeg(), result.end.outDeg()),
                () -> assertEquals(graph.getNumberOfPaths(), graph.getNumberOfPaths())
                );
    }
    
    public static void compareGraphs(String src, FlowGraph result){
        compareGraphs(new FlowGraph(DIR+src+".sfg"),result);
    }
    
    public static void compareGraphs(String src, String code, AbstractFlowGraphBuilder builder){
        ParseResult<Statement> parsed = PARSER.parseStatement(code);
        Statement stmt = parsed.getResult().get();
        FlowGraph result = builder.explore(stmt);
        compareGraphs(src, result);
    }
    
    public static void compareGraphs(String[] src, String[] code, AbstractFlowGraphBuilder builder){
        for(int i = 0; i < code.length; ++i){
            compareGraphs(src[i],code[i],builder);
        }
    }
}
