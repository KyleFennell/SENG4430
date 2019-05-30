package used;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;

public class SpecialHashSet<T> implements Set<T> {

	int size;
	int maxSize;
	ArrayList<T> array;
	IdMaker<T> make;
	
	
	public SpecialHashSet(IdMaker<T> make){
		this.maxSize = make.getMaxID();
		array= new ArrayList<T>(maxSize);
		this.make=make;
		
	}
	
	public int getMaxSize(){
		return maxSize;
	}
	
	@Override
	public int size() {
		return size;
	}

	@Override
	public boolean isEmpty() {
		return size==0;
	}

	@Override
	public boolean contains(Object o) {
		return array.get(make.getID((T)o))==null ? false : true;
	}

	@Override
	public Iterator<T> iterator() {
		return array.iterator();
	}

	@Override
	public Object[] toArray() {
		return array.toArray();
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return array.toArray(a);
	}

	@Override
	public boolean add(T e) {
		if(array.get(make.getID (e))==null){
			array.set(make.getID(e), e);
			++size;
			return true;
		}else{
			return false;
		}
	}

	@Override
	public boolean remove(Object o) {
		if(array.get(make.getID((T)o))==null){
			return false;
		}else{
			array.remove(make.getID((T)o));
			--size;
			return true;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) {
		for( Object temp : c){
			if(!this.contains(temp)){
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		for(T temp : c){
			if(!this.add(temp)){
				return false;
			}else{
				++size;
			}
		}
		return true;
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		boolean b = false;
		for(T temp : array){
			if(!c.contains(temp)){
				array.remove(make.getID(temp));
				--size;
				b = true;
			}
		}
		return b;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		boolean b = false;
		for(T temp : array){
			if(c.contains(temp)){
				array.remove(make.getID(temp));
				--size;
				b = true;
			}
		}
		return b;
	}

	@Override
	public void clear() {
		array.clear();		
	}

}
