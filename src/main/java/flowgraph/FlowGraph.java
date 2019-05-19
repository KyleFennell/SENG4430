/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowgraph;

import com.github.javaparser.utils.Pair;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;

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
    
}
