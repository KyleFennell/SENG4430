package graph;

public class GraphEdge {
	 /**
	  * initialized a start and a end of the Graph
	  */
	  
	 private final GraphNode start;
	 private final GraphNode end;

	 /**
	  * constructor for a start and a end of the Graph
	  */
	  
	 public GraphEdge(GraphNode start, GraphNode end){
	     this.start = start;
	     this.end = end;
	 }
	 
	  /**
	  * @return the start node of the Graph
	  */
	 
	 public GraphNode getStart(){
	     return this.start;
	 }
	 
	  /**
	  * @return the last node of the Graph
	  */
	 
	 public GraphNode getEnd(){
	     return this.end;
	 }   
}
