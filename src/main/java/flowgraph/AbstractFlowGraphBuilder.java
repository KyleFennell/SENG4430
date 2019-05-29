package flowgraph;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithBody;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.BreakStmt;
import com.github.javaparser.ast.stmt.ContinueStmt;
import com.github.javaparser.ast.stmt.DoStmt;
import com.github.javaparser.ast.stmt.ForEachStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.LabeledStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.Statement;
import com.github.javaparser.ast.stmt.SwitchEntry;
import com.github.javaparser.ast.stmt.SwitchStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.utils.Pair;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.ListIterator;
import java.util.Optional;

/**
 * Project          : Software Quality Assignment 1
 * Class name       : AbstractFlowGraphBuilder
 * @author(s)       : Nicolas Klenert
 * Date             : 26/05/19
 * Purpose          : Abstract class, which has the implementations
 *                    AcyclicFlowGraphBuilder and CyclicFlowGraphBuilder
 */
public abstract class AbstractFlowGraphBuilder {
    
    /**
     * breakEndPoint must be set in the beginning of every loop and switch
     * and deleted in the end of every loop and switch
     */
    protected FlowGraph.FlowGraphNode breakEndPoint;
    
    /**
     * labeledBreakEndPoints must also be set whenever there is a labled segment
     * A labeledBreakEndPoint must also be deleted when the inner Statement is not longer explored
     * 
     */
    protected HashMap<String,FlowGraph.FlowGraphNode> labeledBreakEndPoints;
    protected FlowGraph.FlowGraphNode returnEndPoint;
    
    /** Resolves an if-else statements so it can have multiple then statements and one optional else statement.
     * 
     * @param stmt if statement which should be resolved
     * @return a set of then statements and maximal one else statement.
     * The boolean indicates, if there is an else statement inside the set
     */
    protected Pair<HashSet<Node>,Boolean> ifStatementResolver(IfStmt stmt){
        HashSet<Node> set;
        Optional<Statement> optionalElseStmt = stmt.getElseStmt();
        boolean elseExist = optionalElseStmt.isPresent();
        // test if there is an else stmt
        if(elseExist){
            Statement elseStmt = optionalElseStmt.get();
            //test if it is an if stmt (that is, if-else)
            if(elseStmt instanceof IfStmt){
                //call the function again and use his results
                //(the return could be here, just add the then-statement to the set) -> better performance
                Pair<HashSet<Node>,Boolean> childResult = ifStatementResolver((IfStmt)elseStmt);
                set = childResult.a;
                elseExist = childResult.b;
            }else{
                //ther statement is a "real" else clause
                set = new HashSet<>();
                set.add(elseStmt);
            }
        }else{
            set = new HashSet<>();
        }
        //if not, create a new set, put the thenStmt and optional elseStmt inside of it and be happy
        set.add(stmt.getThenStmt());
        return new Pair<>(set, elseExist);
    }
    
    protected FlowGraph exploreIfStatement(IfStmt stmt){
        //look at else-clause, there could be nested if-statements
        //bool = true if there is not a final else-clause
        Pair<HashSet<Node>,Boolean> result = ifStatementResolver(stmt);           
        FlowGraph graph = new FlowGraph(!result.b);
        //insert all flowgraphs from the subtrees
        for(Node childNode : result.a){
            FlowGraph childGraph = explore(childNode,true);
            //if(childGraph.end.outDeg() == 0){
            graph.parallel_append(childGraph);
            //}else{
            //    graph.parallel_append_start(childGraph);   
            //}
        }
        //TODO: the endpoint of graph can be inaccessable! Test (method(){if(){return;}else{return;}})
        //parallel_append looks if it is unreachable. If it is, it won't be connected
        //and the node gets lost through the garbage collector!
        return graph;
    }
    
    protected FlowGraph exploreBreakStatement(BreakStmt stmt){
        String label = null;
        Optional<Expression> opt = stmt.getValue();
        if(opt.isPresent() && opt.get().isNameExpr()){
            NameExpr name = (NameExpr) opt.get();
            label = name.getName().getIdentifier();
        }
        
        if(label == null){
            if(breakEndPoint == null){
                //TODO: throw Exception? Or what? The source code has errors
            }
            return new FlowGraph(breakEndPoint);
        }else{
            if(!labeledBreakEndPoints.containsKey(label)){
                //TODO: throw Exception? Or what? The source code has errors
            }
            return new FlowGraph(labeledBreakEndPoints.get(label));
        }
    }
    
    protected FlowGraph exploreLabeledStatement(LabeledStmt stmt){
      FlowGraph graph = new FlowGraph();
      String key = stmt.getLabel().getIdentifier();
      labeledBreakEndPoints.put(key, graph.end);
      graph = explore(stmt.getStatement(),true).serial_merge(graph);
      labeledBreakEndPoints.remove(key); //not necessary, but good for debugging
      return graph;
    }
    
    protected abstract FlowGraph exploreContinueStatement(ContinueStmt stmt);
       
