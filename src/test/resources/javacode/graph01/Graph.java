package graph;

public class Graph {
	 int n;  //number of nodes
	 GraphNode[] nodes;
	 
	 //size of the Graph by numbers of nodes
	 public Graph(int size){
	     this.n = size;
	     nodes = new GraphNode[size];
	     
	     for(int i=0; i<size; ++i){
	         nodes[i]=new GraphNode(i);
	     }
	 }
	 
	 /**
	  * connect two nodes with each other; generate a edge between them and return it
	  */
	 
	 public GraphEdge addEdge(int from, int to) throws IllegalArgumentException{
	     if(from<0||to<0)
	        throw new IllegalArgumentException("Knoten haben nur positive Indexe");
	    if(to>=n||from>=n)
	        throw new IllegalArgumentException("Es gibt keinen Knoten, mit einem so großen Index");
	    
	    GraphEdge edge = new GraphEdge(nodes[from], nodes[to]);
	   nodes[from].addEdge(edge);
	   //nodes[to].addEdge(egde);
	   
	   return edge;
	    
	 }
	 
	 /**
	  * @return index of a node
	  */
	 public GraphNode getNode(int index){
	     return nodes[index];
	 }
	 
	  /**
	  *@return return the quantity of the nodes
	  */
	 public int nodeCount(){
		   return this.n;
	 }  
}
