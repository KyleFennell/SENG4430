package used;

import java.util.ArrayList;
import java.util.Comparator;

public class ModList<T> implements Circle<T> {

	int ref;
	ArrayList<T> array;
	Comparator<T> comp;
	
	public ModList(Comparator<T> comp, int size){
		this.array = new ArrayList<T>(size);
		this.comp=comp;
		this.ref=-1;
	}
	
	
	@Override
	public void add(T t) {
		array.add(t);
		ref=array.size();
	}
	
	@Override
	public boolean find(T t) {
		for(int i =0; i<array.size(); i++){
			if(comp.compare(array.get(i),t)==0){
				ref=i;
				return true;
			}
		}
		return false;
	}
	
	@Override
	public T getValue() {
		return array.get(ref);
	}
	@Override
	public T getRight() {
		if(ref==array.size()-1){
			return array.get(0);
		}else{
		return array.get(ref+1);
		}
	}
	@Override
	public T getLeft() {
		if(ref==0){
			return array.get(array.size());
		}else{
			return array.get(ref-1);
		}
	}
	@Override
	public void turnLeft() {
		if(ref==0){
			ref = array.size();
		}else{
			ref--;
		}
	}
	@Override
	public void turnRight() {
		if(ref==array.size()-1){
			ref=0;
		}else{
		ref++;
		}
	}
	@Override
	public boolean contains(T t) {
		for(int i =0; i<array.size(); i++){
			if(comp.compare(array.get(i),t)==0){
				return true;
			}
		}
		return false;
	}
	
	@Override
	public int circleSize() {
		return array.size();
	}
	
	
	
}
