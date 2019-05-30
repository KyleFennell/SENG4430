package Fields;

public class GraphNode{
  private final SimpleLinkedList<GraphEdge> edges= new SimpleLinkedList<GraphEdge>();
  private final int index;
  
  
  public GraphNode(){
    this.index=-1;
  }
  
  public GraphNode(int i){
    this.index= i;
  }
  
  public void addEdge(GraphEdge a){
    this.edges.add(a);
    return;
  }
  
  public SimpleLinkedList<GraphEdge> getEdges(){
    return this.edges;
  }
  
  public int id(){
    return this.index;
  }
  
}