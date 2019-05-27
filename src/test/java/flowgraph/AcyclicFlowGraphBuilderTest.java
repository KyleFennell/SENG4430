package flowgraph;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : AcyclicFlowGraphBuilderTest
 * @author(s)       : Nicolas Klenert
 * Date             : 26/05/19
 * Purpose          : Tets FlowGraphs created from AcyclicFlowGraphBuilder
 */
public class AcyclicFlowGraphBuilderTest {
    
    private final AcyclicFlowGraphBuilder builder = new AcyclicFlowGraphBuilder();
    
    public AcyclicFlowGraphBuilderTest() {
    }
    
    @BeforeAll
    public static void setUpClass() {
    }
    
    @AfterAll
    public static void tearDownClass() {
    }
    
    @BeforeEach
    public void setUp() {
    }
    
    @AfterEach
    public void tearDown() {
    }

   @Test
   public void testLoopStatements(){
       final String[] strings = {"for(;;){x=0;}","for(;;){for(;;){x=0;}}"};
       final String[] files = {"path1","path2"};
       //TODO: add tests
   }
    
}
