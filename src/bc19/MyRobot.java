package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public int[] rotationTries;
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public ArrayList<String> directions;
	ArrayList<Integer> previousLocations;
	public boolean haveCastle;
	public int[] castleLocation;
	public Action turn() {
		turn++;
		log("Global karbonite: "+karbonite);
		log("Global fuel: "+fuel);
		int[] firstRotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	
		rotationTries=firstRotationTries;
		/*passableMap = getPassableMap();
		visibleRobotMap = getVisibleRobotMap();
		karboniteMap=getKarboniteMap();
		fuelMap=getFuelMap();
		previousLocations=new ArrayList<Integer>();
		haveCastle=false;
		castleLocation=new int[2];*/
		if(turn==1) {
			directions=new ArrayList<String>();
			setDirectionsArrayList();
			haveCastle=false;
			castleLocation=new int[2];
		}
		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				log("Building a pilgrim.");
				return buildUnit(SPECS.PILGRIM, 1, 0);
			}
			log("Castle X: "+me.x);
			log("Castle Y: "+me.y);
		}

		if (me.unit == SPECS.PILGRIM) {
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
		log("Did nothing");
		return null;

	}
	
	public boolean canGiveStuff(Robot me) {
		int absoluteXCastleDistance=Math.abs(castleLocation[0]-me.x);
		int absoluteYCastleDistance=Math.abs(castleLocation[1]-me.y);
		if(absoluteXCastleDistance==0||absoluteXCastleDistance==1) {
			if(absoluteYCastleDistance==0||absoluteYCastleDistance==1) {
				if(me.karbonite==20||me.fuel==100) {
					return true;
				}
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
		if(karboniteMap[me.y][me.x]==true&&me.karbonite<20) {
			return true;
		}
		return false;
	}
	
	public boolean canMineFuel(Robot me) {
		if(fuelMap[me.y][me.x]==true&&me.fuel<100) {
			return true;
		}
		return false;
	}

	public void setDirectionsArrayList() {
		directions.add("NORTH");
		directions.add("NORTHEAST");
		directions.add("EAST");
		directions.add("SOUTHEAST");
		directions.add("SOUTH");
		directions.add("SOUTHWEST");
		directions.add("WEST");
		directions.add("NORTHWEST");
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

		/*if (me.unit == SPECS.CRUSADER) {
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
		}*/
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
		if (me.unit == SPECS.CRUSADER) {
			if (distance <= 16) {
				return true;
			}
			return false;
		} else if (me.unit == SPECS.PROPHET) {
			if (distance >= 16 && distance <= 64) {
				return true;
			}
			return false;
		} else if (me.unit == SPECS.PREACHER) {
			if (distance <= 16) {
				return true;
			}
			return false;
		}
		return false;
	}

	public boolean attack(Robot opponent) {
		int dx = opponent.x - me.x;
		int dy = opponent.y - me.y;
		try {
			attack(dx, dy);
			return true;
		} catch (Exception e) {
			return false;
		}
	}

	public int[] searchForKarboniteLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		for (int i = 0; i < karboniteMap.length; i++) {
			for (int j = 0; j < karboniteMap[i].length; j++) {
				if(karboniteMap[i][j]==true) {
					double distance = findDistance(me, j, i);
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
		log("My robot's X location: "+me.x);
		log("My robot's Y location: "+me.y);
		log("Karbonite X location: " +location[0]);
		log("Karbonite Y location: "+location[1]);
		log("Distance: "+minDistance);
		return location;
	}

	public int[] searchForFuelLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		for (int i = 0; i < fuelMap.length; i++) {
			for (int j = 0; j < fuelMap[i].length; j++) {
				if(fuelMap[i][j]==true) {
					double distance = findDistance(me, j, i);
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
		log("My robot's X location: "+me.x);
		log("My robot's Y location: "+me.y);
		log("Fuel X location: " +location[0]);
		log("Fuel Y location: "+location[1]);
		log("Distance: "+minDistance);
		return location;
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

}