package graph;

public class GraphNode {
	/**
	  * initialized a edge and a index for each node
	  */
	    
	   private final SimpleLinkedList<GraphEdge> edges;
	   private final int index;
	   
	  /**
	  * constructor for a node with the index -1 because at this moment there is no distance to the start node
	  */
	   
	   public GraphNode(){
	       this.index=-1;
	       this.edges= new SimpleLinkedList <GraphEdge>();
	      
	   }
	   
	  /**
	  * assign each node an index
	  */
	   
	   public GraphNode(int i){
	       this.index= i;
	       edges = new SimpleLinkedList<GraphEdge>();
	   }
	   
	  /**
	  * add an edge to a node
	  */
	  
	   public void addEdge(GraphEdge a){
	    this.edges.add(a);
	    return;
	   }

	  /**
	  * return the edges of one node
	  */

	    public SimpleLinkedList<GraphEdge> getEdges(){
	        return this.edges;
	    } 
	    
	  /**
	  * return the index of one node
	  */
	    
	    public int id(){
	        return this.index;
	    }
	    
}
