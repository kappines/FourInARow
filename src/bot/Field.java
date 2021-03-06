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
	public Boolean addDisc(int column, int botId) {
		mLastError = "";
		if (column < mCols) {
			for (int y = mRows-1; y >= 0; y--) { // From bottom column up
				if (mBoard[column][y] == 0) {
					mBoard[column][y] = botId;
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
	 * check if there's a vertical win
	 * @param column
	 * @param botId
	 * @return
	 */
	public boolean verticalWin(int column, int botId){
		int chainLength = 0;
		int maxChainLength = 0;
		for(int row = 0; row < mRows; row++){
			if(getDisc(column, row) == botId) {
				chainLength++;
			} else {
				maxChainLength = Math.max(maxChainLength, chainLength);
				chainLength = 0;
			}
		}
		maxChainLength = Math.max(maxChainLength, chainLength);
		return (maxChainLength >= 4);
	}
	
	public boolean horizontalWin(int row, int botId){
		int chainLength = 0;
		int maxChainLength = 0;
		for(int column = 0; column < mCols; column++){
			if(getDisc(column, row) == botId) {
				chainLength++;
			} else {
				maxChainLength = Math.max(maxChainLength, chainLength);
				chainLength = 0;
			}
		}
		maxChainLength = Math.max(maxChainLength, chainLength);
		return (maxChainLength >= 4);
	}
	
	public boolean ascendingDiagonalWin(int column, int row, int botId){
		int chainLength = 0;
		int maxChainLength = 0;
		int r = row + Math.min(column, mRows - 1 - row);
		for(int c = column - Math.min(column, mRows - 1 - row); c < mCols && r >= 0; c++){
			if(getDisc(c, r) == botId) {
				chainLength++;
			} else {
				maxChainLength = Math.max(maxChainLength, chainLength);
				chainLength = 0;
			}
			r--;
		}
		maxChainLength = Math.max(maxChainLength, chainLength);
		return (maxChainLength >= 4);
	}
	
	public boolean descendingDiagonalWin(int column, int row, int botId){
		int chainLength = 0;
		int maxChainLength = 0;
		int r = row - Math.min(column, row);
		for(int c = column - Math.min(column, row); c < mCols && r < mRows; c++){
			if(getDisc(c, r) == botId) {
				chainLength++;
			} else {
				maxChainLength = Math.max(maxChainLength, chainLength);
				chainLength = 0;
			}
			r++;
		}
		maxChainLength = Math.max(maxChainLength, chainLength);
		return (maxChainLength >= 4);
	}
	
	/**
	 * returns true if 4 are aligned
	 * @param column
	 * @param row
	 * @param botId
	 * @return
	 */
	public boolean simpleWin(int column, int row, int botId){
		return (verticalWin(column, botId) || horizontalWin(row, botId) 
		|| ascendingDiagonalWin(column, row, botId) || descendingDiagonalWin(column, row, botId));
	}
	
	/**
	 * returns the number of the free spaces directly touching the given position
	 * @param column
	 * @param row
	 * @return
	 */
	public int freeAdjacentSpaces(int column, int row) {
		int freeSpaces = 0;
		int r = 0;
		for (int c = column - 1; c <= column + 1; c += 2) {
			if (c >= 0 && c < mCols) {
				for (r = Math.max(row - 1, 0); r < Math.min(row + 2, mRows); r++) {
					if (this.getDisc(c, r) == 0)
						freeSpaces++;
				}
			}
		}
		if (row > 0) {
			freeSpaces++;
		}
		return freeSpaces;
	}
	
	/**
	 * returns minimum number of discs to add in order to make a vertical win if disc is placed in "column"
	 * discards (returns MAX_VALUE) if chain can not attain 4 in the future
	 * @param column
	 * @param botId
	 * @return
	 */
	public int verticalTurnsToWin(int column, int row, int botId){
		int chainLength = 0;
		int potentialChain = 0;
		int score = 0;

		for(int r = 0; r < mRows; r++) {			
			int currDisc = getDisc(column, r);
			if(currDisc == 0){
				potentialChain++;
			} else if (currDisc == botId) {
				chainLength++;
				potentialChain++;
			} else {
				break;
			}
		}
		
		if(potentialChain >= 4){
			score = 4 - chainLength;
			//System.err.println("v: " + score);
			return score;
		}
		return Integer.MAX_VALUE;
	}
	
	public int horizontalTurnsToWin(int column, int row, int botId){
		boolean chainContainsDisc = false;
		int columnScore = 0;
		int currChainScore = 0;
		int minScore = 0;
		Queue<Integer> queue = new LinkedList<>();
		
		for(int c = 0; c < mCols; c++) {
			int currDisc = getDisc(c, row);
			if(currDisc == botId || currDisc == 0){
				if(c == column){
					chainContainsDisc = true;
				}
				if(currDisc == botId) {
					columnScore = 0;
				} else {
					columnScore = rowIfAddDisc(c) - row + 1;
				}
				queue.add(columnScore);
				currChainScore += columnScore;
				if(queue.size() == 4){
					minScore = currChainScore;
				} else if(queue.size() > 4){
					currChainScore -= queue.poll();
					minScore = Math.min(minScore, currChainScore);
				}
			} else if(!chainContainsDisc) {
				queue.clear();
				columnScore = 0;
				currChainScore = 0;
				minScore = 0;
			}
			
			if(currDisc != 0 && currDisc != botId && chainContainsDisc)
				break;	
		}
		
		if(queue.size() >= 4) {
			//System.err.println("h: " + minScore);
			return minScore;
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * returns minimum number of discs to add in order to make a desc. diagonal win if disc is placed in "column"
	 * discards (returns MAX_VALUE) if chain can not attain 4 in the future
	 * @param column
	 * @param row
	 * @param botId
	 * @return
	 */
	public int descendingDiagonalTurnsToWin(int column, int row, int botId){
		boolean chainContainsDisc = false;
		int columnScore = 0;
		int currChainScore = 0;
		int minScore = 0;
		Queue<Integer> queue = new LinkedList<>();

		int r = row - Math.min(column, row);
		for(int c = column - Math.min(column, row); c < mCols && r < mRows; c++) {
			int currDisc = getDisc(c, r);
			if(currDisc == botId || currDisc == 0){
				if(c == column){
					chainContainsDisc = true;
				}
				if(currDisc == botId) {
					columnScore = 0;
				} else {
					columnScore = rowIfAddDisc(c) - r + 1;
				}				
				queue.add(columnScore);
				currChainScore += columnScore;
				if(queue.size() == 4){
					minScore = currChainScore;
				} else if(queue.size() > 4){
					currChainScore -= queue.poll();
					minScore = Math.min(minScore, currChainScore);
				}
			} else if(!chainContainsDisc) {
				queue.clear();
				columnScore = 0;
				currChainScore = 0;
				minScore = 0;
			}
			
			if(currDisc != 0 && currDisc != botId && chainContainsDisc)
				break;	
			
			r++;
		}
		
		if(queue.size() >= 4) {
			//System.err.println("d: " + minScore);
			return minScore;
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * returns minimum number of discs to add in order to make an asc. diagonal win if disc is placed in "column"
	 * discards (returns MAX_VALUE) if chain can not attain 4 in the future
	 * @param column
	 * @param row
	 * @param botId
	 * @return
	 */
	public int ascendingDiagonalTurnsToWin(int column, int row, int botId){		
		boolean chainContainsDisc = false;
		int columnScore = 0;
		int currChainScore = 0;
		int minScore = 0;
		Queue<Integer> queue = new LinkedList<>();
		
		int r = row + Math.min(column, mRows - 1 - row);
		for(int c = column - Math.min(column, mRows -1 - row); c < mCols && r >= 0; c++) {
			int currDisc = getDisc(c, r);
			if(currDisc == botId || currDisc == 0){
				if(c == column){
					chainContainsDisc = true;
				}
				if(currDisc == botId) {
					columnScore = 0;
				} else {
					columnScore = rowIfAddDisc(c) - r + 1;
				}
				queue.add(columnScore);
				currChainScore += columnScore;
				if(queue.size() == 4){
					minScore = currChainScore;
				} else if(queue.size() > 4){
					currChainScore -= queue.poll();
					minScore = Math.min(minScore, currChainScore);
				}
			} else if(!chainContainsDisc) {
				queue.clear();
				columnScore = 0;
				currChainScore = 0;
				minScore = 0;
			}
			
			if(currDisc != 0 && currDisc != botId && chainContainsDisc)
				break;
			r--;
		}
		
		if(queue.size() >= 4) {
			//System.err.println("a: " + minScore);
			return minScore;
		}
		return Integer.MAX_VALUE;
	}
	
	/**
	 * returns the number of turns to win if the player with botId is sure to win in the future, -1 otherwise
	 * @param column
	 * @param row
	 * @param botId
	 * @return
	 */
	int unavoidableWin(int column, int row, int botId){
		/*if(row >= 2){
			cloneField.addDisc(column, botId);
			if(simpleWin(column, row - 1, botId)){
				cloneField = new Field(this);
				cloneField.addDisc(column, botId % 2 + 1);
				cloneField.addDisc(column, botId);
				if (simpleWin(column, row - 2, botId)){
					System.err.println("complex win 1 for player " + botId);
					return true;
				}
			}
		}*/
		
		int turnsToWin = Integer.MAX_VALUE;
		
		//complex win 1: if two following positions in any column can align 4 on the next turn (can't be blocked)
		Field cloneField;
		Field futureField;
		int futureRow;
		int otherBotId = botId % 2 + 1;
		for(int c = 0; c < mCols; c++){
			if (!(isColumnFull(c))) {
				cloneField = new Field(this);
				futureRow = cloneField.rowIfAddDisc(c);
				if(futureRow >= 1){
					for(int r = futureRow; r >= 1; r--){
						cloneField = new Field(this);
						for(int i = 0; i < futureRow - r; i++){
							cloneField.addDisc(c, otherBotId);
						}
						futureField = new Field(cloneField);
						futureField.addDisc(c, botId);
						if(futureField.simpleWin(c, r, botId)){
							futureField = new Field(cloneField);
							futureField.addDisc(c, otherBotId);
							futureField.addDisc(c, botId);
							if (futureField.simpleWin(c, r - 1, botId)){
								System.err.println("complex win 1 for player " + botId);
								turnsToWin = Math.min(turnsToWin, 2 * (futureRow - r + 1));
							}
						}
					}
				}
			}
		}
		
		//complex win 2: if two positions are available to align 4 on the next turn (can't be blocked)
		int winningPos = 0;
		for(int c = 0; c < mCols; c++){
			if(!(isColumnFull(c))){
				cloneField = new Field(this);
				int r = cloneField.rowIfAddDisc(c);
				cloneField.addDisc(c, botId);
				if(cloneField.simpleWin(c, r, botId)){
					winningPos++;
					if (winningPos >= 2)
						break;
				}
			}
		}
		if(winningPos >= 2){
			System.err.println("complex win 2 for player " + botId);
			turnsToWin = Math.min(turnsToWin, 2);
		}
		
		if (turnsToWin == Integer.MAX_VALUE)
			turnsToWin = -1;
		return turnsToWin;
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
