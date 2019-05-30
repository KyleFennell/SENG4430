package flowgraph;

import com.github.javaparser.utils.Pair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.stream.Stream;

/** Represents the flow of code. Used to calculate different metrics.
 * 
 *  <p>Almost all Methods in this class are changing the given parameter and the {@code FlowGraph} itself.<br>
 *  Therefore handle this class with caution.</p>
 * 
 * <p>Project          : Software Quality Assignment 1<br>
 *    Date             : 04/05/19</p>
 * 
 * @author Nicolas Klenert
 * @see AbstractFlowGraphBuilder
 * 
 */
public class FlowGraph {
    protected static class FlowGraphNode{
        Collection<FlowGraphNode> to;
        Collection<FlowGraphNode> from;
        
        protected FlowGraphNode(){
            to = new HashSet<>();
            from = new HashSet<>();
        }
        
        protected void addTo(FlowGraphNode target){
            to.add(target);
            target.from.add(this);
        }
        
        protected void deleteFrom(FlowGraphNode target){
            from.remove(target);
            target.to.remove(this);
        }
        
        /** Transfer all links from victim to the called node. 
         * 
         * The references of victim are NOT updated. Discard it after the operation!
         * 
         * @param victim 
         */
        protected void merge(FlowGraphNode victim){
            for(FlowGraphNode node : victim.to){
                this.addTo(node);
                node.from.remove(victim);
            }
            for(FlowGraphNode node : victim.from){
                node.addTo(this);
                node.to.remove(victim);
            }
        }
        
        /** Here the node itself is the victim!
         * 
         * All links to node are going to go to the start of the graph
         * and all links going outside of the node are going to be outside
         * of the end of the graph.
         * 
         * @param graph 
         */
        protected void merge(FlowGraph graph){
            for(FlowGraphNode node : this.from){
                node.addTo(graph.start);
                node.to.remove(this);
            }
            for(FlowGraphNode node : this.to){
                graph.end.addTo(node);
                node.from.remove(this);
            }
        }
        
        protected int inDeg(){
            return from.size();
        }
        
        protected int outDeg(){
            return to.size();
        }
    }
    
    protected FlowGraphNode start;
    protected FlowGraphNode end;
    
    public FlowGraph(){
        start = new FlowGraphNode();
        end = start;
    }
    
    public FlowGraph(boolean bool){
        start = new FlowGraphNode();
        end = new FlowGraphNode();
        if(bool){
            start.addTo(end);
        }
    }
    
    public static Pair<FlowGraph,Pair<FlowGraphNode,FlowGraphNode>> createLoopFlowGraph(boolean doLoop){
        FlowGraph graph = new FlowGraph(false);
        FlowGraphNode control = new FlowGraphNode();
        FlowGraphNode innerCode = new FlowGraphNode();
        graph.start.addTo(doLoop ? innerCode : control);
        control.addTo(graph.end);
        control.addTo(innerCode);
        innerCode.addTo(control);
        return new Pair<>(graph,new Pair<>(control,innerCode));
    }
    
    protected FlowGraph(FlowGraphNode node){
        start = new FlowGraphNode();
        end = start;
        end.addTo(node);
    }
    
    /** Creates a Flowgraph from a file. Numbers are used for the identification of the nodes.
     *  The start and endnode of the graph are given in the line starting with *!
     *  All other lines give the edges of the graph as "fromNode toNode".
     *  Lines starting with # are comments and such ignored.
     *
     * @param fileSrc path to file
     */
    public FlowGraph(String fileSrc){
        HashMap<Integer,FlowGraphNode> map = new HashMap<>();
        
        java.util.function.Function<String,FlowGraphNode> getter = (String string) -> {
            int number = Integer.parseInt(string);
            if(map.containsKey(number)){
                return map.get(number);
            }
            FlowGraphNode node = new FlowGraphNode();
            map.put(number,node);
            return node;
        };
        
        Path sourcePath = Paths.get(fileSrc);
        if(!Files.isRegularFile(sourcePath)){
            utils.Logger.error("File to create FlowGraph not found! Filepath: "+fileSrc);
        }
        try (Stream<String> stream = Files.lines(Paths.get(fileSrc))) {
            stream.forEach((String line) -> {
                if(line.startsWith("*")){
                    //startnode and endnode
                    String[] numbers = line.split(" ");
                    FlowGraphNode startNode = getter.apply(numbers[1]);
                    FlowGraphNode endNode = getter.apply(numbers[2]);
                    start = startNode;
                    end = endNode;
                }else if(!line.startsWith("#")){
                    //line is not a comment
                    String[] numbers = line.split(" ");
                    FlowGraphNode fromNode = getter.apply(numbers[0]);
                    FlowGraphNode toNode = getter.apply(numbers[1]);
                    fromNode.addTo(toNode);
                }
            });
        } catch (IOException ex) {
            utils.Logger.error("File to create FlowGraph not found! Filepath: "+fileSrc);
        }
    }
    
