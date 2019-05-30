package sudoku;

public class SudokuSolver {
	Sudoku sudoku;
	
	public SudokuSolver(Sudoku sudoku){
		this.sudoku=sudoku;
	}
	
	public int[][] solve(){
		
		
		
		
		
	}
	
	private boolean isInRow(byte row, byte number){
		for(byte i=0; i<9; ++i)
			if(sudoku.grid[row][i]==number)
				return true;
		return false;
	}
	
	private boolean isInColumn(byte column, byte number){
		for(byte i=0; i<9; ++i)
			if(sudoku.grid[i][column]==number)
				return true;
		return false;
	}
	
	private boolean isInSquare(byte square, byte number){
		byte i=(byte) (square/3);
		byte j=(byte) (square%3);
		
		for(byte a=0; a<3; ++a){
			for(byte b=0; b<3; ++b){
				if(sudoku.grid[i][j]==number)
					return true;
				++j;
			}
			++i;
		}
		return false;
	}
	
	private byte countRow(byte row){
		byte counter=0;
		for(byte i=0; i<9; ++i)
			if(sudoku.grid[row][i]==0)
				++counter;
		return counter;
	}
	
	private byte countColumn(byte column){
		byte counter=0;
		for(byte i=0; i<9; ++i)
			if(sudoku.grid[i][column]==0)
				++counter;
		return counter;
	}
	
	private byte countSquare(byte square){
		byte counter=0;
		byte i=(byte) (square/3);
		byte j=(byte) (square%3);
		
		for(byte a=0; a<3; ++a){
			for(byte b=0; b<3; ++b){
				if(sudoku.grid[i][j]==0)
					++counter;
				++j;
			}
			++i;
		}
		return counter;
	}
	
	private byte findMax(){
		//search all squares,rows and columns to have the most numbers already written
		byte min=12;
		byte indexMin=-1;
		
		
		for(byte i=0; i<10; ++i){
			if(Math.min(countRow(i),countColumn(i))>countSquare(i))
				if(countSquare(i)<min){
				indexMin=(byte)(i+20);
				min=countSquare(i);
				}
			else if(countRow(i)<min){
					indexMin=i;
					min=countRow(i);
			}
			else if(countColumn(i)<min){
				indexMin=(byte)(1+10);
				min=countColumn(i);
			}		
				
		}
		return indexMin;
			
	}
	
	
	
}
