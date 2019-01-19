package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
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
	public int[] botCounts;
	public int[] center;
	public Action turn() {
		turn++;
		log("Global karbonite: "+karbonite);
		log("Global fuel: "+fuel);
		int[] firstRotationTries = { 0, -1, 1, -2, 2, -3, 3 };
		rotationTries=firstRotationTries;
		//visibleRobotMap = getVisibleRobotMap();

		//previousLocations=new ArrayList<Integer>();
		if(turn==1) {
			passableMap = getPassableMap();
			directions=new ArrayList<String>();
			setDirectionsArrayList();
			haveCastle=false;
			karboniteMap=getKarboniteMap();
			fuelMap=getFuelMap();
			castleLocation=new int[2];
			botCounts=new int[3];
			center=new int[2];
			center[0]=31;
			center[1]=31;
		}
		if (me.unit == SPECS.CASTLE) {
			log("I am a castle");
			if(turn==1) {
				botCounts[0]=0;
				botCounts[1]=0;
				botCounts[2]=0;
			}
			log("Pilgrim count: "+botCounts[0]);
			log("Preacher count: "+botCounts[1]);
			log("Crusader count: "+botCounts[2]);
			if(karbonite>=10&&fuel>=50) {
				int pilgrimCount=botCounts[0];
				if(botCounts[0]<6) {
					BuildAction buildingPilgrim=makePilgrims();
					if(buildingPilgrim!=null) {
						botCounts[0]=pilgrimCount+1;
						return buildingPilgrim;
					}
				}
			}
			if(karbonite>=30&&fuel>=50) {
				int preacherCount=botCounts[1];
				if(botCounts[1]<3) {
					BuildAction buildingPreacher=makePreachers();
					if(buildingPreacher!=null) {
						botCounts[1]=preacherCount+1;
						return buildingPreacher;
					}
				}
			}
			if(karbonite>=20&&fuel>=50) {
				int crusaderCount=botCounts[2];
					BuildAction buildingCrusader=makeCrusaders();
					if(buildingCrusader!=null) {
						botCounts[2]=crusaderCount+1;
						return buildingCrusader;
					}
				
			}
		}

		if (me.unit == SPECS.PILGRIM) {
			log("I am a pilgrim");
			log("My karbonite: "+me.karbonite);
			log("My fuel: "+me.fuel);
			log("Have Castle: "+haveCastle);
			log("My Castle X: "+castleLocation[0]);
			log("My Castle Y: "+castleLocation[1]);
			log("My X position: "+me.x);
			log("My Y position: "+me.y);
			if(!haveCastle) {
				if(locateNearbyCastle(me)) {
					haveCastle=true;
				}
			}
			int[] karboniteLocationFind=searchForKarboniteLocation();
			log("Karbonite X: "+karboniteLocationFind[0]);
			log("Karbonite Y: "+karboniteLocationFind[1]);
			return pathFind(me,karboniteLocationFind);
			/*int[] fuelLocationFind=searchForFuelLocation();
			if(canMineKarbonite(me)||canMineFuel(me)) {
				return mine();
			}
			if(canGiveStuff(me)) {
				int xCastle=castleLocation[0]-me.x;
				int yCastle=castleLocation[1]-me.y;
				return give(xCastle,yCastle,me.karbonite,me.fuel);
			}
			if(me.karbonite==20||me.fuel==100) {
				return pathFind(me,castleLocation);
			} else {
				//if(findDistance(me,karboniteLocationFind[0],karboniteLocationFind[1])>=findDistance(me,fuelLocationFind[0],fuelLocationFind[1])) {
				//	return pathFind(me,fuelLocationFind);
				//} else {
			return pathFind(me,karboniteLocationFind);
				//}
			}*/
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
		/*if(me.unit==SPECS.CRUSADER) {
			log("I am a crusader");
			if(fuel>=10) {
				HashSet<Robot> enemies=findBadGuys();
				if(enemies.size()==0) {
					return pathFind(me,center);
				}
				log("Enemies size: "+enemies.size());
				Robot closeBadGuy=findBadGuy(me,enemies);
				try {
					log("Bad guy's health: "+closeBadGuy.health);
					log("Other bad guy data "+closeBadGuy.x);
					return attack(closeBadGuy.x-me.x,closeBadGuy.y-me.y);
				} catch (Exception e) {
					log("Can't attack the man");
					try {
						int[] closeBadGuyLocation=new int[2];
						closeBadGuyLocation[0]=closeBadGuy.x;
						closeBadGuyLocation[1]=closeBadGuy.y;
						log("X coor bad: "+closeBadGuyLocation[0]);
						log("Y coor bad: "+closeBadGuyLocation[1]);
						return pathFind(me,closeBadGuyLocation);
					} catch (Exception ef) {
						log("Can find the man");
					}
				}
			}
		}*/
		//Crusader pseudocode: Looks for badGuys, if sees none, pathFind to the center of the Map
		//If see some, attack if possible, otherwise pathfind
		if(me.unit==SPECS.PREACHER) {
			if(fuel>=15) {
				HashSet<Robot> potentialEnemies=findBadGuys();
				AttackAction maybeSauce=preacherAttack(me,potentialEnemies);
				if(maybeSauce!=null) {
					return maybeSauce;
				}
			}
		}
		log("Did nothing");
		return null;

	}
	
	
	//Find closest enemy
	//Loop through various locations, and try to attack there
	
	public AttackAction preacherAttack(Robot me, HashSet<Robot> potentialEnemies) {
		Robot targetBadGuy=findBadGuy(me,potentialEnemies);
		while(targetBadGuy==null&&potentialEnemies.size()>0) {
			potentialEnemies.remove(targetBadGuy);
			targetBadGuy=findBadGuy(me,potentialEnemies);
		}
		if(potentialEnemies.size()==0) {
			return null;
		}
		int xDistance=targetBadGuy.x-me.x;
		int yDistance=targetBadGuy.y-me.y;
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
	
	
	
	public Robot findBadGuy(Robot me,HashSet<Robot> potentialEnemies) {
		double distance=Double.MAX_VALUE;
		Robot closeBot=null;
		Iterator<Robot> badGuyIter=potentialEnemies.iterator();
		while(badGuyIter.hasNext()) {
			Robot aBadGuy=badGuyIter.next();
			double badGuyDistance=findDistance(me,aBadGuy);
			log("Distance: "+badGuyDistance);
			if(badGuyDistance<distance) {
				log("Found closer robot");
				distance=badGuyDistance;
				log("New closest distance: "+distance);
				closeBot=aBadGuy;
			}
			
		}
		if(closeBot==null) {
			log("Still null boo");
		} else {
			log("You the man");
		}
		return closeBot;
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
		//log("Optimal Direction: "+optimalDirection);
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
	//	log("My robot's X location: "+me.x);
	//	log("My robot's Y location: "+me.y);
	//	log("Karbonite X location: " +location[0]);
	//	log("Karbonite Y location: "+location[1]);
	//	log("Distance: "+minDistance);
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
	//	log("My robot's X location: "+me.x);
	//	log("My robot's Y location: "+me.y);
	//	log("Fuel X location: " +location[0]);
	//	log("Fuel Y location: "+location[1]);
	//	log("Distance: "+minDistance);
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
				if (closestDistance > distance) {
					closestDistance = distance;
					closestBot = badGuy;
				}
		}
		return closestBot;
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
	
	public BuildAction makePilgrims() {
		try {
			return buildUnit(SPECS.PILGRIM,-1,-1);
		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,-1,0);
		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,-1,1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,0,-1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,0,1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,1,-1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,1,0);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PILGRIM,1,1);

		} catch (Exception e) {
			
		}
		return null;
	}
	
	public BuildAction makePreachers() {
		try {
			return buildUnit(SPECS.PREACHER,-1,-1);
		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,1,1);
		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,-1,1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,1,-1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,0,1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,0,-1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,1,0);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.PREACHER,-1,0);

		} catch (Exception e) {
			
		}
		return null;
	}
	
	public BuildAction makeCrusaders() {
		try {
			return buildUnit(SPECS.CRUSADER,-1,-1);
		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,1,1);
		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,-1,1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,1,-1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,0,1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,0,-1);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,1,0);

		} catch (Exception e) {
			
		}
		try {
			return buildUnit(SPECS.CRUSADER,-1,0);

		} catch (Exception e) {
			
		}
		return null;
	}

}