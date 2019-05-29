package flowgraph;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;

/** Creates guaranteed acyclic {@code FlowGraphs} out of some {@link Node} and it's children.
 * 
 *  <p>This class contains only "explore" methods, which have a similar
 * effect as the one described in {@link AbstractFlowGraphBuilder}<br>
 * 
 *  Because their use case are similar, we won't give a JavaDoc for these functions.</p>
 * 
 * <p>Project          : Software Quality Assignment 1<br>
 *    Date             : 26/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see AbstractFlowGraphBuilder
 * @see CyclicFlowGraphBuilder
 * 
 */
public class AcyclicFlowGraphBuilder extends AbstractFlowGraphBuilder{
    
    @Override
    protected FlowGraph exploreContinueStatement(ContinueStmt stmt){
        return null;
    }
    
    @Override
    protected <T extends Node> FlowGraph exploreLoopStatement(NodeWithBody<T> stmt){
        return exploreLoopLikeStatements(stmt);
    }
    
    @Override
    protected FlowGraph exploreDoStatement(DoStmt stmt){
        return exploreLoopLikeStatements(stmt);
    }
    
    private <T extends Node> FlowGraph exploreLoopLikeStatements(NodeWithBody<T> stmt){
        FlowGraph graph = new FlowGraph(true);
        breakEndPoint = graph.end;
        graph = explore(stmt.getBody(),true).serial_merge(graph);
        breakEndPoint = null;
        return graph;
    }
}
