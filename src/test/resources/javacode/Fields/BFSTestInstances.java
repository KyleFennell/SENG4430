package Fields;

import java.util.Random;

/**
 * Some static test instances for testing Breadth First Search.
 * @author Jan-Philipp Kappmeier
 */
public class BFSTestInstances {
  final static BFSTestInstances SIMPLE_1;
  final static BFSTestInstances SIMPLE_2;
  final static BFSTestInstances LAST_NODE;
  final static BFSTestInstances SMALL;
  final static BFSTestInstances DISJOINT_TRIANGLE;
  final static BFSTestInstances STAR;
  final static BFSTestInstances WHEEL;
  final static BFSTestInstances COMPLETE;
  final static BFSTestInstances COMPLETE_NO_CYCLES;
  final static BFSTestInstances LINE;
  final static BFSTestInstances BINARY_TREE;
  
  /**
  * Returns an array containing all supported test instances.
  * @return an array containing all supported test instances
  */
  static BFSTestInstances[] getAllInstances() {
    return new BFSTestInstances[]{SIMPLE_1, SIMPLE_2, LAST_NODE, SMALL, DISJOINT_TRIANGLE, STAR, WHEEL, COMPLETE, LINE, BINARY_TREE};
  }
  
  /** The graph for a test instance. */
  private final Graph g;
  /** The start node for a test instance. */
  private final int startNode;
  /** The expected distances. */
  private final int[] expected;
  /** A name for the instance.*/
  private final String name;
  
  /**
  * Initializes a instance.
  * @param g the graph
  * @param expected the expected distances
  * @param name the name
  * @param startNode the start node
  */
  private BFSTestInstances( Graph g, int[] expected, String name, int startNode ) {
    this.g = g;
    this.startNode = startNode;
    this.expected = expected;
    this.name = name;
  }
  
  /**
  * Returns the graph.
  * @return the graph
  */
  public Graph getGraph() {
    return g;
  }
  
  /**
  * Returns the index of the start node.
  * @return the index of the start node
  */
  public int startNode() {
    return startNode;
  }
  
  /**
  * Returns the size of the instance, which equals the size of the graph.
  * @return the size of the instance
  */
  public int size() {
    return g.nodeCount();
  }
  
  /**
  * Returns the distance from the start node to node with given index.
  * @param i the index of the node
  * @return the distnace from the start node to {@code i}
  */
  public int getDistance( int i ) {
    return expected[i];
  }
  
  /**
  * Returns the name of the instance.
  * @return the name of the instance
  */
  public String getName() {
    return name;
  }
  
  /**
  * Initializer for the static instances.
  */
  static {
    Graph g = new Graph( 4 );
    g.addEdge( 0, 1 );
    g.addEdge( 0, 2 );
    g.addEdge( 1, 2 );
    g.addEdge( 1, 3 );
    g.addEdge( 2, 3 );
    SIMPLE_1 = new BFSTestInstances( g, new int[]{0, 1, 1, 2}, "Simple 1", 0 );
    LAST_NODE = new BFSTestInstances( g, new int[]{Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE, 0}, "Last node", 3 );
    
    g = new Graph( 4 );
    g.addEdge( 0, 1 );
    g.addEdge( 0, 3 );
    g.addEdge( 0, 2 );
    g.addEdge( 1, 2 );
    g.addEdge( 1, 3 );
    g.addEdge( 2, 3 );
    SIMPLE_2 = new BFSTestInstances( g, new int[]{0, 1, 1, 1}, "Simple 2", 0 );
    
    g = new Graph( 1 );
    SMALL = new BFSTestInstances( g, new int[]{0}, "Small", 0 );
    
    g = new Graph( 6 );
    g.addEdge( 0, 1 );
    g.addEdge( 1, 2 );
    g.addEdge( 2, 0 );
    g.addEdge( 3, 4 );
    g.addEdge( 4, 5 );
    g.addEdge( 5, 3 );
    DISJOINT_TRIANGLE = new BFSTestInstances( g, new int[]{2, 0, 1, Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE}, "Disjoint triangles", 1 );
    
    g = new Graph( 101 );
    for( int i = 1; i <= 100; ++i )
    g.addEdge( 0, i );
    int[] expected = new int[101];
    for( int i = 1; i <= 100; ++i )
    expected[i] = 1;
    expected[0] = 0;
    STAR = new BFSTestInstances( g, expected, "Star", 0 );
    
    g = new Graph( 101 );
    for( int i = 1; i <= 100; ++i ) {
      g.addEdge( 0, i );
      g.addEdge( i, ((i + 1) % 100) + 1 );
    }
    expected = new int[101];
    for( int i = 1; i <= 100; ++i )
    expected[i] = 1;
    expected[0] = 0;
    WHEEL = new BFSTestInstances( g, expected, "Star", 0 );
    
    expected = new int[32];
    Random r = new Random();
    int rnd = r.nextInt( expected.length );
    g = new Graph( expected.length );
    for( int i = 0; i < expected.length - 1; ++i )
    for( int j = i + 1; j < expected.length; ++j ) {
      g.addEdge( i, j );
      g.addEdge( j, i );
    }
    for( int i = 0; i < expected.length; ++i )
    expected[i] = 1;
    expected[rnd] = 0;
    COMPLETE = new BFSTestInstances( g, expected, "Complete", rnd );
    
    expected = new int[16];
    rnd = r.nextInt( expected.length );
    g = new Graph( expected.length );
    for( int i = 0; i < expected.length - 1; ++i )
    for( int j = i + 1; j < expected.length; ++j )
    g.addEdge( i, j );
    for( int i = 0; i < rnd; ++i )
    expected[i] = Integer.MAX_VALUE;
    expected[rnd] = 0;
    for( int i = rnd + 1; i < expected.length; ++i )
    expected[i] = 1;
    COMPLETE_NO_CYCLES = new BFSTestInstances( g, expected, "Complete, no cycles", rnd );
    
    expected = new int[42];
    g = new Graph( expected.length );
    for( int i = 0; i < expected.length - 1; )
    g.addEdge( i, ++i );
    for( int i = 0; i < expected.length; ++i )
    expected[i] = i;
    LINE = new BFSTestInstances( g, expected, "Line", 0 );
    
    expected = new int[127];
    int i = 1;
    Queue<Tuple> q = new Queue<Tuple>();
    q.enqueue( new Tuple( 0, 0 ) );
    g = new Graph( expected.length );
    while( i < expected.length ) {
      Tuple current = q.dequeue();
      g.addEdge( current.x, current.x * 2 + 1 );
      expected[i++] = current.y + 1;
      q.enqueue( new Tuple( current.x * 2 + 1, current.y + 1 ) );
      g.addEdge( current.x, current.x * 2 + 2 );
      expected[i++] = current.y + 1;
      q.enqueue( new Tuple( current.x * 2 + 2, current.y + 1 ) );
    }
    BINARY_TREE = new BFSTestInstances( g, expected, "Binary tree", 0 );
  }
  
  /**
  * Simple inner class storing two values.
  */
  private static class Tuple {
    private int x;
    private int y;
    
    public Tuple( int x, int y ) {
      this.x = x;
      this.y = y;
    }
  }
}
