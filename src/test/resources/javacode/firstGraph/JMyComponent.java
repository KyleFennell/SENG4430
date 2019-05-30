package firstGraph;

import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.geom.Point2D;
import java.awt.Graphics;
import java.awt.Graphics2D;
import javax.swing.JComponent;

/**
 * A simple component that draws a line, a centered rectangle and has the
 * ability to store a set of points that are also drawn.
 * @author Jan-Philipp Kappmeier
 */
public class JMyComponent extends JComponent {
	/** List that stores points to be drawn. */
	private SimpleLinkedList<Point2D.Double> points = new SimpleLinkedList<Point2D.Double>();

	/**
	 * Draws the component.
	 * @param g the graphics context
	 */
	public void paintComponent( Graphics g ) {
		super.paintComponent( g );

		// Cast graphics component to correct type
		Graphics2D g2d = (Graphics2D)g;

		// Clear background
		g2d.setColor( Color.white );
		g2d.fillRect( 0, 0, getWidth(), getHeight() );

		// Set line width for drawing to 3 pixel
		g2d.setStroke( new BasicStroke( 3 ) );

		// Draw a diagonal line from upper left to lower right corner
		g2d.setColor( Color.black );
		g2d.drawLine( 0, 0, getWidth(), getHeight());

		// Set drawing color to red
		g2d.setColor( Color.red );

		// Define a rectangle by two points
		Point2D.Double p1 = new Point2D.Double( 200, 200 );
		Point2D.Double p2 = new Point2D.Double( 230, 260 );
		double rectWidth = p2.x - p1.x;
		double rectHeight = p2.y - p1.y;

		// Transform the points to be centered and scaled
		Point2D.Double p1_t = transform( p1, rectWidth, rectHeight, p1 );
		Point2D.Double p2_t = transform( p1, rectWidth, rectHeight, p2 );

		// Draw the transformed rectangle
		g2d.drawRect( (int)p1_t.x, (int)p1_t.y, (int)(p2_t.x-p1_t.x), (int)(p2_t.y - p1_t.y) );

		// Draw all points in the list
		g2d.setColor( Color.blue );
		for( points.reset(); points.isValid(); points.advance() ) {
			g2d.fillOval( (int)points.getCurrent().getX()-5, (int)points.getCurrent().getY()-5, 10, 10);
		}
	}

	/**
	 * Transforms a point. It is supposed that {@code corner} is the upper right
	 * coordinate of a rectangle with a given width and height. The transformation
	 * applicated to the submitted point {@code toTransform} scales and moves
	 * all points in the interior and on the border of the rectangle such that the
	 * rectangle fills a maximal area of the component. The rectangle will be
	 * centered.
	 * @param corner the corner of the rectangle
	 * @param rectWidth the width of the rectangle
	 * @param rectHeight the height of the rectangle
	 * @param toTransform the point that is actually transformed. For correct scaling, it should lie inside the rectangle.
	 * @return
	 */
	public Point2D.Double transform( Point2D.Double corner, double rectWidth, double rectHeight, Point2D.Double toTransform ) {
		// Set available size to total size of component.
		double availableHeight = getHeight();
		double availableWidth = getWidth();

		// Calculate the scale factor to be the minimum of vertical and horizontal scale factors
		double scaleHeight = availableHeight/rectHeight;
		double scaleWidth = availableWidth/rectWidth;
		double scale = Math.min(scaleHeight, scaleWidth);

		// Calculate the offset to recenter the rectangle
		double xOffset = -corner.x * scale + (availableWidth - rectWidth*scale)*0.5;
		double yOffset = -corner.y * scale + (availableHeight - rectHeight*scale)*0.5;

		// Return the point
		return new Point2D.Double( toTransform.x * scale + xOffset, toTransform.y * scale + yOffset );
	}

	/**
	 * Adds a point to the list of points that is drawn.
	 * @param p a point
	 */
	public void addPoint( Point2D.Double p ) {
		points.add( p );
	}

	/**
	 * Clears the list of points that is drawn.
	 */
	void clear() {
		points.reset();
		while( points.isValid() )
			points.remove();
	}
}
