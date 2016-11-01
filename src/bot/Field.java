// Copyright 2015 theaigames.com (developers@theaigames.com)

//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at

//        http://www.apache.org/licenses/LICENSE-2.0

//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//	
//    For the full copyright and license information, please view the LICENSE
//    file that was distributed with this source code.

package bot;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.LinkedList;
import java.util.Queue;

/**
 * Field class
 * 
 * Field class that contains the field status data and various helper functions.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class Field {	
	private int[][] mBoard;
	private int mCols = 0, mRows = 0;
	private String mLastError = "";
	public int mLastColumn = 0;
	
	public Field(int columns, int rows) {
		mBoard = new int[columns][rows];
		mCols = columns;
		mRows = rows;
		clearBoard();
	}
	
	public Field(Field f){
		mCols = f.mCols;
		mRows = f.mRows;
		mBoard = new int[mCols][mRows];
		int j;
		for(int i = 0; i < mCols; i++){
			for(j = 0; j < mRows; j++){
				mBoard[i][j] = f.getDisc(i, j);
			}
		}
	}
	
	/**
	 * @return : Returns the number of columns in the field.
	 */
	public int getNrColumns() {
		return mCols;
	}
	
	/**
	 * @return : Returns the number of rows in the field.
	 */
	public int getNrRows() {
		return mRows;
	}
	
	/**
	 * Sets the number of columns (this clears the board)
	 * @param args : int cols
	 */
	public void setColumns(int cols) {
		mCols = cols;
		mBoard = new int[mCols][mRows];
	}

	/**
	 * Sets the number of rows (this clears the board)
	 * @param args : int rows
	 */
	public void setRows(int rows) {
		mRows = rows;
		mBoard = new int[mCols][mRows];
	}
	
	/**
	 * Adds a disc to the board
	 * @param args : command line arguments passed on running of application
	 * @return : true if disc fits, otherwise false
	 */
	public Boolean addDisc(int column, int disc) {
		mLastError = "";
		if (column < mCols) {
			for (int y = mRows-1; y >= 0; y--) { // From bottom column up
				if (mBoard[column][y] == 0) {
					mBoard[column][y] = disc;
					mLastColumn = column;
					return true;
				}
			}
			mLastError = "Column is full.";
		} else {
			mLastError = "Move out of bounds.";
		}
		return false;
	}
	
	/**
	 * get row in which the disk will be if dropped in column
	 * @param column
	 * @return
	 */
	public int rowIfAddDisc(int column) {
		if (column < mCols) {
			for (int y = mRows-1; y >= 0; y--) { // From bottom column up
				if (mBoard[column][y] == 0) {
					return y;
				}
			}
		}
		return -1;
	}
	
	/**
	 * returns minimum number of discs to add in order to make a vertical win if disc is placed in "column"
	 * discards (returns -1) if chain can not attain 4 in the future
	 * @param column
	 * @param disc
	 * @return
	 */
	public int verticalTurnsToWin(int column, int row, int disc){
		int chainLength = 0;
		int potentialChain = 0;
		int score = 0;
		Queue<Integer> queue = new LinkedList<>();

		for(int r = 0; r < mRows; r++) {			
			int currDisc = getDisc(column, r);
			if(currDisc == 0){
				potentialChain++;
			} else if (currDisc == disc) {
				if (r == row)
				chainLength++;
				potentialChain++;
			} else {
				break;
			}
		}
		
		if(potentialChain >= 4){
			score = 4 - chainLength;
			return score;
		}
		return -1;
	}
	
	public int horizontalTurnsToWin(int column, int row, int disc){
		int chainLength = 0;
		//int potentialChain = 0;
		boolean chainContainsDisc = false;
		boolean stopChain = false;
		int score = 0;
		int currChainScore = 0;
		int minScore = 0;
		Queue<Integer> queue = new LinkedList<>();
		
		for(int c = 0; c < mCols; c++) {			
			int currDisc = getDisc(c, row);
			
			if(currDisc == disc || currDisc == 0){
				//potentialChain++;
				
				score = (c == column) ? 0 : row - rowIfAddDisc(c);
				queue.add(score);
				currChainScore += score;
				if(queue.size() == 4){
					minScore = currChainScore;
				} else if(queue.size() > 4){
					currChainScore -= queue.poll();
					minScore = Math.min(minScore, currChainScore);
				}
				
				if (currDisc == disc) {
					if (c == column){
						chainContainsDisc = true;
					}
					if (!stopChain)
						chainLength++;
				} else if(currDisc == 0){
					if(chainContainsDisc){
						stopChain = true;
					} else {
						chainLength = 0;
					}
				}
			} else if(!chainContainsDisc) {
				chainLength = 0;
				//potentialChain = 0;
				queue.clear();
			}
			
			if(currDisc != 0 && currDisc != disc && chainContainsDisc)
				break;	
		}
		
		//if(potentialChain >= 4)
			//return chainLength;
		if(queue.size() >= 4)
			return minScore;
		return -1;
	}
	
	
	/**
	 * returns length of max chain containing the disc added in "column"
	 * discards (returns 1) if chain can not attain 4 in the future
	 * @param column
	 * @param disc
	 * @return
	 */
	public int verticalChain(int column, int row, int disc){
		int chainLength = 0;
		int potentialChain = 0;
		boolean chainContainsDisc = false;
		boolean stopChain = false;
		for(int r = 0; r < mRows; r++) {
			
			//System.out.print(getDisc(column, r));
			
			int currDisc = getDisc(column, r);
			if (currDisc == disc) {
				if (r == row)
					chainContainsDisc = true;
				if (!stopChain)
					chainLength++;
				potentialChain++;
			} else if (!chainContainsDisc) {
				chainLength = 0;
				if(currDisc == 0)
					potentialChain++;
				else
					potentialChain = 0;
			} else {
				stopChain = true;
				if (currDisc == 0)
					potentialChain++;
				else
					break;
			}
		}
		
		//System.out.println();
		//System.out.println("max vertical chain = " + maxChain);
		
		if(potentialChain >= 4)
			return chainLength;
		return 1;
	}
	
	public int horizontalChain(int column, int row, int disc){
		int chainLength = 0;
		int potentialChain = 0;
		boolean chainContainsDisc = false;
		boolean stopChain = false;
		
		for(int c = 0; c < mCols; c++) {			
			int currDisc = getDisc(c, row);
			if (currDisc == disc) {
				if (c == column)
					chainContainsDisc = true;
				if (!stopChain)
					chainLength++;
				potentialChain++;
			} else if (!chainContainsDisc) {
				chainLength = 0;
				if(currDisc == 0)
					potentialChain++;
				else
					potentialChain = 0;
			} else {
				stopChain = true;
				if (currDisc == 0)
					potentialChain++;
				else
					break;
			}
		}
		if(potentialChain >= 4)
			return chainLength;
		return 1;
		
		/*
		int chainLength = 0;
		boolean chainContainsDisc = false;
		for(int c = 0; c < mCols; c++) {			
			if (getDisc(c, row) == disc) {
				chainLength++;
				if(c == column)
					chainContainsDisc = true;
			} else if (!chainContainsDisc) {
				chainLength = 0;
			} else
				break;
		}		
		return chainLength;
		*/
		
		//System.out.print(getDisc(c, r));
	}
	
	/**
	 * returns length of longest diagonal chain of "disc" discs
	 * checking ascending and descending diagonals
	 * @param column
	 * @param row
	 * @param disc
	 * @return
	 */
	public int diagonalChain(int column, int row, int disc){
		int maxChain = 0;
		int maxPotentialChain = 0;

		//descending
		int chainLength = 0;
		int potentialChain = 0;
		boolean chainContainsDisc = false;
		boolean stopChain = false;

		int r = row - Math.min(column, row);
		for(int c = column - Math.min(column, row); c < mCols && r < mRows; c++) {
			int currDisc = getDisc(c, r);
			if (currDisc == disc) {
				if (c == column)
					chainContainsDisc = true;
				if (!stopChain)
					chainLength++;
				potentialChain++;
			} else if (!chainContainsDisc) {
				chainLength = 0;
				if(currDisc == 0)
					potentialChain++;
				else
					potentialChain = 0;
			} else {
				stopChain = true;
				if (currDisc == 0)
					potentialChain++;
				else
					break;
			}
			r++;
		}
		
		maxChain = chainLength;
		maxPotentialChain = potentialChain;
		
		//System.out.println();
		//System.out.println("max descending chain = " + maxChain);
		
		//ascending
		chainLength = 0;
		potentialChain = 0;
		chainContainsDisc = false;
		stopChain = false;
		
		r = row + Math.min(column, mRows - 1 - row);
		for(int c = column - Math.min(column, mRows -1 - row); c < mCols && r >= 0; c++) {
			int currDisc = getDisc(c, r);
			if (currDisc == disc) {
				if (c == column)
					chainContainsDisc = true;
				if (!stopChain)
					chainLength++;
				potentialChain++;
			} else if (!chainContainsDisc) {
				chainLength = 0;
				if(currDisc == 0)
					potentialChain++;
				else
					potentialChain = 0;
			} else {
				stopChain = true;
				if (currDisc == 0)
					potentialChain++;
				else
					break;
			}
			r--;
		}

		//System.out.println();
		//System.out.println("max diagonal chain = " + maxChain);
		
		maxChain = Math.max(maxChain, chainLength);
		maxPotentialChain = Math.max(maxPotentialChain, potentialChain);
		if(maxPotentialChain >= 4)
			return maxChain;
		return 1;
	}
	
	/**
	 * Returns the current piece on a given column and row
	 * @param args : int column, int row
	 * @return : int
	 */
	public int getDisc(int column, int row) {
		return mBoard[column][row];
	}
	
	/**
	 * Checks whether the given column is full
	 * @return : Returns true when given column is full, otherwise returns false.
	 */
	public boolean isColumnFull(int column) {
		return (mBoard[column][0] != 0);
	}
	
	/**
	 * Checks whether the field is full
	 * @return : Returns true when field is full, otherwise returns false.
	 */
	public boolean isFull() {
		for (int x = 0; x < mCols; x++)
		  //for (int y = 0; y < mRows; y++)
		    if (mBoard[x][0] == 0)
		      return false; // At least one cell is not filled
		// All cells are filled
		return true;
	}
	
	/**
	 * Clear the board
	 */
	public void clearBoard() {
		for (int x = 0; x < mCols; x++) {
			for (int y = 0; y < mRows; y++) {
				mBoard[x][y] = 0;
			}
		}
	}
	
	/**
	 * Returns reason why addDisc returns false
	 * @param args : 
	 * @return : reason why addDisc returns false
	 */
	public String getLastError() {
		return mLastError;
	}
	
	@Override
	/**
	 * Creates comma separated String with every cell.
	 * @param args : 
	 * @return : String
	 */
	public String toString() {
		String r = "";
		int counter = 0;
		for (int y = 0; y < mRows; y++) {
			for (int x = 0; x < mCols; x++) {
				if (counter > 0) {
					r += ",";
				}
				r += mBoard[x][y];
				counter++;
			}
		}
		return r;
	}
	
	/**
	 * Initialise field from comma separated String
	 * @param String : 
	 */
	public void parseFromString(String s) {
		s = s.replace(';', ',');
		String[] r = s.split(",");
		int counter = 0;
		for (int y = 0; y < mRows; y++) {
			for (int x = 0; x < mCols; x++) {
				mBoard[x][y] = Integer.parseInt(r[counter]); 
				counter++;
			}
		}
	}
	
}
