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
    	//TODO : bloquer si ça crée une situation où deux positions seront gagnantes
    	
    	
    	int priority = Integer.MAX_VALUE;
		int move = -1;
		int ennemyBotId = BotParser.mBotId % 2 + 1;
		
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
					//go if blocks ennemy simple win
					Field ennemyCloneField = new Field(field);
					ennemyCloneField.addDisc(column, ennemyBotId);
					if (ennemyCloneField.simpleWin(column, row, ennemyBotId)) {
						move = column;
						priority = COUNTER_SIMPLE_WIN;
						System.err.println("not losing");
					} else if(priority > MAKE_COMPLEX_WIN) {
						//check if move will not help ennemy win on next turn
						if(!cloneField.isColumnFull(column)){
							Field futureField = new Field(cloneField);
							int futureRow = futureField.rowIfAddDisc(column);
							futureField.addDisc(column, ennemyBotId);
							if (futureField.simpleWin(column, futureRow, ennemyBotId)){
								System.err.println("Don't play, helps ennemy win");
								continue;
							}
						}
						
						//go if complex win
						if(cloneField.unavoidableWin(column, row, BotParser.mBotId)){
							move = column;
							priority = MAKE_COMPLEX_WIN;
							System.err.println("winning");
						} else if (priority > COUNTER_COMPLEX_WIN){
							if(ennemyCloneField.unavoidableWin(column, row, ennemyBotId)){
								move = column;
								priority = COUNTER_COMPLEX_WIN;
								System.err.println("not losing");
							} else if (priority > 0){
								//check if it is a better move than before
								int minPlayerTurnsToWin = Math.min(
										Math.min(cloneField.verticalTurnsToWin(column, row, BotParser.mBotId),
												cloneField.horizontalTurnsToWin(column, row, BotParser.mBotId)),
										Math.min(cloneField.ascendingDiagonalTurnsToWin(column, row, BotParser.mBotId),
												cloneField.descendingDiagonalTurnsToWin(column, row, BotParser.mBotId)));
								System.err.println("minimum turns to win: " + minPlayerTurnsToWin);
								if(priority > minPlayerTurnsToWin){
									move = column;
									priority = minPlayerTurnsToWin;
									System.err.println("Priority changed to " + priority);
								}
							}
						}
					}
				}
			}
		}
		if(move == -1){
			System.err.println("No decent move, picking valid move...");
			move = 0;
			while(field.isColumnFull(move)){
				move++;
			}
		}
        return move;
    }
    
    public static void main(String[] args) {
    	BotParser parser = new BotParser(new BotStarter());
    	parser.run();
 	}
 	
 }
