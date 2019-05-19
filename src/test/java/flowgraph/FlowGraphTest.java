package flowgraph;

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
public class FlowGraphTest {
    
    public FlowGraphTest() {
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
    public void testParallelAppend(){
        FlowGraph graph = new FlowGraph();
        graph.parallel_append(new FlowGraph(true));
        assertEquals(3,graph.getEdgeCount());
        assertEquals(3,graph.getNodeCount());
        graph.parallel_append(new FlowGraph(false));
        assertEquals(4,graph.getEdgeCount());
        assertEquals(4,graph.getNodeCount());
        graph.parallel_append(new FlowGraph());
        assertEquals(6,graph.getEdgeCount());
        assertEquals(5,graph.getNodeCount());
    }
    
    @Test
    public void testSerialMerge(){
        FlowGraph graph = new FlowGraph();
        graph.serial_merge(new FlowGraph(true));
        assertEquals(graph.getEdgeCount(),1);
        assertEquals(graph.getNodeCount(),2);
        graph.serial_merge(new FlowGraph());
        assertEquals(graph.getEdgeCount(),1);
        assertEquals(graph.getNodeCount(),2);
        graph.serial_merge(new FlowGraph(false)); //no change because there is no connection
        assertEquals(graph.getEdgeCount(),1);
        assertEquals(graph.getNodeCount(),2);
    }
    
}
