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

    /**
     * Makes a turn. Edit this method to make your bot smarter.
     *
     * @return The column where the turn was made.
     */
    public int makeTurn() {
    	
    	//TODO: chaine en vertical > chaine en horizontal > chaine en diagonale
    	
    	/**
    	 * PRIORITIES:
    	 * 1: win (4 in a row)
    	 * 2: not lose (block a chain of 3 ennemies)
    	 * 3: continue a chain (3 in a row)
    	 * 4: continue a chain (2 in a row)
    	 * 5: block a chain (block a chain of 2 ennemies)
    	 * 6: block a chain (block a chain of 1 ennemies)
    	 * 7: other
    	 */
    	int priority = 8;
		int move = 0;
		int ennemyBotId = BotParser.mBotId % 2 + 1;
		
		for(int column = 0; column < field.getNrColumns() && priority > 1; column++){
			System.out.println("\n" + column);
			if(!(field.isColumnFull(column))){
				
				//System.out.println("currently examining column " + column);
				
				int row = field.rowIfAddDisc(column);
				//System.out.println(field.toString());
				
				Field cloneField = new Field(field);
				cloneField.addDisc(column, BotParser.mBotId);
				
				if(!cloneField.isColumnFull(column)){
					//if making this move lets the opponent win on the next turn, do not play it
					Field futureField = new Field(cloneField);
					int futureRow = futureField.rowIfAddDisc(column);
					futureField.addDisc(column, ennemyBotId);
					int maxEnnemy = Math.max(Math.max(futureField.verticalChain(column, futureRow, ennemyBotId),
							futureField.horizontalChain(column, futureRow, ennemyBotId)),
							futureField.diagonalChain(column, futureRow, ennemyBotId));
					//System.out.println(maxEnnemy + "\n");
					if (maxEnnemy >= 4)
						continue;
				}
				
				int maxPlayerChain = Math.max(Math.max(cloneField.verticalChain(column, row, BotParser.mBotId),
						cloneField.horizontalChain(column, row, BotParser.mBotId)),
						cloneField.diagonalChain(column, row, BotParser.mBotId));
				
				System.out.println(maxPlayerChain);
				
				if(maxPlayerChain >= 4) {
					move = column;
					priority = 1;
					//System.out.println("priority : 1");
				} else if (priority > 2) {
					
					//System.out.println();
					cloneField = new Field(field);
					//System.out.println(cloneField.toString());
					cloneField.addDisc(column, ennemyBotId);
					int maxEnnemyChain = Math.max(Math.max(cloneField.verticalChain(column, row, ennemyBotId),
							cloneField.horizontalChain(column, row, ennemyBotId)),
							cloneField.diagonalChain(column, row, ennemyBotId));
					
					System.out.println(maxEnnemyChain);
					
					if(maxEnnemyChain >= 4) {
						move = column;
						priority = 2;
						//System.out.println("priority : 2");
					} else if (maxPlayerChain >= maxEnnemyChain) {
						switch(maxPlayerChain) {
						case 2:
							if(priority > 4) {
								move = column;
								priority = 4;
								//System.out.println("priority : 4");
							}
							break;
						case 3:
							if(priority > 3) {
								move = column;
								priority = 3;
								//System.out.println("priority : 3");
							}
							break;
						default:
							if(priority > 7) {
								move = column;
								priority = 7;
								//System.out.println("priority : 7");
							}
						}
					} else {
						switch(maxEnnemyChain) {
						case 2:
							if(priority > 6) {
								move = column;
								priority = 6;
								//System.out.println("priority : 6");
							}
							break;
						case 3:
							if(priority > 5) {
								move = column;
								priority = 5;
								//System.out.println("priority : 5");
							}
							break;
						default:
							if(priority > 7) {
								move = column;
								priority = 7;
								//System.out.println("priority : 7");
							}
						}
					}
				}
			}
		}
		//System.out.println("move = " + move);
        return move;
    }
    
    public static void main(String[] args) {
    	BotParser parser = new BotParser(new BotStarter());
    	parser.run();
 	}
 	
 }
