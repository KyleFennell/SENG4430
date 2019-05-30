package flowgraph;

import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.expr.ConditionalExpr;
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

/** Defines most methods needed to construct a {@link FlowGraph} from a parse tree generated by {@link JavaParser}.
 * 
 *  <p>This abstract class does the heavy work. Because some {@code FlowGraphs} shouldn't
 *  be cyclic, the implementation to handle loop statements and such are delayed
 *  to child classes.</p>
 * 
 *  <p>This class contains almost only "explore" methods, which are used to
 *  search recursively through the parse tree. The method name and the parameter
 *  are giving away for which node the function is responsible for.<br>
 * 
 *  Because their use case are similar, we won't give a JavaDoc for these functions.</p>
 * 
 * <p>Project          : Software Quality Assignment 1<br>
 *    Date             : 14/05/19</p>
 * 
 *  <p>Extended by {@link CyclicFlowGraphBuilder} and {@link AcyclicFlowGraphBuilder}</p>
 * 
 * <p> Main entry point of this class is the function {@link explore}</p>
 * 
 * @author Nicolas Klenert
 * @see CyclicFlowGraphBuilder
 * @see AcyclicFlowGraphBuilder
 * 
 */
public abstract class AbstractFlowGraphBuilder {
    
    /** Variable to save the Node to which a break statement would point to.
     * 
     * breakEndPoint must be set in the beginning of every loop and switch statement
     * and deleted in the end of every loop and switch statement
     */
    protected FlowGraph.FlowGraphNode breakEndPoint;
    
    /** Map from the name of a label to their corresponding Node.
     * 
     * labeledBreakEndPoints must also be set whenever there is a labled segment
     * A labeledBreakEndPoint must also be deleted when the inner Statement is not longer explored
     */
    protected HashMap<String,FlowGraph.FlowGraphNode> labeledBreakEndPoints;
    
    /** Variable to save the Node to which a return statement would point to.
     * 
     * returnEndPoint has to be set for each Constructor and Method.
     */
    protected FlowGraph.FlowGraphNode returnEndPoint;
    
    AbstractFlowGraphBuilder(){
        labeledBreakEndPoints = new HashMap<>();
    }
    
    /** Describes behaviour of a GraphBuilder when seeing a {@code LoopStatement}.
     * 
     * <p>Some metrics are only usable if loops are handled in different ways.
     * Such the implementation of this function as well as {@link exploreDoStatement} 
     * and {@link exploreContinueStatement} are left for the children of this
     * class to implement.</p>
     * 
     * @param <T> class of the loop (While, ForEach or For)
     * @param stmt the loop statement to explore
     * @return {@code FlowGraph} representing the flow of code inside and the loop itself
     * 
     * @see exploreDoStatement
     */
    protected abstract <T extends Node> FlowGraph exploreLoopStatement(NodeWithBody<T> stmt);
    
    /** Describes behaviour of a GraphBuilder when seeing a do-while statement.
     * 
     * <p>Some metrics are only usable if loops are handled in different ways.
     * Such the implementation of this function as well as {@link exploreDoStatement} 
     * and {@link exploreContinueStatement} are left for the children of this
     * class to implement.</p>
     * 
     * @param stmt the loop statement to explore
     * @return {@code FlowGraph} representing the flow of code inside and the loop itself
     */
    protected abstract FlowGraph exploreDoStatement(DoStmt stmt);
    
