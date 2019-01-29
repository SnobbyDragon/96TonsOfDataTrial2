package bc19;

import bc19.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class MyRobot extends BCAbstractRobot {
	public int turn;
    public int mapYSize, mapXSize; // size of the map, length y and length x
    public boolean horizontalReflection;
    public int[][] visibleRobotMap;
    public boolean[][] passableMap;
    public boolean[][] karboniteMap;
    public boolean[][] fuelMap;

    public final int[][] adjacents = new int[][] { new int[] { 0, 1 }, new int[] { -1, 1 }, new int[] { -1, 0 },
            new int[] { -1, -1 }, new int[] { 0, -1 }, new int[] { 1, -1 }, new int[] { 1, 0 }, new int[] { 1, 1 } };
    public int numCastles;
    public int deadCastles = 0;
    public int[] castleIDs = new int[3];
    public int[][] castleLocations = new int[3][2]; // {{x, y}, {x, y}, {x, y}}
    public int[][] evilCastleLocations = new int[3][2]; // {{x, y}, {x, y}, {x, y}}
    public ArrayList<Integer>[] robots = new ArrayList[6];
    public int numFuelMines = 0;
    public int numKarbMines = 0;
    public int maxPilgrims;
    // pathing
    public ArrayList<int[]> path = null;
    public int pathIndex;
    // PROPHET LATTICE OP
    public int castleDirection;
    public int sideDirection;
    public boolean inPosition;
    public int castleIndex;
    public int top;
    public int left;
    public int numPilgrims = 0;
    public int numKarbos = 0;
    public int numFuels = 0;
    public int depositsInClump;
    public int[][] karboSpots;
    public int[][] fuelSpots;
    public int[] clumpNumDeposits;
    public int clumpIndex;
    public boolean[] isClumpOccupied;
    public ArrayList<int[]> karbosInUse = new ArrayList<int[]>();
    public ArrayList<int[]> fuelsInUse = new ArrayList<int[]>();
    public int[] home;
    public final int clumpRadius2 = 25;
    public ArrayList<int[]> clumpCenters;
    public ArrayList<ArrayList<int[]>> clumps;
    public int[][] allDeposits;

    public Action turn() {
    	turn++;
        if (me.turn == 1) {
            this.passableMap = this.getPassableMap();
            this.mapYSize = this.passableMap.length;
            this.mapXSize = this.passableMap[0].length;
            this.karboniteMap = this.getKarboniteMap();
            this.fuelMap = this.getFuelMap();
            this.getNumKarbos();
            this.getNumFuels();
            for (int i = 0; i < 6; i++) {
                robots[i] = new ArrayList<Integer>();
            }

            horizontalReflection = this.reflect();
        }
        this.visibleRobotMap = this.getVisibleRobotMap();
        switch (me.unit) {
        case 0:
            return castle();
        case 1:
            return church();
        case 2:
            return pilgrim();
        case 3:
            return crusader();
        case 4:
            return prophet();
        case 5:
            return preacher();
        }
        return null;
    }

    public Action castle() {
        if (me.turn == 1) {

            numCastles = 1;
            this.castleLocations[0] = new int[] { me.x, me.y };
            castleIDs[0] = me.id;
            robots[0].add(me.id);

            fillAllMines();
            getClumpDepositNums();
            findClumps();
            findClumpCenters();

            for (Robot r : getVisibleRobots()) {
                if (r.team == me.team && r.id != me.id) {
                    castleIDs[numCastles] = r.id;
                    this.castleLocations[numCastles] = new int[] { r.x, r.y };
                    numCastles++;
                    robots[0].add(r.id);
                }
            }

            int[] closestDeposit = findClosestDeposit();
            for (int i = 0; i < clumps.size(); i++) {
                if (clumps.get(i).contains(closestDeposit)) {
                    depositsInClump = clumps.get(i).size();
                    clumpIndex = i;
                    isClumpOccupied[i] = true;
                    break;
                }
            }

            maxPilgrims = (int) Math.floor(Math.min(numFuelMines * 1.25, numFuelMines * .75 + numKarbMines))
                    - robots[0].size();

            if (numCastles > 1) {
                castleTalk(me.x);

                for (int i = 1; i < numCastles; i++) {
                    Robot castle = getRobot(robots[0].get(i));
                    if (castle.turn == 1) {
                        castleLocations[i][0] = castle.castle_talk;
                    }
                }
            }

            return null;
        } else if (me.turn == 2) {
            if (numCastles > 1) {
                castleTalk(me.y);

                for (int i = 1; i < numCastles; i++) {
                    Robot castle = getRobot(robots[0].get(i));
                    if (castle.turn == 2) {
                        castleLocations[i][1] = castle.castle_talk;
                    } else {
                        castleLocations[i][0] = castle.castle_talk;
                    }
                }
            }
        }

        else if (me.turn == 3) {
            if (numCastles > 1) {
                for (int i = 1; i < numCastles; i++) {
                    Robot castle = getRobot(robots[0].get(i));
                    if (castle.turn == 2) {
                        castleLocations[i][1] = castle.castle_talk;
                    }
                }
            }
            this.getEnemyCastleLocs();
            log("castles = " + numCastles);
        }

        for (int i = 0; i < numCastles; i++) {
            int castleID = castleIDs[i];
            if (castleID == -1) {
                continue;
            }
            Robot castle = getRobot(castleID);
            if (castle == null) {
                castleIDs[i] = -1;
                continue;
            }
            if (castle.castle_talk != 0) {
                isClumpOccupied[castle.castle_talk - 1] = true;
            }
        }
        if (numPilgrims < depositsInClump) { // need more pilgrims
            if (this.canBuild(SPECS.PILGRIM)) {
                int[] build = this.checkAdjacentBuildAvailable();
                if (build != null) {
                    numPilgrims++;
                    signal(64 * me.y + me.x, 2);
                    castleTalk(clumpIndex + 1);
                    return this.buildUnit(SPECS.PILGRIM, build[1] - this.me.x, build[0] - this.me.y);
                }
            }
        }
        for (int i = 0; i < clumpCenters.size(); i++) { // going to a new clump
            if (isClumpOccupied[i]) {
                continue;
            }
            int[] center = clumpCenters.get(i);
            clumpIndex = i;
            isClumpOccupied[i] = true;
            int[] build = this.checkAdjacentBuildAvailable();
            if (build != null) {
                signal(64 * center[1] + center[0], 2);
                castleTalk(clumpIndex + 1);
                numPilgrims++;
                return this.buildUnit(SPECS.PILGRIM, build[1] - this.me.x, build[0] - this.me.y);
            }
        }

        // defense
        Robot badGuy = this.findPrimaryEnemyTypeDistance(this.findBadGuys());
        if (badGuy != null) {
            if (this.canBuild(SPECS.PREACHER)
                    && getRobot(this.visibleRobotMap[badGuy.y][badGuy.x]).unit != SPECS.PILGRIM) {
                int[] build = this.checkAdjacentBuildAvailable();
                castleTalk(SPECS.PREACHER);
                return buildUnit(SPECS.PREACHER, build[0] - this.me.x, build[1] - this.me.y);
            }
            // castle attack
            return this.attack(badGuy.x - this.me.x, badGuy.y - this.me.y);
        }

        // not enough resources
        if (fuel < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_FUEL + numPilgrims + 2
                || karbonite < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_KARBONITE) {
            return null;
        }

        // prophet lattice
        if (numPilgrims >= maxPilgrims) {
            if (fuel >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_FUEL + 2 + numPilgrims * 6
                    && karbonite >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_KARBONITE) {
                if (this.me.turn < 50 && this.karbonite < 50) {
                    return null;
                }
                int shouldBuild;
                if (fuel >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_FUEL * (robots[0].size() + robots[1].size()) + 2
                        + robots[2].size() * 6
                        && karbonite >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_KARBONITE * robots[0].size()) {
                    shouldBuild = 0;
                } else {
                    shouldBuild = (int) (Math.random() * (robots[0].size() + robots[1].size()));
                }
              
                if (shouldBuild == 0) {
                    int[] build = checkAdjacentAvailableRandom();
                    if (build != null) {
                        castleTalk(4);
                        //Arrays.sort(castleIDs);
                        int karboniteLevel = 25 * castleIDs.length;
                        for(int selecFirst=0;selecFirst<castleIDs.length-1;selecFirst++) {
                            int index=selecFirst;
                            for(int selecSecond=selecFirst+1;selecSecond<castleIDs.length;selecSecond++) {
                                if(castleIDs[selecSecond]<castleIDs[index]) {
                                    index=selecSecond;
                                }
                            }
                            int smallerNumber=castleIDs[index];
                            castleIDs[index]=castleIDs[selecFirst];
                            castleIDs[selecFirst] = smallerNumber;
                        }
                        //Arrays.sort(castleIDs);
                        //return buildUnit(4, build[0], build[1]);
                        for (int i = 0; i < castleIDs.length; i++) {
                            if (me.id == castleIDs[i]) {
                            	if(turn>=900) {
                                	return buildUnit(SPECS.CRUSADER,build[0],build[1]);
                                }
                                if (karbonite >= karboniteLevel) {
                                    return buildUnit(4, build[0], build[1]);

                                }
                            }
                            karboniteLevel = karboniteLevel - 25;
                        }
                    }
                }
            }
        }
        // nothing
        return null;
    }

    public Action church() {
        if (me.turn == 1) {
            this.fillAllMines();
            this.getClumpDepositNums();
            this.findClumps();

            numPilgrims = 1;
            home = new int[] { me.x, me.y };
            int[] closestDeposit = findClosestDeposit();
            for (ArrayList<int[]> clump : clumps) {
                if (clump.contains(closestDeposit)) {
                    depositsInClump = clump.size();
                }
            }

            maxPilgrims = (int) Math.floor(Math.min(numFuelMines * 1.25, numFuelMines * .75 + numKarbMines))
                    - robots[0].size();
        }
        if (numPilgrims < depositsInClump) {
            if (this.canBuild(SPECS.PILGRIM)) {
                int[] build = this.checkAdjacentBuildAvailable();
                if (build != null) {
                    numPilgrims++;
                    signal(64 * me.y + me.x, 2);
                    return this.buildUnit(SPECS.PILGRIM, build[1] - this.me.x, build[0] - this.me.y);
                }
            }
        }

        Robot badGuy = this.findPrimaryEnemyTypeDistance(this.findBadGuys());
        if (badGuy != null) {
            if (this.canBuild(SPECS.PREACHER)
                    && getRobot(this.visibleRobotMap[badGuy.y][badGuy.x]).unit != SPECS.PILGRIM) {
                int[] build = this.checkAdjacentBuildAvailable();
                castleTalk(SPECS.PREACHER);
                return buildUnit(SPECS.PREACHER, build[0] - this.me.x, build[1] - this.me.y);
            }
        }

        // not enough resources
        if (fuel < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_FUEL + numPilgrims + 2
                || karbonite < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_KARBONITE) {
            return null;
        }
        
       
        // prophet lattice
        if (numPilgrims >= maxPilgrims) {
            if (fuel >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_FUEL + 2 + numPilgrims * 6
                    && karbonite >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_KARBONITE) {
                if (this.me.turn < 20 && this.karbonite < 50) {
                    return null;
                }
                int shouldBuild;
                if (fuel >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_FUEL * (robots[0].size() + robots[1].size()) + 2
                        + robots[2].size() * 6
                        && karbonite >= SPECS.UNITS[SPECS.PROPHET].CONSTRUCTION_KARBONITE * robots[0].size()) {
                    shouldBuild = 0;
                } else {
                    shouldBuild = (int) (Math.random() * (robots[0].size() + robots[1].size()));
                }
                if (shouldBuild == 0) {
                    int[] build = checkAdjacentAvailableRandom();
                    if (build != null) {
                        castleTalk(4);
                        if(turn>=900) {
                        	return buildUnit(SPECS.CRUSADER,build[0],build[1]);
                        } else {
                        	return buildUnit(4, build[0], build[1]);
                        }
                    }
                }
            }
        }
        return null;
    }

    public Action pilgrim() {
        if (me.turn == 1) {
            this.getCastle();
            getEnemyCastleLocs();
            maxPilgrims = (int) Math.floor(Math.min(numFuelMines * 1.25, numFuelMines * .75 + numKarbMines));
        }
        Robot base = null;
        for (int[] move : adjacents) {
            int testX = me.x + move[0];
            int testY = me.y + move[1];
            if (testX <= -1 || testX >= this.mapXSize || testY <= -1 || testY >= this.mapYSize) { // invalid spot
                continue;
            }
            Robot robot = getRobot(visibleRobotMap[testY][testX]);
            if (visibleRobotMap[testY][testX] > 0 && (robot.unit == SPECS.CASTLE || robot.unit == SPECS.CHURCH)
                    && robot.team == me.team) { // found friendly castle/church
                base = robot;
                karbosInUse.clear();
                fuelsInUse.clear();
                if (me.turn == 1 && isRadioing(base)) {
                    this.fillAllMines();
                    home = new int[] { base.signal % 64, base.signal / 64 };
                    break;
                }
            }
        }

        if (path != null && path.size() > 0) {
            int[] nextMove = path.get(0);
            int dx = nextMove[0] - me.x;
            int dy = nextMove[1] - me.y;
            if (visibleRobotMap[nextMove[1]][nextMove[0]] <= 0) {
                if (fuel >= (dx * dx + dy * dy) * SPECS.UNITS[SPECS.PILGRIM].FUEL_PER_MOVE) {
                    path.remove(0);
                    return move(dx, dy);
                }
            }
        }

        if (me.karbonite == SPECS.UNITS[SPECS.PILGRIM].KARBONITE_CAPACITY
                || me.fuel == SPECS.UNITS[SPECS.PILGRIM].FUEL_CAPACITY) { // give resources
            if (base != null) {
                return this.give(base.x - me.x, base.y - me.y, me.karbonite, me.fuel);
            }
            path = bfs(home[0], home[1]); // return to home
            if (path == null) { // cant get to home
                return null;
            }
            int[] nextMove = path.get(0);
            int dx = nextMove[0] - me.x;
            int dy = nextMove[1] - me.y;
            if (fuel >= (dx * dx + dy * dy) * SPECS.UNITS[SPECS.PILGRIM].FUEL_PER_MOVE) {
                path.remove(0);
                return move(dx, dy);
            }
            // not enough fuel to move
            return null;
        }
        if ((this.karboniteMap[me.y][me.x] || this.fuelMap[me.y][me.x]) && this.visibleRobotMap[home[1]][home[0]] > 0) {
            if (fuel == 0) {
                // not enough fuel
                return null;
            }
            // can mine and home is available
            return mine();
        }
        int[] point;
        if (this.visibleRobotMap[home[1]][home[0]] > 0) { // home in sight, go to deposits to mine
            point = findClosestDeposit();
            if (!tilesInRange(point, home, clumpRadius2)) {
                // robot going to another clump
            }
        } else { // build a church
            if (Math.abs(home[0] - me.x) + Math.abs(home[1] - me.y) == 1) {
                if (this.canBuild(SPECS.CHURCH)) {
                    return buildUnit(SPECS.CHURCH, home[0] - me.x, home[1] - me.y);
                }
                // can't build a church b/c not enough resources (probs karbonite)
                return null;
            }
            this.visibleRobotMap[home[1]][home[0]] = 4096;
            point = home;
        }

        if (point == null) {
            point = home;
        }

        path = bfs(point[0], point[1]); // move to potential church place
        if (path == null) {
            return null;
        }
        int[] nextMove = path.get(0);
        int dx = nextMove[0] - me.x;
        int dy = nextMove[1] - me.y;
        if (fuel >= (dx * dx + dy * dy) * SPECS.UNITS[SPECS.PILGRIM].FUEL_PER_MOVE) {
            path.remove(0);
            return move(dx, dy);
        }
        return null;
    }

    public Action crusader() {
        return null;
    }

    public Action prophet() {
        if (me.turn == 1) {
            getCastle();
            inPosition = false;
        }

        Robot badGuy = this.findPrimaryEnemyTypeDistance(this.findBadGuys());
        if (badGuy != null) {
            // attack
            return attack(badGuy.x - this.me.x, badGuy.y - this.me.y);
        }
        if (!inPosition) {
            if (path == null || path.size() <= pathIndex
                    || visibleRobotMap[path.get(pathIndex)[1]][path.get(pathIndex)[0]] > 0) {
                path = this.lattice();
            }

            if (path == null || path.size() <= pathIndex
                    || visibleRobotMap[path.get(pathIndex)[1]][path.get(pathIndex)[0]] > 0) {
                int[] mov = checkAdjacentAvailableRandom();
                if (mov != null) {
                    if ((me.x + mov[0] + me.y + mov[1]) % 2 == 0) {
                        inPosition = true;
                    }
                    return move(mov[0], mov[1]);
                }
                return null;
            }

            int[] mov = new int[] { path.get(pathIndex)[0] - me.x, path.get(pathIndex)[1] - me.y };
            pathIndex += 1;
            if ((me.x + mov[0] + me.y + mov[1]) % 2 == 0 && !adjacentToHome(me.x + mov[0], me.y + mov[1])) {
                inPosition = true;
            }
            return move(mov[0], mov[1]);
        }
        return null;
    }

    public Action preacher() {
        if (me.turn == 1) {
            findCastleDirection();
            getCastle();
            if (castleDirection % 2 == 0) {
                sideDirection = (((int) (Math.random() * 2)) * 4 + castleDirection + 2) % 8;
            }
        }

        AttackAction atk = this.preacherAttack();
        if (atk != null) {
            return atk;
        }

        return null;
    }

    public boolean reflect() {
        int top = (this.mapYSize + 1) / 2;
        int left = (this.mapXSize + 1) / 2;

        for (int i = 0; i < top; i++) {
            for (int j = 0; j < left; j++) {
                if (this.passableMap[i][j] != this.passableMap[this.mapYSize - 1 - i][j]) {
                    return true;
                } else if (this.passableMap[i][j] != this.passableMap[i][this.mapXSize - 1 - j]) {
                    return false;
                }
            }
        }
        for (int i = this.mapYSize; i > top; i--) {
            for (int j = this.mapXSize; j > left; j--) {
                if (this.passableMap[i][j] != this.passableMap[this.mapYSize - 1 - i][j]) {
                    return true;
                } else if (this.passableMap[i][j] != this.passableMap[i][this.mapXSize - 1 - j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public void getCastle() {
        Robot[] robo = getVisibleRobots();
        for (Robot r : robo) {
            if (r.unit == SPECS.CASTLE) {
                castleLocations[0] = new int[] { r.x, r.y };
                this.numCastles++;
                robots[0].add(r.id);
            }
        }
    }

    public int[] findClosestKarbo() {
        int minDistance = this.mapYSize * this.mapYSize;
        int[] ans = null;
        for (int x = 0; x < this.mapXSize; x++) {
            outer: for (int y = 0; y < this.mapYSize; y++) {
                if (this.karboniteMap[y][x]) {
                    int[] temp = new int[] { x, y };
                    for (int[] out : karbosInUse) {
                        if (out[0] == temp[0] && out[1] == temp[1]) {
                            if (visibleRobotMap[y][x] == 0) {
                                karbosInUse.remove(out);
                            } else {
                                continue outer;
                            }
                        }
                    }
                    if (visibleRobotMap[y][x] > 0) {
                        karbosInUse.add(temp);
                        continue outer;
                    }
                    int dx = x - me.x;
                    int dy = y - me.y;
                    if (dx * dx + dy * dy < minDistance) {
                        ans = temp;
                        minDistance = dx * dx + dy * dy;
                    }
                }
            }
        }
        return ans;
    }

    public int[] findClosestFuel() {
        int minDistance = this.mapYSize * this.mapYSize;
        int[] ans = new int[] { 0, 0 };
        for (int x = 0; x < this.mapXSize; x++) {
            outer: for (int y = 0; y < this.mapYSize; y++) {
                if (this.fuelMap[y][x]) {
                    int[] temp = new int[] { x, y };
                    for (int[] out : fuelsInUse) {
                        if (out[0] == temp[0] && out[1] == temp[1]) {
                            if (visibleRobotMap[y][x] == 0) {
                                fuelsInUse.remove(out);
                            } else {
                                continue outer;
                            }
                        }
                    }
                    if (visibleRobotMap[y][x] > 0) {
                        fuelsInUse.add(temp);
                        continue outer;
                    }
                    int dx = x - me.x;
                    int dy = y - me.y;
                    if (dx * dx + dy * dy < minDistance) {
                        ans = temp;
                        minDistance = dx * dx + dy * dy;
                    }
                }
            }
        }
        return ans;
    }

    public void getEnemyCastleLocs() {
        for (int i = 0; i < this.numCastles; i++) {
            if (horizontalReflection) {
                evilCastleLocations[i][0] = this.mapYSize - 1 - castleLocations[i][0];
                evilCastleLocations[i][1] = castleLocations[i][1];
            } else {
                evilCastleLocations[i][0] = castleLocations[i][0];
                evilCastleLocations[i][1] = this.mapYSize - 1 - castleLocations[i][1];
            }
        }
    }

    public HashSet<Robot> findBadGuys() {
        HashSet<Robot> theBadGuys = new HashSet<Robot>();
        Robot[] visibleBots = this.getVisibleRobots();
        for (int i = 0; i < visibleBots.length; i++) {
            if (me.team != visibleBots[i].team) {
                theBadGuys.add(visibleBots[i]);
            }
        }
//      this.log("bad guys = " + theBadGuys.size());
        return theBadGuys;
    }

    // Finds closest enemy robot
    public Robot findPrimaryEnemyDistance(HashSet<Robot> potentialEnemies) {
        double distance = Double.MAX_VALUE;
        Robot closeBot = null;
        Iterator<Robot> badGuyIter = potentialEnemies.iterator();
        Robot aBadGuy;
        double badGuyDistance;
        while (badGuyIter.hasNext()) {
            aBadGuy = badGuyIter.next();
            badGuyDistance = this.findDistance(me, aBadGuy);
            if (badGuyDistance < distance && this.canAttack(badGuyDistance)) {
                distance = badGuyDistance;
                closeBot = aBadGuy;
            }

        }
        if (closeBot != null) {
            this.log("found an enemy!");
        }
        return closeBot;
    }

    // groups enemies by type
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

    // finds the closest enemy with priority by type
    public Robot findPrimaryEnemyTypeDistance(HashSet<Robot> potentialEnemies) {
        HashMap<Integer, HashSet<Robot>> groupedEnemies = groupByType(potentialEnemies);
        if (!groupedEnemies.get(SPECS.PREACHER).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PREACHER));
        }
        if (!groupedEnemies.get(SPECS.CRUSADER).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CRUSADER));
        }
        if (!groupedEnemies.get(SPECS.PROPHET).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PROPHET));
        }
        if (!groupedEnemies.get(SPECS.CASTLE).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CASTLE));
        }
        if (!groupedEnemies.get(SPECS.PILGRIM).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PILGRIM));
        }
        return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CHURCH));
    }

    public void getNumKarbos() {
        for (boolean[] row : this.karboniteMap) {
            for (boolean b : row) {
                this.numKarbos += b ? 1 : 0;
            }
        }
    }

    public void getNumFuels() {
        for (boolean[] row : this.fuelMap) {
            for (boolean b : row) {
                this.numFuels += b ? 1 : 0;
            }
        }
    }

    public void getDepositSpots() {
        karboSpots = new int[numKarbos][2];
        fuelSpots = new int[numFuels][2];
        int karboIndex = 0;
        int fuelIndex = 0;
        for (int x = 0; x < this.mapXSize; x++) {
            for (int y = 0; y < this.mapYSize; y++) {
                if (this.karboniteMap[y][x]) {
                    karboSpots[karboIndex][0] = x;
                    karboSpots[karboIndex][1] = y;
                    karboIndex++;
                } else if (this.fuelMap[y][x]) {
                    fuelSpots[fuelIndex][0] = x;
                    fuelSpots[fuelIndex][1] = y;
                    fuelIndex++;
                }
            }
        }
    }

    public void fillAllMines() {
        this.getDepositSpots();
        allDeposits = new int[numKarbos + numFuels][];
        int i = 0;
        for (int[] mine : karboSpots) {
            allDeposits[i] = mine;
            i++;
        }
        for (int[] mine : fuelSpots) {
            allDeposits[i] = mine;
            i++;
        }
    }

    public void getClumpDepositNums() {
        clumpNumDeposits = new int[allDeposits.length];
        int index = 0;
        for (int[] check : allDeposits) {
            int count = 0;
            for (int[] mine : allDeposits) {
                if (tilesInRange(check, mine, clumpRadius2)) {
                    count++;
                }
            }
            clumpNumDeposits[index++] = count;
        }
    }

    public void displayAllClumps(ArrayList<HashSet<int[]>> allClumps) {
        for (int i = 0; i < allClumps.size(); i++) {
            log("Clump " + i + ": " + allClumps.get(i));
        }
    }

    public ArrayList<HashSet<int[]>> findAllClumps(ArrayList<int[]> sortedResources) {
        ArrayList<HashSet<int[]>> everyClump = new ArrayList<HashSet<int[]>>();
        while (sortedResources.size() > 0) {
            everyClump.add(findClump(sortedResources));
            int lastIndex = everyClump.size() - 1;
            Iterator<int[]> clumpRemovalFromArrayListIterator = everyClump.get(lastIndex).iterator();
            while (clumpRemovalFromArrayListIterator.hasNext()) {
                sortedResources.remove(clumpRemovalFromArrayListIterator.next());
            }
        }
        return everyClump;
    }

    public HashSet<int[]> findClump(ArrayList<int[]> sortedResources) {
        HashSet<int[]> clump = new HashSet<int[]>();
        clump.add(sortedResources.get(0));
        for (int i = 1; i < sortedResources.size(); i++) {
            Iterator<int[]> clumpIterator = clump.iterator();
            while (clumpIterator.hasNext()) {
                int[] aClumpLocation = clumpIterator.next();
                if (this.findDistance(sortedResources.get(i), aClumpLocation) <= 9) {
                    clump.add(sortedResources.get(i));
                }
            }
        }
        return clump;
    }

    public double findDistance(int[] location1, int[] location2) {
        int xDistance = location2[1] - location1[1];
        int yDistance = location2[0] - location1[0];
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
    }

    public ArrayList<int[]> findSortedResources() {
        boolean[][] karboniteMap = getKarboniteMap();
        boolean[][] fuelMap = getFuelMap();
        ArrayList<int[]> sortedResources = new ArrayList<int[]>();

        // Goes through the map
        // Checks to make sure I got my y's and x's correct
        for (int y = 0; y < karboniteMap.length; y++) {
            for (int x = 0; x < karboniteMap[y].length; x++) {
                if (karboniteMap[y][x] == true) {
                    int[] location = new int[2];
                    location[0] = y;
                    location[1] = x;
                    sortedResources.add(location);
                }
                if (fuelMap[y][x] == true) {
                    int[] location = new int[2];
                    location[0] = y;
                    location[1] = x;
                    sortedResources.add(location);
                }
            }
        }
        quickSort(sortedResources, 0, sortedResources.size() - 1);
        // At this point, sortedResources has all of the fuel and karbonite locations
        // from the map
        return sortedResources;
    }

    public void quickSort(ArrayList<int[]> resources, int start, int end) {
        if (start < end) { // general case
            int pivot = partition(resources, start, end);
            // sort left sublist
            quickSort(resources, start, pivot - 1);
            // sort the right sublist
            quickSort(resources, pivot + 1, end);
        }
    }

    public int partition(ArrayList<int[]> resources, int start, int end) {
        int[] pivot;
        int endOfLeft;
        int midIndex = (start + end) / 2;
        swap(resources, start, midIndex);
        pivot = resources.get(start);
        endOfLeft = start;
        for (int i = start + 1; i <= end; i++) {
            log("Resources get i: " + resources.get(i));
            log("Pivot: " + pivot);
            if (findDistance(resources.get(i)) < findDistance(pivot)) {
                endOfLeft = endOfLeft + 1;
                swap(resources, endOfLeft, i);
            }
        }
        swap(resources, start, endOfLeft);
        return endOfLeft;
    }

    public static void swap(ArrayList<int[]> resources, int i, int j) {
        int[] tmp = resources.get(i);
        resources.set(i, resources.get(j));
        resources.set(j, tmp);
    }

    public void findClumps() {
        ArrayList<int[]> deposits = new ArrayList<>();
        clumps = new ArrayList<>();
        while (deposits.size() != allDeposits.length) {
            int b = -1;
            for (int i = 0; i < clumpNumDeposits.length; i++) {
                if (deposits.contains(allDeposits[i])) {
                    continue;
                }
                if (b == -1 || clumpNumDeposits[i] > clumpNumDeposits[b]) {
                    b = i;
                }
            }
            int[] best = allDeposits[b];
            ArrayList<int[]> currentClump = new ArrayList<>();
            currentClump.add(best);
            deposits.add(best);
            for (int[] mine : allDeposits) {
                if (deposits.contains(mine)) {
                    continue;
                }
                if (tilesInRange(best, mine, clumpRadius2)) {
                    currentClump.add(mine);
                    deposits.add(mine);
                }
            }
            clumps.add(currentClump);
        }
    }

    public void findClumpCenters() {
        clumpCenters = new ArrayList<int[]>();
        int clusterSize;
        double sumX, sumY;
        for (ArrayList<int[]> cluster : clumps) {
            clusterSize = cluster.size();
            sumX = 0;
            sumY = 0;
            for (int[] mine : cluster) {
                sumX += mine[0];
                sumY += mine[1];
            }
            int avgX = (int) Math.round(sumX / clusterSize);
            int avgY = (int) Math.round(sumY / clusterSize);

            if (this.passableMap[avgY][avgX] && !this.fuelMap[avgY][avgX] && !this.karboniteMap[avgY][avgX]) { // actual
                                                                                                                // center
                                                                                                                // is
                                                                                                                // good
                clumpCenters.add(new int[] { avgX, avgY });
            } else { // find next option
                int[] best1 = new int[] { 0, 0 }, best2 = new int[] { 0, 0 };
                double closestDistance = Double.MAX_VALUE;
                double distance;
                for (int x = 0; x < this.mapXSize; x++) {
                    for (int y = 0; y < this.mapYSize; y++) {
                        distance = this.findDistance(x, y, avgX, avgY);
                        if (!this.passableMap[y][x]) { // skip impassable
                            continue;
                        }
                        if (distance < closestDistance) {
                            closestDistance = distance;
                            if (!this.fuelMap[y][x] && !this.karboniteMap[y][x]) { // not on deposit
                                best1[0] = x;
                                best1[1] = y;
                            } else { // on deposit
                                best2[0] = x;
                                best2[1] = y;
                            }
                        }
                    }
                }
                if (best1[0] != 0 && best1[1] != 0) { // found center not on deposit
                    clumpCenters.add(best1);
                } else {
                    clumpCenters.add(best2);
                }
            }
        }
        isClumpOccupied = new boolean[clumpCenters.size()];
    }

    public boolean tilesInRange(int[] tile1, int[] tile2, int rangeSquared) {
        return ((tile1[0] - tile2[0]) * (tile1[0] - tile2[0])
                + (tile1[1] - tile2[1]) * (tile1[1] - tile2[1]) <= rangeSquared);
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

    public double findDistance(int[] location) {
        int xDistance = location[0] - me.x;
        int yDistance = location[1] - me.y;
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
    }

    // Finds distance squared between two pairs of coordinates
    public double findDistance(int x1, int y1, int x2, int y2) {
        int xDistance = x1 - x2;
        int yDistance = y1 - y2;
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
    }

    // checks if the robot is in attack range
    public boolean canAttack(double distance) {
        return this.getMinAttackRangeRadius(this.me.unit) <= Math.sqrt(distance)
                && Math.sqrt(distance) <= this.getMaxAttackRangeRadius(this.me.unit)
                && this.fuel >= this.getAttackFuel(this.me.unit);
    }

    // other robots can attack?
    public boolean canAttack(Robot r, double distance) {
        return this.getMinAttackRangeRadius(r.unit) <= Math.sqrt(distance)
                && Math.sqrt(distance) <= this.getMaxAttackRangeRadius(r.unit);
    }

    // can this unit be built? do we have enough fuel and karbonite? is there room?
    public boolean canBuild(int type) {
        return this.fuel > SPECS.UNITS[type].CONSTRUCTION_FUEL
                && this.karbonite > SPECS.UNITS[type].CONSTRUCTION_KARBONITE
                && this.checkAdjacentBuildAvailable() != null;
    }

    // checks if adjacent tiles are available. used for making units. checks tiles
    // closer to the middle of the map first. //TODO build pilgrims on deposits, and
    // other units not on deposits. if possible
    public int[] checkAdjacentBuildAvailable() {
        int x = this.me.x;
        int y = this.me.y;
        int dx = x - this.mapXSize / 2;
        int dy = y - this.mapYSize / 2;
        if (dx > 0) { // robot is to the east of center, check left first, then up down, then right
            if (x > 0) { // can check left
                if (dy > 0) { // robot is to the south of center, check up then middle then down
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x - 1] && visibleRobotMap[y - 1][x - 1] == 0) { // checks up left
                            return new int[] { y - 1, x - 1 };
                        }
                    }
                    if (this.passableMap[y][x - 1] && visibleRobotMap[y][x - 1] == 0) { // checks middle left
                        return new int[] { y, x - 1 };
                    }
                    if (y < mapYSize - 1) { // can check down
                        if (this.passableMap[y + 1][x - 1] && visibleRobotMap[y + 1][x - 1] == 0) { // checks down left
                            return new int[] { y + 1, x - 1 };
                        }
                    }
                } else { // robot is to the north or level of center, check down then middle then up
                    if (y > 0) { // can check up
                        if (y < mapYSize - 1) { // can check down
                            if (this.passableMap[y + 1][x - 1] && visibleRobotMap[y + 1][x - 1] == 0) { // checks down
                                                                                                        // left
                                return new int[] { y + 1, x - 1 };
                            }
                        }
                        if (this.passableMap[y][x - 1] && visibleRobotMap[y][x - 1] == 0) { // checks middle left
                            return new int[] { y, x - 1 };
                        }
                        if (this.passableMap[y - 1][x - 1] && visibleRobotMap[y - 1][x - 1] == 0) { // checks up left
                            return new int[] { y - 1, x - 1 };
                        }
                    }
                }
            }
            if (dy > 0) { // robot is to the south of center, check up then down
                if (y > 0) { // can check up
                    if (this.passableMap[y - 1][x] && visibleRobotMap[y - 1][x] == 0) { // checks middle up
                        return new int[] { y - 1, x };
                    }
                }
                if (y < mapYSize - 1) { // can check down
                    if (this.passableMap[y + 1][x] && visibleRobotMap[y + 1][x] == 0) { // checks middle down
                        return new int[] { y + 1, x };
                    }
                }
            } else { // robot is to the north or level of center, check down then up
                if (y < mapYSize - 1) { // can check down
                    if (this.passableMap[y + 1][x] && visibleRobotMap[y + 1][x] == 0) { // checks middle down
                        return new int[] { y + 1, x };
                    }
                }
                if (y > 0) { // can check up
                    if (this.passableMap[y - 1][x] && visibleRobotMap[y - 1][x] == 0) { // checks middle up
                        return new int[] { y - 1, x };
                    }
                }
            }
            if (x < mapXSize - 1) { // can check right
                if (dy > 0) { // robot is to the south of center, check up then middle then down
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x + 1] && visibleRobotMap[y - 1][x + 1] == 0) { // checks up right
                            return new int[] { y - 1, x + 1 };
                        }
                    }
                    if (this.passableMap[y][x + 1] && visibleRobotMap[y][x + 1] == 0) { // checks middle right
                        return new int[] { y, x + 1 };
                    }
                    if (y < mapYSize) { // can check down
                        if (this.passableMap[y + 1][x + 1] && visibleRobotMap[y + 1][x + 1] == 0) { // checks down right
                            return new int[] { y + 1, x + 1 };
                        }
                    }
                } else { // robot is north or level of center, check down then middle then up
                    if (y < mapYSize) { // can check down
                        if (this.passableMap[y + 1][x + 1] && visibleRobotMap[y + 1][x + 1] == 0) { // checks down right
                            return new int[] { y + 1, x + 1 };
                        }
                    }
                    if (this.passableMap[y][x + 1] && visibleRobotMap[y][x + 1] == 0) { // checks middle right
                        return new int[] { y, x + 1 };
                    }
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x + 1] && visibleRobotMap[y - 1][x + 1] == 0) { // checks up right
                            return new int[] { y - 1, x + 1 };
                        }
                    }
                }
            }
        } else { // robot is to the west or level of center, check right first, then up down,
                    // then left
            if (x < mapXSize - 1) { // can check right
                if (dy > 0) { // robot is to the south of center, check up then middle then down
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x + 1] && visibleRobotMap[y - 1][x + 1] == 0) { // checks up right
                            return new int[] { y - 1, x + 1 };
                        }
                    }
                    if (this.passableMap[y][x + 1] && visibleRobotMap[y][x + 1] == 0) { // checks middle right
                        return new int[] { y, x + 1 };
                    }
                    if (y < mapYSize) { // can check down
                        if (this.passableMap[y + 1][x + 1] && visibleRobotMap[y + 1][x + 1] == 0) { // checks down right
                            return new int[] { y + 1, x + 1 };
                        }
                    }
                } else { // robot is north or level of center, check down then middle then up
                    if (y < mapYSize) { // can check down
                        if (this.passableMap[y + 1][x + 1] && visibleRobotMap[y + 1][x + 1] == 0) { // checks down right
                            return new int[] { y + 1, x + 1 };
                        }
                    }
                    if (this.passableMap[y][x + 1] && visibleRobotMap[y][x + 1] == 0) { // checks middle right
                        return new int[] { y, x + 1 };
                    }
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x + 1] && visibleRobotMap[y - 1][x + 1] == 0) { // checks up right
                            return new int[] { y - 1, x + 1 };
                        }
                    }
                }
            }
            if (dy > 0) { // robot is to the south of center, check up then down
                if (y > 0) { // can check up
                    if (this.passableMap[y - 1][x] && visibleRobotMap[y - 1][x] == 0) { // checks middle up
                        return new int[] { y - 1, x };
                    }
                }
                if (y < mapYSize - 1) { // can check down
                    if (this.passableMap[y + 1][x] && visibleRobotMap[y + 1][x] == 0) { // checks middle down
                        return new int[] { y + 1, x };
                    }
                }
            } else { // robot is to the north or level of center, check down then up
                if (y < mapYSize - 1) { // can check down
                    if (this.passableMap[y + 1][x] && visibleRobotMap[y + 1][x] == 0) { // checks middle down
                        return new int[] { y + 1, x };
                    }
                }
                if (y > 0) { // can check up
                    if (this.passableMap[y - 1][x] && visibleRobotMap[y - 1][x] == 0) { // checks middle up
                        return new int[] { y - 1, x };
                    }
                }
            }
            if (x > 0) { // can check left
                if (dy > 0) { // robot is to the south of center, check up then middle then down
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x - 1] && visibleRobotMap[y - 1][x - 1] == 0) { // checks up left
                            return new int[] { y - 1, x - 1 };
                        }
                    }
                    if (this.passableMap[y][x - 1] && visibleRobotMap[y][x - 1] == 0) { // checks middle left
                        return new int[] { y, x - 1 };
                    }
                    if (y < mapYSize - 1) { // can check down
                        if (this.passableMap[y + 1][x - 1] && visibleRobotMap[y + 1][x - 1] == 0) { // checks down left
                            return new int[] { y + 1, x - 1 };
                        }
                    }
                } else { // robot is north or level of center, check down then middle then up
                    if (y < mapYSize - 1) { // can check down
                        if (this.passableMap[y + 1][x - 1] && visibleRobotMap[y + 1][x - 1] == 0) { // checks down left
                            return new int[] { y + 1, x - 1 };
                        }
                    }
                    if (this.passableMap[y][x - 1] && visibleRobotMap[y][x - 1] == 0) { // checks middle left
                        return new int[] { y, x - 1 };
                    }
                    if (y > 0) { // can check up
                        if (this.passableMap[y - 1][x - 1] && visibleRobotMap[y - 1][x - 1] == 0) { // checks up left
                            return new int[] { y - 1, x - 1 };
                        }
                    }
                }
            }
        }
        return null; // surrounded by impassable terrain
    }

    // gets the movement speed radius of a unit
    public int getMovementRangeRadius(int unit) {
        return (int) Math.sqrt(SPECS.UNITS[unit].SPEED);
    }

    // gets the minimum attack range radius of a unit
    public int getMinAttackRangeRadius(int unit) {
        return (int) Math.sqrt(SPECS.UNITS[unit].ATTACK_RADIUS[0]);
    }

    // gets the maximum attack range radius of a unit
    public int getMaxAttackRangeRadius(int unit) {
        return (int) Math.sqrt(SPECS.UNITS[unit].ATTACK_RADIUS[1]);
    }

    // gets the vision range radius of a unit
    public int getVisionRangeRadius(int unit) {
        return (int) Math.sqrt(SPECS.UNITS[unit].VISION_RADIUS);
    }

    // gets attacking fuel
    public int getAttackFuel(int unit) {
        return SPECS.UNITS[unit].ATTACK_FUEL_COST;
    }

    // gets attack damage
    public int getAttackDamage(int unit) {
        return SPECS.UNITS[unit].ATTACK_DAMAGE;
    }

    public AttackAction preacherAttack() {
        Robot[] robots = this.getVisibleRobots();
        int maxAttackRange = this.getMaxAttackRangeRadius(this.me.unit);
        // this.log(maxAttackRange + "");
        int x = -1, y = -1, maxValue = 0;
        int[][] robotMap = new int[this.mapYSize][this.mapXSize];
        for (int[] row : robotMap) {
            Arrays.fill(row, 0);
        }
        for (Robot robot : robots) {
            robotMap[robot.y][robot.x] = this.getRobotValue(robot);
            // this.log(robotMap[robot.y][robot.x] + "");
        }
        int value;
        for (int r = Math.max(this.me.y - maxAttackRange, 1); r < Math.min(this.me.y + maxAttackRange + 1,
                this.mapYSize - 1); r++) {
            for (int c = Math.max(this.me.x - maxAttackRange, 1); c < Math.min(this.me.x + maxAttackRange + 1,
                    this.mapXSize - 1); c++) {
                if (this.canAttack(Math.sqrt(this.findDistance(this.me, c, r)))) {
                    // this.log("can attack");
                    value = this.sumAdjacent(robotMap, r, c);
                    // this.log(value + " x=" + c + " y=" + r + " turn =" + this.turn);
                    if (maxValue < value) {
                        maxValue = value;
                        x = c;
                        y = r;
                    }
                }
            }
        }
        // this.log("c=" + x + " r=" + y);
        // this.log("x=" + (x-this.me.x) + " y=" + (y-this.me.y));
        return this.attack(x - this.me.x, y - this.me.y);
    }

    // gets the robot value
    public int getRobotValue(Robot r) {
        if (r.team == this.me.team) { // ally
            if (r.unit == SPECS.CASTLE) {
                return -10; // we REALLY don't want to hit our own castles
            }
            if (r.unit == SPECS.PREACHER) {
                return -4; // we really don't want to hit our own preachers
            }
            if (r.unit == SPECS.CRUSADER) {
                return -3;
            }
            if (r.unit == SPECS.PILGRIM) {
                return -2;
            }
            if (r.unit == SPECS.PROPHET) {
                return -3;
            }
            return -1;
        } else { // enemy
            if (r.unit == SPECS.CRUSADER) {
                return 2;
            }
            if (r.unit == SPECS.PREACHER) {
                return 3;
            }
            if (r.unit == SPECS.PILGRIM) {
                return 1;
            }
            if (r.unit == SPECS.PROPHET) {
                return 2;
            }
            return 1; // unlikely to see structures, but it'll just be 1
        }
    }

    // finds the robot average of a 3x3 area
    public int sumAdjacent(int[][] map, int x, int y) {
        int sum = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                sum += map[x + i][y + j];
            }
        }
        return sum;
    }

    public int[] findClosestDeposit() {
        int minDistance = this.mapYSize * this.mapYSize;
        int[] ans = null;

        for (int[] spot : fuelSpots) {
            if (fuelsInUse.contains(spot)) {
                if (visibleRobotMap[spot[1]][spot[0]] == 0) {
                    fuelsInUse.remove(spot);
                }
                continue;
            }
            if (visibleRobotMap[spot[1]][spot[0]] > 0) {
                fuelsInUse.add(spot);
                continue;
            }
            int dx = spot[0] - me.x;
            int dy = spot[1] - me.y;
            if (dx * dx + dy * dy < minDistance) {
                ans = spot;
                minDistance = dx * dx + dy * dy;
            }
        }
        for (int[] spot : karboSpots) {
            if (karbosInUse.contains(spot)) {
                if (visibleRobotMap[spot[1]][spot[0]] == 0) {
                    karbosInUse.remove(spot);
                }
                continue;
            }
            if (visibleRobotMap[spot[1]][spot[0]] > 0) {
                karbosInUse.add(spot);
                continue;
            }
            int dx = spot[0] - me.x;
            int dy = spot[1] - me.y;
            if (dx * dx + dy * dy < minDistance) {
                ans = spot;
                minDistance = dx * dx + dy * dy;
            }
        }
        return ans;
    }

    // bfs
    public ArrayList<int[]> bfs(int finalX, int finalY) {
        pathIndex = 0;

        boolean occupied = visibleRobotMap[finalY][finalX] > 0;
        int[] closest = null;
        double minDistance = this.findDistance(finalX, finalY, this.me.x, this.me.y);
        int speed = this.getMovementRangeRadius(this.me.unit);
        LinkedList<int[]> toVisit = new LinkedList<int[]>();
        int[] current = new int[] { me.x, me.y };
        int[] tracer = new int[this.mapYSize * this.mapYSize];
        Arrays.fill(tracer, -1);

        int dx, dy;
        int[] newSpot;
        int left, top, right, bottom;
        while (!(current[0] == finalX && current[1] == finalY)) {
            left = Math.max(0, current[0] - speed);
            top = Math.max(0, current[1] - speed);
            right = Math.min(this.mapXSize - 1, current[0] + speed);
            bottom = Math.min(this.mapYSize - 1, current[1] + speed);

            for (int x = left; x <= right; x++) {
                dx = x - current[0];
                for (int y = top; y <= bottom; y++) {
                    dy = y - current[1];
                    if (dx * dx + dy * dy <= speed * speed && this.passableMap[y][x] && (visibleRobotMap[y][x] <= 0)) {
                        if (tracer[y * this.mapYSize + x] != -1) {
                            continue;
                        }
                        newSpot = new int[] { x, y };
                        tracer[y * this.mapYSize + x] = current[1] * this.mapYSize + current[0];

                        if (occupied) {
                            if (this.findDistance(finalX, finalY, x, y) < minDistance) {
                                minDistance = this.findDistance(finalX, finalY, x, y);
                                closest = newSpot;
                                continue;
                            }
                        }
                        toVisit.add(newSpot);
                    }
                }
            }

            if (occupied && closest != null) {
                current = closest;
                break;
            }

            current = toVisit.poll();
            if (current == null) {
                return null;
            }
        }
        ArrayList<int[]> results = new ArrayList<>();
        while (tracer[current[1] * this.mapYSize + current[0]] != -1) {
            results.add(0, current);
            int previous = tracer[current[1] * this.mapYSize + current[0]];
            current = new int[] { previous % this.mapYSize, (int) (previous / this.mapYSize) };
        }
        return results;
    }

    public int[] checkAdjacentAvailableRandom() {
        int randomNum, x, y;
        randomNum = (int) (Math.random() * 8);
        int i = 0;
        do {
            randomNum += 1;
            randomNum %= 8;
            i++;
            x = me.x + adjacents[randomNum][0];
            y = me.y + adjacents[randomNum][1];

            if (i >= 8) {
                return null;
            }
        } while (x < 0 || x >= this.mapYSize || y < 0 || y >= this.mapYSize || !this.passableMap[y][x]
                || this.visibleRobotMap[y][x] > 0);

        return adjacents[randomNum];
    }

    public void findCastleDirection() {
        if (castleLocations[0][0] - me.x == 0) {
            castleDirection = castleLocations[0][1] - me.y * -2 + 2;
        } else if (castleLocations[0][0] - me.x == -1) {
            castleDirection = castleLocations[0][1] - me.y * -1 + 2;
        } else if (castleLocations[0][0] - me.x == 1) {
            castleDirection = castleLocations[0][1] - me.y + 6;
        }
    }

    public int[] goLattice() {
        int[] spot;
        int x, y;
        if (castleDirection % 2 == 0) {
            int randomDirection = (int) (Math.random() * 2);
            if (randomDirection == 0) {
                spot = adjacents[(castleDirection + 4) % 8];
            } else {
                spot = adjacents[sideDirection];
            }
            spot = new int[] { spot[0] * 2, spot[1] * 2 };
            x = me.x + spot[0];
            y = me.y + spot[1];
            if (x >= 0 && x < this.mapYSize && y >= 0 && y < this.mapYSize && this.passableMap[y][x]
                    && this.visibleRobotMap[y][x] <= 0) {
                return spot;
            }
            spot = (randomDirection != 0) ? (adjacents[(castleDirection + 4) % 8]) : adjacents[sideDirection];
            spot = new int[] { spot[0] * 2, spot[1] * 2 };
            x = me.x + spot[0];
            y = me.y + spot[1];
            if (x >= 0 && x < this.mapYSize && y >= 0 && y < this.mapYSize && this.passableMap[y][x]
                    && this.visibleRobotMap[y][x] <= 0) {
                return spot;
            }
        } else {
            int randomDirection = ((int) (Math.random() * 2)) * 2 - 1;
            spot = adjacents[(castleDirection + 4 + randomDirection) % 8];
            spot = new int[] { spot[0] * 2, spot[1] * 2 };
            x = me.x + spot[0];
            y = me.y + spot[1];
            if (x >= 0 && x < this.mapYSize && y >= 0 && y < this.mapYSize && this.passableMap[y][x]
                    && this.visibleRobotMap[y][x] <= 0) {
                return spot;
            }
            spot = adjacents[(castleDirection + 4 - randomDirection) % 8];
            spot = new int[] { spot[0] * 2, spot[1] * 2 };
            x = me.x + spot[0];
            y = me.y + spot[1];
            if (x >= 0 && x < this.mapYSize && y >= 0 && y < this.mapYSize && this.passableMap[y][x]
                    && this.visibleRobotMap[y][x] <= 0) {
                return spot;
            }
        }

        return null;
    }

    public boolean adjacentToHome(int x, int y) {
        if (Math.abs(x - castleLocations[0][0]) <= 1 && Math.abs(y - castleLocations[0][1]) <= 1) {
            return true;
        }
        return false;
    }

    public ArrayList<int[]> lattice() {
        int x, y, range;
        for (int visionRange = 1; visionRange < 64; visionRange++) {
            range = (int) (Math.floor(Math.sqrt(visionRange)));
            for (int dx = -range; dx <= range; dx++) {
                for (int dy = -range; dy <= range; dy++) {
                    if (dx * dx + dy * dy == visionRange) {
                        x = me.x + dx;
                        y = me.y + dy;
                        if (onMap(x, y) && this.passableMap[y][x] && this.visibleRobotMap[y][x] <= 0
                                && !this.fuelMap[y][x] && !this.karboniteMap[y][x] && (x + y) % 2 == 0
                                && !adjacentToHome(x, y)) {
                            return this.bfs(x, y);
                        }
                    }
                }
            }
        }
        return null;
    }

    public boolean onMap(int x, int y) {
        return (x >= 0 && x < this.mapYSize && y >= 0 && y < this.mapYSize);
    }

    public Robot getTheCastle(int num) {
        Robot[] visb = getVisibleRobots();

        for (Robot cast : visb) {
            if (cast.id == robots[0].get(num)) {
                return cast;
            }
        }
        return null;
    }

}
