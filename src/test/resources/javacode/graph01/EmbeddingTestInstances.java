package graph;

import java.awt.geom.Point2D;

/**
 * Some static test instances for the embedded graph.
 * @author Jan-Philipp Kappmeier
 */
public class EmbeddingTestInstances {
	final static EmbeddingTestInstances SIMPLE;
	final static EmbeddingTestInstances SMALL;
	final static EmbeddingTestInstances DISJOINT_TRIANGLE;
	final static EmbeddingTestInstances STAR;
	final static EmbeddingTestInstances WHEEL;
	final static EmbeddingTestInstances COMPLETE;
	final static EmbeddingTestInstances COMPLETE_NO_CYCLES;
	final static EmbeddingTestInstances LINE;
	final static EmbeddingTestInstances BINARY_TREE;

	/**
	 * Returns an array containing all supported test instances.
	 * @return an array containing all supported test instances
	 */
	static EmbeddingTestInstances[] getAllInstances() {
		return new EmbeddingTestInstances[]{SIMPLE, SMALL, DISJOINT_TRIANGLE, STAR, WHEEL, COMPLETE, COMPLETE_NO_CYCLES, LINE, BINARY_TREE };
	}

	/** The graph for a test instance. */
	private final EmbeddedGraph g;
	/** A name for the instance.*/
	private final String name;

	/**
	 * Initializes a instance.
	 * @param g the graph
	 * @param expected the expected distances
	 * @param name the name
	 * @param startNode the start node
	 */
	private EmbeddingTestInstances( EmbeddedGraph g, String name ) {
		this.g = g;
		this.name = name;
	}

	/**
	 * Returns the graph.
	 * @return the graph
	 */
	public EmbeddedGraph getGraph() {
		return g;
	}

	/**
	 * Returns the size of the instance, which equals the size of the graph.
	 * @return the size of the instance
	 */
	public int size() {
		return g.nodeCount();
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
		Point2D.Double[] coordinates = new Point2D.Double[4];
		coordinates[0] = new Point2D.Double(-14,0);
		coordinates[1] = new Point2D.Double(0,10);
		coordinates[2] = new Point2D.Double(0,-10);
		coordinates[3] = new Point2D.Double(14,0);
		EmbeddedGraph g = new EmbeddedGraph( coordinates );
		g.addEdge( 0, 1 );
		g.addEdge( 0, 2 );
		g.addEdge( 1, 2 );
		g.addEdge( 1, 3 );
		g.addEdge( 2, 3 );
		SIMPLE = new EmbeddingTestInstances( g, "Simple" );

		coordinates = new Point2D.Double[1];
		coordinates[0] = new Point2D.Double();
		g = new EmbeddedGraph( coordinates );
		SMALL = new EmbeddingTestInstances( g, "Small" );

		coordinates = new Point2D.Double[6];
		coordinates[0] = new Point2D.Double(0,0);
		coordinates[1] = new Point2D.Double(100,0);
		coordinates[2] = new Point2D.Double(50,50);
		coordinates[3] = new Point2D.Double(150,0);
		coordinates[4] = new Point2D.Double(250,0);
		coordinates[5] = new Point2D.Double(200,-50);
		g = new EmbeddedGraph( coordinates );
		g.addEdge( 0, 1 );
		g.addEdge( 1, 2 );
		g.addEdge( 2, 0 );
		g.addEdge( 3, 4 );
		g.addEdge( 4, 5 );
		g.addEdge( 5, 3 );
		DISJOINT_TRIANGLE = new EmbeddingTestInstances( g, "Disjoint triangles" );

		coordinates = new Point2D.Double[101];
		for( int i = 1; i <= 100; ++i )
			coordinates[i] = new Point2D.Double( Math.sin( 3.6*i ), Math.cos( 3.6*i) );
		coordinates[0] = new Point2D.Double();
		g = new EmbeddedGraph( coordinates );
		for( int i = 1; i <= 100; ++i )
			g.addEdge( 0, i );
		STAR = new EmbeddingTestInstances( g, "Star" );

		coordinates = new Point2D.Double[41];
		for( int i = 1; i <= 40; ++i )
			coordinates[i] = new Point2D.Double( Math.sin( (360/40)*i ), Math.cos( (360/40)*i) );
		coordinates[0] = new Point2D.Double();
		g = new EmbeddedGraph( coordinates );
		for( int i = 1; i <= 40; ++i ) {
			g.addEdge( 0, i );
			g.addEdge( i, ((i + 1) % 40) + 1 );
		}
		WHEEL = new EmbeddingTestInstances( g, "Wheel" );

		coordinates = new Point2D.Double[16];
		int count = 0;
		for( int i = 0; i < 4; ++i )
			for( int j = 0; j < 4; ++j ) {
				coordinates[count++] = new Point2D.Double( 100*i, 100*j );
			}
		g = new EmbeddedGraph( coordinates );
		for( int i = 0; i < coordinates.length - 1; ++i )
			for( int j = i + 1; j < coordinates.length; ++j ) {
				g.addEdge( i, j );
				g.addEdge( j, i );
			}
		COMPLETE = new EmbeddingTestInstances( g, "Complete" );
		coordinates = new Point2D.Double[16];
		count = 0;
		for( int i = 0; i < 4; ++i )
			for( int j = 0; j < 4; ++j )
				coordinates[count++] = new Point2D.Double( 100*i, 100*j );
		g = new EmbeddedGraph( coordinates );
		for( int i = 0; i < coordinates.length - 1; ++i )
			for( int j = i + 1; j < coordinates.length; ++j )
				g.addEdge( i, j );
		COMPLETE_NO_CYCLES = new EmbeddingTestInstances( g, "Complete, no cycles" );

		coordinates = new Point2D.Double[22];
		for( int i = 0; i < coordinates.length; ++i )
			coordinates[i] = new Point2D.Double( 2*i, i );
		g = new EmbeddedGraph( coordinates );
		for( int i = 0; i < coordinates.length - 1; )
			g.addEdge( i, ++i );
		LINE = new EmbeddingTestInstances( g, "Line" );

		coordinates = new Point2D.Double[63];
		int i = 1;
		Queue<Tuple> q = new Queue<Tuple>();
		q.enqueue( new Tuple( 0, 0 ) );
		while( i < coordinates.length ) {
			Tuple current = q.dequeue();
			coordinates[i] = new Point2D.Double( current.x * 0.30, current.y );
			i++;
			q.enqueue( new Tuple( current.x * 2 + 1, current.y + 1 ) );
			coordinates[i] = new Point2D.Double( current.x* 0.30, current.y );			
			i++;
			q.enqueue( new Tuple( current.x * 2 + 2, current.y + 1 ) );
		}
		coordinates[0] = new Point2D.Double();
		g = new EmbeddedGraph( coordinates );
		i = 1;
		q = new Queue<Tuple>();
		q.enqueue( new Tuple( 0, 0 ) );
		while( i < coordinates.length ) {
			Tuple current = q.dequeue();
			g.addEdge( current.x, current.x * 2 + 1 );
			i++;
			q.enqueue( new Tuple( current.x * 2 + 1, current.y + 1 ) );
			g.addEdge( current.x, current.x * 2 + 2 );
			i++;
			q.enqueue( new Tuple( current.x * 2 + 2, current.y + 1 ) );
		}
		BINARY_TREE = new EmbeddingTestInstances( g, "Binary tree" );
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