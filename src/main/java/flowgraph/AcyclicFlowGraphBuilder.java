package flowgraph;

import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : AcyclicFlowGraphBuilder
 * @author(s)       : Nicolas Klenert
 * Date             : 26/05/19
 * Purpose          : Creates guaranteed acyclic FlowGraphs out of a parse tree.
 */
public class AcyclicFlowGraphBuilder extends AbstractFlowGraphBuilder{
    
    @Override
    protected FlowGraph exploreContinueStatement(ContinueStmt stmt){
        return null;
    }
    
    @Override
    protected FlowGraph exploreLoopStatement(NodeWithBody stmt){
        return exploreLoopLikeStatements(stmt);
    }
    
    @Override
    protected FlowGraph exploreDoStatement(DoStmt stmt){
        return exploreLoopLikeStatements(stmt);
    }
    
    private FlowGraph exploreLoopLikeStatements(NodeWithBody stmt){
        FlowGraph graph = new FlowGraph(true);
        breakEndPoint = graph.end;
        graph = explore(stmt.getBody(),true).serial_merge(graph);
        breakEndPoint = null;
        return graph;
    }
}
