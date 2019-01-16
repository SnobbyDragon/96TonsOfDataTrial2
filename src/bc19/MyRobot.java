package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public int mapYSize, mapXSize; //size of the map, length y and length x
	public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
	public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
	public boolean haveCastle = false;
	public int[] castleLocation = new int[2];
	public HashMap<String, Integer> bots = new HashMap<String, Integer>();
	public int[] crusaderTarget = new int[2];
	public boolean crusadeMode = false;
	public int crusadeTurns = 0;

	public Action turn() {
		turn++;
		if (turn == 1)
		{
			//gets maps
			passableMap = getPassableMap();
			karboniteMap = getKarboniteMap();
			fuelMap = getFuelMap();
			mapYSize = passableMap.length;
			mapXSize = passableMap[0].length;
			//first target center of the map
			crusaderTarget[0] = mapXSize/2;
			crusaderTarget[1] = mapYSize/2;

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
//		if (this.crusadeMode) {
//			this.crusadeTurns++;
//			this.log(this.crusaderTarget[0] + " " + this.crusaderTarget[1]);
//		}
		if (me.unit == SPECS.CASTLE) { //castle
			if (bots.get("pilgrims") < 5) { //build 5 pilgrims
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
//			log("My Castle X: "+castleLocation[0]);
//			log("My Castle Y: "+castleLocation[1]);
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
				int xCastle = castleLocation[0] - this.me.x;
				int yCastle = castleLocation[1] - this.me.y;
				return give(xCastle,yCastle,me.karbonite,me.fuel);
			}
			if (haveCastle && (me.karbonite==20||me.fuel==100)) {
//				this.log("returning to castle");
				return pathFind(castleLocation);
			}
			else {
				int[] closestKarbonite = searchForKarboniteLocation();
				int[] closestFuel = searchForFuelLocation();
//				this.log("pilgrim at x=" + this.me.x + " y=" + this.me.y + "\nkarbo at x=" + closestKarbonite[0] + " y=" + closestKarbonite[1] + "\nfuel at x=" + closestFuel[0] + " y=" + closestFuel[1]);
				if (crusadeMode || findDistance(me,closestKarbonite[0],closestKarbonite[1]) >= findDistance(me,closestFuel[0],closestFuel[1])) {
					return pathFind(closestFuel);
				}
				else {
					return pathFind(closestKarbonite);
				}
			}
		}
		if (me.unit == SPECS.CRUSADER) { //crusader
			//move crusade target every 50 turns
//			this.log(crusaderTarget[0] + " " + crusaderTarget[1]);
			this.setCrusadeTarget(50);
			if (fuel >= 10) {
				HashSet<Robot> enemies = findBadGuys();
				if (enemies.size() == 0 && this.fuel > 300) {
					return pathFind(crusaderTarget);
				}
//				log("Enemies size: "+enemies.size());
				Robot closeBadGuy = findBadGuy(enemies);
				try {
//					log("Bad guy's health: " + closeBadGuy.health);
//					log("Other bad guy data " + closeBadGuy.x);
					return attack(closeBadGuy.x-me.x,closeBadGuy.y-me.y);
				} catch (Exception e) {
//					log("Can't attack the man");
					try {
						int[] closeBadGuyLocation=new int[2];
						closeBadGuyLocation[0]=closeBadGuy.x;
						closeBadGuyLocation[1]=closeBadGuy.y;
//						log("X coor bad: "+closeBadGuyLocation[0]);
//						log("Y coor bad: "+closeBadGuyLocation[1]);
						return pathFind(closeBadGuyLocation);
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
//				if (this.findDistance(this.me, this.castleLocation[0], this.castleLocation[1]) == 1) {
//					return this.move(this.me.x - this.castleLocation[0], this.me.y - this.castleLocation[1]);
//				}
//			}
		}
		if(me.unit==SPECS.PREACHER) { //preacher
			if (!this.haveCastle) {
				this.locateNearbyCastle();
			}
			if (this.haveCastle && this.isAdjacentToCastle()) { //get out of the way
				
			}
			if (fuel >= 15) {
				HashSet<Robot> enemies = findBadGuys();
				Robot targetBadGuy = findPrimaryEnemyHealth(enemies);
				try {
					return attack(targetBadGuy.x-me.x,targetBadGuy.y-me.y);
				} catch (Exception e) {

				}
			}
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
	
	//TODO update this
	public void setCrusadeTarget(int interval) {
//		this.log(this.crusadeTurns + "");
		if (this.turn == interval) { //up
//			this.log("up crusade target");
			this.crusaderTarget[0] = this.mapXSize/2;
			this.crusaderTarget[1] = this.mapYSize/4;
		}
		else if (this.turn == 2*interval) { //up right
			this.crusaderTarget[0] = this.mapXSize*3/4;
			this.crusaderTarget[1] = this.mapYSize/4;
		}
		else if (this.turn == 3*interval) { //right
			this.crusaderTarget[0] = this.mapXSize*3/4;
			this.crusaderTarget[1] = this.mapYSize/2;
		}
		else if (this.turn == 4*interval) { //down right
			this.crusaderTarget[0] = this.mapXSize*3/4;
			this.crusaderTarget[1] = this.mapYSize*3/4;
		}
		else if (this.turn == 5*interval) { //down
			this.crusaderTarget[0] = this.mapXSize/2;
			this.crusaderTarget[1] = this.mapYSize*3/4;
		}
		else if (this.turn == 6*interval) { //down left
			this.crusaderTarget[0] = this.mapXSize/4;
			this.crusaderTarget[1] = this.mapYSize*3/4;
		}
		else if (this.turn == 7*interval) { //left
			this.crusaderTarget[0] = this.mapXSize/4;
			this.crusaderTarget[1] = this.mapYSize/2;
		}
		else if (this.turn == 8*interval) { //up left
			this.crusaderTarget[0] = this.mapXSize/4;
			this.crusaderTarget[1] = this.mapYSize/4;
		}
	}

	//Can this pilgrim give stuff to the castle
	public boolean canGiveStuff() {
		int absoluteXCastleDistance=Math.abs(castleLocation[0]-this.me.x);
		int absoluteYCastleDistance=Math.abs(castleLocation[1]-this.me.y);
		if(absoluteXCastleDistance==0||absoluteXCastleDistance==1) {
			if(absoluteYCastleDistance==0||absoluteYCastleDistance==1) {
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
				castleLocation[0] = goodGuy.x;
				castleLocation[1] = goodGuy.y;
				return true;
			}
		}
		return false;
	}

	public boolean canMineKarbonite() {
		return karboniteMap[me.y][me.x] && me.karbonite<20;
	}

	public boolean canMineFuel() {
		return fuelMap[me.y][me.x] && me.fuel<100;
	}

	public MoveAction pathFind(int[] finalLocation) {
//		this.log("moving toward x=" + finalLocation[0] + " y=" + finalLocation[1]);
		if (fuel <= 30 || finalLocation[0]==-1) { //not enough fuel, or -1 b/c can't find karbo or fuel
//			this.log("cannot move");
			return null;
		}
		int xDistance = finalLocation[0] - me.x;
		int yDistance = finalLocation[1] - me.y;
		int quadrant;
		double absoluteXDistance = Math.abs(xDistance);
		double absoluteYDistance = Math.abs(yDistance);
		double radianAngle;
		double piHalf = Math.PI / 2;
		double piEight = Math.PI / 8;
		double piThreeEight = piEight * 3;
		String optimalDirection = "";
		if (xDistance >= 0 && yDistance >= 0) {
			quadrant = 1;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHEAST";
			}
		} else if (xDistance <= 0 && yDistance >= 0) {
			quadrant = 2;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHWEST";
			}
		} else if (xDistance <= 0 && yDistance <= 0) {
			quadrant = 3;
			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHWEST";
			}
		} else if (xDistance >= 0 && yDistance <= 0) {
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
					if (canMove(me, me.x, me.y + 1)) {
						if(alreadyBeenHere(me,0,1)==false) {
							return move(0,1);
						}
					} else if (canMove(me, me.x, me.y + 2)) {
						if(alreadyBeenHere(me,0,2)==false) {
							return move(0,2);
						}
					} else if (canMove(me, me.x, me.y + 3)) {
						if(alreadyBeenHere(me,0,3)==false) {
							return move(0,3);
						}
					}

				} else if (index == 1) {
					if (canMove(me, me.x + 1, me.y + 1)) {
						if(alreadyBeenHere(me,1,1)==false) {
							return move(1,1);
						}
					} else if (canMove(me, me.x + 2, me.y + 2)) {
						if(alreadyBeenHere(me,2,2)==false) {
							return move(2,2);
						}
					} else if (canMove(me, me.x + 2, me.y + 1)) {
						if(alreadyBeenHere(me,2,1)==false) {
							return move(2,1);
						}
					} else if (canMove(me, me.x + 1, me.y + 2)) {
						if(alreadyBeenHere(me,1,2)==false) {
							return move(1,2);
						}
					}
				} else if (index == 2) {
					if (canMove(me, me.x + 1, me.y)) {
						if(alreadyBeenHere(me,1,0)==false) {
							return move(1,0);
						}
					} else if (canMove(me, me.x + 2, me.y)) {
						if(alreadyBeenHere(me,2,0)==false) {
							return move(2,0);
						}
					} else if (canMove(me, me.x + 3, me.y)) {
						if(alreadyBeenHere(me,3,0)==false) {
							return move(3,0);
						}
					}
				} else if (index == 3) {
					if (canMove(me, me.x + 1, me.y - 1)) {
						if(alreadyBeenHere(me,1,-1)==false) {
							return move(1,-1);
						}
					} else if (canMove(me, me.x + 2, me.y - 2)) {
						if(alreadyBeenHere(me,2,-2)==false) {
							return move(2,-2);
						}
					} else if (canMove(me, me.x + 2, me.y - 1)) {
						if(alreadyBeenHere(me,2,-1)==false) {
							return move(2,-1);
						}
					} else if (canMove(me, me.x + 1, me.y - 2)) {
						if(alreadyBeenHere(me,1,-2)==false) {
							return move(1,-2);
						}
					}
				} else if (index == 4) {
					if (canMove(me, me.x, me.y - 1)) {
						if(alreadyBeenHere(me,0,-1)==false) {
							return move(0,-1);
						}
					} else if (canMove(me, me.x, me.y - 2)) {
						if(alreadyBeenHere(me,0,-2)==false) {
							return move(0,-2);
						}
					} else if (canMove(me, me.x, me.y - 3)) {
						if(alreadyBeenHere(me,0,-3)==false) {
							return move(0,-3);
						}
					}
				} else if (index == 5) {
					if (canMove(me, me.x - 1, me.y - 1)) {
						if(alreadyBeenHere(me,-1,-1)==false) {
							return move(-1,-1);
						}
					} else if (canMove(me, me.x - 2, me.y - 2)) {
						if(alreadyBeenHere(me,-2,-2)==false) {
							return move(-2,-2);
						}
					} else if (canMove(me, me.x - 2, me.y - 1)) {
						if(alreadyBeenHere(me,-2,-1)==false) {
							return move(-2,-1);
						}
					} else if (canMove(me, me.x - 1, me.y - 2)) {
						if(alreadyBeenHere(me,-1,-2)==false) {
							return move(-1,-2);
						}
					}
				} else if (index == 6) {
					if (canMove(me, me.x - 1, me.y)) {
						if(alreadyBeenHere(me,-1,0)==false) {
							return move(-1,0);
						}
					} else if (canMove(me, me.x - 2, me.y)) {
						if(alreadyBeenHere(me,-2,0)==false) {
							return move(-2,0);
						}
					} else if (canMove(me, me.x - 3, me.y)) {
						if(alreadyBeenHere(me,-3,0)==false) {
							return move(-3,0);
						}
					}
				} else if (index == 7) {
					if (canMove(me, me.x - 1, me.y + 1)) {
						if(alreadyBeenHere(me,-1,1)==false) {
							return move(-1,1);
						}
					} else if (canMove(me, me.x - 2, me.y + 2)) {
						if(alreadyBeenHere(me,-2,2)==false) {
							return move(-2,2);
						}
					} else if (canMove(me, me.x - 2, me.y + 1)) {
						if(alreadyBeenHere(me,-2,1)==false) {
							return move(-2,1);
						}
					} else if (canMove(me, me.x - 1, me.y + 2)) {
						if(alreadyBeenHere(me,-1,2)==false) {
							return move(-1,2);
						}
					}
				}
			}
		} else {
			for (int i = 0; i < rotationTries.length; i++) {
				int index = (directions.indexOf(optimalDirection) + i) % 8;
				if (index == 0) {
					if (canMove(me, me.x, me.y + 1)) {
						if(alreadyBeenHere(me,0,1)==false) {
							return move(0,1);
						}
					} else if (canMove(me, me.x, me.y + 2)) {
						if(alreadyBeenHere(me,0,2)==false) {
							return move(0,2);
						}
					}

				} else if (index == 1) {
					if (canMove(me, me.x + 1, me.y + 1)) {
						if(alreadyBeenHere(me,1,1)==false) {
							return move(1,1);
						}
					}
				} else if (index == 2) {
					if (canMove(me, me.x + 1, me.y)) {
						if(alreadyBeenHere(me,1,0)==false) {
							return move(1,0);
						}
					} else if (canMove(me, me.x + 2, me.y)) {
						if(alreadyBeenHere(me,1,0)==false) {
							return move(1,0);
						}
					}
				} else if (index == 3) {
					if (canMove(me, me.x + 1, me.y - 1)) {
						if(alreadyBeenHere(me,1,-1)==false) {
							return move(1,-1);
						}
					}
				} else if (index == 4) {
					if (canMove(me, me.x, me.y - 1)) {
						if(alreadyBeenHere(me,0,-1)==false) {
							return move(0,-1);
						}
					} else if (canMove(me, me.x, me.y - 2)) {
						if(alreadyBeenHere(me,0,-2)==false) {
							return move(0,-2);
						}
					}
				} else if (index == 5) {
					if (canMove(me, me.x - 1, me.y - 1)) {
						if(alreadyBeenHere(me,-1,-1)==false) {
							return move(-1,-1);
						}
					}
				} else if (index == 6) {
					if (canMove(me, me.x - 1, me.y)) {
						if(alreadyBeenHere(me,-1,0)==false) {
							return move(-1,0);
						}
					} else if (canMove(me, me.x - 2, me.y)) {
						if(alreadyBeenHere(me,-2,0)==false) {
							return move(-2,0);
						}
					}
				} else if (index == 7) {
					if (canMove(me, me.x - 1, me.y + 1)) {
						if(alreadyBeenHere(me,-1,1)==false) {
							return move(-1,1);
						}
					}
				}
			}
		}
		return null;
	}

	public boolean canMove(Robot me, int finalX, int finalY) {
		return passableMap[finalY][finalX] && visibleRobotMap[finalY][finalX] == 0;
	}

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
		int x = this.me.x;
		int y = this.me.y;
		return Math.abs(x-this.castleLocation[0])==1 && Math.abs(y-this.castleLocation[1])==1;
	}

	public int[] checkAdjacentAvailable() {
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
			if (y < mapYSize - 1) { //can check down
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
		if (y < mapYSize - 1) { //can check down
			if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
				return new int[]{x, y+1};
			}
		}
		if (x < mapXSize - 1) { //can check right
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
		return null; //surrounded by impassable terrain
	}

	
	public int[] searchForKarboniteLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		double distance;
		for (int i = 0; i < mapYSize; i++) {
			for (int j = 0; j < mapXSize; j++) {
				if (karboniteMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
					//this.log("i am here x=" + this.me.x + " y=" + this.me.y + "  could mine here x=" + j + " y=" + i);
					distance = findDistance(me, j, i);
					if (distance < minDistance) {
						minDistance = distance;
						minXCoordinate = j;
						minYCoordinate = i;
					}
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
		double distance;
		for (int i = 0; i < mapYSize; i++) {
			for (int j = 0; j < mapXSize; j++) {
				if (fuelMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
					distance = findDistance(me, j, i);
					if (distance < minDistance) {
						minDistance = distance;
						minXCoordinate = j;
						minYCoordinate = i;
					}
				}
			}
		}
		int[] location = new int[2];
		location[0] = minXCoordinate;
		location[1] = minYCoordinate;
		return location;
	}

	public Action makeUnit(int type) {
		int[] spot = this.checkAdjacentAvailable();
		return this.buildUnit(type, spot[0] - this.me.x, spot[1] - this.me.y);
	}

	//Pilgrims run away when they see dangerous enemies TODO: fix this
	public Action pilgrimRunAway() {
		HashSet<Robot> nearbyEnemies = this.findBadGuys();
		Robot closestEnemy = this.findClosestThreat(nearbyEnemies);
		int x = 0, y = 0;
		x -= this.me.x - closestEnemy.x;
		y -= this.me.y - closestEnemy.y;
		return this.move(x, y); //replace with our move/pathing method later
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
	public Robot findBadGuy(HashSet<Robot> potentialEnemies) {
		double distance=Double.MAX_VALUE;
		Robot closeBot=null;
		Iterator<Robot> badGuyIter=potentialEnemies.iterator();
		while(badGuyIter.hasNext()) {
			Robot aBadGuy=badGuyIter.next();
			double badGuyDistance=findDistance(me,aBadGuy);
//			log("Distance: "+badGuyDistance);
			if(badGuyDistance<distance) {
//				log("Found closer robot");
				distance=badGuyDistance;
//				log("New closest distance: "+distance);
				closeBot=aBadGuy;
			}
			
		}
//		if(closeBot==null) {
//			log("Still null boo");
//		} else {
//			log("You the man");
//		}
		return closeBot;
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

	public Robot findPrimaryEnemyTypeHealth(HashSet<Robot> potentialEnemies) {
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

	public boolean canBuild(int type) {
		return this.fuel >= SPECS.UNITS[type].CONSTRUCTION_FUEL && this.karbonite >= SPECS.UNITS[type].CONSTRUCTION_KARBONITE && this.checkAdjacentAvailable()!=null;
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

}
