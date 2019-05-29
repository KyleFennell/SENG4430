package flowgraph;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.utils.Pair;
import java.util.HashMap;

/** Creates possible cyclic {@code FlowGraphs} out of some {@link Node} and it's children.
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
 * @see AcyclicFlowGraphBuilder
 * 
 */
public class CyclicFlowGraphBuilder extends AbstractFlowGraphBuilder{
    
    /** 
     * Value to save the node to which the continue Statements must point to.
     */
    private FlowGraph.FlowGraphNode continueStartPoint;
    
    /**
     * Map of the name of labels and their corresponding nodes.
     */
    protected HashMap<String,FlowGraph.FlowGraphNode> labeledContinueStartPoints;
    
    public CyclicFlowGraphBuilder(){
        super();
        labeledContinueStartPoints = new HashMap<>();
    }
    
    @Override
    protected FlowGraph exploreLabeledStatement(LabeledStmt stmt){
      FlowGraph graph = new FlowGraph(false);
      String key = stmt.getLabel().getIdentifier();
      labeledContinueStartPoints.put(key, graph.start);
      labeledBreakEndPoints.put(key, graph.end);
      graph = explore(stmt.getStatement(),true).merge(graph);
      //not necessary, but good for debugging
      labeledBreakEndPoints.remove(key);
      labeledContinueStartPoints.remove(key);
      return graph;
    }
    
    @Override
    protected FlowGraph exploreContinueStatement(ContinueStmt stmt){
        if(stmt.getLabel().isPresent()){
            return new FlowGraph(labeledContinueStartPoints.get(stmt.getLabel().get().getIdentifier()));
        }else{
            return new FlowGraph(continueStartPoint);
        }
    }
    
    @Override
    protected <T extends Node> FlowGraph exploreLoopStatement(NodeWithBody<T> stmt){
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
