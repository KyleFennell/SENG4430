package used;

import java.util.Comparator;

/**
 * Best if you don´t know how many entry you have.
 * @author NICO
 *
 * @param <T>
 */
public class CircleList<T> implements Circle<T> {

	private CircleNode ref;
	private int circleSize;
	private Comparator<T> comp;
	
//	public CircleList(){
//		ref=null;
//		circleSize=0;
//	}
	
	public CircleList(Comparator<T> comp){
		ref=null;
		circleSize=0;
		this.comp=comp;
	}
		

public T getValue(){
	return ref.value;
}

public T getRight(){
	return ref.right.value;
}

public T getLeft(){
	return ref.left.value;
}
	
	private class CircleNode{
		CircleNode left;
		CircleNode right;
		T value;
		
		public CircleNode(T value){
			this.value=value;
			this.left=this;
			this.right=this;
		}
		
		public CircleNode(T value, CircleNode left, CircleNode right){
			this.value = value;
			this.left=left;
			this.right=right;
		}
	}

	@Override
	public void turnLeft() {
		ref=ref.left;
		
	}

	@Override
	public void turnRight() {
		ref=ref.right;
		
	}

	@Override
	public boolean contains(T t) {
		CircleNode temp = ref;
		boolean b = find(t);
		ref = temp;
		return b;
	}

	@Override
	public void add(T t) {
		
			if(ref==null){
				ref = new CircleNode(t);
			}else if(circleSize==1){
				CircleNode temp = new CircleNode(t,ref,ref);
				ref.left=temp;
				ref.right=temp;
				ref=temp;
			}else{
				CircleNode temp = new CircleNode(t,ref, ref.right);
				ref.right.left = temp;
				ref.right = temp;
			}
			++circleSize;
		
		
	}


	@Override
	public boolean find(T t) {
		
		boolean find = false;
	
		for(int i=0; i<circleSize; ++i){
			if(comp.compare(ref.value, t)==0){
				find=true;
				break;
			}
			ref=ref.right;
		}
		
		return find;		
	}


	@Override
	public int circleSize() {
		return circleSize;
	}
	
}
