package flowgraph;

import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.utils.Pair;
import java.util.HashMap;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : CyclicFlowGraphBuilder
 * @author(s)       : Nicolas Klenert
 * Date             : 26/05/19
 * Purpose          : Creates possible cyclic FlowGraphs out of a parse tree.
 */
public class CyclicFlowGraphBuilder extends AbstractFlowGraphBuilder{
       
    private FlowGraph.FlowGraphNode continueStartPoint;
    protected HashMap<String,FlowGraph.FlowGraphNode> labeledContinueStartPoints;
    
    @Override
    protected FlowGraph exploreLabeledStatement(LabeledStmt stmt){
      //TODO: think about it. It the change acceptable? Or just test it!
      FlowGraph graph = new FlowGraph();
      String key = stmt.getLabel().getIdentifier();
      labeledContinueStartPoints.put(key, graph.start);
      labeledBreakEndPoints.put(key, graph.end);
      graph = explore(stmt.getStatement(),true).serial_merge(graph);
      //not necessary, but good for debugging
      labeledBreakEndPoints.remove(key);
      labeledContinueStartPoints.remove(key);
      return graph;
    }
    
    @Override
    protected FlowGraph exploreContinueStatement(ContinueStmt stmt){
        //TODO: Continue statements CAN HAVE a label
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
