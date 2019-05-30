package used;

import java.util.Comparator;

public interface Circle<T> {

	/**
	 * add t as the right Element of the current reference.
	 */
	public void add(T t);
	
	/**
	 * set the reference to the object if the circle contains it.
	 * @param t the object to find
	 * @return true if the object is in the circle
	 */
	public boolean find(T t);
	public T getValue();
	public T getRight();
	public T getLeft();
	public void turnLeft();
	public void turnRight();
	public boolean contains(T t);
	public int circleSize();
	
}
