/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowgraph;

import com.github.javaparser.utils.Pair;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Stream;

/**
 *
 * @author Nicolas Klenert
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
     * @param fileSrc path of file
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
            //TODO: change logging
            Logger.getLogger(FlowGraph.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public FlowGraph serial_merge(FlowGraph graph){
        end.merge(graph.start);
        end = graph.end;
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
    
    public int getEdgeCount(){
        HashSet<FlowGraphNode> visited = new HashSet();
        LinkedList<FlowGraphNode> queue = new LinkedList();
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
    
    public int getNodeCount(){
        HashSet<FlowGraphNode> visited = new HashSet();
        LinkedList<FlowGraphNode> queue = new LinkedList();
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
       
    public int getNumberOfPaths(){
        return getNumberOfPaths(1);
    }        
    
    public int getNumberOfPaths(int incrementer){
        HashMap<FlowGraphNode, Integer> label = new HashMap<>();
        HashSet<FlowGraphNode> visited = new HashSet<>();
        LinkedList<FlowGraphNode> order = new LinkedList<>(); //works as a stack
        LinkedList<FlowGraphNode> queue = new LinkedList<>();
        //The order is important because it lets us working on an cyclic graph
        HashMap<FlowGraphNode, FlowGraphNode> before = new HashMap<>();
        
        //create function for calculating if node is already in the path
        java.util.function.BiPredicate<FlowGraphNode,FlowGraphNode> inPath = (candidate,pathEnding) -> {
            if(candidate == end){
                return true;
            }
            FlowGraphNode node = pathEnding;
            while(node != end){
                if(candidate == node){
                    return true;
                }
                node = before.get(node);
            }
            return false;
        };
        
        //first find all loops, give the already visited nodes a temporary label and calculate the order
        queue.add(end);
        while(!queue.isEmpty()){
            FlowGraphNode node = queue.pop();
            order.push(node);
            for(FlowGraphNode child : node.from){
                if(inPath.test(child,node)){
                    //increment label of the child
                    label.put(child, label.getOrDefault(child, 0) + incrementer);
                }else{
                    queue.add(child);
                    before.put(child, node);
                }
            }
        }
        
        //second add all labels together
        label.put(start, 1);
        visited.add(start);
        while(!order.isEmpty()){
            FlowGraphNode node = order.pop();
            if(visited.add(node)){
               //add all its ancestors together
               int sum = 0;
               for(FlowGraphNode anc : node.from){
                   sum += label.get(anc);
               }
               label.put(node, sum);
            }
        }
        return label.get(end);
    }
    
}
