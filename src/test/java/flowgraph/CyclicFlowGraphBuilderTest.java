package flowgraph;

import com.github.javaparser.JavaParser;
import com.github.javaparser.ParseResult;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.utils.Pair;
import java.util.HashSet;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;

/**
 *
 * @author Nicolas Klenert
 */
public class CyclicFlowGraphBuilderTest {
    
    private final CyclicFlowGraphBuilder builder = new CyclicFlowGraphBuilder();
    private final JavaParser parser = new JavaParser();
    
    
    /** Parameterized Test used to compare created FlowGraph of CyclicFlowGraphBuilder with given graph.
     * 
     * @param code code snippet used to create the FlowGraph
     * @param file filename of the saved graph
     */
    @DisplayName("CyclicFlowGraphBuilder behaviour")
    @ParameterizedTest(name = "[{index}]: {2}")
    @CsvFileSource(resources = "/flowgraph/testdata/cyclic.csv", numLinesToSkip = 1)
    public void testAcyclicFlowGraphBuilder(String code, String file){
       assumeTrue(file != null && !file.isEmpty(),"graph "+file+" not given!");
       TestUtils.compareGraphs(file, code, builder);
    }
    
    /**
     * Test of ifStatementResolver method, of class FlowGraphBuilder.
     */
    @Test
    public void testIfStatementResolver() {
        
        final String[] ifStrings = {"if(true){x=0;}",//if1.sfg
            "if(true){ x = 0;} else if(false) {x=1;}",//if2.sfg
            "if(true){ x = 0;} else { if(false) {x=1;} }",//if3.sfg
            "if(true) x=0; else if(true) x=1; else if(false) x=2; else x=3",//if4.svg
            "if(true){if(true){x=0;}if(true){x=1;}}"};//if5.svg
        
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
     * Test of explore method regarding continue statements, of class FlowGraphBuilder.
     */
    @Test
    @Disabled
    public void testExploreLoopsWithContinues(){
        //only exist as an reminder: Continues can also be labeled!
    }
    
    /**
     * Test of explore method regarding methods, of class FlowGraphBuilder.
     */
    @Test
    @Disabled
    public void testMethod(){
        
    }
    
}
