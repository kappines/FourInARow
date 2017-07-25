// // Copyright 2015 theaigames.com (developers@theaigames.com)

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

import java.util.Random;
import javax.print.attribute.standard.MediaSize.Other;

/**
 * BotStarter class
 * 
 * Magic happens here. You should edit this file, or more specifically
 * the makeTurn() method to make your bot do more than random moves.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

public class BotStarter {	
    Field field;
    
    final int MAKE_SIMPLE_WIN = -4;
    final int COUNTER_SIMPLE_WIN = -3;
    final int MAKE_COMPLEX_WIN = -2;
    final int COUNTER_COMPLEX_WIN = -1;
    final int LOWEST_PRIORITY = MAKE_SIMPLE_WIN;

    /**
     * Makes a turn. Edit this method to make your bot smarter.
     *
     * @return The column where the turn was made.
     */
    public int makeTurn() {
    	int priority = Integer.MAX_VALUE;
		int move = -1;
		int moveFreedom = 0;
		int turnsToComplexWin = Integer.MAX_VALUE;
		int enemyBotId = BotParser.mBotId % 2 + 1;
		
		for(int column = 0; column < field.getNrColumns() && priority > LOWEST_PRIORITY; column++){
			System.err.println("\n" + "Examining column: " + column);
			if(!(field.isColumnFull(column))){
				int row = field.rowIfAddDisc(column);
				
				Field cloneField = new Field(field);
				cloneField.addDisc(column, BotParser.mBotId);
				
				//go if simple win
				if(cloneField.simpleWin(column, row, BotParser.mBotId)) {
					move = column;
					priority = MAKE_SIMPLE_WIN;
					System.err.println("winning");
				} else {
					
					//go if blocks enemy simple win
					Field enemyCloneField = new Field(field);
					enemyCloneField.addDisc(column, enemyBotId);
					if (enemyCloneField.simpleWin(column, row, enemyBotId)) {
						move = column;
						priority = COUNTER_SIMPLE_WIN;
						System.err.println("not losing");
					} else if(priority > COUNTER_SIMPLE_WIN) {
						
						//don't go if move helps enemy win on next turn
						if(!cloneField.isColumnFull(column)){
							Field futureField = new Field(cloneField);
							int futureRow = futureField.rowIfAddDisc(column);
							futureField.addDisc(column, enemyBotId);
							if (futureField.simpleWin(column, futureRow, enemyBotId)
									|| futureField.unavoidableWin(column, futureRow, enemyBotId) != -1
									|| cloneField.unavoidableWin(column, row, enemyBotId) != -1){
								System.err.println("Don't play, helps enemy win");
								continue;
							}
						}
						
						//check if complex win or counters complex win
						//note: the enemy takes one more turn to win since they play right after yourself, hence the +1
						int ttcw = cloneField.unavoidableWin(column, row, BotParser.mBotId);
						int enemy_ttcw = enemyCloneField.unavoidableWin(column, row, enemyBotId);
						System.err.println("ttcw: " + ttcw + ", enemy_ttcw: " + enemy_ttcw);
						if(ttcw != -1 && (ttcw < turnsToComplexWin 
								|| (ttcw == turnsToComplexWin && priority == COUNTER_COMPLEX_WIN))) {
							move = column;
							if (enemy_ttcw != -1 && enemy_ttcw + 1 < ttcw){
								priority = COUNTER_COMPLEX_WIN;
								turnsToComplexWin = enemy_ttcw + 1;
								System.err.println("not losing");
							} else {
								priority = MAKE_COMPLEX_WIN;
								turnsToComplexWin = ttcw;
								System.err.println("winning");
							}
						} else if (enemy_ttcw != -1 && enemy_ttcw + 1 < turnsToComplexWin) {
							move = column;
							priority = COUNTER_COMPLEX_WIN;
							turnsToComplexWin = enemy_ttcw + 1;
							System.err.println("not losing");
						} else if (priority > 0){
							
							//check if it is a better move than before
							int minPlayerTurnsToWin = Math.min(
									Math.min(cloneField.verticalTurnsToWin(column, row, BotParser.mBotId),
											cloneField.horizontalTurnsToWin(column, row, BotParser.mBotId)),
									Math.min(cloneField.ascendingDiagonalTurnsToWin(column, row, BotParser.mBotId),
											cloneField.descendingDiagonalTurnsToWin(column, row, BotParser.mBotId)));
							System.err.println("minimum turns to win: " + minPlayerTurnsToWin);
							int freeAdjacentSpaces = cloneField.freeAdjacentSpaces(column, row);
							System.err.println("free spaces: " + freeAdjacentSpaces);
							if(priority > minPlayerTurnsToWin 
									|| (priority == minPlayerTurnsToWin && freeAdjacentSpaces > moveFreedom)){
								move = column;
								priority = minPlayerTurnsToWin;
								moveFreedom = freeAdjacentSpaces;
								System.err.println("Priority changed to " + priority);
							}
						}
					}
				}
			}
		}
		if(move == -1){
			System.err.println("No decent move, picking valid move...");
			move = 3;
			int i = 1;
			int way = 1;
			while(field.isColumnFull(move)){
				move += i*way;
				way *= -1;
				i++;
			}
		}
        return move;
    }
    
    public static void main(String[] args) {
    	BotParser parser = new BotParser(new BotStarter());
    	parser.run();
 	}
 	
 }
