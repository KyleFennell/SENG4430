package Fields;

/**
 *
 */
public class BFS {
  /** The flag value for unvisited nodes. */
  private final int WHITE = 0;
  /** The flag value for nodes that are active. */
  private final int GREY = 1;
  /** The flag value for nodes that are finished. */
  private final int BLACK = 2;
  
  /** The graph. */
  private final Graph graph;
  /** The start node. */
  private GraphNode startNode;
  /** The node distances from the start node. */
  private final int[] dist;
  /** The predecessors on the shortest path. */
  private final GraphNode[] pred;
  /** An array storing the states 'WHITE', 'GREY', 'BLACK' during computation. */
  private final int[] color;
  
  /**
  * Initializes a Breadth First Search with a given {@link Graph} and the
  * index of a node from which the search is started.
  * @param graph the graph
  * @param startNodeIndex the index
  * @throws IllegalArgumentException if the index is negative or too large.
  */
  public BFS( Graph graph, int startNodeIndex ) {
    this.graph = graph;
    if( startNodeIndex < 0 || startNodeIndex >= graph.nodeCount() )
    throw new IllegalArgumentException( "Start node was out of allowed range. Was: " + startNodeIndex );
    this.startNode = graph.getNode( startNodeIndex );
    dist = new int[graph.nodeCount()];
    pred = new GraphNode[graph.nodeCount()];
    color  = new int[graph.nodeCount()];
    for( int i = 0; i < graph.nodeCount(); ++i )
    dist[i] = Integer.MAX_VALUE;
  }
  
  /**
  * Performs the Breadth First Search starting from the start node defined in
  * the constructor.
  */
  public void run() {
    //int startIndex=startNode.id();
    //color[startIndex]=GREY;
    //dist[startIndex]=0;
    
    dist[startNode.id()]=0;
    Queue<GraphNode> queue= new Queue<GraphNode>();
    queue.enqueue(startNode);
    GraphNode current;
    SimpleLinkedList<GraphEdge> edgeNode;
    
    while(!queue.isEmpty()){
      current=queue.dequeue();
      edgeNode=current.getEdges();
      for(edgeNode.reset();edgeNode.isValid();edgeNode.advance()){
        if(color[edgeNode.getCurrent().getEnd().id()]==WHITE){
          queue.enqueue(edgeNode.getCurrent().getEnd());
          color[edgeNode.getCurrent().getEnd().id()]=GREY;
          pred[edgeNode.getCurrent().getEnd().id()]=current;
          dist[edgeNode.getCurrent().getEnd().id()]=dist[current.id()]+1;
        }
      }
      color[current.id()]=BLACK;
    }
    
    
    
    
    
  }
  
  /**
  * <p>The main method calls the Breadth First Search algorithm for several
  * instances defined in {@link BFSTestInstances}. It gives out expected
  * distances and actual computation results.</p>
  * @param args
  */
  public static void main( String... args ) {
    // Run a specific test instance.
    // Uncomment for execution.
    //BFS b = new BFS( BFSTestInstances.SIMPLE_1.getGraph(), BFSTestInstances.SIMPLE_1.startNode() );
    //b.run();
    //checkResult( b.dist, BFSTestInstances.SIMPLE_1 );
    
    // Choose one of the following available instances (instead of SIMPLE_1):
    //SIMPLE_1: a simple instance with 4 nodes and 5 edges.
    // SIMPLE_2: another simple instance with 4 nodes.
    // LAST_NODE: the same instance, but start node is the last one (only reachable)
    // SMALL: only a single node
    // DISJOINT_TRIANGLE: two triangles, only one is reachable
    // STAR: a star with one center node
    // WHEEL: a wheel with one center node. like a star but with additional edges.
    // COMPLETE: a complete graph containing cycles. everything is reachable in distance 1.
    // COMPLETE_NO_CYCLES: a complete graph without cycles. depending on the start node, only some nodes are reachable in distance 1.
    // LINE: a line.
    // BINARY_TREE: a tree.
    
    
    // Comment the following if you do not want to run all tests!
    for( BFSTestInstances instance : BFSTestInstances.getAllInstances() ) {
      BFS b = new BFS( instance.getGraph(), instance.startNode() );
      b.run();
      if( checkResult( b.dist, instance ) )
      System.out.println( instance.getName() + " OK\n" );
      else
      System.out.println( "Instance " + instance.getName() + " was not ok!\n" );
    }
  }
  
  /**
  * Checks whether a given set of distances computed is correct. Gives out the
  * expected and the actual results as strings.
  * @param resultDist the results of the BFS implementation in this class.
  * @param instance  the test instance
  * @return
  */
  public static boolean checkResult( int[] resultDist, BFSTestInstances instance ) {
    if( resultDist == null )
    throw new IllegalArgumentException( "Result set must not be null!" );
    if( resultDist.length == 0 )
    throw new IllegalArgumentException( "Result set must at least contain one node." );
    if( resultDist.length != instance.size() ) {
      System.out.println( "Sizes of given results and expected for instance are not the same! Given size: " + resultDist.length + " Expected size: " + instance.size() );
      return false;
    }
    StringBuilder given = new StringBuilder();
    StringBuilder expected = new StringBuilder();
    boolean correct = true;
    for( int i = 0; i < resultDist.length - 1; ++i ) {
      given.append( resultDist[i] ).append( ", ");
      expected.append( instance.getDistance( i ) ).append( ", ");
      if( resultDist[i] != instance.getDistance( i ) )
      correct = false;
    }
    given.append( resultDist[resultDist.length-1] );
    expected.append( instance.getDistance( resultDist.length - 1 ) );
    
    System.out.println( "Given:    " + given.toString() );
    System.out.println( "Expected: " + expected.toString() );
    return correct;
  }
}
