package Fields;

public class Graph{
  
  int n;
  GraphNode[] nodes;
  
  public Graph(int size){
    this.n = size;
    nodes = new GraphNode[size];
    
    for(int i=0; i<size; ++i){
      nodes[i]=new GraphNode(i);
    }
  }
  
  public GraphEdge addEdge(int from, int to) throws IllegalArgumentException{
    if(from<0||to<0)
    throw new IllegalArgumentException("Knoten haben nur positive Indexe");
    if(to>=n||from>=n)
    throw new IllegalArgumentException("Es gibt keinen Knoten, mit einem so groﬂen Index");
    
    GraphEdge edge = new GraphEdge(nodes[from], nodes[to]);
    nodes[from].addEdge(edge);
    //nodes[to].addEdge(egde);
    
    return edge;
    
  }
  
  public GraphNode getNode(int index){
    return nodes[index];
  }
  
  public int nodeCount(){
    return this.n;
  }
  
}