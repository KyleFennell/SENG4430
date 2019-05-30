package graph;

public class CountingBubbleSort extends CountingSort {

	
	public CountingBubbleSort(ArrayComponent a){
		super(a);
	}
	
	public void sort(){
		for(int i=0; i<save.length; ++i)
			numbers[i]=save[i];
		
	for(int j=0;j<numbers.length;++j)	
		for(int i=save.length-1; i>j && getSwaps()<maxSwaps;--i)
			if(numbers[i]<numbers[i-1])
				swap(i,i-1);
		
		
		
	}
	
	public String getName(){
		String s= new String("BubbleSort");
		return s;
		
	}
	
	
	
}