    /** Merges the end node of the graph to the start node of the other graph.
     * 
     * This process is NOT symmetric.
     * 
     * @param graph the graph which will be appended at the end
     * @return A FlowGraph which has exactly double the length.
     */
    public FlowGraph serial_merge(FlowGraph graph){
        end.merge(graph.start);
        end = graph.end;
        return this;
    }
    
    /** Merges start and end node of two graphs together.
     * 
     *  This process is symmetric.
     * 
     * @param graph the victim of the process. Will be changed by this process.
     * @return FlowGraph which contains all nodes except the start and end node of the victim graph.
     */
    public FlowGraph merge(FlowGraph graph){
        start.merge(graph.start);
        end.merge(graph.end);
        return this;
    }
    
    public FlowGraph serial_merge(Collection<FlowGraph> graphs){
        for(FlowGraph graph : graphs){
            this.serial_merge(graph);
        }
        return this;
    }
    
    public FlowGraph parallel_append(FlowGraph graph){
        if(graph.end.outDeg() != 0){
            //if graphs end is already has an outlet, do not connect it!
            return parallel_append_start(graph);
        }else if(graph.end.inDeg() == 0 && graph.start != graph.end){
            //graphs end is not reachable, so delete the node
            return parallel_append_start(graph);
        }
        start.addTo(graph.start);
        graph.end.addTo(end);
        return this;
    }
    
    private FlowGraph parallel_append_start(FlowGraph graph){
        start.addTo(graph.start);
        return this;
    }
        
    public FlowGraph parallel_append_detour(FlowGraph graph, FlowGraph detour){
        if(graph.end.outDeg() != 0){
            //if graphs end is already has an outlet, do not connect it!
            return parallel_append_start(graph);
        }else if(graph.end.inDeg() == 0 && graph.start != graph.end){
            //graphs end is not reachable, so delete the node
            return parallel_append_start(graph);
        }
        start.addTo(graph.start);
        graph.end.addTo(detour.start);
        return this;
    }
    
    @Deprecated
    protected FlowGraph insertIntoStartNode(FlowGraph graph){
        start.merge(graph.start);
        return this;
    }
    
    public FlowGraph parallel_append(Collection<FlowGraph> graphs){
        for(FlowGraph graph : graphs){
            this.parallel_append(graph);
        }
        return this;
    }
    
    /** Calculates the number of edges that are accessible from the start node.
     *
     *  The function calculates the number of edges dynamically,
     *  that is it's calculates the number of edges each time.
     *  If the FlowGraph is not changing and the number of edges are needed
     *  more than once, the user is responsible of saving the number and using it,
     *  instead of calculating the number of edges each time.
     * 
     * @return number of edges
     */
    public int getEdgeCount(){
        HashSet<FlowGraphNode> visited = new HashSet<>();
        LinkedList<FlowGraphNode> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        int counter = 0;
        while(!queue.isEmpty()){
            FlowGraphNode node = queue.pop();
            counter += node.outDeg();
            for(FlowGraphNode child : node.to){
                if(visited.add(child)){
                    queue.add(child);
                }
            }
        }
        return counter;
    }
    
    /** Calculates the number of nodes that are accessible from the start node.
     *
     *  The function calculates the number of nodes dynamically,
     *  that is it's calculates the number of nodes each time.
     *  If the FlowGraph is not changing and the number of nodes are needed
     *  more than once, the user is responsible of saving the number and using it,
     *  instead of calculating the number of nodes each time.
     * 
     * @return number of nodes
     */
    public int getNodeCount(){
        HashSet<FlowGraphNode> visited = new HashSet<>();
        LinkedList<FlowGraphNode> queue = new LinkedList<>();
        queue.add(start);
        visited.add(start);
        while(!queue.isEmpty()){
            FlowGraphNode node = queue.pop();
            for(FlowGraphNode child : node.to){
                if(visited.add(child)){
                    queue.add(child);
                }
            }
        }
        return visited.size();
    }
    
    public int getCyclomaticComplexity(){
        return getEdgeCount() - getNodeCount() +2;
    }
    
    /** Calculates the number of different path trough the graph.
     * 
     * @return number of different paths of the FlowGraph or -1 if there an infinite many (because it's cyclic)
     */
    public int getNumberOfPaths(){       
        HashMap<FlowGraphNode, Integer> number = new HashMap<>(); //all nodes above 0
        LinkedList<FlowGraphNode> queue = new LinkedList<>(); //all nodes with 0 and labeled
        HashMap<FlowGraphNode, Integer> label = new HashMap<>();
        
        label.put(start,1);
        queue.add(start);
        do{
            FlowGraphNode node = queue.pop();
            for(FlowGraphNode child : node.to){
                int count = number.getOrDefault(child, child.inDeg()) -1;
                if(count == 0){
                    queue.add(child);
                    number.remove(child);
                    //label
                    int sum = 0;
                    for(FlowGraphNode anc : child.from){
                        sum += label.get(anc);
                    }
                    label.put(child,sum);
                }else{
                    number.put(child, count);
                }
            }
        }while(!queue.isEmpty());
        
        if(!number.isEmpty()){
            //TODO: there has to be a loop in the FlowGraph! Error!
            return -1;
        }
        
        return label.get(end);
    }
    
}
