package used;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

public class PrioritySetQueue<T> extends PriorityQueue<T>{
	
	/**
	 * don´t ask me what this is
	 */
	private static final long serialVersionUID = 1L;
	private Set<T> set;
	
	public PrioritySetQueue(int i, Comparator<T> comparator, Set<T> set) {
		super(i,comparator);
		this.set=set;
	}
	
	public PrioritySetQueue(int i, Comparator<T> comparator) {
		super(i,comparator);
		set = new HashSet<T>();
	}

	/** Important: add use the offer-Method
	 * @return false, if the super-method throws false or if the Element is already in the Queue 
	 * 
	 */
	public boolean offer(T t){
		if(set.add(t)){
			return super.offer(t);
		}else{
			return false;
		}
	}
	
	/**
	 * @return true, if all items in the collection can be added
	 */
	public boolean addAll(Collection<? extends T>  coll){
		boolean b = true;
		for(T t : coll){
			if(!this.add(t)){
				b=false;
			}
		}
		return b;
	}
	
	/**Important:
	 * remove does not re-allow instances to can be put in the queue again.
	 * 
	 * Therefore you have to use pollWith() or the removeWith() Method.
	 * 
	 */
	public T remove(){
		//set.remove(super.element());
		return super.remove();
	}
	
	public boolean remove(Object o){
		set.remove(o);
		return super.remove(o);
	}
	
	public T poll(){
		//set.remove(super.element());
		return super.poll();
	}
	
	public void clear(){
		set.clear();
		super.clear();
	}
	
	public boolean contains(Object o){
		return set.contains(o);
	}
	
	public void cheat(T t){
		super.add(t);
	}
	
	public boolean set(Collection<T> coll){
		return set.addAll(coll);
	}
	
	public boolean set(T t){
		return set.add(t);
	}
	
	public int sizeHash(){
		return set.size();
	}
	
	public void clear(Collection<T> coll){
		super.clear();
		set.retainAll(coll);
	}
	
}
