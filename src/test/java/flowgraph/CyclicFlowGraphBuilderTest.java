package flowgraph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
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
public class CyclicFlowGraphBuilderTest {
    
    private final CyclicFlowGraphBuilder builder = new CyclicFlowGraphBuilder();
    private final JavaParser parser = new JavaParser();
    
    private final String[] ifStrings = {"if(true){x=0;}",//if1.sfg
            "if(true){ x = 0;} else if(false) {x=1;}",//if2.sfg
            "if(true){ x = 0;} else { if(false) {x=1;} }",//if3.sfg
            "if(true) x=0; else if(true) x=1; else if(false) x=2; else x=3",//if4.svg
            "if(true){if(true){x=0;}if(true){x=1;}}"};//if5.svg
   
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
        boolean[] bools = {false,false,true,true,false};
        int[] sizes = {1,2,2,4,1};
                
        for(int i = 0; i < ifStrings.length; ++i){
            ParseResult<Statement> parsed = parser.parseStatement(ifStrings[i]);
            IfStmt stmt = (IfStmt)parsed.getResult().get();
            Pair<HashSet<Node>, Boolean> result = builder.ifStatementResolver(stmt);
            assertEquals(sizes[i], result.a.size());
            assertEquals(bools[i], result.b);
        }
    }

    /**
     * Test of explore method regarding if statements, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreIf() {       
        String[] graphs = {"if1","if2","if3","if4","if5"};
        TestUtils.compareGraphs(graphs,ifStrings,builder);
    }
    
    /**
     * Test of explore method regarding switch statements, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreSwitch() {
        final String[] switchStrings = {"switch(x){case 1: x=0; break; case 2: break;}",
            "switch(x){case 1: x=0; break; case 2: x=1;}",
            "switch(x){case 1: x=0; break; default: x=1;}",
            "switch(x){case 1: x=0; default: x=1;}",
            "switch(x){case 1: case 2: x=0; break; default: x=1;}",
            "switch(x){case 1: x=0;}",
            "switch(x){default: x=0;}",//really special case -> start node connected to end node. Nothing else. (End node necessary, if some breaks apper)
            "switch(x){}",
            "switch(x){case 1: switch(y){case 1: break; case 2: break;} case 2: break; default: break;}",
            "switch(x){default: if(x) break; else x=0;}"}; //works the same as a normale if() else statement but if case skips a node
        
        final String[] graphs = {"if2","if2","switch1","switch2","switch1","if1","simple2","simple1","test2","skip1"};
        TestUtils.compareGraphs(graphs,switchStrings,builder);
    }
    
    /**
     * Test of explore method regarding for, for-each and while statements, of class FlowGraphBuilder.
     * 
     */
    @Test
    public void testExploreLoop() {       
        final String[] loopStrings = {"for(int i=0; i < 10; ++i){x=0;}",
            "for(int i : arr){x=0;}",
            "while(i < 0){x=0;}",
            "for(;;){for(;;){x=0;}}"};
        
        final String[] graphSrc = {"loop1","loop1","loop1","loop2"};
        TestUtils.compareGraphs(graphSrc,loopStrings,builder);
    }
    
    /**
     * Test of explore method regarding do-while statements, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreDoWhileLoop() {
        final String doWhileString = "do {x=0;} while(i < 0);";
        final String graphSrc = "do1";
        TestUtils.compareGraphs(graphSrc,doWhileString,builder);
    }
    
    /**
     * Test of explore method regarding ternary expressions, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreTernary(){
        //ConditionalsExpression
    }

    /**
     * Test of explore method regarding loops with break statements, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreLoopsWithBreaks(){        
        final String[] string = {"for(;;){if(true) break;}"};
        final String[] graphs = {"break1"};
        TestUtils.compareGraphs(graphs,string,builder);
    }
    
    /**
     * Test of explore method regarding labeled break statements, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreLoopsWithLabeledBreaks(){
        //LabeledStmt
        
    }
    
    /**
     * Test of explore method regarding continue statements, of class FlowGraphBuilder.
     */
    @Test
    public void testExploreLoopsWithContinues(){
        
    }
    
    /**
     * Test of explore method regarding methods, of class FlowGraphBuilder.
     */
    @Test
    public void testMethod(){
        
    }
    
}
