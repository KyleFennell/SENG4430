package graph;

public abstract class CountingSort implements Sorter {
	int[] numbers;
	int[] save;
	int maxSwaps;
	int swaps;
	ArrayComponent a;
	
	
	public CountingSort(){
		swaps=0;
	}
	
	public CountingSort(ArrayComponent a){
		swaps=0;
		this.a=a;
	}
	
	public void setNumbers(int[] numbers){
		this.numbers=new int[numbers.length];
		for(int i=0;i<numbers.length;++i)
			this.numbers[i]=numbers[i];
		this.save=numbers;
		this.swaps=0;
	}
	
	public void setUpTo(int i){
		this.maxSwaps=i;
		this.swaps=0;
	}
	
	public int getSwaps(){
		return swaps;
	}
	
	public void swap(int i, int j){
		int buffer= numbers[i];
		numbers[i]= numbers[j];
		numbers[j]=buffer;
		++swaps;
		a.setArray(numbers);
		
	}
	
	public int[] getNumbers(){
		return this.numbers;
	}
	
	
}
