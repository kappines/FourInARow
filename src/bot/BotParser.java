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
import java.util.Scanner;

/**
 * MyBot class
 * 
 * Main class that will keep reading output from the engine.
 * Will either update the bot state or get actions.
 * 
 * @author Jim van Eeden <jim@starapple.nl>, Joost de Meij <joost@starapple.nl>
 */

/*
settings timebank 10000
settings time_per_move 500
settings player_names player1,player2
settings your_bot player1
settings your_botid 1
settings field_columns 7
settings field_rows 6
update game round 1
update game field 0,0,0,0,0,0,0;0,0,0,0,0,0,0;2,0,0,1,0,0,0;1,1,0,2,2,0,0;1,1,0,2,2,0,0;1,2,2,2,1,0,0
action move 10000
 */

//update game field 0,0,0,0,0,0,0;0,0,0,0,0,0,0;0,0,0,0,0,0,0;0,0,0,0,0,0,0;0,0,0,0,0,0,0;0,0,0,0,0,0,0

public class BotParser {
    
	final Scanner scan;
    final BotStarter bot;
    
    private Field mField;
    public static int mBotId = 0;

    
    public BotParser(BotStarter bot) {
		this.scan = new Scanner(System.in);
		this.bot = bot;
	}
    
    public void run() {
        mField = new Field(0, 0);
        while(scan.hasNextLine()) {
            String line = scan.nextLine();

            if(line.length() == 0) {
                continue;
            }

            String[] parts = line.split(" ");
            
            if(parts[0].equals("settings")) {
                if (parts[1].equals("field_columns")) {
                    mField.setColumns(Integer.parseInt(parts[2]));
                }
                if (parts[1].equals("field_rows")) {
                    mField.setRows(Integer.parseInt(parts[2]));
                }
                if (parts[1].equals("your_botid")) {
                    mBotId = Integer.parseInt(parts[2]);
                }
            } else if(parts[0].equals("update")) { /* new field data */
                if (parts[2].equals("field")) {
                    String data = parts[3];
                    mField.parseFromString(data); /* Parse Field with data */
                }
            } else if(parts[0].equals("action")) {
                if (parts[1].equals("move")) { /* move requested */
                	bot.field = new Field(mField);
                    int column = bot.makeTurn();
                    System.out.println("place_disc " + column);
                }
            }
            else { 
                System.out.println("unknown command");
            }
        }
    }
}