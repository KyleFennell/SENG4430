package flowgraph;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 *
 * @author Nicolas Klenert
 */
public class FlowGraphTest {
    
    private final String dir = "src/test/resources/flowgraph/graphs/";

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
        assertEquals(1,graph.getEdgeCount());
        assertEquals(2,graph.getNodeCount());
        graph.serial_merge(new FlowGraph());
        assertEquals(1,graph.getEdgeCount());
        assertEquals(2,graph.getNodeCount());
        graph.serial_merge(new FlowGraph(false)); //no change because there is no connection
        assertEquals(1,graph.getEdgeCount());
        assertEquals(2,graph.getNodeCount());
    }
    
    @Test
    public void testMerge(){
        FlowGraph graph = new FlowGraph(true);
        graph.merge(new FlowGraph(true));
        assertEquals(1,graph.getEdgeCount());
        assertEquals(2,graph.getNodeCount());
    }
    
    @Test
    public void testNumberOfPaths(){
        FlowGraph empty = new FlowGraph();
        assertEquals(1,empty.getNumberOfPaths());
        FlowGraph easyLoop = FlowGraph.createLoopFlowGraph(false).a;
        assertEquals(4,easyLoop.getNodeCount());
        assertEquals(-1,easyLoop.getNumberOfPaths());
        //more test are done in tandem with testFlowGraphFromFile
    }
    
    @Test
    public void testFlowGraphFromFile(){
        String[] files = {"test1","test2","loop1","abstract1","abstract2"};
        int[] nodes = {4,8,4,12,7};
        int[] edges = {4,11,4,13,8};
        int[] paths = {2,5,-1,-1,-1};
        
        for(int i = 0; i < files.length; ++i){
            FlowGraph graph = new FlowGraph(dir+files[i]+".sfg");
            assertEquals(nodes[i],graph.getNodeCount());
            assertEquals(edges[i],graph.getEdgeCount());
            assertEquals(paths[i],graph.getNumberOfPaths());
        }
        //TODO: test more!
    }
}
