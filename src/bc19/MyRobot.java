package bc19;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	boolean[][] passableMap = getPassableMap();
	int[][] visibleRobotMap = getVisibleRobotMap();
	ArrayList<String> directions;

	public Action turn() {
		turn++;

		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				log("Building a pilgrim.");
				return buildUnit(SPECS.PILGRIM, 1, 0);
			}
		}

		if (me.unit == SPECS.PILGRIM) {
			if (turn == 1) {
				log("I am a pilgrim.");

				// log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
			}
		}

		return null;

	}

	public ArrayList<String> setDirectionsArrayList() {
		directions.add("NORTH");
		directions.add("NORTHEAST");
		directions.add("EAST");
		directions.add("SOUTHEAST");
		directions.add("SOUTH");
		directions.add("SOUTHWEST");
		directions.add("WEST");
		directions.add("NORTHWEST");
		return directions;
	}

	public MoveAction pathFind(Robot me, int[] finalLocation) {
		if (fuel <= 30) {
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
		double piSevenEight = piEight * 7;
		String optimalDirection = "";
		if (xDistance >= 0 && yDistance >= 0) {
			quadrant = 1;
			radianAngle = Math.tan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piSevenEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHEAST";
			}
		} else if (xDistance <= 0 && yDistance >= 0) {
			quadrant = 2;
			radianAngle = Math.tan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piSevenEight && radianAngle <= piHalf) {
				optimalDirection = "NORTH";
			} else {
				optimalDirection = "NORTHWEST";
			}
		} else if (xDistance <= 0 && yDistance <= 0) {
			quadrant = 3;
			radianAngle = Math.tan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "WEST";
			} else if (radianAngle >= piSevenEight && radianAngle <= piHalf) {
				optimalDirection = "SOUTH";
			} else {
				optimalDirection = "SOUTHWEST";
			}
		} else if (xDistance >= 0 && yDistance <= 0) {
			quadrant = 4;
			radianAngle = Math.tan(absoluteYDistance / absoluteXDistance);
			if (radianAngle >= 0 && radianAngle <= piEight) {
				optimalDirection = "EAST";
			} else if (radianAngle >= piSevenEight && radianAngle <= piHalf) {
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
						return move(0, 1);
					} else if (canMove(me, me.x, me.y + 2)) {
						return move(0, 2);
					} else if (canMove(me, me.x, me.y + 3)) {
						return move(0, 3);
					}

				} else if (index == 1) {
					if (canMove(me, me.x + 1, me.y + 1)) {
						return move(1, 1);
					} else if (canMove(me, me.x + 2, me.y + 2)) {
						return move(2, 2);
					} else if (canMove(me, me.x + 2, me.y + 1)) {
						return move(2, 1);
					} else if (canMove(me, me.x + 1, me.y + 2)) {
						return move(1, 2);
					}
				} else if (index == 2) {
					if (canMove(me, me.x + 1, me.y)) {
						return move(1, 0);
					} else if (canMove(me, me.x + 2, me.y)) {
						return move(2, 0);
					} else if (canMove(me, me.x + 3, me.y)) {
						return move(3, 0);
					}
				} else if (index == 3) {
					if (canMove(me, me.x + 1, me.y - 1)) {
						return move(1, -1);
					} else if (canMove(me, me.x + 2, me.y - 2)) {
						return move(2, -2);
					} else if (canMove(me, me.x + 2, me.y - 1)) {
						return move(2, -1);
					} else if (canMove(me, me.x + 1, me.y - 2)) {
						return move(1, -2);
					}
				} else if (index == 4) {
					if (canMove(me, me.x, me.y - 1)) {
						return move(0, -1);
					} else if (canMove(me, me.x, me.y - 2)) {
						return move(0, -2);
					} else if (canMove(me, me.x, me.y - 3)) {
						return move(0, -3);
					}
				} else if (index == 5) {
					if (canMove(me, me.x - 1, me.y - 1)) {
						return move(-1, -1);
					} else if (canMove(me, me.x - 2, me.y - 2)) {
						return move(-2, -2);
					} else if (canMove(me, me.x - 2, me.y - 1)) {
						return move(-2, -1);
					} else if (canMove(me, me.x - 1, me.y - 2)) {
						return move(-1, -2);
					}
				} else if (index == 6) {
					if (canMove(me, me.x - 1, me.y)) {
						return move(-1, 0);
					} else if (canMove(me, me.x - 2, me.y)) {
						return move(-2, 0);
					} else if (canMove(me, me.x - 3, me.y)) {
						return move(-3, 0);
					}
				} else if (index == 7) {
					if (canMove(me, me.x - 1, me.y + 1)) {
						return move(-1, 1);
					} else if (canMove(me, me.x - 2, me.y + 2)) {
						return move(-2, 2);
					} else if (canMove(me, me.x - 2, me.y + 1)) {
						return move(-2, 1);
					} else if (canMove(me, me.x - 1, me.y + 2)) {
						return move(-1, 2);
					}
				}
			}
		} else {
			for (int i = 0; i < rotationTries.length; i++) {
				int index = (directions.indexOf(optimalDirection) + i) % 8;
				if (index == 0) {
					if (canMove(me, me.x, me.y + 1)) {
						return move(0, 1);
					} else if (canMove(me, me.x, me.y + 2)) {
						return move(0, 2);
					}

				} else if (index == 1) {
					if (canMove(me, me.x + 1, me.y + 1)) {
						return move(1, 1);
					}
				} else if (index == 2) {
					if (canMove(me, me.x + 1, me.y)) {
						return move(1, 0);
					} else if (canMove(me, me.x + 2, me.y)) {
						return move(2, 0);
					}
				} else if (index == 3) {
					if (canMove(me, me.x + 1, me.y - 1)) {
						return move(1, -1);
					}
				} else if (index == 4) {
					if (canMove(me, me.x, me.y - 1)) {
						return move(0, -1);
					} else if (canMove(me, me.x, me.y - 2)) {
						return move(0, -2);
					}
				} else if (index == 5) {
					if (canMove(me, me.x - 1, me.y - 1)) {
						return move(-1, -1);
					}
				} else if (index == 6) {
					if (canMove(me, me.x - 1, me.y)) {
						return move(-1, 0);
					} else if (canMove(me, me.x - 2, me.y)) {
						return move(-2, 0);
					}
				} else if (index == 7) {
					if (canMove(me, me.x - 1, me.y + 1)) {
						return move(-1, 1);
					}
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

	public int[] searchForKarboniteLocaation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		boolean[][] currentKarboniteMap = getKarboniteMap();
		for (int i = 0; i < currentKarboniteMap.length; i++) {
			for (int j = 0; j < currentKarboniteMap[i].length; j++) {
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

	public int[] searchForFuelLocaation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		boolean[][] currentFuelMap = getFuelMap();
		for (int i = 0; i < currentFuelMap.length; i++) {
			for (int j = 0; j < currentFuelMap[i].length; j++) {
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