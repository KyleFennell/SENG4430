package used;

public interface IdMaker<T> {

	public int getID(T t);
	public int getMaxID();
	
}
