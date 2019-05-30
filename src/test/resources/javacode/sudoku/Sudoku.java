package sudoku;

import java.util.Arrays;
import java.lang.StringBuffer;

/**
 * Sudoku.java
 * Created: 14.01.2014, 13:17:06
 */

/**
 *
 */
public class Sudoku {
	int[][] grid;
  

	public Sudoku(int[][] grid)throws IllegalArgumentException{
		this.grid=grid;
		if(!isGridValid())
			throw new IllegalArgumentException("Das Feld gehört nicht zu einem Sudoku!");
	}
	
	public String toString(){
		
		StringBuffer string=new StringBuffer("");
		for(int i=0; i<9; ++i)
			string.append(Arrays.toString(grid[i]));
		return string.toString();
	}
	
	public int getValue(int a, int b){
		return grid[a][b];
	}
	
	public static boolean isValid(int[][] grid){
		return isValidTest(grid,1);
	}
	
	public boolean isGridValid(){
		return isValidTest(grid,0);
	}
	
	/**
	   * Checks whether a two dimensional grid represents a valid solution for a 9x9
	   * Sudoku.
	   * @param grid the grid of numbers
	   * @return {@code true} if the solution is valid, {@code false} otherwise.
	   */
	public static boolean isValidTest(int[][] grid, int avaible) {
      if( grid == null ) {
        return false;
      }
      if( grid.length != 9 ) {
        return false;
      }
      for( int i = 0; i < grid.length; i++ ) {
        if( grid[i].length != grid.length ) {
          return false;
        }
        for( int j = 0; j < grid.length; j++ ) {
          if (grid[i][j] < avaible || grid[i][j] > 9) {
            return false;
          }
        }
      }
      // Check rows
      for( int i = 0; i < grid.length; i++ ) {
        boolean[] occurs = new boolean[grid.length + 1];
        for( int j = 0; j < grid.length; j++ ) {
          if (occurs[grid[i][j]]) {
            return false;
          } else {
            occurs[grid[i][j]] = true;
          }
        }
      }
      // Check columns
      for( int i = 0; i < grid.length; i++ ) {
        boolean[] occurs = new boolean[grid.length + 1];
        for( int j = 0; j < grid.length; j++ ) {
          if( occurs[grid[j][i]] ) {
            return false;
          } else {
            occurs[grid[j][i]] = true;
          }
        }
      }
      // Check squares
      for( int i = 0; i < grid.length; i++ ) {
        boolean[] occurs = new boolean[grid.length + 1];
        for( int j = 0; j < grid.length; j++ ) {
          if( occurs[grid[(i / 3) * 3 + j / 3][(i % 3) * 3 + j % 3]] ) {
            return false;
          } else {
            occurs[grid[(i / 3) * 3 + j / 3][(i % 3) * 3 + j % 3]] = true;
          }
        }
      }
      return true;
    }
}