    protected FlowGraph exploreSwitchStatement(SwitchStmt stmt){
        NodeList<SwitchEntry> entries = stmt.getEntries();
        if(entries.isEmpty()){
            //TODO: The source code is stupid (switch without any cases)
            return null;
        }else if(entries.size() == 1 && entries.get(0).getLabels().isEmpty()){
            //TODO: The source code is stupid (switch with only default case)
            
            //first thing to do is to set the breakEndPoint
            FlowGraph graph = new FlowGraph(true);
            breakEndPoint = graph.end;
            graph = explore(entries.get(0),true).serial_merge(graph);
            breakEndPoint = null; //not necessary, but good for debugging
            return graph;
        }
        //we have a fairly normal switch statement
        boolean defaultExist = entries.get(entries.size()-1).getLabels().isEmpty();
        FlowGraph graph = new FlowGraph(!defaultExist);
        //TODO: look at labels and figure out if a default case is even necessary
        breakEndPoint = graph.end;
        //set up last flowgraph and connect it normally
        FlowGraph last = explore(entries.get(entries.size()-1).getStatements(),true);
        graph.parallel_append(last);
        ListIterator<SwitchEntry> listIter = entries.listIterator(entries.size()-1);
        while(listIter.hasPrevious()){
            SwitchEntry entry = (SwitchEntry) listIter.previous();
            if(!entry.isEmpty()){
                //ignore it, if it's empty (more than one case for the same snippet)
                FlowGraph temp = explore(entry.getStatements(),true);
                graph.parallel_append_detour(temp, last);
                last = temp;
            }
        }
        breakEndPoint = null; //not necessary, but good for debugging
        return graph;
    }
    
    protected abstract <T extends Node> FlowGraph exploreLoopStatement(NodeWithBody<T> stmt);
    protected abstract FlowGraph exploreDoStatement(DoStmt stmt);
    
    protected FlowGraph exploreMethodDeclaration(MethodDeclaration method){
        //look if the method decleration has a body
        Optional<BlockStmt> opt = method.getBody();
        if(!opt.isPresent()){
            return null;
        }
        returnEndPoint = new FlowGraph.FlowGraphNode();
        FlowGraph graph = explore(opt.get());
        if(graph.end.inDeg() > 0 && graph.end.outDeg() == 0){
            //not all possibilities are returning (at least one return statement is missing)
        }
        graph.end = returnEndPoint;
        returnEndPoint = null;
        return graph; 
    }
    
    protected FlowGraph exploreReturnStatement(ReturnStmt stmt){
        return new FlowGraph(returnEndPoint);
    }
    
    public FlowGraph explore(Node node){
        return explore(node,true);
    }
    
    public FlowGraph explore(Node node, boolean noNull) {
        FlowGraph graph;
        if(node == null){
            graph = null;
        }else if(node instanceof IfStmt){
            graph = exploreIfStatement((IfStmt)node);
        }else if(node instanceof BreakStmt){
            graph = exploreBreakStatement((BreakStmt)node);
        }else if(node instanceof SwitchStmt){
            graph = exploreSwitchStatement((SwitchStmt)node);
        }else if(node instanceof LabeledStmt){
            graph = exploreLabeledStatement((LabeledStmt)node);
        }else if(node instanceof ContinueStmt){
            graph = exploreContinueStatement((ContinueStmt)node);
        }else if(node instanceof DoStmt){
            graph = exploreDoStatement((DoStmt) node);
        }else if(node instanceof WhileStmt){
            //All WhileStmts implement this specific interface
            @SuppressWarnings("unchecked")
            NodeWithBody<WhileStmt> cast = (NodeWithBody<WhileStmt>) node;
            graph = exploreLoopStatement(cast);
        }else if(node instanceof ForStmt){
            //All ForStmts implement this specific interface
            @SuppressWarnings("unchecked")
            NodeWithBody<ForStmt> cast = (NodeWithBody<ForStmt>) node;
            graph = exploreLoopStatement(cast);
        }else if(node instanceof ForEachStmt){
            //All ForEachStmts implement this specific interface
            @SuppressWarnings("unchecked")
            NodeWithBody<ForEachStmt> cast = (NodeWithBody<ForEachStmt>) node;
            graph = exploreLoopStatement(cast);
        }else if(node instanceof MethodDeclaration){//methods (yet to included: lambda methods, MethodReferenceExpr and so on)
            graph = exploreMethodDeclaration((MethodDeclaration) node);
        }else if(node instanceof ReturnStmt){
            graph = exploreReturnStatement((ReturnStmt) node);
        }else{
            graph = explore(node.getChildNodes(), false);   
        }
        
        if(graph == null && noNull){
            return new FlowGraph();
        }
        return graph;
    }
        
    private FlowGraph explore(Collection<? extends Node> col, boolean noNull){
       LinkedList<FlowGraph> list = new LinkedList<>();
        if (col != null && !col.isEmpty()) {
            for (Node child : col) {
                FlowGraph childGraph = explore(child, false);
                if(childGraph != null){
                    list.add(childGraph);
                }
            }
        }
        if(list.isEmpty()){
           return noNull? new FlowGraph() : null;
        }
        FlowGraph head = list.poll();
        for(FlowGraph graph : list){
            if(head.end.inDeg() == 0){
                //TODO: the next graphs are all unreachable!
            }
            head.serial_merge(graph);
        }
        return head;
    }
}
