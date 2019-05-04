/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowgraph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.utils.Pair;
import java.util.HashSet;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Nicolas Klenert
 */
public class FlowGraphBuilderTest {
    
    private final String[] ifStrings = {"if(true){x=0;}",
            "if(true){ x = 0;} else if(false) {x=1;}",
            "if(true){ x = 0;} else { if(false) {x=1;} }",
            "if(true) x=0; else if(true) x=1; else if(false) x=2; else x=3",
            "if(true){if(true){x=0;}if(true){x=1;}}"};
    
    private final String[] switchStrings = {"switch(x){case 1: x=0; break; case 2: break;}",
            "switch(x){case 1: x=0; break; case 2: x=1;}",
            "switch(x){case 1: x=0; break; default: x=1;}",
            "switch(x){case 1: x=0; default: x=1;}",
            "switch(x){case 1: case 2: x=0; break; default: x=1;}",
            "switch(x){case 1: x=0;}",
            "switch(x){default: x=0;}",
            "switch(x){}"};
    
    private final String[] loopStrings = {"for(int i=0; i < 10; ++i){x=0;}",
            "for(int i : arr){x=0;}",
            "while(i < 0){x=0;}"};
    
    private final String doWhileString = "do {x=0;} while(i < 0);";

    @BeforeAll
    public static void setUpClass() throws Exception {
    }

    @AfterAll
    public static void tearDownClass() throws Exception {
    }

    @BeforeEach
    public void setUp() throws Exception {
    }

    @AfterEach
    public void tearDown() throws Exception {
    }

    /**
     * Test of ifStatementResolver method, of class FlowGraphBuilder.
     */
    @Test
    public void testIfStatementResolver() {
        JavaParser parser = new JavaParser();
        
        boolean[] bools = {false,false,true,true,false};
        int[] sizes = {1,2,2,4,1};
                
        for(int i = 0; i < ifStrings.length; ++i){
            ParseResult<Statement> parsed = parser.parseStatement(ifStrings[i]);
            IfStmt stmt = (IfStmt)parsed.getResult().get();
            Pair<HashSet<Node>, Boolean> result = FlowGraphBuilder.ifStatementResolver(stmt);
            assertEquals(sizes[i], result.a.size());
            assertEquals(bools[i], result.b);
        }
    }

    /**
     * Test of explore method, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreIf() {
        
        JavaParser parser = new JavaParser();
        
        int[] nodes = {3,4,6,6,7};
        int[] edges = {3,5,7,8,9};
        
        for(int i = 0; i < ifStrings.length; ++i){
            ParseResult<Statement> parsed = parser.parseStatement(ifStrings[i]);
            IfStmt stmt = (IfStmt)parsed.getResult().get();
            FlowGraph result = FlowGraphBuilder.explore(stmt);
            assertEquals(nodes[i], result.getNodeCount());
            assertEquals(edges[i], result.getEdgeCount());
        }
    }
    
  /**
     * Test of explore method, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreSwitch() {
        
        JavaParser parser = new JavaParser();
        
        int[] nodes = {4,4,4,4,4,3,1,1};
        int[] edges = {5,5,4,4,4,3,0,0};
        int[] inDeg = {3,3,2,1,2,2,0,0};
        
        for(int i = 0; i < switchStrings.length; ++i){
            ParseResult<Statement> parsed = parser.parseStatement(switchStrings[i]);
            SwitchStmt stmt = (SwitchStmt)parsed.getResult().get();
            FlowGraph result = FlowGraphBuilder.explore(stmt);
            //assertEquals(nodes[i], result.getNodeCount());
            //assertEquals(edges[i], result.getEdgeCount());
            //assertEquals(inDeg[i], result.end.inDeg());
        }
    }
    
    /**
     * Test of explore method, of class FlowGraphBuilder.
     * 
     * regarding ForStmt, ForEachStmt, WhileStmt
     */
    @Test
    public void testExploreLoop() {
        
        JavaParser parser = new JavaParser();
        
        int node = 3;
        int edge = 3;
        int startInDeg = 1;
        int startOutDeg = 2;
        int endInDeg = 1;
        int endOutDeg = 0;

        
        for(int i = 0; i < loopStrings.length; ++i){
            ParseResult<Statement> parsed = parser.parseStatement(loopStrings[i]);
            Statement stmt = parsed.getResult().get();
            FlowGraph result = FlowGraphBuilder.explore(stmt);
            //assertEquals(node, result.getNodeCount());
            //assertEquals(edge, result.getEdgeCount());
            //assertEquals(startInDeg, result.start.inDeg());
            //assertEquals(startOutDeg, result.start.outDeg());
            //assertEquals(endInDeg, result.end.inDeg());
            //assertEquals(endOutDeg, result.end.outDeg());
        }
    }
    
    /**
     * Test of explore method, of class FlowGraphBuilder.
     * 
     * regarding DoStmt
     */
    @Test
    public void testExploreDoWhileLoop() {
        
        JavaParser parser = new JavaParser();
        
        int node = 2;
        int edge = 2;
        int startInDeg = 1;
        int startOutDeg = 2;
        int endInDeg = 1;
        int endOutDeg = 0;
        
        ParseResult<Statement> parsed = parser.parseStatement(doWhileString);
        Statement stmt = parsed.getResult().get();
        FlowGraph result = FlowGraphBuilder.explore(stmt);
        //assertEquals(node, result.getNodeCount());
        //assertEquals(edge, result.getEdgeCount());
        //assertEquals(startInDeg, result.start.inDeg());
        //assertEquals(startOutDeg, result.start.outDeg());
        //assertEquals(endInDeg, result.end.inDeg());
        //assertEquals(endOutDeg, result.end.outDeg());
    }
    
    @Test
    public void testExploreTernary(){
        //ConditionalsExpression
    }

    @Test
    public void testExploreLoopsWithBreaks(){
        
    }
    
    @Test
    public void testExploreLoopsWithLabeledBreaks(){
        //LabeledStmt
        
    }
    
}
