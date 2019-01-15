package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyRobot extends BCAbstractRobot {
	public int turn = 0;
	private int lastHealth;
	public boolean[][] karboniteMap = getKarboniteMap();
	public boolean[][] passableMap = getPassableMap();
	public boolean[][] fuelMap = getFuelMap();
	private String lastMove;

	public Action turn() {
		turn++;
		passableMap = getPassableMap();

		

		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				return buildUnit(SPECS.PILGRIM,1,0);
			}
			
			
		}
		
		if (me.unit == SPECS.CHURCH) {
			
		}

		if (me.unit == SPECS.PILGRIM) {
			if (turn ==1) {
			}
			
			if(canMineKarbonite()||canMineFuel()) {
				return mine();
			}


			if(!fuelMap[me.y][me.x]&&!karboniteMap[me.y][me.x]){
				//methods return null if karb or fuel cannot be reached, or coords of reachable karb/fuel
				int[] karboniteLocationFind=searchForKarboniteLocation();
				int[] fuelLocationFind=searchForFuelLocation();
				//moves to mineable spot
				if(!karboniteLocationFind.equals(null)&&me.karbonite<20){
					return move (karboniteLocationFind[1]-me.x,karboniteLocationFind[0]-me.y);

				}
				if(!fuelLocationFind.equals(null)&&me.fuel<100){
					return move (fuelLocationFind[1]-me.x,fuelLocationFind[0]-me.y);
				}
			}

			int[][]visiMap = getVisibleRobotMap();

			//stuff for testing
			if(me.x+1<visiMap.length&&visiMap[me.y][me.x+1]==0&&passableMap[me.y][me.x+1]&&me.x+1<passableMap[0].length){
				if(!lastMove.equals("right")){
					lastMove = "right";
					return move(1,0);
				}
			}

			if(me.y+1<visiMap.length&&visiMap[me.y+1][me.x]==0&&me.y+1<passableMap.length&&passableMap[me.y+1][me.x]){
				if(!lastMove.equals("down")){
					lastMove = "down";
					return move(0,1);
				}
			}

			if(me.x-1>=0&&visiMap[me.y][me.x-1]==0&&me.x-1>=0&&passableMap[me.y][me.x-1]){
				if(!lastMove.equals("left")){
					lastMove = "left";
					return move(-1,0);
				}
			}

			if(me.y-1>=0&&visiMap[me.y-1][me.x]==0&&me.y-1>=0&&passableMap[me.y-1][me.x]){
				if(!lastMove.equals("up")){
					lastMove = "up";
					return move(0,-1);
				}
			}
			boolean t = true;

			if(me.x+1<visiMap.length&&visiMap[me.y][me.x+1]==0&&me.x+1<passableMap[0].length&&passableMap[me.y][me.x+1]){
				if(t){
					lastMove = "right";
					
					return move(1,0);
				}
			}

			if(me.y+1<visiMap.length&&visiMap[me.y+1][me.x]==0&&me.y+1<passableMap.length&&passableMap[me.y+1][me.x]){
				if(t){
					lastMove = "down";
					return move(0,1);
				}
			}

			if(me.x-1>=0&&visiMap[me.y][me.x-1]==0&&me.x-1>=0&&passableMap[me.y][me.x-1]){
				if(t){
					lastMove = "left";
					return move(-1,0);
				}
			}

			if(me.y-1>=0&&visiMap[me.y-1][me.x]==0&&me.y-1>=0&&passableMap[me.y-1][me.x]){
				if(t){
					lastMove = "up";
					return move(0,-1);
				}
			}
			
		}
		
		//sends signal max of 10 sqaure radius if damage was just taken
    	if(doAlertEnemiesNearby()){
    		int sigRad = 10;
    		if(sigRad>fuel) {
    			sigRad=fuel;
    		}
    		signal(696969,sigRad);
    	}
		
    		
    	
    	lastHealth = me.health;


		return null;

	}
	
	//if damage was taken, sends signal
    	//696969 is alert value :)
    	//broadcasts sq radius of 10 or, if less than 10 fuel, longest possible distance
    	public boolean doAlertEnemiesNearby() {
    		if(me.health<this.lastHealth) {
    			//log("i took damage");
    			
    			//log("sending alert");
    			return true;
    			
    		}
    		return false;
    	}
	
	//returns true if pilgrim can mine fuel
    	public boolean canMineFuel() {
    		
    		if (me.unit == SPECS.PILGRIM && (fuelMap[me.y][me.x])) {
    			if(me.fuel<100){
    				return true;
    			}
    		}
    		return false;
   	 }
		    
	//returns true if pilgrim can mine karbonite
    	public boolean canMineKarbonite() {
    	if (me.unit == SPECS.PILGRIM && (karboniteMap[me.y][me.x])) {
    		if(me.karbonite<20){
				return true;
    		}
    	}
    	return false;
    	}

    	//searches for karbonite within movement range
    	//can only move to one of 8 surrounding squares, should probably be fixed
    	public int[] searchForKarboniteLocation() {
			int[] location = new int[2];
			int[][] visible = getVisibleRobotMap();

			for(int row = -1;row<2;row++){
				for(int col = -1;col<2;col++){
					if((me.x+col>=0&&me.x+col<fuelMap[0].length)&&(me.y+row>=0&&me.y+row<fuelMap.length)){
						if((karboniteMap[row+me.y][col+me.x]&&map[row+me.y][col+me.x])&&visible[row+me.y][col+me.x]==0){
							int[] goTo = {row+me.y,col+me.x};
							
							return goTo;
						}
					}
				}
			}
			return null;
	}

	//searches for fuel within movement range
	//can only move to one of 8 surrounding squares, should probably be fixed
	public int[] searchForFuelLocation() {
			int[] location = new int[2];
			int[][] visible = getVisibleRobotMap();

			for(int row = -1;row<2;row++){
				for(int col = -1;col<2;col++){
					if((me.x+col>=0&&me.x+col<fuelMap[0].length)&&(me.y+row>=0&&me.y+row<fuelMap.length)){
						if((fuelMap[row+me.y][col+me.x]&&map[row+me.y][col+me.x])&&visible[row+me.y][col+me.x]==0){
							int[] goTo = {row+me.y,col+me.x};
							
							return goTo;
						}
					}
				}
			}
			return null;
	}
		    
	
}
