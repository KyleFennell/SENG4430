package flowgraph;

import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.utils.Pair;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : CyclicFlowGraphBuilder
 * @author(s)       : Nicolas Klenert
 * Date             : 26/05/19
 * Purpose          : Creates possible cyclic FlowGraphs out of a parse tree.
 */
public class CyclicFlowGraphBuilder extends AbstractFlowGraphBuilder{
       
    private FlowGraph.FlowGraphNode continueStartPoint;
    
    @Override
    protected FlowGraph exploreContinueStatement(ContinueStmt stmt){
        return new FlowGraph(continueStartPoint);
    }
    
    @Override
    protected FlowGraph exploreLoopStatement(NodeWithBody stmt){
        Pair<FlowGraph,Pair<FlowGraph.FlowGraphNode,FlowGraph.FlowGraphNode>> triple = FlowGraph.createLoopFlowGraph(false);
        continueStartPoint = triple.b.a;
        breakEndPoint = triple.a.end;
        triple.b.b.merge(explore(stmt.getBody(),true));
        continueStartPoint = null;
        breakEndPoint = null;
        return triple.a;
    }
    
    @Override
    protected FlowGraph exploreDoStatement(DoStmt stmt){
        Pair<FlowGraph,Pair<FlowGraph.FlowGraphNode,FlowGraph.FlowGraphNode>> triple = FlowGraph.createLoopFlowGraph(true);
        continueStartPoint = triple.b.a;
        breakEndPoint = triple.a.end;
        triple.b.b.merge(explore(stmt.getBody(),true));
        continueStartPoint = null;
        breakEndPoint = null;
        return triple.a;
    }
}
