package tower;

import java.util.Stack;

public class TowersOfBerlinSolver {
	Tower[] tow  =new Tower[3];
	Stack<DiskAction> act=new Stack<DiskAction>();

	
		public TowersOfBerlinSolver(Tower left, Tower middle, Tower right){
			tow[0]=left;
			tow[1]=middle;
			tow[2]=right;
		}
		
		public Stack<DiskAction> solve(){
			insertTower(tow[0].getMaxHeight());
			splitTower(tow[0].getMaxHeight()-1);
			

			return act;
		}
		
		public Stack<DiskAction> getActions(){
			return act;
		}
		
		private void moveDisk(int from, int to){
			act.push(new DiskAction(from,to));
		}
		
		private void moveTower(int from, int to, int i, int temp){
			if(i>1){
				moveTower(from,temp,i-1,to);
				moveDisk(from,to);
				moveDisk(from,to);
				moveTower(temp,to,i-1,from);
			}
			else{
				moveDisk(from,to);
				moveDisk(from,to);
			}
		}
		
		private void insertTower(int i){
			if(i>1){
				insertTower(i-1);
//				moveDisk(1,2);
				moveTower(0,2,i-1,1);
				moveDisk(1,0);
				//if(i%2==0){
				//	moveTower(1,2,i-1,0);
				//	moveTower(2,0,i-1,1);
				//}
				//else
				moveTower(2,0,i-1,1);
				//swapTower (1,0,i-1,2);
				}
			else{
				moveDisk(1,0);
				
			}
		}
		
		private void swapTower (int from, int to, int i, int temp){
			if(i>0){
				
			moveTower (1,0,i-1,2);
			moveDisk (1,2);
			moveTower (0,2,i-1,2);
			moveDisk (1,0);
			moveTower (2,1,i-1,2);
			moveDisk (2,0);
			swapTower(from,to,i-1,temp);
			}
			else
				moveDisk (1,2);
				moveDisk (1,0);
				moveDisk (2,0);
		}
		
		private void splitTower(int i){
			

			moveTower(0,2,i+1,1);
			moveTower(2,1,i+1,0);
			
			while(i>0){
				moveTower(1,2,i,0);
				moveDisk(1,0);
				moveTower(2,1,i,0);
				--i;
			}
		
				moveDisk(1,0);

		
		}
		
		private void splitTower2(int i){
			while(i>0){
				moveTower(0,2,i,1);
				moveDisk(0,1);
				moveTower(2,0,i,1);
				--i;
			}
		
				moveDisk(0,1);
		}
		
		
		
		
}