    /** Describes behaviour of a GraphBuilder when seeing a continue statement.
     * 
     * <p>Some metrics are only usable if loops are handled in different ways.
     * Such the implementation of this function as well as {@link exploreDoStatement} 
     * and {@link exploreContinueStatement} are left for the children of this
     * class to implement.</p>
     * 
     * @param stmt the statement to explore
     * @return {@code FlowGraph} containing only one node which
     * is pointed to the {@code FlowGraphNode} representing the entry point
     * after the continue statement.
     */
    protected abstract FlowGraph exploreContinueStatement(ContinueStmt stmt);
    
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
        Pair<HashSet<Node>,Boolean> result = ifStatementResolver(stmt);           
        FlowGraph graph = new FlowGraph(!result.b);
        //insert all flowgraphs from the subtrees
        for(Node childNode : result.a){
            graph.parallel_append(explore(childNode,true));
        }
        return graph;
    }
    
    protected FlowGraph exploreConditionalExpression(ConditionalExpr expr){
        FlowGraph graph = new FlowGraph(false);
        graph.parallel_append(explore(expr.getThenExpr()));
        graph.parallel_append(explore(expr.getElseExpr()));
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
                 utils.Logger.error("Source Code is damaged, FlowGraph and such the metrics "
                        + "which are dependent can be wrong!");
                return null;
            }
            return new FlowGraph(breakEndPoint);
        }else{
            if(!labeledBreakEndPoints.containsKey(label)){
                utils.Logger.error("Source Code is damaged, FlowGraph and such the metrics "
                        + "which are dependent can be wrong!");
                return null;
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
       
    protected FlowGraph exploreSwitchStatement(SwitchStmt stmt){
        NodeList<SwitchEntry> entries = stmt.getEntries();
        if(entries.isEmpty()){
            utils.Logger.warning("Source code has a switch statement without entries");
            return null;
        }else if(entries.size() == 1 && entries.get(0).getLabels().isEmpty()){
            utils.Logger.warning("Source code has a switch statement with only default case");
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
    
    protected FlowGraph exploreMethodDeclaration(MethodDeclaration method){
        //look if the method decleration has a body
        Optional<BlockStmt> opt = method.getBody();
        if(!opt.isPresent()){
            return null;
        }
        FlowGraph graph = new FlowGraph(false);
        returnEndPoint = graph.end;
        FlowGraph child = explore(opt.get());
        if(!method.getType().isVoidType() && child.end.inDeg() > 0 && child.end.outDeg() == 0){
            utils.Logger.error("At least one return statement is missing");
        }
        returnEndPoint = null;
        return child.serial_merge(graph); 
    }
    
    protected FlowGraph exploreConstructorDeclaration(ConstructorDeclaration constructor){
        FlowGraph graph = new FlowGraph(false);
        returnEndPoint = graph.end;
        FlowGraph child = explore(constructor.getBody());
        returnEndPoint = null;
        return child.serial_merge(graph);
    }
    
    protected FlowGraph exploreReturnStatement(ReturnStmt stmt){
        if(returnEndPoint == null){
            utils.Logger.error("Source Code is damaged, FlowGraph and such the metrics "
                        + "which are dependent can be wrong!");
            return null;
        }
        if(stmt.getExpression().isPresent()){
           return explore(stmt.getExpression().get()).serial_merge(new FlowGraph(returnEndPoint));
        }
        return new FlowGraph(returnEndPoint);
    }
    
    /** Main entry point. Always returns a not-null {@code FlowGraph}.
     * 
     * Explores recursively {@code node} and it's children and creates a {@code FlowGraph} out of it.
     * 
     * @param node start point of the search algorithm
     * @return graph representing the flow of code with node as start point
     * 
     * @see FlowGraph
     */
    public FlowGraph explore(Node node){
        return explore(node,true);
    }
    
    /** Main entry point. Can return null.
     * 
     * Explores recursively {@code node} and it's children and creates a {@code FlowGraph} out of it.
     * 
     * @param node start point of the search algorithm
     * @param noNull if false, null as return value is allowed
     * @return graph representing the flow of code or null if there is no decision tree
     */
    public FlowGraph explore(Node node, boolean noNull) {
        FlowGraph graph;
        if(node == null){
            graph = null;
        }else if(node instanceof IfStmt){
            graph = exploreIfStatement((IfStmt)node);
        }else if(node instanceof ConditionalExpr){
            graph = exploreConditionalExpression((ConditionalExpr) node);
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
        }else if(node instanceof ConstructorDeclaration){
            graph = exploreConstructorDeclaration((ConstructorDeclaration) node);
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
                //the next graphs are all unreachable, so just return head
                return head;
            }
            head.serial_merge(graph);
        }
        return head;
    }
}
