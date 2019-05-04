/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package flowgraph;

import java.util.Collection;
import java.util.HashSet;

/**
 *
 * @author Nicolas Klenert
 */
public class FlowGraph {
    protected class FlowGraphNode{
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
        
        /** The references of victim are NOT updated. Discard it after the operation!
         * 
         * @param victim 
         */
        protected void merge(FlowGraphNode victim){
            for(FlowGraphNode node : victim.to){
                addTo(node);
                node.from.remove(victim);
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
    
    private int edgeCount;
    private int nodeCount;
    
    public FlowGraph(){
        start = new FlowGraphNode();
        end = start;
        nodeCount = 1;
        edgeCount = 0;
    }
    
    public FlowGraph(boolean bool){
        this.nodeCount = 0;
        start = new FlowGraphNode();
        end = new FlowGraphNode();
        nodeCount = 2;
        edgeCount = 0;
        if(bool){
            start.addTo(end);
            edgeCount = 1;
        }
    }
    
    protected FlowGraph(FlowGraphNode node){
        start = new FlowGraphNode();
        end = start;
        end.addTo(node);
        nodeCount = 1;
        edgeCount = 1;
    } 
    
    public FlowGraph serial_merge(FlowGraph graph){
        end.merge(graph.start);
        end = graph.end;
        edgeCount += graph.edgeCount;
        nodeCount += graph.nodeCount - 1;
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
            graph.nodeCount -= 1;
            return parallel_append_start(graph);
        }
        start.addTo(graph.start);
        graph.end.addTo(end);
        edgeCount += graph.edgeCount + 2;
        nodeCount += graph.nodeCount;
        return this;
    }
    
    private FlowGraph parallel_append_start(FlowGraph graph){
        start.addTo(graph.start);
        edgeCount += graph.edgeCount + 1;
        nodeCount += graph.nodeCount;
        return this;
    }
        
    public FlowGraph parallel_append_detour(FlowGraph graph, FlowGraph detour){
        //if graphs end is not reachable or it already has an outlet, do not connect it!
        if(graph.end.inDeg() == 0 || graph.end.outDeg() != 0){
            return parallel_append_start(graph);
        }
        start.addTo(graph.start);
        graph.end.addTo(detour.start);
        edgeCount += graph.edgeCount + 2;
        nodeCount += graph.nodeCount;
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
        return edgeCount;
    }
    
    public int getNodeCount(){
        return nodeCount;
    }
    
}
