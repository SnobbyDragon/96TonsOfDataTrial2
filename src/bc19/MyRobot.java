package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.stream.Collectors;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public int[] rotationTries;
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public ArrayList<String> directions;
	public ArrayList<Integer> previousLocations;
	public boolean haveCastle; 
	public int[] castleLocation;
    public HashMap<String, Integer> bots;
    public int castleNum;
	
	public Action turn() {
		turn++;
		if (turn == 1)
		{
			log("turn: " + turn);
			rotationTries = new int[]{ 0, -1, 1, -2, 2, -3, 3 };
			passableMap = getPassableMap();
			visibleRobotMap = getVisibleRobotMap();
			karboniteMap = getKarboniteMap();
			fuelMap = getFuelMap();
			directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
			previousLocations = new ArrayList<Integer>();
			haveCastle = false;
			castleLocation = new int[2];
			bots = new HashMap<String, Integer>();
			if (bots.get("pilgrims") == null)
			bots.put("pilgrims", 0);
			if (bots.get("crusaders") == null)
			bots.put("crusaders", 0);
			if (bots.get("preachers") == null)
			bots.put("preachers", 0);
			castleNum = 0;
		}
			if (me.unit == SPECS.CASTLE) {
				log("karb: " + karbonite + " fuel: " + fuel);
				log("pilgrim number: " + bots.get("pilgrims"));
				if (turn ==1)
				{
				castleNum++;
				log(castleNum + "");
				}
				if (bots.get("pilgrims") != 5 + castleNum) {
					if (this.canBuild(SPECS.PILGRIM))  {
						log("built pilgrim at x=" + this.checkAdjacentAvailable()[0] + " y=" + this.checkAdjacentAvailable()[1] + "\ncastle at x=" + this.me.x + " y=" + this.me.y);
						log("castle num:" + castleNum);
						bots.put("pilgrims", bots.get("pilgrims") + 1);
						return this.makeUnit(SPECS.PILGRIM);
					}
				}
				if (bots.get("crusaders") != 10 + castleNum*2)
					if (this.canBuild(SPECS.CRUSADER)) {
						log("built crusader");
						bots.put("crusaders", bots.get("crusaders") + 1);
						return this.makeUnit(SPECS.CRUSADER);
					}
				if (bots.get("preachers") != castleNum*3)
					if(this.canBuild(SPECS.PREACHER)) {
						log("built preacher");
						bots.put("preachers", bots.get("preachers") + 1);
						return this.makeUnit(SPECS.PREACHER);
					}
				log("didnt build anything");
			}
		if (me.unit == SPECS.PILGRIM) {
			if (me.health <= 0)
			{
				bots.put("pilgrims", bots.get("pilgrims") - 1);
			}
			log("My karbonite: "+me.karbonite);
			log("My fuel: "+me.fuel);
			log("Have Castle: "+haveCastle);
			log("My Castle X: "+castleLocation[0]);
			log("My Castle Y: "+castleLocation[1]);
			if(!haveCastle) {
				if(locateNearbyCastle(me)) {
					haveCastle=true;
				}
			}
			int[] karboniteLocationFind=searchForKarboniteLocation();
			int[] fuelLocationFind=searchForFuelLocation();
			if(canMineKarbonite(me)||canMineFuel(me)) {
				return mine();
			}
			if(canGiveStuff(me)) {
				int xCastle=castleLocation[0]-me.x;
				int yCastle=castleLocation[1]-me.y;
				return give(xCastle,yCastle,me.karbonite,me.fuel);
			}
			if(me.karbonite==20) {
				return pathFind(me,castleLocation);
			} else {
				if(findDistance(me,karboniteLocationFind[0],karboniteLocationFind[1])>=findDistance(me,fuelLocationFind[0],fuelLocationFind[1])) {
					return pathFind(me,fuelLocationFind);
				} else {
					return pathFind(me,karboniteLocationFind);
				}
			}
			/*if(canMineFuel(me)||canMineKarbonite(me)) {
				return mine();
			}
			if (!haveCastle) {
				if(locateNearbyCastle(me)) {
					haveCastle=true;
				}
				// log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
			}
			if(canGiveStuff(me)) {
				int xCastle=castleLocation[0]-me.x;
				int yCastle=castleLocation[1]-me.y;
				return give(xCastle,yCastle,me.karbonite,me.fuel);
			}
			if(me.karbonite==20||me.fuel==100) {
				return pathFind(me,castleLocation);
			} else {
				int[] closestKarbonite=searchForKarboniteLocation();
				int[] closestFuel=searchForFuelLocation();
				if(findDistance(me,closestKarbonite[0],closestKarbonite[1])>=findDistance(me,closestFuel[0],closestFuel[1])) {
					return pathFind(me,closestFuel);
				} else {
					return pathFind(me,closestKarbonite);
				}
				
				
			}*/
		}
		
		
		if (me.unit == SPECS.CRUSADER) {
			if (me.health <= 0)
			{
				bots.put("crusaders", bots.get("crusaders") - 1);
			}
			Robot enemy = this.findPrimaryEnemyType(this.findBadGuys());
			this.attack(enemy.x, enemy.y);
		}
		
		
		
		if(me.unit == SPECS.PREACHER)
		{
			if (me.health <= 0)
			{
				bots.put("preachers", bots.get("preachers") - 1);
			}
			try {
				if(fuel>=15)
				{
				Robot enemy = this.findPrimaryEnemyDistance(this.findBadGuys());
				return this.attack(enemy.x, enemy.y);
				}
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	return null;
}

	public boolean canGiveStuff(Robot me) {
		int absoluteXCastleDistance=Math.abs(castleLocation[0]-me.x);
		int absoluteYCastleDistance=Math.abs(castleLocation[1]-me.y);
		if(absoluteXCastleDistance==0||absoluteXCastleDistance==1) {
			if(absoluteYCastleDistance==0||absoluteYCastleDistance==1) {
				return true;
			}
		}
		return false;
	}
	
	
	public boolean locateNearbyCastle(Robot me) {
		Robot[] visibleRobots=getVisibleRobots();
		for(int i=0;i<visibleRobots.length;i++) {
			if(visibleRobots[i].unit==SPECS.CASTLE&&visibleRobots[i].team==me.team) {
				castleLocation[0]=visibleRobots[i].x;
				castleLocation[1]=visibleRobots[i].y;
				return true;
			}
		}
		return false;
	}
	
	
	public boolean canMineKarbonite(Robot me) {
		if(karboniteMap[me.x][me.y]==true&&me.karbonite<20) {
			return true;
		}
		return false;
	}

	public boolean canMineFuel(Robot me) {
		if(fuelMap[me.x][me.y]==true&&me.fuel<100) {
			return true;
		}
		return false;
	}

	public double findDistance(double xDistance, double yDistance) {
		return Math.pow(xDistance, 2)+Math.pow(yDistance, 2);
	}
	
	public MoveAction pathFind(Robot me, int[] finalLocation) {
		if (fuel <= 30) {
			return null;
		}
		int xDistance = finalLocation[0] - me.x;
		int yDistance = finalLocation[1] - me.y;
		double officialDistance=findDistance(xDistance,yDistance);
		if(xDistance==0&&yDistance==0) {
			return null;
		}
		log("X distance: "+xDistance);
		log("Y distance: "+yDistance);
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
		log("Optimal Direction: "+optimalDirection);
		for(int i=0;i<rotationTries.length;i++) {
			int index=(directions.indexOf(optimalDirection)+i)%8;
			if(index==0) {
				if(officialDistance==1) {
					try {
						return move(0,-1);
					} catch (Exception d) {
						
					}
				}
				try {
				return move(0,-2);
				} catch (Exception e) {
					try {
						return move(0,-1);
					} catch (Exception f) {
						
					}
				}
			} else if(index==1) {
				try {
				return move(1,-1);
				} catch (Exception e) {
									
				}
			} else if(index==2) {
				if(officialDistance==1) {
					try {
						return move(1,0);
					} catch (Exception d) {
						
					}
				}
				try {
				return move(2,0);
				} catch (Exception e) {
					try {
						return move(1,0);
					} catch (Exception f) {
						
					}
				}
			} else if(index==3) {
				try {
				return move(1,1);
				} catch (Exception e) {
					
				}
			} else if(index==4) {
				if(officialDistance==1) {
					try {
						return move(0,1);
					} catch (Exception d) {
						
					}
				}
				try {
				return move(0,2);
				} catch (Exception e) {
					try {
						return move(0,1);
					} catch (Exception f) {
						
					}
				}
			} else if(index==5) {
				try {
				return move(-1,1);
				} catch (Exception e) {
					
				}
			} else if(index==6) {
				if(officialDistance==1) {
					try {
						return move(-1,0);
					} catch (Exception d) {
						
					}
				}
				try {
				return move(-2,0);
				} catch (Exception e) {
					try {
						return move(-1,0);
					} catch (Exception f) {
						
					}
				}
			} else if(index==7) {
				try {
				return move(-1,-1);
				} catch (Exception e) {
					
				}
			}
		}
		return null;
	}
	
	public boolean canMove(Robot me, int finalX, int finalY) {
		if (passableMap[finalX][finalY] == false) {
			return false;
		}
		if (visibleRobotMap[finalX][finalY] == 0) {
			return true;
		}
		return false;
	}
	
	public boolean canMove(int finalX, int finalY)
	{
		if (passableMap[finalX][finalY] == false) {
			return false;
		}
		if (visibleRobotMap[finalX][finalY] == 0) {
			return true;
		}
		return false;	
	}

	public boolean alreadyBeenHere(Robot me, int dx, int dy) {
		boolean alreadyOccupied=false;
		for(int prev=0;prev<previousLocations.size();prev+=2) {
			if(me.x+dx==previousLocations.get(prev)&&me.y+dy==previousLocations.get(prev+1)) {
				alreadyOccupied=true;
			}
		}
		if(alreadyOccupied==false) {
			if(previousLocations.size()>=6) {
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

	
	public int[] checkAdjacentPassable() {
		int x = this.me.x;
		int y = this.me.y;
		if (x > 0) { //can check left
			if (y > 0) { //can check up
				if (this.map[y-1][x-1]) { //checks up left
					return new int[]{x-1, y-1};
				}
			}
			if (this.map[y][x-1]) { //checks middle left
				return new int[]{x-1, y};
			}
			if (y < this.map.length - 1) { //can check down
				if (this.map[y+1][x-1]) { //checks down left
					return new int[]{x-1, y+1};
				}
			}
		}
		if (y > 0) { //can check up
			if (this.map[y-1][x]) { //checks middle up
				return new int[]{x, y-1};
			}
		}
		if (y < this.map.length - 1) { //can check down
			if (this.map[y+1][x]) { //checks middle down
				return new int[]{x, y+1};
			}
		}
		if (x < this.map[0].length - 1) { //can check right
			if (y > 0) { //can check up
				if (this.map[y-1][x+1]) { //checks up right
					return new int[]{x+1, y-1};
				}
			}
			if (this.map[y][x+1]) { //checks middle right
				return new int[]{x+1, y};
			}
			if (y < this.map.length - 1) { //can check down
				if (this.map[y+1][x+1]) { //checks down right
					return new int[]{x+1, y+1};
				}
			}
		}
		return new int[]{x, y}; //surrounded by impassable terrain
	}
	
	
	public int[] searchForKarboniteLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		for (int i = 0; i < karboniteMap.length; i++) {
			for (int j = 0; j < karboniteMap[i].length; j++) {
				double distance = findDistance(me, i, j);
				if (distance < minDistance) {
					minDistance = distance;
					minXCoordinate = i;
					minYCoordinate = j;
				}
			}
		}
		int[] location = new int[2];
		location[0] = minXCoordinate;
		location[1] = minYCoordinate;
		return location;
	}

	
	public int[] searchForFuelLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		for (int i = 0; i < fuelMap.length; i++) {
			for (int j = 0; j < fuelMap[i].length; j++) {
				double distance = findDistance(me, i, j);
				if (distance < minDistance) {
					minDistance = distance;
					minXCoordinate = i;
					minYCoordinate = j;
				}
			}
		}
		int[] location = new int[2];
		location[0] = minXCoordinate;
		location[1] = minYCoordinate;
		return location;
	}

	
	public Action makePilgrims() {
		int[] spot = checkAdjacentPassable();
		return buildUnit(SPECS.PILGRIM, spot[0], spot[1]);
	}

	public Action makeCrusaders() {
		int[] spot = checkAdjacentPassable();
		return buildUnit(SPECS.CRUSADER, spot[0], spot[1]);
	}
//I THINK ITS SENSE NEARBY ALLIES
	/*
	public List<Robot> senseNearbyAllies() {
		List<Robot> nearbyRobots = new ArrayList<Robot>(Arrays.asList(this.getVisibleRobots()));
		List<Robot> allies = nearbyRobots.stream().filter(robot -> robot.team == this.me.team).collect(Collectors.toList());
		return allies;
	}
*/
	//	public List<Robot> senseNearbyEnemies() {
	//		List<Robot> nearbyRobots = new ArrayList<Robot>(Arrays.asList(this.getVisibleRobots()));
	//		List<Robot> enemies = nearbyRobots.stream().filter(robot -> robot.team != this.me.team).collect(Collectors.toList());
	//		return enemies;
	//	}

	//	public Robot findClosestRobot(List<Robot> robots) {
	//		int leastDistance = 65;
	//		int distance;
	//		int index = 0;
	//		for (int i = 0; i < robots.size(); i++) {
	//			distance = findDiscreteDistance(robots.get(i).x, robots.get(i).y);
	//			if (leastDistance > distance) {
	//				index = i;
	//				leastDistance = distance;
	//			}
	//		}
	//		return robots.get(index);
	//	}

	//	public int findDiscreteDistance(int x, int y) { //calculates distance between this robot and another point (distance = number of moves)
	//		int dx = Math.abs(this.me.x - x);
	//		int dy = Math.abs(this.me.y - y);
	//		return dx + dy;
	//	}

	
	public Action pilgrimRunAway() {
		HashSet<Robot> nearbyEnemies = this.findBadGuys();
		Robot closestEnemy = this.findPrimaryEnemyDistance(nearbyEnemies);
		int x = 0, y = 0;
		x -= this.me.x - closestEnemy.x;
		y -= this.me.y - closestEnemy.y;
		return this.move(x, y); //replace with our move/pathing method later
	}
	

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

	public Robot findPrimaryEnemyHealth(HashSet<Robot> potentialEnemies) {
		int lowestHealth = Integer.MAX_VALUE;
		Robot weakestBot = null;
		Iterator<Robot> iter = potentialEnemies.iterator();
		while (iter.hasNext()) {
			Robot badGuy = iter.next();
			if (canAttack(findDistance(me, badGuy))) {
				if (lowestHealth > badGuy.health) {
					lowestHealth = badGuy.health;
					weakestBot = badGuy;
				}
			}
		}
		return weakestBot;
	}

	// Finds distance between two robots
	public double findDistance(Robot me, Robot opponent) {
		int xDistance = opponent.x - me.x;
		int yDistance = opponent.y - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}

	// Finds distance between a robot and a pair of coordinates
	public double findDistance(Robot me, int x, int y) {
		int xDistance = x - me.x;
		int yDistance = y - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}

	public boolean canAttack(double distance) {
		return this.getMinAttackRangeRadius() <= distance && distance <= this.getMaxAttackRangeRadius();
	}

	public Robot findPrimaryEnemyDistance(HashSet<Robot> potentialEnemies) {
		double closestDistance = Double.MAX_VALUE;
		Robot closestBot = null;
		Iterator<Robot> iter = potentialEnemies.iterator();
		while (iter.hasNext()) {
			Robot badGuy = iter.next();
			double distance = findDistance(me, badGuy);
			if (canAttack(distance)) {
				if (closestDistance > distance) {
					closestDistance = distance;
					closestBot = badGuy;
				}
			}
		}
		return closestBot;
	}

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

	
	public Robot findPrimaryEnemyType(HashSet<Robot> potentialEnemies) {
		HashMap<Integer, HashSet<Robot>> groupedEnemies = groupByType(potentialEnemies);
		if (!groupedEnemies.get(SPECS.PREACHER).isEmpty()) {
			return findPrimaryEnemyHealth(groupedEnemies.get(SPECS.PREACHER));
		}
		if (!groupedEnemies.get(SPECS.CRUSADER).isEmpty()) {
			return findPrimaryEnemyHealth(groupedEnemies.get(SPECS.CRUSADER));
		}
		if (!groupedEnemies.get(SPECS.PILGRIM).isEmpty()) {
			return findPrimaryEnemyHealth(groupedEnemies.get(SPECS.PILGRIM));
		}
		if (!groupedEnemies.get(SPECS.CASTLE).isEmpty()) {
			return findPrimaryEnemyHealth(groupedEnemies.get(SPECS.CASTLE));
		}
		if (!groupedEnemies.get(SPECS.CHURCH).isEmpty()) {
			return findPrimaryEnemyHealth(groupedEnemies.get(SPECS.CHURCH));
		}
		return findPrimaryEnemyHealth(groupedEnemies.get(SPECS.PROPHET));
	}

	public int getMovementRangeRadius() {
		return (int)Math.sqrt(SPECS.UNITS[this.me.unit].SPEED);
	}

	public int getMinAttackRangeRadius() {
		return (int)Math.sqrt(SPECS.UNITS[this.me.unit].ATTACK_RADIUS[0]);
	}

	public int getMaxAttackRangeRadius() {
		return (int)Math.sqrt(SPECS.UNITS[this.me.unit].ATTACK_RADIUS[1]);
	}
	public boolean canBuild(int type) {
		return this.fuel >= SPECS.UNITS[type].CONSTRUCTION_FUEL && this.karbonite >= SPECS.UNITS[type].CONSTRUCTION_KARBONITE && this.checkAdjacentAvailable()!=null;
	}
	public Action makeUnit(int type) {
		int[] spot = this.checkAdjacentAvailable();
		return this.buildUnit(type, spot[0] - this.me.x, spot[1] - this.me.y);
	}
	public int[] checkAdjacentAvailable() {
		visibleRobotMap = this.getVisibleRobotMap();
		int x = this.me.x;
		int y = this.me.y;
		if (x > 0) { //can check left
			if (y > 0) { //can check up
				if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
					return new int[]{x-1, y-1};
				}
			}
			if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
				return new int[]{x-1, y};
			}
			if (y < this.passableMap.length - 1) { //can check down
				if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
					return new int[]{x-1, y+1};
				}
			}
		}
		if (y > 0) { //can check up
			if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
				return new int[]{x, y-1};
			}
		}
		if (y < this.passableMap.length - 1) { //can check down
			if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
				return new int[]{x, y+1};
			}
		}
		if (x < this.passableMap[0].length - 1) { //can check right
			if (y > 0) { //can check up
				if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
					return new int[]{x+1, y-1};
				}
			}
			if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
				return new int[]{x+1, y};
			}
			if (y < this.passableMap.length - 1) { //can check down
				if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
					return new int[]{x+1, y+1};
				}
			}
		}
		return null; //surrounded by impassable terrain
	}

}
