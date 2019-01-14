package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyRobot extends BCAbstractRobot {
	public int turn = 0;
	private int lastHealth;

	public Action turn() {
		turn++;
		//log("Turn: "+turn);

		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				//log("Building a pilgrim.");
				return buildUnit(SPECS.PILGRIM,1,0);
			}
		}
		
		if (me.unit == SPECS.CHURCH) {
			
		}

		if (me.unit == SPECS.PILGRIM) {
			if (turn == 1) {
				//log("I am a pilgrim.");

				//log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
			}
		}
		
		//sends signal max of 10 sqaure radius if damage was just taken
    		alertEnemiesNearby();
		
		//mines if mining is possible
    		//May want to change to only mine if safe
    		if (canMineFuel()) {
    			return mine();
    		}
    		if(canMineKarbonite()) {
    			return mine();
    		}
    	
    		lastHealth = me.health;
    		//log("health: "+me.health);
    		//log("last health: "+this.lastHealth);


		return null;

	}
	
	//if damage was taken, sends signal
    	//696969 is alert value :)
    	//broadcasts sq radius of 10 or, if less than 10 fuel, longest possible distance
    	public void alertEnemiesNearby() {
    		if(me.health<this.lastHealth) {
    			//log("i took damage");
    			int sigRad = 10;
    			if(sigRad>me.fuel) {
    				sigRad=me.fuel;
    			}
    			//log("sending alert");
    			signal(696969,sigRad);
    		}
    	}
	
	//returns true if pilgrim can mine fuel
    	public boolean canMineFuel() {
    		if (me.unit == SPECS.PILGRIM && (fuelMap[me.y][me.x])) {
    			if (me.fuel>0&&me.fuel<100){
    				//log("mining fuel");
    				return true;
    			}
    		}
   	 }
		    
	//returns true if pilgrim can mine karbonite
    	public boolean canMineKarbonite() {
    	if (me.unit == SPECS.PILGRIM && (karboniteMap[me.y][me.x])) {
			if(me.fuel>0&&me.karbonite<20) {
				//log("mining karbonite");
				return true;
			}
    	}
    	}
		    
	
}
