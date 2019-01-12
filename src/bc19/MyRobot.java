package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyRobot extends BCAbstractRobot {
	public int turn = 0;

	public Action turn() {
		turn++;

		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				log("Building a pilgrim.");
				return buildUnit(SPECS.PILGRIM,1,0);
			}
		}
		
		if (me.unit == SPECS.CHURCH) {
			
		}

		if (me.unit == SPECS.PILGRIM) {
			if (turn == 1) {
				log("I am a pilgrim.");

				//log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
			}
		}

		return null;

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
	
	public Action makePilgrims() {
		int[] spot = checkAdjacentPassable();
		return buildUnit(SPECS.PILGRIM, spot[0], spot[1]);
	}
	
	public Action makeCrusaders() {
		int[] spot = checkAdjacentPassable();
		return buildUnit(SPECS.CRUSADER, spot[0], spot[1]);
	}
	
	public List<Robot> senseNearbyEnemies() {
		List<Robot> nearbyRobots = new ArrayList<Robot>(Arrays.asList(this.getVisibleRobots()));
		List<Robot> enemies = nearbyRobots.stream().filter(robot -> robot.team == this.me.team).collect(Collectors.toList());
		return enemies;
	}
	
	public Robot findClosestRobot(List<Robot> robots) {
		int leastDistance = 65;
		int distance;
		int index = 0;
		for (int i = 0; i < robots.size(); i++) {
			distance = findDistance(robots.get(i).x, robots.get(i).y);
			if (leastDistance > distance) {
				index = i;
				leastDistance = distance;
			}
		}
		return robots.get(index);
	}
	
	public int findDistance(int x, int y) { //calculates distance between this robot and another point (distance = number of moves)
		int dx = this.me.x - x;
		int dy = this.me.y - y;
		return Math.abs(dx) > Math.abs(dy) ? dx : dy;
	}
	
	public Action pilgrimRunAway() {
		List<Robot> nearbyEnemies = this.senseNearbyEnemies();
		Robot closestEnemy = findClosestRobot(nearbyEnemies);
		int x = 0, y = 0;
		x -= this.me.x - closestEnemy.x;
		y -= this.me.y - closestEnemy.y;
		return this.move(x, y); //replace with our move/pathing method later
	}
	
	public Action moveToOptimalAttack() { //get distance from closest enemy, and move such that the enemy is as far as possible but still in attack range
		List<Robot> nearbyEnemies = this.senseNearbyEnemies();
		Robot closestEnemy = findClosestRobot(nearbyEnemies);
		int move = findDistance(closestEnemy.x, closestEnemy.y) - getMaxAttackRangeRadius();
		return this.move(, ); //replace with our move/pathing method later
	}
	
//	public void alertKilledEnemy() { //not sure how to return killed coordinates because of this communication system
//		int value, radius;
//		this.signal(value, radius);
//	}
	
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