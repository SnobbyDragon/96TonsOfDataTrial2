package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public class Point {
		public int x;
		public int y;

		public Point() {
			
		}
		
		public Point(int x, int y) {
			setPoint(x, y);
		}

		public void setPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
	}
	public int turn;
	public final int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public int mapYSize, mapXSize; //size of the map, length y and length x
	public HashSet<Point> karboLocations;
	public HashSet<Point> fuelLocations;
	public int karboDepositNum;
	public int fuelDepositNum;
	public int closeKarboNum, farKarboNum;
	public int closeFuelNum, farFuelNum;
	public final int CLOSE = 5, FAR = 10;
	public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
	public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
	public boolean haveCastle = false;
	public Point castleLocation = new Point(); //location of castle
	public Point crusaderTarget = new Point(); //location of crusader target
	public HashMap<String, Integer> bots = new HashMap<String, Integer>(); //castles know what bots they have created
	public boolean crusadeMode = false; //are we in full attack mode
	public Point closestKarbonite; // the closest karbonite point for pilgrims
	public Point closestFuel; // the closest fuel point for pilgrims
	public Action turn() {
		turn++;
		if (turn == 1)
		{
			//gets radius and not radius^2
			//			this.log(this.getMovementRangeRadius(this.me.unit) + "");
			//what are these numbers?
			//			this.log("crusader=" + SPECS.CRUSADER);
			//			this.log("preacher=" + SPECS.PREACHER);
			//			this.log("pilgrim=" + SPECS.PILGRIM);

			//gets maps
			passableMap = getPassableMap();
			karboniteMap = getKarboniteMap();
			fuelMap = getFuelMap();

			//map size set
			mapYSize = passableMap.length;
			mapXSize = passableMap[0].length;

			//sets locations of deposits
			karboLocations = this.getKarboniteLocations();
			fuelLocations = this.getFuelLocations();
			//			this.log(karboLocations.toString());

			//sets number of deposits
			karboDepositNum = this.karboLocations.size();
			fuelDepositNum = this.fuelLocations.size();
			//close and far deposits
			closeKarboNum = this.findCloseKarboDepositNum(CLOSE); //closer than 5
			farKarboNum = this.findFarKarboDepositNum(FAR); //farther than 10
			closeFuelNum = this.findCloseFuelDepositNum(CLOSE); //closer than 5
			farFuelNum = this.findFarFuelDepositNum(FAR); //farther than 10

			//first target center of the map
			crusaderTarget.setPoint(mapXSize/2, mapYSize/2);

			//records number of robots
			bots.put("pilgrims", 0);
			bots.put("crusaders", 0);
			bots.put("preachers", 0);
			//this.log("x=" + this.me.x + " y=" + this.me.y);
			//this.logMap(this.karboniteMap);
		}
		visibleRobotMap = this.getVisibleRobotMap(); //get visible robots every turn
		if (bots.get("crusaders") >= 7) {
			this.crusadeMode = true;
		}
		if (me.unit == SPECS.CASTLE) { //castle
			if (this.karbonite > 40 && this.turn <= 3) { //preachers to protect in the beginning
				if (this.canBuild(SPECS.PREACHER)) {
					bots.put("preachers", bots.get("preachers") + 1);
					return this.makeUnit(SPECS.PREACHER);
				}
			}
			if (this.makeMorePilgrims() || (this.crusadeMode && this.makeEvenMorePilgrims())) {
				if (this.canBuild(SPECS.PILGRIM))  {
					//log("built pilgrim at x=" + this.checkAdjacentAvailable()[0] + " y=" + this.checkAdjacentAvailable()[1] + "\ncastle at x=" + this.me.x + " y=" + this.me.y);
					bots.put("pilgrims", bots.get("pilgrims") + 1);
					return this.makeUnit(SPECS.PILGRIM);
				}
			}
			if (bots.get("preachers") < 3) { //build 3 preachers
				if (this.canBuild(SPECS.PREACHER)) {
					//					log("built preacher");
					bots.put("preachers", bots.get("preachers") + 1);
					return this.makeUnit(SPECS.PREACHER);
				}
			}
			if (bots.get("preachers") == 3 && (turn >= 100 || this.karbonite >= 50)) { //build infinite crusaders
				if (this.canBuild(SPECS.CRUSADER)) {
					//					log("built crusader");
					bots.put("crusaders", bots.get("crusaders") + 1);
					return this.makeUnit(SPECS.CRUSADER);
				}
			}
		}
		if (me.unit == SPECS.PILGRIM) { //pilgrim
			//			log("I am a pilgrim");
			//			log("My karbonite: "+me.karbonite);
			//			log("My fuel: "+me.fuel);
			//			log("Have Castle: "+haveCastle);
			//			log("My Castle X: "+castleLocation.x);
			//			log("My Castle Y: "+castleLocation.y);
			//			log("My X position: "+me.x);
			//			log("My Y position: "+me.y);
			if (canMineFuel()||canMineKarbonite()) {
				//				this.log("mining");
				return mine();
			}
			if (!haveCastle) {
				if(locateNearbyCastle()) {
					haveCastle = true;
				}
			}
			if (haveCastle && canGiveStuff()) {
				//				this.log("giving to castle, karbo=" + this.me.karbonite + " fuel=" + this.me.fuel);
				int xCastle = castleLocation.getX() - this.me.x;
				int yCastle = castleLocation.getY() - this.me.y;
				return give(xCastle, yCastle, me.karbonite, me.fuel);
			}
			if (haveCastle && (me.karbonite==20||me.fuel==100)) {
				//				this.log("returning to castle");
				return pathFind(castleLocation);
			}
			else {
				if(closestKarbonite==(null)) {
					closestKarbonite = this.searchForKarboniteLocation();
				}
				if(closestFuel==(null)) {
					closestFuel = this.searchForFuelLocation();
				}
				//				this.log("pilgrim at x=" + this.me.x + " y=" + this.me.y + "\nkarbo at x=" + closestKarbonite[0] + " y=" + closestKarbonite[1] + "\nfuel at x=" + closestFuel[0] + " y=" + closestFuel[1]);
				if (crusadeMode || findDistance(this.me, closestKarbonite.getX(), closestKarbonite.getY()) >= findDistance(this.me, closestFuel.getX(), closestFuel.getY())) {
					return pathFind(closestFuel);
				}
				else {
					return pathFind(closestKarbonite);
				}
			}
		}
		if (me.unit == SPECS.CRUSADER) { //crusader
			//move crusade target every so turns
			//			this.log(crusaderTarget.x + " " + crusaderTarget.y);
			this.setCrusadeTarget(this.mapXSize/2);
			if (fuel >= 10) {
				HashSet<Robot> enemies = findBadGuys();
				if (enemies.size() == 0 && this.fuel > 100) {
					return pathFind(crusaderTarget);
				}
				//				log("Enemies size: "+enemies.size());
				Robot closeBadGuy = findPrimaryEnemyDistance(enemies);
				try {
//					log("Bad guy's health: " + closeBadGuy.health);
//					log("Other bad guy data " + closeBadGuy.x);
					return attack(closeBadGuy.x - me.x,closeBadGuy.y - me.y);
				} catch (Exception e) {
//					log("Can't attack the man");
					try {
//						log("X coor bad: "+closeBadGuyLocation[0]);
//						log("Y coor bad: "+closeBadGuyLocation[1]);
						return pathFind(new Point(closeBadGuy.x, closeBadGuy.y));
					} catch (Exception ef) {
						//						log("Can find the man");
					}
				}
			}
			//			HashSet<Robot> enemies = this.findBadGuys();
			//			if (!enemies.isEmpty()) {
			//				Robot enemy = this.findPrimaryEnemyTypeHealth(enemies);
			//				return attack(enemy.x - this.me.x, enemy.y - this.me.y);
			//			}
			//			if (!haveCastle) { //finds castle upon spawn
			//				if(locateNearbyCastle()) {
			//					haveCastle = true;
			//				}
			//				//moves away from castle on spawn
			//				if (this.findDistance(this.me, this.castleLocation.x, this.castleLocation.y) == 1) {
			//					return this.move(this.me.x - this.castleLocation.x, this.me.y - this.castleLocation.y);
			//				}
			//			}
		}
		if (me.unit==SPECS.PREACHER) { //preacher
			//			if (!this.haveCastle) {
			//				this.locateNearbyCastle();
			//			}
			//			if (this.haveCastle && this.isAdjacentToCastle()) { //get out of the way
			//				
			//			}
			if (fuel >= 15) { //TODO: optimize attack
				//				//investigating AoE --> 3x3 area. it's the attacked square and all the adjacents to that square
				//				this.log(this.me.health + " health");
				//				return this.attack(1, 1);

				HashSet<Robot> potentialEnemies = findBadGuys();
				AttackAction maybeSauce = preacherAttack(potentialEnemies);
				if (maybeSauce != null) {
					return maybeSauce;
				}
			}
		}
		if (me.unit == SPECS.PROPHET) { //prophet
			//lead the way for crusaders. probably needs to use signaling near crusaders because vision isn't shared
		}
		return null;
	}

	//Used to log map coordinates
	public void logMap(boolean[][] map) {
		for (int r = 0; r < mapYSize; r++) {
			for (int c = 0; c < mapXSize; c++) {
				if (map[r][c]) {
					this.log("y=" + r + " x=" + c);
				}
			}
		}
	}
	public Action crusaderAttack(HashSet<Robot> potentialEnemies) {
		//Create arraylist of preachers, prophets, crusaders, castles, churches, pilgrims
		ArrayList<Robot> preachers=new ArrayList<Robot>();
		ArrayList<Robot> prophets=new ArrayList<Robot>();
		ArrayList<Robot> crusaders=new ArrayList<Robot>();
		ArrayList<Robot> castles=new ArrayList<Robot>();
		ArrayList<Robot> churches=new ArrayList<Robot>();
		ArrayList<Robot> pilgrims=new ArrayList<Robot>();
		//Iterate through potentialEnemies and do the following
		Iterator<Robot> potentialEnemiesIterator=potentialEnemies.iterator();
		while(potentialEnemiesIterator.hasNext()) {
			Robot enemy=potentialEnemiesIterator.next();
			//Check the type
			//Based on the type, it would put it in the corresponding arraylist
			if(enemy.unit==SPECS.PREACHER) {
				preachers.add(enemy);
			} else if(enemy.unit==SPECS.PROPHET) {
				prophets.add(enemy);
			} else if(enemy.unit==SPECS.CRUSADER) {
				crusaders.add(enemy);
			} else if(enemy.unit==SPECS.CASTLE) {
				castles.add(enemy);
			} else if(enemy.unit==SPECS.CHURCH) {
				churches.add(enemy);
			} else if(enemy.unit==SPECS.PILGRIM) {
				pilgrims.add(enemy);
			}
		}
		//ArrayList sorts at very end
		preachers=sortArrayListByDistance(preachers);
		prophets=sortArrayListByDistance(prophets);
		crusaders=sortArrayListByDistance(crusaders);
		castles=sortArrayListByDistance(castles);
		churches=sortArrayListByDistance(churches);
		pilgrims=sortArrayListByDistance(pilgrims);
		
		//sortArrayListByDistance(ArrayList<Robot> robots)
		//Run through preacher arraylist
		for(int i=0;i<preachers.size();i++) {
			Robot thePreacher=preachers.get(i);
			int distanceX=thePreacher.x-me.x;
			int distanceY=thePreacher.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through prophet arraylist
		for(int i=0;i<prophets.size();i++) {
			Robot theProphet=prophets.get(i);
			int distanceX=theProphet.x-me.x;
			int distanceY=theProphet.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//If attack fails, return move towards prophets
		for(int i=0;i<prophets.size();i++) {
			Robot theProphet=prophets.get(i);
			int distanceX=theProphet.x-me.x;
			int distanceY=theProphet.y-me.y;
			//pathfind here
		}
		
		//Run through crusader arraylist
		for(int i=0;i<crusaders.size();i++) {
			Robot theCrusader=crusaders.get(i);
			int distanceX=theCrusader.x-me.x;
			int distanceY=theCrusader.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through castle arraylist
		for(int i=0;i<castles.size();i++) {
			Robot theCastle=castles.get(i);
			int distanceX=theCastle.x-me.x;
			int distanceY=theCastle.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through church arraylist
		for(int i=0;i<churches.size();i++) {
			Robot theChurch=churches.get(i);
			int distanceX=theChurch.x-me.x;
			int distanceY=theChurch.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through pilgrim arraylist
		for(int i=0;i<pilgrims.size();i++) {
			Robot thePilgrim=pilgrims.get(i);
			int distanceX=thePilgrim.x-me.x;
			int distanceY=thePilgrim.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//If attack fails, return move towards preachers
		for(int i=0;i<preachers.size();i++) {
			Robot thePreacher=preachers.get(i);
			int distanceX=thePreacher.x-me.x;
			int distanceY=thePreacher.y-me.y;
			//pathfind here
		}
		
		//If attack fails, return move towards crusaders
		for(int i=0;i<crusaders.size();i++) {
			Robot theCrusader=crusaders.get(i);
			int distanceX=theCrusader.x-me.x;
			int distanceY=theCrusader.y-me.y;
			//pathfind here
		}
		
		//If attack fails, return move towards castles
		for(int i=0;i<castles.size();i++) {
			Robot theCastles=castles.get(i);
			int distanceX=theCastles.x-me.x;
			int distanceY=theCastles.y-me.y;
			//pathfind here
		}
		
		//If attack fails, return move towards churches
		for(int i=0;i<churches.size();i++) {
			Robot theChurches=churches.get(i);
			int distanceX=theChurches.x-me.x;
			int distanceY=theChurches.y-me.y;
			//pathfind here
		}
		
		//If attack fails, return move towards pilgrims
		for(int i=0;i<pilgrims.size();i++) {
			Robot thePilgrim=pilgrims.get(i);
			int distanceX=thePilgrim.x-me.x;
			int distanceY=thePilgrim.y-me.y;
			//pathfind here
		}
		return null;
	}
	
	public ArrayList<Robot> sortArrayListByDistance(ArrayList<Robot> robots) {
		robots=quickSort(robots,0,robots.size());
		return robots;
	}
	
	public ArrayList<Robot> quickSort(ArrayList<Robot> a, int start, int end) {
		if(start<end) {
			int pivot = partition(a, start, end);
		      // sort left sublist
		      quickSort(a,start,pivot-1);
		      // sort the right sublist
		      quickSort(a,pivot+1,end);
		}
		return a;
	}
	
	public int partition(ArrayList<Robot> a, int start, int end) {
		Robot pivot;
		int endOfLeft;
		int midIndex = (start+end)/2;
		swap(a,start,midIndex); 
		pivot=a.get(start);
		endOfLeft=start;
		for (int i=start+1; i<=end; i++) {
			double aiDistance=findDistance(me,a.get(i));
			double pivotDistance=findDistance(me,pivot);
		      if (aiDistance<pivotDistance) {
		           endOfLeft=endOfLeft+1;
		           swap(a,endOfLeft,i);
		      }
		}
		swap(a,start,endOfLeft);  
		return endOfLeft;
	}

	public static void swap(ArrayList<Robot> a, int i, int j) {
		Robot tmp = a.get(i);
		a.set(i, a.get(j));
		a.set(j, tmp);
	}
//
	//TODO update this
	public void setCrusadeTarget(int interval) {
		//		this.log(this.crusadeTurns + "");
		if (this.turn == interval) { //checks reflective vertical
			this.crusaderTarget.setPoint(this.me.x, this.mapYSize - this.me.y);
		}
		else if (this.turn == 2*interval) { //checks reflective both (diagonal)
			this.crusaderTarget.setPoint(this.mapXSize - this.me.x, this.mapYSize - this.me.y);
		}
		else if (this.turn == 3*interval) { //checks reflective horizontal
			this.crusaderTarget.setPoint(this.mapXSize - this.me.x, this.me.y);
		}
		else if (this.turn == 4*interval) { //up
			//			this.log("up crusade target");
			this.crusaderTarget.setPoint(this.mapXSize/2, this.mapYSize/4); 
		}
		else if (this.turn == 5*interval) { //up right
			this.crusaderTarget.setPoint(this.mapXSize*3/4, this.mapYSize/4);
		}
		else if (this.turn == 6*interval) { //right
			this.crusaderTarget.setPoint(this.mapXSize*3/4, this.mapYSize/2);
		}
		else if (this.turn == 7*interval) { //down right
			this.crusaderTarget.setPoint(this.mapXSize*3/4, this.mapYSize*3/4);
		}
		else if (this.turn == 8*interval) { //down
			this.crusaderTarget.setPoint(this.mapXSize/2, this.mapYSize*3/4);
		}
		else if (this.turn == 9*interval) { //down left
			this.crusaderTarget.setPoint(this.mapXSize/4, this.mapYSize*3/4);
		}
		else if (this.turn == 10*interval) { //left
			this.crusaderTarget.setPoint(this.mapXSize/4, this.mapYSize/2);
		}
		else if (this.turn == 11*interval) { //up left
			this.crusaderTarget.setPoint(this.mapXSize/4, this.mapYSize/4);
		}
	}

	//Can this pilgrim give stuff to the castle
	public boolean canGiveStuff() {
		int absoluteXCastleDistance = Math.abs(castleLocation.getX() - this.me.x);
		int absoluteYCastleDistance = Math.abs(castleLocation.getY() - this.me.y);
		if(absoluteXCastleDistance == 0 || absoluteXCastleDistance == 1) {
			if(absoluteYCastleDistance == 0 || absoluteYCastleDistance==1) {
				return this.me.karbonite > 0 || this.me.fuel > 0;
			}
		}
		return false;
	}

	public boolean locateNearbyCastle() {
		HashSet<Robot> goodGuys = this.findGoodGuys();
		Iterator<Robot> iter = goodGuys.iterator();
		while (iter.hasNext()) {
			Robot goodGuy = iter.next();
			if(goodGuy.unit == SPECS.CASTLE && goodGuy.team == this.me.team) {
				castleLocation.setPoint(goodGuy.x, goodGuy.y);
				return true;
			}
		}
		return false;
	}

	//can this pilgrim mine karbonite
	public boolean canMineKarbonite() {
		return karboniteMap[me.y][me.x] && me.karbonite<20;
	}

	//can this pilgrim mine fuel
	public boolean canMineFuel() {
		return fuelMap[me.y][me.x] && me.fuel<100;
	}

	//Path finding algorithm for moving
		// Path finding algorithm for moving
	public MoveAction pathFind(Point finalLocation) {
		// this.log("moving toward x=" + finalLocation[0] + " y=" + finalLocation[1]);
		if (fuel <= 30 || finalLocation.getX() == -1) { // not enough fuel, or -1 b/c can't find karbo or fuel
			// this.log("cannot move");
			return null;
		}
		int xDistance = finalLocation.getX() - me.x;
		int yDistance = finalLocation.getY() - me.y;
		try {
			return move(xDistance,yDistance);
		} catch (Exception e) {
			
		}
		int quadrant;
		double absoluteXDistance = Math.abs(xDistance);
		double absoluteYDistance = Math.abs(yDistance);
		double radianAngle;
		double piHalf = Math.PI / 2;
		double piEight = Math.PI / 8;
		double piThreeEight = piEight * 3;
		String optimalDirection = "";
		if (xDistance >= 0 && yDistance <= 0) {
			quadrant = 1;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHEAST";
			}
		} else if (xDistance <= 0 && yDistance <= 0) {
			quadrant = 2;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHWEST";
			}
		} else if (xDistance <= 0 && yDistance >= 0) {
			quadrant = 3;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHWEST";
			}
		} else if (xDistance >= 0 && yDistance >= 0) {
			quadrant = 4;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHEAST";
			}
		}
		// Figure out what quadrant it is
		// Based on the quadrant, find the angle
		// Based on the angle, determine the optimal direction
		// See if you can move in that direction
		// If you can move, check to make sure it doesn't line up with previous
		// locations
		// Move, and update the previous location list
		// See if you can jump
		// If you can move, check to make sure it doesn't line up with previous
		// locations
		// Move, and update the previous location list
		// Switch directions and repeat this whole process
		if (me.unit == SPECS.CRUSADER) {
			for (int i = 0; i < rotationTries.length; i++) {
				int index = (directions.indexOf(optimalDirection) + i) % 8;
				if (index == 0) {
					MoveAction maybeMoveHere=move(0,-3);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(0,-2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(0,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}

				} else if (index == 1) {
					MoveAction maybeMoveHere=move(2,-2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(1,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(2,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(1,-2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 2) {
					MoveAction maybeMoveHere=move(3,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(2,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(1,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 3) {
					MoveAction maybeMoveHere=move(2,2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(1,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(2,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(1,2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 4) {
					MoveAction maybeMoveHere=move(0,3);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(0,2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(0,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 5) {
					MoveAction maybeMoveHere=move(-2,2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-1,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-2,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-1,2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 6) {
					MoveAction maybeMoveHere=move(-3,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-2,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-1,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 7) {
					MoveAction maybeMoveHere=move(-2,-2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-1,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-2,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-1,-2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				}
			}
		} else {
			for (int i = 0; i < rotationTries.length; i++) {
				int index = (directions.indexOf(optimalDirection) + i) % 8;
				if (index == 0) {
					MoveAction maybeMoveHere=move(0,-2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(0,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}

				} else if (index == 1) {
					MoveAction maybeMoveHere=move(1,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 2) {
					MoveAction maybeMoveHere=move(2,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(1,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 3) {
					MoveAction maybeMoveHere=move(1,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 4) {
					MoveAction maybeMoveHere=move(0,2);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(0,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 5) {
					MoveAction maybeMoveHere=move(-1,1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 6) {
					MoveAction maybeMoveHere=move(-2,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
					maybeMoveHere=move(-1,0);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				} else if (index == 7) {
					MoveAction maybeMoveHere=move(-1,-1);
					if(maybeMoveHere!=null) {
						return maybeMoveHere;
					}
				}
			}
		}
		return null;
	}
	//BFS pathing
	public MoveAction bfs(int[] finalLocation) {

		return null;
	}

	//can this robot move
	public boolean canMove(int finalX, int finalY) {
		return passableMap[finalY][finalX] && visibleRobotMap[finalY][finalX] == 0;
	}

	//has this robot already been there
	public boolean alreadyBeenHere(Robot me, int dx, int dy) {
		boolean alreadyOccupied = false;
		for(int prev = 0; prev < previousLocations.size(); prev+=2) {
			if(me.x+dx == previousLocations.get(prev) && me.y+dy == previousLocations.get(prev+1)) {
				alreadyOccupied = true;
			}
		}
		if (!alreadyOccupied) {
			if (previousLocations.size() >= 6) {
				previousLocations.remove(4);
				previousLocations.remove(5);
				previousLocations.add(0, me.x);
				previousLocations.add(1, me.y);
			} else {
				previousLocations.add(0, me.x);
				previousLocations.add(1, me.y);
			}
		}
		return alreadyOccupied;
	}

	//is this unit next to a castle?
	public boolean isAdjacentToCastle() {
		return Math.abs(this.me.x-this.castleLocation.getX())==1 && Math.abs(this.me.y-this.castleLocation.getY())==1;
	}

	//checks if adjacent tiles are available. used for making units. checks tiles closer to the middle of the map first.
	public int[] checkAdjacentAvailable() {
		int x = this.me.x;
		int y = this.me.y;
		int dx = x - this.mapXSize/2;
		int dy = y - this.mapYSize/2;
		if (dx > 0) { //robot is to the east of center, check left first, then up down, then right
			if (x > 0) { //can check left
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new int[]{x-1, y-1};
						}
					}
					if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
						return new int[]{x-1, y};
					}
					if (y < mapYSize - 1) { //can check down
						if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
							return new int[]{x-1, y+1};
						}
					}
				}
				else { //robot is to the north or level of center, check down then middle then up
					if (y > 0) { //can check up
						if (y < mapYSize - 1) { //can check down
							if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
								return new int[]{x-1, y+1};
							}
						}
						if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
							return new int[]{x-1, y};
						}
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new int[]{x-1, y-1};
						}
					}
				}
			}
			if (dy > 0) { //robot is to the south of center, check up then down
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new int[]{x, y-1};
					}
				}
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new int[]{x, y+1};
					}
				}
			}
			else { //robot is to the north or level of center, check down then up
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new int[]{x, y+1};
					}
				}
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new int[]{x, y-1};
					}
				}
			}
			if (x < mapXSize - 1) { //can check right
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new int[]{x+1, y-1};
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new int[]{x+1, y};
					}
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new int[]{x+1, y+1};
						}
					}
				}
				else { //robot is north or level of center, check down then middle then up
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new int[]{x+1, y+1};
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new int[]{x+1, y};
					}
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new int[]{x+1, y-1};
						}
					}
				}
			}
		}
		else { //robot is to the west or level of center, check right first, then up down, then left
			if (x < mapXSize - 1) { //can check right
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new int[]{x+1, y-1};
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new int[]{x+1, y};
					}
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new int[]{x+1, y+1};
						}
					}
				}
				else { //robot is north or level of center, check down then middle then up
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new int[]{x+1, y+1};
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new int[]{x+1, y};
					}
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new int[]{x+1, y-1};
						}
					}
				}
			}
			if (dy > 0) { //robot is to the south of center, check up then down
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new int[]{x, y-1};
					}
				}
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new int[]{x, y+1};
					}
				}
			}
			else { //robot is to the north or level of center, check down then up
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new int[]{x, y+1};
					}
				}
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new int[]{x, y-1};
					}
				}
			}
			if (x > 0) { //can check left
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new int[]{x-1, y-1};
						}
					}
					if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
						return new int[]{x-1, y};
					}
					if (y < mapYSize - 1) { //can check down
						if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
							return new int[]{x-1, y+1};
						}
					}
				}
				else { //robot is north or level of center, check down then middle then up
					if (y < mapYSize - 1) { //can check down
						if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
							return new int[]{x-1, y+1};
						}
					}
					if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
						return new int[]{x-1, y};
					}
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new int[]{x-1, y-1};
						}
					}
				}
			}
		}
		return null; //surrounded by impassable terrain
	}

	//returns a HashSet of all the karbo location coordinates
	public HashSet<Point> getKarboniteLocations() {
		HashSet<Point> karboniteLocations = new HashSet<Point>();
		for (int i = 0; i < this.mapYSize; i++) {
			for (int j = 0; j < this.mapXSize; j++) {
				if (this.karboniteMap[i][j]) {
					//					this.log("adding karbo deposit location");
					karboniteLocations.add(new Point(j, i));
				}
			}
		}
		return karboniteLocations;
	}

	//returns a HashSet of all the fuel location coordinates
	public HashSet<Point> getFuelLocations() {
		HashSet<Point> fuelLocations = new HashSet<Point>();
		for (int i = 0; i < this.mapYSize; i++) {
			for (int j = 0; j < this.mapXSize; j++) {
				if (this.fuelMap[i][j]) {
					fuelLocations.add(new Point(j, i));
				}
			}
		}
		return fuelLocations;
	}

	//searches for the closest karbonite
	public Point searchForKarboniteLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		double distance;
		Iterator<Point> iter = this.karboLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			distance = findDistance(this.me, location.getX(), location.getY());
			if (this.visibleRobotMap[location.getY()][location.getX()]<=0 && (location.getX()!=this.me.x&&location.getY()!=this.me.y) && distance < minDistance) {
				minDistance = distance;
				minXCoordinate = location.getX();
				minYCoordinate = location.getX();
			}
		}
		//		for (int i = 0; i < mapYSize; i++) {
		//			for (int j = 0; j < mapXSize; j++) {
		//				if (karboniteMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
		//					//this.log("i am here x=" + this.me.x + " y=" + this.me.y + "  could mine here x=" + j + " y=" + i);
		//					distance = findDistance(me, j, i);
		//					if (distance < minDistance) {
		//						minDistance = distance;
		//						minXCoordinate = j;
		//						minYCoordinate = i;
		//					}
		//				}
		//			}
		//		}
		return new Point(minXCoordinate, minYCoordinate);
	}

	//searches for the closest fuel
	public Point searchForFuelLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		double distance;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			distance = findDistance(this.me, location.getX(), location.getY());
			if (this.visibleRobotMap[location.getY()][location.getX()]<=0 && (location.getX()!=this.me.x&&location.getY()!=this.me.y) && distance < minDistance) {
				minDistance = distance;
				minXCoordinate = location.getX();
				minYCoordinate = location.getY();
			}
		}
		//		for (int i = 0; i < mapYSize; i++) {
		//			for (int j = 0; j < mapXSize; j++) {
		//				if (fuelMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
		//					distance = findDistance(me, j, i);
		//					if (distance < minDistance) {
		//						minDistance = distance;
		//						minXCoordinate = j;
		//						minYCoordinate = i;
		//					}
		//				}
		//			}
		//		}
		return new Point(minXCoordinate, minYCoordinate);
	}

	//builds a unit where available
	public Action makeUnit(int type) {
		int[] spot = this.checkAdjacentAvailable();
		this.log("x=" + spot[0] + " y=" + spot[1]);
		return this.buildUnit(type, spot[0] - this.me.x, spot[1] - this.me.y);
	}

	//should this castle make pilgrims (based on number of nearby deposits)
	public boolean makeMorePilgrims() {
		return this.bots.get("pilgrims") < (this.closeFuelNum + this.closeKarboNum + 2)/2;
	}

	//should this castle make more pilgrims to sustain the crusade? TODO: finish this
	public boolean makeEvenMorePilgrims() {
		//pilgrims mine 10 fuel per turn, so takes 10 turns to get to 100 fuel full capacity
		//pilgrims can move 2 tiles per turn, so in 10 turns, pilgrims can move 20 tiles, thus max efficiency means a round trip of 20 tiles?
		return this.bots.get("pilgrims") < (this.closeFuelNum + this.closeKarboNum + 2)/2 + this.farFuelNum;
	}

	//finds the number of far away fuel deposits (fuel >= x tiles away from castle)
	public int findFarFuelDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) >= x ? 1 : 0;
		}
		return num;
	}

	//finds the number of close fuel deposits (fuel <= x tiles away from castle)
	public int findCloseFuelDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) <= x ? 1 : 0;
		}
		return num;
	}

	//finds the number of far away karbo deposits (karbo >= x tiles away from castle)
	public int findFarKarboDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) >= x ? 1 : 0;
		}
		return num;
	}

	//finds the number of close karbo deposits (karbo <= x tiles away from castle)
	public int findCloseKarboDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) <= x ? 1 : 0;
		}
		return num;
	}

	// Finds distance squared between two robots
	public double findDistance(Robot me, Robot opponent) {
		int xDistance = opponent.x - me.x;
		int yDistance = opponent.y - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}

	// Finds distance squared between a robot and a pair of coordinates
	public double findDistance(Robot me, int x, int y) {
		int xDistance = x - me.x;
		int yDistance = y - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}

	//Pilgrims run away when they see dangerous enemies TODO: fix this
	public Action pilgrimRunAway() {
		HashSet<Robot> nearbyEnemies = this.findBadGuys();
		Robot closestEnemy = this.findClosestThreat(nearbyEnemies);
		return this.move(this.me.x - closestEnemy.x, this.me.y - closestEnemy.y); //replace with our move/pathing method later
	}

	//Finds all ally robots in vision range
	public HashSet<Robot> findGoodGuys() {
		HashSet<Robot> theGoodGuys = new HashSet<Robot>();
		Robot[] visibleBots = getVisibleRobots();
		for (int i = 0; i < visibleBots.length; i++) {
			if (me.team == visibleBots[i].team) {
				theGoodGuys.add(visibleBots[i]);
			}
		}
		return theGoodGuys;
	}

	//Finds all enemy robots in vision range
	public HashSet<Robot> findBadGuys() {
		HashSet<Robot> theBadGuys = new HashSet<Robot>();
		Robot[] visibleBots = getVisibleRobots();
		for (int i = 0; i < visibleBots.length; i++) {
			if (me.team != visibleBots[i].team) {
				theBadGuys.add(visibleBots[i]);
			}
		}
		return theBadGuys;
	}

	//Finds closest enemy robot
	public Robot findPrimaryEnemyDistance(HashSet<Robot> potentialEnemies) {
		double distance = Double.MAX_VALUE;
		Robot closeBot = null;
		Iterator<Robot> badGuyIter = potentialEnemies.iterator();
		while(badGuyIter.hasNext()) {
			Robot aBadGuy = badGuyIter.next();
			double badGuyDistance = findDistance(me, aBadGuy);
			//			log("Distance: "+badGuyDistance);
			if(badGuyDistance < distance) {
				//				log("Found closer robot");
				distance = badGuyDistance;
				//				log("New closest distance: "+distance);
				closeBot = aBadGuy;
			}

		}
		//		if(closeBot==null) {
		//			log("Still null boo");
		//		} else {
		//			log("You the man");
		//		}
		return closeBot;
	}

	//finds the closest enemy with priority by type
	public Robot findPrimaryEnemyTypeDistance(HashSet<Robot> potentialEnemies) {
		HashMap<Integer, HashSet<Robot>> groupedEnemies = groupByType(potentialEnemies);
		if (!groupedEnemies.get(SPECS.PREACHER).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PREACHER));
		}
		if (!groupedEnemies.get(SPECS.CRUSADER).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CRUSADER));
		}
		if (!groupedEnemies.get(SPECS.PILGRIM).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PILGRIM));
		}
		if (!groupedEnemies.get(SPECS.CASTLE).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CASTLE));
		}
		if (!groupedEnemies.get(SPECS.CHURCH).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CHURCH));
		}
		return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PROPHET));
	}

	//finds the closest enemy that can attack
	public Robot findClosestThreat(HashSet<Robot> potentialEnemies) {
		HashMap<Integer, HashSet<Robot>> groupedEnemies = groupByType(potentialEnemies);
		if (!groupedEnemies.get(SPECS.PREACHER).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PREACHER));
		}
		if (!groupedEnemies.get(SPECS.CRUSADER).isEmpty()) {
			return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CRUSADER));
		}
		return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PROPHET));
	}

	//checks if the robot is in attack range
	public boolean canAttack(double distance) {
		return this.getMinAttackRangeRadius(this.me.unit) <= distance && distance <= this.getMaxAttackRangeRadius(this.me.unit);
	}

	//finds the optimal place for preachers to attack (for AoE to be most effective) //TODO: finish this
	public AttackAction preacherAttack() {
		int maxAttackRange = this.getMaxAttackRangeRadius(this.me.unit);
		int dx = 0, dy = 0;
		int[][] weightedMap = new int[this.mapYSize][this.mapXSize]; //new int[this.getVisionRangeRadius(this.me.unit)*2+1][this.getVisionRangeRadius(this.me.unit)*2+1]; //TODO: size of vision radius. need to shift if I want to make this smaller matrix
		for (int r = Math.max(this.me.y - maxAttackRange, 1); r < Math.min(this.me.y + maxAttackRange, this.mapYSize-1); r++) {
			for (int c = Math.max(this.me.x - maxAttackRange, 1); c < Math.min(this.me.x + maxAttackRange, this.mapYSize-1); c++) {
				weightedMap[r][c] = this.averageAdjacent(this.visibleRobotMap, r, c);
			}
		}
		return this.attack(dx, dy);
	}

	//finds the average of a 3x3 area
	public int averageAdjacent(int[][] area, int x, int y) {
		int sum = 0;
		for (int i = -1; i < 2; i++) {
			for (int j = -1; j < 2; j++) {
				sum += area[x + i][y + j];
			}
		}
		return sum/9;
	}

	public AttackAction preacherAttack(HashSet<Robot> potentialEnemies) {
		Robot targetBadGuy = this.findPrimaryEnemyDistance(potentialEnemies);
		while(targetBadGuy == null && potentialEnemies.size() > 0) {
			potentialEnemies.remove(targetBadGuy);
			targetBadGuy = findPrimaryEnemyDistance(potentialEnemies);
		}
		if(potentialEnemies.size() == 0) {
			return null;
		}
		int xDistance = targetBadGuy.x - me.x;
		int yDistance = targetBadGuy.y - me.y;
		double absoluteXDistance = Math.abs(xDistance);
		double absoluteYDistance = Math.abs(yDistance);
		double radianAngle;
		double piHalf = Math.PI / 2;
		double piEight = Math.PI / 8;
		double piThreeEight = piEight * 3;
		String optimalDirection = "";
		if (xDistance >= 0 && yDistance <= 0) {
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHEAST";
			}
		} else if (xDistance <= 0 && yDistance <= 0) {
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHWEST";
			}
		} else if (xDistance <= 0 && yDistance >= 0) {
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHWEST";
			}
		} else if (xDistance >= 0 && yDistance >= 0) {
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHEAST";
			}
		}
		//		int xDistance=targetBadGuy.x-me.x;
		//		int yDistance=targetBadGuy.y-me.y;
		AttackAction possibleAction;
		if(optimalDirection=="NORTH") {
			try {
				possibleAction=attack(0, -4);
				if(possibleAction != null) {
					return attack(0,-4);
				}
			} catch (Exception e) {

			}
		} else if(optimalDirection=="NORTHEAST") {
			try {
				possibleAction=attack(2, -3);
				if(possibleAction != null) {
					return attack(2,-3);
				}
			} catch (Exception e) {

			}

		} else if(optimalDirection=="EAST") {
			try {
				possibleAction=attack(4, 0);
				if(possibleAction != null) {
					return attack(4,0);
				}
			} catch (Exception e) {

			}
		} else if(optimalDirection=="SOUTHEAST") {
			try {
				possibleAction=attack(3, 2);
				if(possibleAction != null) {
					return attack(3,2);
				}
			} catch (Exception e) {

			}
		} else if(optimalDirection=="SOUTH") {
			try {
				possibleAction=attack(0, 4);
				if(possibleAction != null) {
					return attack(0,4);
				}
			} catch (Exception e) {

			}
		} else if(optimalDirection=="SOUTHWEST") {
			try {
				possibleAction=attack(-2, 3);
				if(possibleAction != null) {
					return attack(-2, 3);
				}
			} catch (Exception e) {

			}
		} else if(optimalDirection=="WEST") {
			try {
				possibleAction=attack(-4, 0);
				if(possibleAction != null) {
					return attack(-4,0);
				}
			} catch (Exception e) {

			}
		} else if(optimalDirection=="NORTHWEST") {
			try {
				possibleAction=attack(-3, -2);
				if(possibleAction != null) {
					return attack(-3,-2);
				}
			} catch (Exception e) {

			}
		}
		return null;
	}

	//groups enemies by type
	public HashMap<Integer, HashSet<Robot>> groupByType(HashSet<Robot> potentialEnemies) {
		HashMap<Integer, HashSet<Robot>> groupedEnemies = new HashMap<Integer, HashSet<Robot>>();
		groupedEnemies.put(SPECS.CRUSADER, new HashSet<Robot>());
		groupedEnemies.put(SPECS.PREACHER, new HashSet<Robot>());
		groupedEnemies.put(SPECS.PROPHET, new HashSet<Robot>());
		groupedEnemies.put(SPECS.PILGRIM, new HashSet<Robot>());
		groupedEnemies.put(SPECS.CASTLE, new HashSet<Robot>());
		groupedEnemies.put(SPECS.CHURCH, new HashSet<Robot>());
		Iterator<Robot> iter = potentialEnemies.iterator();
		Robot badGuy;
		while (iter.hasNext()) {
			badGuy = iter.next();
			groupedEnemies.get(badGuy.unit).add(badGuy);
		}
		return groupedEnemies;
	}

	//can this unit be built? do we have enough fuel and karbonite?
	public boolean canBuild(int type) {
		return this.fuel >= SPECS.UNITS[type].CONSTRUCTION_FUEL && this.karbonite >= SPECS.UNITS[type].CONSTRUCTION_KARBONITE && this.checkAdjacentAvailable()!=null;
	}

	//gets the movement speed radius of a unit
	public int getMovementRangeRadius(int unit) {
		return SPECS.UNITS[unit].SPEED;
	}

	//gets the minimum attack range radius of a unit
	public int getMinAttackRangeRadius(int unit) {
		return SPECS.UNITS[unit].ATTACK_RADIUS[0];
	}

	//gets the maximum attack range radius of a unit
	public int getMaxAttackRangeRadius(int unit) {
		return SPECS.UNITS[unit].ATTACK_RADIUS[1];
	}

	//gets the vision range radius of a unit
	public int getVisionRangeRadius(int unit) {
		return SPECS.UNITS[unit].VISION_RADIUS;
	}
	
		public MoveAction preacherMovesOutOfTheWay() {
		MoveAction maybe=move(1,1);
		if(maybe!=null) {
			return maybe;
		}
		maybe=move(-1,-1);
		if(maybe!=null) {
			return maybe;
		}
		maybe=move(1,-1);
		if(maybe!=null) {
			return maybe;
		}maybe=move(-1,1);
		if(maybe!=null) {
			return maybe;
		}
		maybe=move(1,0);
		if(maybe!=null) {
			return maybe;
		}
		maybe=move(-1,0);
		if(maybe!=null) {
			return maybe;
		}
		maybe=move(0,1);
		if(maybe!=null) {
			return maybe;
		}
		maybe=move(0,-1);
		if(maybe!=null) {
			return maybe;
		}
		return null;
	}
	
		public MoveAction pathFind(Point finalLocation) {
		// this.log("moving toward x=" + finalLocation[0] + " y=" + finalLocation[1]);
		if (fuel <= 30 || finalLocation.getX() == -1) { // not enough fuel, or -1 b/c can't find karbo or fuel
			// this.log("cannot move");
			return null;
		}
		int xDistance = finalLocation.getX() - me.x;
		int yDistance = finalLocation.getY() - me.y;
		try {
			return move(xDistance,yDistance);
		} catch (Exception e) {
			
		}
		int quadrant;
		double absoluteXDistance = Math.abs(xDistance);
		double absoluteYDistance = Math.abs(yDistance);
		double radianAngle;
		double piHalf = Math.PI / 2;
		double piEight = Math.PI / 8;
		double piThreeEight = piEight * 3;
		String optimalDirection = "";
		if (xDistance >= 0 && yDistance <= 0) {
			quadrant = 1;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHEAST";
			}
		} else if (xDistance <= 0 && yDistance <= 0) {
			quadrant = 2;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHWEST";
			}
		} else if (xDistance <= 0 && yDistance >= 0) {
			quadrant = 3;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHWEST";
			}
		} else if (xDistance >= 0 && yDistance >= 0) {
			quadrant = 4;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHEAST";
			}
		}
		// Figure out what quadrant it is
		// Based on the quadrant, find the angle
		// Based on the angle, determine the optimal direction
		// See if you can move in that direction
		// If you can move, check to make sure it doesn't line up with previous
		// locations
		// Move, and update the previous location list
		// See if you can jump
		// If you can move, check to make sure it doesn't line up with previous
		// locations
		// Move, and update the previous location list
		// Switch directions and repeat this whole process
		if (me.unit == SPECS.CRUSADER) {
			for (int i = 0; i < rotationTries.length; i++) {
				int index = (directions.indexOf(optimalDirection) + i) % 8;
				if (index == 0) {
					try {
						move(0,-3);
					} catch (Exception e) {
						try {
							move(0,-2);
						} catch (Exception ef) {
							try {
								move(0,-1);
							} catch (Exception eg) {
								
							}
						}
					}

				} else if (index == 1) {
					try {
						move(2,-2);
					} catch (Exception e) {
						try {
							move(1,-1);
						} catch (Exception ef) {
							try {
								move(2,-1);
							} catch (Exception eg) {
								try {
									move(1,-2);
								} catch (Exception egh) {
									
								}
							}
						}
					}
					
				} else if (index == 2) {
					try {
						move(3,0);
					} catch (Exception e) {
						try {
							move(2,0);
						} catch (Exception ef) {
							try {
								move(1,0);
							} catch (Exception eg) {
								
							}
						}
					}
				} else if (index == 3) {
					try {
						move(2,2);
					} catch (Exception e) {
						try {
							move(1,1);
						} catch (Exception ef) {
							try {
								move(2,1);
							} catch (Exception eg) {
								try {
									move(1,2);
								} catch (Exception egh) {
									
								}
							}
						}
					}
				} else if (index == 4) {
					try {
						move(0,3);
					} catch (Exception e) {
						try {
							move(0,2);
						} catch (Exception ef) {
							try {
								move(0,1);
							} catch (Exception eg) {
								
							}
						}
					}
				} else if (index == 5) {
					try {
						move(-2,2);
					} catch (Exception e) {
						try {
							move(-1,1);
						} catch (Exception ef) {
							try {
								move(-2,1);
							} catch (Exception eg) {
								try {
									move(-1,2);
								} catch (Exception egh) {
									
								}
							}
						}
					}
				} else if (index == 6) {
					try {
						move(-3,0);
					} catch (Exception e) {
						try {
							move(-2,0);
						} catch (Exception ef) {
							try {
								move(-1,0);
							} catch (Exception eg) {
								
							}
						}
					}
				} else if (index == 7) {
					try {
						move(-2,-2);
					} catch (Exception e) {
						try {
							move(-1,-1);
						} catch (Exception ef) {
							try {
								move(-2,-1);
							} catch (Exception eg) {
								try {
									move(-1,-2);
								} catch (Exception egh) {
									
								}
							}
						}
					}
				}
			}
		} else {
			for (int i = 0; i < rotationTries.length; i++) {
				int index = (directions.indexOf(optimalDirection) + i) % 8;
				if (index == 0) {
					try {
						move(0,-2);
					} catch (Exception ef) {
						try {
							move(0,-1);
						} catch (Exception eg) {
							
						}
					}

				} else if (index == 1) {
					try {
						move(1,-1);
					} catch (Exception ef) {
						
					}
				} else if (index == 2) {
					try {
						move(2,0);
					} catch (Exception ef) {
						try {
							move(1,0);
						} catch (Exception eg) {
							
						}
					}
				} else if (index == 3) {
					try {
						move(1,1);
					} catch (Exception ef) {
						
					}
				} else if (index == 4) {
					try {
						move(0,2);
					} catch (Exception ef) {
						try {
							move(0,1);
						} catch (Exception eg) {
							
						}
					}
				} else if (index == 5) {
					try {
						move(-1,1);
					} catch (Exception ef) {
						
					}
				} else if (index == 6) {
					try {
						move(-2,0);
					} catch (Exception ef) {
						try {
							move(-1,0);
						} catch (Exception eg) {
							
						}
					}
				} else if (index == 7) {
					try {
						move(-1,1);
					} catch (Exception ef) {
						
					}
				}
			}
		}
		return null;
	}

}
