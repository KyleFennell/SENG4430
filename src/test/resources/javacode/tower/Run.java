package tower;

import java.util.LinkedList;
import java.util.Arrays;

public class Run {
	
	
	
	public static void main(String... args){
		int[] test = {3,3,3,5,6,2,1,8,0,3,2,9,5,8,3,8,0,2,1,4,6};
		System.out.println(Arrays.toString(sortArray(test)));
		
		
	}
	
	public static LinkedList<Integer> runFinder(int[] array){
	int index = 0;
	int length = 0;
	int testlength = 1;
	
	LinkedList<Integer> list = new LinkedList<Integer>();
	
	if(array==null)
		return list;
	if(array.length==0)
		return list;
	
	for(int i=0; i < array.length-1; ++i){
		if(array[i+1]>=array[i])
			++testlength;
		else
			if(testlength>length){
				length=testlength;
				index=i-length+1;
				testlength=1;
			}
			else
				testlength=1;
	}
	
	for(int i=0;i<length;++i)
		list.add(array[index+i]);
	
	return list;
	
	}
	

	public static int[] sortArray(int[] a){
		int[] counter = new int[10];
		int index=0;
		for(int i=0; i<counter.length; ++i)
			counter[i]=0;
		
		for(int i=0; i<a.length;++i){
			++counter[a[i]];
		}
	
			System.out.println(Arrays.toString(counter));
		
		for(int j=0;j<counter.length;++j){
			
			for(int i=index; i<index+counter[j]; ++i){
				a[i]=j;}
			index+=counter[j];
		}
		
		return a;
	}
	
//	public static int[] fillArray(int[] a, int number, int index, int length){
//		for(int i=index; i<length; ++i)
//			a[i]=number;
//		
//		return a;
//	}
	
	
}
