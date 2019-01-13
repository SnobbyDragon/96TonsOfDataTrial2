package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public int origin; //coordinates of castle or church

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
		if (turn == 1) { //is a robot, not a structure
			//sets coordinates of origin
		}
		if (me.unit == SPECS.PILGRIM) {
			if (turn == 1) {
				log("I am a pilgrim.");

				//log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
			}
		}
		if (me.unit == SPECS.CRUSADER) {
			Robot enemy = this.findPrimaryEnemyType(this.findBadGuys());
			this.attack(enemy.x, enemy.y);
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

	public List<Robot> senseNearbyAllies() {
		List<Robot> nearbyRobots = new ArrayList<Robot>(Arrays.asList(this.getVisibleRobots()));
		List<Robot> allies = nearbyRobots.stream().filter(robot -> robot.team == this.me.team).collect(Collectors.toList());
		return allies;
	}

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

	public int findDiscreteDistance(int x, int y) { //calculates distance between this robot and another point (distance = number of moves)
		int dx = Math.abs(this.me.x - x);
		int dy = Math.abs(this.me.y - y);
		return dx + dy;
	}

	public Action pilgrimRunAway() {
		HashSet<Robot> nearbyEnemies = this.findBadGuys();
		Robot closestEnemy = findClosestRobot(nearbyEnemies);
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

}