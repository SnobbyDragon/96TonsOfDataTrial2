package bc19;

import bc19.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;

public class MyRobot extends BCAbstractRobot {
    // important
	public int mapYSize, mapXSize; //size of the map, length y and length x
    public boolean horizontalReflection;
    public int[][] visibleRobotMap;
    public boolean[][] passableMap;
    public int xorKey; // XOR any signal by this, and any castletalk by this % 256
    // Note: the encodedCastleLocs are sort of separate and thus XOR'd with this % 256
    // separately; don't worry 'bout it.
    public final int[][] adjacentSpaces = new int[][] { // Matrix of adjacent spaces, relative to the Robot
    new int[] {0,1},
    new int[] {-1,1},
    new int[] {-1,0},
    new int[] {-1,-1},
    new int[] {0,-1},
    new int[] {1,-1},
    new int[] {1,0},
    new int[] {1,1}
    };
    public int numCastles; // DO NOT USE for castles. Use robots[0].size() instead.
    public int ourDeadCastles = 0;
    public int[][] castleLocs = new int[3][2]; // {{x, y}, {x, y}, {x, y}}
    public int[][] enemyCastleLocs = new int[3][2]; // {{x, y}, {x, y}, {x, y}}
    public int globalTurn;
    
    // for castles
    public ArrayList<Integer>[] robots = new ArrayList[6];
    public int numFuelMines = 0;
    public int numKarbMines = 0;
    public int pilgrimLim; // will be slightly higher for non-castles, fine since it's just an approx for them
    
    // For pathing
    public ArrayList<int[]> currentPath = null;
    public int locInPath;
    
    // For lattice
    public int castleDir;
    public int sideDir;
    public boolean arrived;
    
    public int castleIndex;
    public int top;
    public int left;
    public int numUnits = 0;
    public int numMines = 0;
    public int numKarbos = 0;
    public int numFuels = 0;
    public int myMineScore;
    public int[][] allKarbos;
    public int[][] allFuels;
    public int[] allMineScores;
    public int currentColonization;
    public boolean[] isMineColonized;
    public ArrayList<int[]> karbosInUse = new ArrayList<>();
    public ArrayList<int[]> fuelsInUse = new ArrayList<>();
    public int[] HOME;
    public int[] castleIDs = new int[3];
    
    public final int mineClusterRadiusSqrd = 25;
    public ArrayList<int[]> mineClusterCenters;
    public ArrayList<ArrayList<int[]>> mineClusters;
    public int[][] allMines;
    
    
    public Action turn() {
        if (me.turn == 1) {
        	this.mapYSize = this.map.length;
        	this.mapXSize = this.map[0].length;
        	this.passableMap = this.getPassableMap();
            for(int i = 0; i < 6; i++)
            {
                robots[i] = new ArrayList<Integer>();
            }
            
            horizontalReflection = getReflDir();
            setXorKey();
        }
        else
        {
            globalTurn += 1;
        }
        this.visibleRobotMap = getVisibleRobotMap();
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
            fillAllMines();
            getMineScores();
            identifyClusters();
            findClusterCenters();
            
            numCastles = 1;
            castleIDs[0] = me.id;
            
            for (Robot r : getVisibleRobots()) {
                if (r.team == me.team && r.id != me.id) {
                    castleIDs[numCastles] = r.id;
                    numCastles += 1;
                }
            }
            
            int[] myMine = findClosestMine();
            for (int i = 0; i < mineClusters.size(); i++) {
                if (mineClusters.get(i).contains(myMine)) {
                    log("found my mine");
                    myMineScore = mineClusters.get(i).size();
                    currentColonization = i;
                    isMineColonized[i] = true;
                }
            }
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
                isMineColonized[castle.castle_talk - 1] = true;
            }
        }
        
        log("castles = " + numCastles);
        /*
         * log("global population: " + numUnits); boolean haveNeighbors = false; for
         * (int[] move : adjacentSpaces) { int tryX = me.x + move[0]; int tryY = me.y +
         * move[1]; if (tryX <= -1 || tryX >= this.mapXSize || tryY <= -1 || tryY >=
         * this.mapYSize) { continue; } if (robotMap[tryY][tryX] > 0) { haveNeighbors =
         * true; break; } } if (haveNeighbors) { signal(numUnits, 2); }
         */
        if (numUnits < myMineScore) {
            if (fuel < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_FUEL + 2
                || karbonite < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_KARBONITE) {
                return null;
            }
            for (int[] move : adjacentSpaces) {
                int buildX = me.x + move[0];
                int buildY = me.y + move[1];
                if (buildX <= -1 || buildX >= this.mapXSize || buildY <= -1 || buildY >= this.mapYSize
                    || this.passableMap[buildY][buildX] || this.visibleRobotMap[buildY][buildX] > 0) {
                    continue;
                }
                numUnits++;
                log("castle is signalling their location at " + (64 * me.y + me.x));
                signal(64 * me.y + me.x, 2);
                castleTalk(currentColonization + 1);
                return buildUnit(SPECS.PILGRIM, move[0], move[1]);
            }
        }
        
        if (/* numUnits >= numMines || */fuel < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_FUEL + 2
            || karbonite < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_KARBONITE) {
            return null;
        }
        
        for (int i = 0; i < mineClusterCenters.size(); i++) {
            if (isMineColonized[i]) {
                continue;
            }
            int[] center = mineClusterCenters.get(i);
            currentColonization = i;
            isMineColonized[i] = true;
            for (int[] move : adjacentSpaces) {
                int buildX = me.x + move[0];
                int buildY = me.y + move[1];
                if (buildX <= -1 || buildX >= this.mapXSize || buildY <= -1 || buildY >= this.mapYSize
                    || this.passableMap[buildY][buildX] || this.visibleRobotMap[buildY][buildX] > 0) {
                    continue;
                }
                numUnits++;
                log("signalling colonization spot at " + (64 * center[1] + center[0]));
                signal(64 * center[1] + center[0], 2);
                castleTalk(currentColonization + 1);
                return buildUnit(SPECS.PILGRIM, move[0], move[1]);
            }
        }
        return null;
    }
    
    public Action church() {
    	if (me.turn == 1) {
            fillAllMines();
            getMineScores();
            identifyClusters();
            numUnits = 1;
            HOME = new int[] {me.x, me.y};
            int[] myMine = findClosestMine();
            for (ArrayList<int[]> cluster : mineClusters) {
                if (cluster.contains(myMine)) {
                    myMineScore = cluster.size();
                }
            }
            log("church is awake with "+myMineScore+" mines");
        }
        if (numUnits < myMineScore) {
            log("trying to build pilgrim");
            if (fuel < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_FUEL + 2
                || karbonite < SPECS.UNITS[SPECS.PILGRIM].CONSTRUCTION_KARBONITE) {
                return null;
            }
            for (int[] move : adjacentSpaces) {
                int buildX = me.x + move[0];
                int buildY = me.y + move[1];
                if (buildX <= -1 || buildX >= this.mapXSize || buildY <= -1 || buildY >= this.mapYSize
                    || this.passableMap[buildY][buildX] || this.visibleRobotMap[buildY][buildX] > 0) {
                    continue;
                }
                numUnits++;
                log("church is signalling their location at " + (64 * me.y + me.x));
                signal(64 * me.y + me.x, 2);
                return buildUnit(SPECS.PILGRIM, move[0], move[1]);
            }
        }
        return null;
    }
    
    public Action pilgrim() {
    	Robot base = null;
        for (int[] move : adjacentSpaces) {
            int testX = me.x + move[0];
            int testY = me.y + move[1];
            if (testX <= -1 || testX >= this.mapXSize || testY <= -1 || testY >= this.mapYSize) {
                continue;
            }
            Robot maybe = getRobot(visibleRobotMap[testY][testX]);
            if (visibleRobotMap[testY][testX] > 0 && (maybe.unit == SPECS.CASTLE || maybe.unit == SPECS.CHURCH)
                && maybe.team == me.team) {
                base = maybe;
                karbosInUse.clear();
                fuelsInUse.clear();
                if (me.turn == 1 && isRadioing(base)) {
                    HOME = new int[] { base.signal % 64, (int) (base.signal / 64) };
                    break;
                }
            }
        }
        
        if (currentPath != null && currentPath.size() > 0) {
            int[] nextMove = currentPath.get(0);
            int dx = nextMove[0] - me.x;
            int dy = nextMove[1] - me.y;
            if (visibleRobotMap[nextMove[1]][nextMove[0]] <= 0) {
                if (fuel >= (dx * dx + dy * dy) * SPECS.UNITS[SPECS.PILGRIM].FUEL_PER_MOVE) {
                    currentPath.remove(0);
                    return move(dx, dy);
                }
            }
        }
        
        if (me.karbonite == SPECS.UNITS[SPECS.PILGRIM].KARBONITE_CAPACITY
            || me.fuel == SPECS.UNITS[SPECS.PILGRIM].FUEL_CAPACITY) {
            if (base != null) {
                return give(base.x - me.x, base.y - me.y, me.karbonite, me.fuel);
            }
            log("gary go from (" + me.x + ", " + me.y + ") to (" + HOME[0] + ", " + HOME[1] + ")");
            currentPath = bfs(HOME[0], HOME[1]);
            if (currentPath == null) {
                log("gary no found home");
                return null;
            }
            int[] nextMove = currentPath.get(0);
            int dx = nextMove[0] - me.x;
            int dy = nextMove[1] - me.y;
            if (fuel >= (dx * dx + dy * dy) * SPECS.UNITS[SPECS.PILGRIM].FUEL_PER_MOVE) {
                currentPath.remove(0);
                log("gary going home");
                return move(dx, dy);
            }
            log("gary no go");
            return null;
        }
        if ((this.karboniteMap[me.y][me.x] || this.fuelMap[me.y][me.x]) && this.visibleRobotMap[HOME[1]][HOME[0]] > 0) {
            if (fuel == 0) {
                return null;
            }
            return mine();
        }
        int[] location;
        if (this.visibleRobotMap[HOME[1]][HOME[0]] > 0) {
            /*
             * if (20 * numUnits > fuel) { location = findClosestFuel(); } else { location =
             * findClosestKarbo(); }
             */
            location = findClosestMine();
            if (!tilesInRange(location, HOME, mineClusterRadiusSqrd)) {
                log("robot is straying away from base");
            }
        }
        
        else {
            if (Math.abs(HOME[0] - me.x) + Math.abs(HOME[1] - me.y) == 1) {
                log("trying to build a church");
                if (karbonite < SPECS.UNITS[SPECS.CHURCH].CONSTRUCTION_KARBONITE
                    || fuel < SPECS.UNITS[SPECS.CHURCH].CONSTRUCTION_FUEL) {
                    return null;
                }
                log("BUILT A CHURCH");
                return buildUnit(SPECS.CHURCH, HOME[0] - me.x, HOME[1] - me.y);
            }
            this.visibleRobotMap[HOME[1]][HOME[0]] = 4096;
            location = HOME;
        }
        
        if (location == null) {
            location = HOME;
        }
        
        currentPath = bfs(location[0], location[1]);
        if (currentPath == null) {
            return null;
        }
        int[] nextMove = currentPath.get(0);
        int dx = nextMove[0] - me.x;
        int dy = nextMove[1] - me.y;
        if (fuel >= (dx * dx + dy * dy) * SPECS.UNITS[SPECS.PILGRIM].FUEL_PER_MOVE) {
            currentPath.remove(0);
            return move(dx, dy);
        }
        return null;
    }
    
    public Action crusader()
    {
        
        if (me.turn == 1)
        {
            pilgrimLim = (int) Math.floor(Math.min(numFuelMines * 1.25, numFuelMines * .75 + numKarbMines));
            getHomeCastle();
            getCastleDir();
            if(castleDir % 2 == 0)
            {
                sideDir = (((int) (Math.random() * 2)) * 4 + castleDir + 2) % 8;
            }
        }
        
        int[] atk = autoAttack();
        if(atk != null)
        {
            if(fuel >= 10)
            {
                return attack(atk[0], atk[1]);
            }
            else
            {
                return null;
            }
        }
        
            if(fuel >= pilgrimLim * 2)
            {
                if(moveAway())
                {
                    int[] mov = exploreLattice();
                    if(mov != null)
                    {
                        return move(mov[0], mov[1]);
                    }
                }
                
            }
        return null;
    }
    
    public Action prophet()
    {
        if (me.turn == 1)
        {
            pilgrimLim = (int) Math.floor(Math.min(numFuelMines * 1.25, numFuelMines * .75 + numKarbMines));
            getHomeCastle();
            arrived = false;
        }
        
        int[] atk = autoAttack();
        if(atk != null)
        {
            return attack(atk[0], atk[1]);
        }
        
        if(!arrived && fuel >= pilgrimLim * 2)
        {
        	if (currentPath == null || currentPath.size() <= locInPath || visibleRobotMap[currentPath.get(locInPath)[1]][currentPath.get(locInPath)[0]] > 0)
        	{
        		currentPath = goToLattice();
        	}

        	if (currentPath == null || currentPath.size() <= locInPath || visibleRobotMap[currentPath.get(locInPath)[1]][currentPath.get(locInPath)[0]] > 0)
        	{
        		log("Prophet BFS returned null (or something invalid). Turn: " + globalTurn);

        		int[] mov = randomAdjSq();
        		if(mov != null)
        		{
        			if((me.x + mov[0] + me.y + mov[1]) % 2 == 0)
        			{
        				arrived = true;
        			}
        			return move(mov[0], mov[1]);
        		}
        		return null;
        	}

        	int[] mov = new int[] {currentPath.get(locInPath)[0] - me.x, currentPath.get(locInPath)[1] - me.y};
        	locInPath += 1;
        	if((me.x + mov[0] + me.y + mov[1]) % 2 == 0 && !isNextToHome(me.x + mov[0], me.y + mov[1]))
        	{
        		arrived = true;
        	}
        	return move(mov[0], mov[1]);
        }
        return null;
    }
    
    public Action preacher()
    {
        if (me.turn == 1)
        {
            pilgrimLim = (int) Math.floor(Math.min(numFuelMines * 1.25, numFuelMines * .75 + numKarbMines));
            getCastleDir();
            getHomeCastle();
            if(castleDir % 2 == 0)
            {
                sideDir = (((int) (Math.random() * 2)) * 4 + castleDir + 2) % 8;
            }
        }
        
        AttackAction atk = preacherAttack();
        if(atk != null)
        {
            return atk;
        }
        
        return null;
    }
    
    public boolean getReflDir() // set hRefl
    {
        int top = (this.mapYSize + 1) / 2;
        int left = (this.mapXSize + 1) / 2;
        
        for (int i = 0; i < top; i++) // Goes through top left quarter of map and tests one cell at a time
        { // for whether it's reflected horizontally then vertically.
            for (int j = 0; j < left; j++) // If a discrepancy is found, method returns.
            {
                if (this.passableMap[i][j] != this.passableMap[this.mapYSize - 1 - i][j]) {
                    return true;
                } else if (this.passableMap[i][j] != this.passableMap[i][this.mapXSize - 1 - j]) {
                    return false;
                }
            }
        }
        
        for (int i = this.mapYSize; i > top; i--) // Checks bottom right quarter same way just in case no return yet.
        {
            for (int j = this.mapXSize; j > left; j--) {
                if (this.passableMap[i][j] != this.passableMap[this.mapYSize - 1 - i][j]) {
                    return true;
                } else if (this.passableMap[i][j] != this.passableMap[i][this.mapXSize - 1 - j]) {
                    return false;
                }
            }
        }
        
        log("it's frickin reflected both ways >:(");
        return true;
    }
    
    public void getHomeCastle()
    {
        for(Robot r : getVisibleRobots())
        {
            if(r.unit == SPECS.CASTLE)
            {
                castleLocs[0] = new int[] {r.x, r.y};
                robots[0].add(r.id);
                globalTurn = r.turn;
            }
        }
    }
    
    public int[] findClosestKarbo() {
        int minDistance = this.mapYSize * this.mapYSize;
        int[] ans = null;
        for (int x = 0; x < this.mapXSize; x++) {
        looping: for (int y = 0; y < this.mapYSize; y++) {
            if (this.karboniteMap[y][x]) {
                int[] temp = new int[] { x, y };
                for (int[] out : karbosInUse) {
                    if (out[0] == temp[0] && out[1] == temp[1]) {
                        if (visibleRobotMap[y][x] == 0) {
                            karbosInUse.remove(out);
                        } else {
                            continue looping;
                        }
                    }
                }
                if (visibleRobotMap[y][x] > 0) {
                    karbosInUse.add(temp);
                    continue looping;
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
        looping: for (int y = 0; y < this.mapYSize; y++) {
            if (this.fuelMap[y][x]) {
                int[] temp = new int[] { x, y };
                for (int[] out : fuelsInUse) {
                    if (out[0] == temp[0] && out[1] == temp[1]) {
                        if (visibleRobotMap[y][x] == 0) {
                            fuelsInUse.remove(out);
                        } else {
                            continue looping;
                        }
                    }
                }
                if (visibleRobotMap[y][x] > 0) {
                    fuelsInUse.add(temp);
                    continue looping;
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
    
    public void setXorKey()
    {
//        int[] parts = new int[4];
//        parts[0] = 5 + fullMap[9][30] + fullMap[18][8] + fullMap[9][0] + fullMap[23][28] + fullMap[15][31];
//        parts[1] = 5 + fullMap[19][3] + fullMap[31][8] + fullMap[10][26] + fullMap[11][11] + fullMap[4][2];
//        parts[2] = 5 + fullMap[6][9] + fullMap[4][20] + fullMap[13][3] + fullMap[18][29] + fullMap[19][12];
//        parts[3] = 5 + fullMap[30][10] + fullMap[31][31] + fullMap[0][0] + fullMap[5][15] + fullMap[1][8];
//        
//        xorKey = parts[3] * 4096 + parts[2] * 256 + parts[1] * 16 + parts[0];
    	xorKey = 2948329;
    }
    
    public void getEnemyCastleLocs()
    {
        for(int i = 0; i < 3; i++)
        {
            if(horizontalReflection)
            {
                enemyCastleLocs[i][0] = this.mapYSize - 1 - castleLocs[i][0];
                enemyCastleLocs[i][1] = castleLocs[i][1];
            }
            else
            {
                enemyCastleLocs[i][0] = castleLocs[i][0];
                enemyCastleLocs[i][1] = this.mapYSize - 1 - castleLocs[i][1];
            }
        }
    }

    
    public Robot[] getEnemiesInRange() {
        Robot[] robots = getVisibleRobots();
        ArrayList<Robot> enms = new ArrayList<Robot>();
        
        for (Robot r : robots) {
            if (r.team != me.team && (r.x - me.x) * (r.x - me.x) + (r.y - me.y) * (r.y - me.y) <= SPECS.UNITS[me.unit].ATTACK_RADIUS[1]
                && (r.x - me.x) * (r.x - me.x) + (r.y - me.y) * (r.y - me.y) >= SPECS.UNITS[me.unit].ATTACK_RADIUS[0]) {
                enms.add(r);
            }
        }
        
        return enms.toArray(new Robot[enms.size()]);
    }
    
    public int[] autoAttack() // Returns null if you don't have enough fuel!
    {
        if(fuel < SPECS.UNITS[me.unit].ATTACK_FUEL_COST)
        {
            return null;
        }
        
        Robot[] robots = getEnemiesInRange();
        
        if (robots.length == 0) {
            return null;
        }
        
        ArrayList<Robot> priorRobs = new ArrayList<Robot>(); // only robots of highest priority type
        boolean found = false;
        int i = 0;
        
        while (!found && i < 6) // make priorRobs
        {
            for (Robot r : robots) {
                if (r.unit == this.getRobotValue(r)) {
                    found = true;
                    priorRobs.add(r);
                }
            }
            i++;
        }
        
        if (priorRobs.size() == 1) {
            return new int[] {priorRobs.get(0).x - me.x, priorRobs.get(0).y - me.y};
        } else if (priorRobs.size() == 0) {
            log("why are there no enemies and yet autoAttack() has gotten all the way here");
            return null;
        }
        
        int lowestID = 4097;
        for (int j = 0; j < priorRobs.size(); j++) {
            if (priorRobs.get(j).id < lowestID) {
                lowestID = priorRobs.get(j).id;
            }
        }
        
        return new int[] {getRobot(lowestID).x - me.x, getRobot(lowestID).y - me.y};
    }
    
    // For preacherAttack()
    public Robot[] getPreacherKillableRobots() // Now returns only units with max health <= 20 in visibility range, but
    // can be edited
    { // to also return damaged units or units 1 space outside visibility range
        Robot[] robots = getVisibleRobots();
        ArrayList<Robot> killable = new ArrayList<Robot>();
        
        for (Robot r : robots) {
            if (r.team != me.team && (r.unit == SPECS.PILGRIM || r.unit == SPECS.PROPHET)) {
                killable.add(r);
            }
        }
        
        return killable.toArray(new Robot[killable.size()]);
    }
    
    // For preacherAttack()
    public Robot[] getAllies() // Now returns only visible allies, but preachers can damage non-visible allies
    // :( plis update
    {
        Robot[] robots = getVisibleRobots();
        ArrayList<Robot> allies = new ArrayList<Robot>();
        
        for (Robot r : robots) {
            if (r.team == me.team) {
                allies.add(r);
            }
        }
        
        return allies.toArray(new Robot[allies.size()]);
    }
    
    // For preacherAttack()
    public Robot[] getEnemyRobots() // Does not return from outside of visibility range :(
    {
        Robot[] robots = getVisibleRobots();
        ArrayList<Robot> enemies = new ArrayList<Robot>();
        
        for (Robot r : robots) {
            if (r.team != me.team && (r.unit == SPECS.CRUSADER || r.unit == SPECS.PREACHER || r.unit == SPECS.CASTLE)) {
                enemies.add(r);
            }
        }
        
        return enemies.toArray(new Robot[enemies.size()]);
    }
    
    // For preacherAttack()
    public Robot[] getEnemyChurches() // Does not return from outside of visibility range :(
    { // Do not combine with other preacherAttack() helper methods. Ask Zain for elaboration if wanted.
        Robot[] robots = getVisibleRobots();
        ArrayList<Robot> buildings = new ArrayList<Robot>();
        
        for (Robot r : robots) {
            if (r.team != me.team && r.unit == SPECS.CHURCH)
            {
                buildings.add(r);
            }
        }
        
        return buildings.toArray(new Robot[buildings.size()]);
    }
    
    // For preacherAttack()
    public boolean squareContainsRobot(Robot r, int centerX, int centerY) // 3x3 square
    {
        if (r.x + 1 >= centerX && r.x - 1 <= centerX && r.y + 1 >= centerY && r.y - 1 <= centerY) {
            return true;
        }
        return false;
    }
    
    public void getMineSpots() {
        allKarbos = new int[numKarbos][2];
        allFuels = new int[numFuels][2];
        int karboIndex = 0;
        int fuelIndex = 0;
        for (int x = 0; x < this.mapXSize; x++) {
            for (int y = 0; y < this.mapYSize; y++) {
                if (this.karboniteMap[y][x]) {
                    allKarbos[karboIndex][0] = x;
                    allKarbos[karboIndex++][1] = y;
                } else if (this.fuelMap[y][x]) {
                    allFuels[fuelIndex][0] = x;
                    allFuels[fuelIndex++][1] = y;
                }
            }
        }
    }
    
    public void fillAllMines() {
        allMines = new int[numKarbos + numFuels][];
        int i = 0;
        for (int[] mine : allKarbos) {
            allMines[i] = mine;
            i++;
        }
        for (int[] mine : allFuels) {
            allMines[i] = mine;
            i++;
        }
    }
    
    // call this on turn 1 after fillAllMines
    public void getMineScores() {
        allMineScores = new int[allMines.length];
        int index = 0;
        for (int[] check : allMines) {
            int count = 0;
            for (int[] mine : allMines) {
                if (tilesInRange(check, mine, mineClusterRadiusSqrd)) {
                    count++;
                }
            }
            allMineScores[index++] = count;
        }
    }
    
    public void identifyClusters() {
        ArrayList<int[]> scannedMines = new ArrayList<>();
        mineClusters = new ArrayList<>();
        while (scannedMines.size() != allMines.length) {
            int bestIndex = -1;
            for (int i = 0; i < allMineScores.length; i++) {
                if (scannedMines.contains(allMines[i])) {
                    continue;
                }
                if (bestIndex == -1 || allMineScores[i] > allMineScores[bestIndex]) {
                    bestIndex = i;
                }
            }
            int[] head = allMines[bestIndex];
            ArrayList<int[]> currentCluster = new ArrayList<>();
            currentCluster.add(head);
            scannedMines.add(head);
            for (int[] mine : allMines) {
                if (scannedMines.contains(mine)) {
                    continue;
                }
                if (tilesInRange(head, mine, mineClusterRadiusSqrd)) {
                    currentCluster.add(mine);
                    scannedMines.add(mine);
                }
            }
            mineClusters.add(currentCluster);
        }
    }
    
    public void findClusterCenters() {
        mineClusterCenters = new ArrayList<>();
        for (ArrayList<int[]> cluster : mineClusters) {
            int clusterSize = cluster.size();
            double sumX = 0;
            double sumY = 0;
            for (int[] mine : cluster) {
                sumX += mine[0];
                sumY += mine[1];
            }
            int avgX = (int) Math.round(sumX / clusterSize);
            int avgY = (int) Math.round(sumY / clusterSize);
            int[] closestMatch = new int[] { 0, 0 };
            int closestDistance = avgX * avgX + avgY * avgY;
            for (int x = 0; x < this.mapXSize; x++) {
                for (int y = 0; y < this.mapYSize; y++) {
                    if (!this.passableMap[y][x]) {
                        continue;
                    }
                    int distance = (x - avgX) * (x - avgX)
                    + (y - avgY) * (y - avgY);
                    if (distance < closestDistance) {
                        closestDistance = distance;
                        closestMatch[0] = x;
                        closestMatch[1] = y;
                    }
                }
            }
            mineClusterCenters.add(closestMatch);
        }
        isMineColonized = new boolean[mineClusterCenters.size()];
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
    
    // Finds distance squared between two int[]s
    public double findDistance(int[] one, int[] two) {
        int xDistance = one[1] - two[1];
        int yDistance = one[0] - two[0];
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
    }
    
    public double findDistance(int[] location) {
		int xDistance = location[1] - me.x;
		int yDistance = location[0] - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}

	//Finds distance squared between two pairs of coordinates
    public double findDistance(int x1, int y1, int x2, int y2) {
        int xDistance = x1 - x2;
        int yDistance = y1 - y2;
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
    }
    
  //checks if the robot is in attack range
    public boolean canAttack(double distance) {
        return this.getMinAttackRangeRadius(this.me.unit) <= Math.sqrt(distance) && Math.sqrt(distance) <= this.getMaxAttackRangeRadius(this.me.unit) && this.fuel >= this.getAttackFuel(this.me.unit);
    }
    
    //other robots can attack?
    public boolean canAttack(Robot r, double distance) {
        return this.getMinAttackRangeRadius(r.unit) <= Math.sqrt(distance) && Math.sqrt(distance) <= this.getMaxAttackRangeRadius(r.unit);
    }
    
  //can this unit be built? do we have enough fuel and karbonite? is there room?
    public boolean canBuild(int type) {
        return this.fuel >= SPECS.UNITS[type].CONSTRUCTION_FUEL && this.karbonite >= SPECS.UNITS[type].CONSTRUCTION_KARBONITE && this.checkAdjacentBuildAvailable()!=null;
    }
    
  //checks if adjacent tiles are available. used for making units. checks tiles closer to the middle of the map first. //TODO build pilgrims on deposits, and other units not on deposits. if possible
    public int[] checkAdjacentBuildAvailable() {
        int x = this.me.x;
        int y = this.me.y;
        int dx = x - this.mapXSize/2;
        int dy = y - this.mapYSize/2;
        if (dx > 0) { //robot is to the east of center, check left first, then up down, then right
            if (x > 0) { //can check left
                if (dy > 0) { //robot is to the south of center, check up then middle then down
                    if (y > 0) { //can check up
                        if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
                            return new int[] {y-1, x-1};
                        }
                    }
                    if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
                        return new int[] {y, x-1};
                    }
                    if (y < mapYSize - 1) { //can check down
                        if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
                            return new int[] {y+1, x-1};
                        }
                    }
                }
                else { //robot is to the north or level of center, check down then middle then up
                    if (y > 0) { //can check up
                        if (y < mapYSize - 1) { //can check down
                            if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
                                return new int[] {y+1, x-1};
                            }
                        }
                        if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
                            return new int[] {y, x-1};
                        }
                        if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
                            return new int[] {y-1, x-1};
                        }
                    }
                }
            }
            if (dy > 0) { //robot is to the south of center, check up then down
                if (y > 0) { //can check up
                    if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
                        return new int[] {y-1, x};
                    }
                }
                if (y < mapYSize - 1) { //can check down
                    if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
                        return new int[] {y+1, x};
                    }
                }
            }
            else { //robot is to the north or level of center, check down then up
                if (y < mapYSize - 1) { //can check down
                    if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
                        return new int[] {y+1, x};
                    }
                }
                if (y > 0) { //can check up
                    if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
                        return new int[] {y-1, x};
                    }
                }
            }
            if (x < mapXSize - 1) { //can check right
                if (dy > 0) { //robot is to the south of center, check up then middle then down
                    if (y > 0) { //can check up
                        if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
                            return new int[] {y-1, x+1};
                        }
                    }
                    if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
                        return new int[] {y, x+1};
                    }
                    if (y < mapYSize) { //can check down
                        if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
                            return new int[] {y+1, x+1};
                        }
                    }
                }
                else { //robot is north or level of center, check down then middle then up
                    if (y < mapYSize) { //can check down
                        if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
                            return new int[] {y+1, x+1};
                        }
                    }
                    if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
                        return new int[] {y, x+1};
                    }
                    if (y > 0) { //can check up
                        if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
                            return new int[] {y-1, x+1};
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
                            return new int[] {y-1, x+1};
                        }
                    }
                    if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
                        return new int[] {y, x+1};
                    }
                    if (y < mapYSize) { //can check down
                        if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
                            return new int[] {y+1, x+1};
                        }
                    }
                }
                else { //robot is north or level of center, check down then middle then up
                    if (y < mapYSize) { //can check down
                        if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
                            return new int[] {y+1, x+1};
                        }
                    }
                    if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
                        return new int[] {y, x+1};
                    }
                    if (y > 0) { //can check up
                        if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
                            return new int[] {y-1, x+1};
                        }
                    }
                }
            }
            if (dy > 0) { //robot is to the south of center, check up then down
                if (y > 0) { //can check up
                    if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
                        return new int[] {y-1, x};
                    }
                }
                if (y < mapYSize - 1) { //can check down
                    if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
                        return new int[] {y+1, x};
                    }
                }
            }
            else { //robot is to the north or level of center, check down then up
                if (y < mapYSize - 1) { //can check down
                    if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
                        return new int[] {y+1, x};
                    }
                }
                if (y > 0) { //can check up
                    if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
                        return new int[] {y-1, x};
                    }
                }
            }
            if (x > 0) { //can check left
                if (dy > 0) { //robot is to the south of center, check up then middle then down
                    if (y > 0) { //can check up
                        if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
                            return new int[] {y-1, x-1};
                        }
                    }
                    if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
                        return new int[] {y, x-1};
                    }
                    if (y < mapYSize - 1) { //can check down
                        if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
                            return new int[] {y+1, x-1};
                        }
                    }
                }
                else { //robot is north or level of center, check down then middle then up
                    if (y < mapYSize - 1) { //can check down
                        if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
                            return new int[] {y+1, x-1};
                        }
                    }
                    if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
                        return new int[] {y, x-1};
                    }
                    if (y > 0) { //can check up
                        if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
                            return new int[] {y-1, x-1};
                        }
                    }
                }
            }
        }
        return null; //surrounded by impassable terrain
    }
    
    //gets the movement speed radius of a unit
    public int getMovementRangeRadius(int unit) {
        return (int)Math.sqrt(SPECS.UNITS[unit].SPEED);
    }
    
    //gets the minimum attack range radius of a unit
    public int getMinAttackRangeRadius(int unit) {
        return (int)Math.sqrt(SPECS.UNITS[unit].ATTACK_RADIUS[0]);
    }
    
    //gets the maximum attack range radius of a unit
    public int getMaxAttackRangeRadius(int unit) {
        return (int)Math.sqrt(SPECS.UNITS[unit].ATTACK_RADIUS[1]);
    }
    
    //gets the vision range radius of a unit
    public int getVisionRangeRadius(int unit) {
        return (int)Math.sqrt(SPECS.UNITS[unit].VISION_RADIUS);
    }
    
    //gets attacking fuel
    public int getAttackFuel(int unit) {
        return SPECS.UNITS[unit].ATTACK_FUEL_COST;
    }
    
    //gets attack damage
    public int getAttackDamage(int unit) {
        return SPECS.UNITS[unit].ATTACK_DAMAGE;
    }
    
    public AttackAction preacherAttack() {
        Robot[] robots = this.getVisibleRobots();
        int maxAttackRange = this.getMaxAttackRangeRadius(this.me.unit);
        //        this.log(maxAttackRange + "");
        int x = -1, y = -1, maxValue = 0;
        int[][] robotMap = new int[this.mapYSize][this.mapXSize];
        for (int[] row : robotMap) {
            Arrays.fill(row, 0);
        }
        for (Robot robot : robots) {
            robotMap[robot.y][robot.x] = this.getRobotValue(robot);
            //            this.log(robotMap[robot.y][robot.x] + "");
        }
        int value;
        for (int r = Math.max(this.me.y - maxAttackRange, 1); r < Math.min(this.me.y + maxAttackRange + 1, this.mapYSize-1); r++) {
            for (int c = Math.max(this.me.x - maxAttackRange, 1); c < Math.min(this.me.x + maxAttackRange + 1, this.mapXSize-1); c++) {
                if (this.canAttack(Math.sqrt(this.findDistance(this.me, c, r)))) {
                    //                    this.log("can attack");
                    value = this.sumAdjacent(robotMap, r, c);
                    //                    this.log(value + " x=" + c + " y=" + r + "   turn =" + this.turn);
                    if (maxValue < value) {
                        maxValue = value;
                        x = c;
                        y = r;
                    }
                }
            }
        }
        //        this.log("c=" + x + " r=" + y);
        //        this.log("x=" + (x-this.me.x) + " y=" + (y-this.me.y));
        return this.attack(x - this.me.x, y - this.me.y);
    }
    
    //gets the robot value
    public int getRobotValue(Robot r) {
        if (r.team == this.me.team) { //ally
            if (r.unit == SPECS.CASTLE) {
                return -10; //we REALLY don't want to hit our own castles
            }
            if (r.unit == SPECS.PREACHER) {
                return -4; //we really don't want to hit our own preachers
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
        }
        else { //enemy
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
            return 1; //unlikely to see structures, but it'll just be 1
        }
    }
    
    //finds the robot average of a 3x3 area
    public int sumAdjacent(int[][] map, int x, int y) {
        int sum = 0;
        for (int i = -1; i < 2; i++) {
            for (int j = -1; j < 2; j++) {
                sum += map[x + i][y + j];
            }
        }
        return sum;
    }
    
    int[] findClosestMine() {
        int minDistance = this.mapYSize * this.mapYSize;
        int[] ans = null;
        
        for (int[] spot : allFuels) {
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
        for (int[] spot : allKarbos) {
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
    public ArrayList<int[]> bfs(int goalX, int goalY) {
        locInPath = 0;
        
        boolean occupied = false;
        if (visibleRobotMap[goalY][goalX] > 0) {
            occupied = true;
        }
//        int fuelCost = SPECS.UNITS[me.unit].FUEL_PER_MOVE;
        int maxRadius = (int) Math.sqrt(SPECS.UNITS[me.unit].SPEED);
        LinkedList<int[]> spots = new LinkedList<>();
        int[] spot = new int[] { me.x, me.y };
        int[] from = new int[this.mapYSize * this.mapYSize];
        for (int i = 0; i < from.length; i++) {
            from[i] = -1;
        }
        
        // these two are only used if occupied == true
        int[] closestSpot = null;
        int closestDistance = (goalX - me.x) * (goalX - me.x) + (goalY - me.y) * (goalY - me.y);
        
        while (!(spot[0] == goalX && spot[1] == goalY)) {
            int left = Math.max(0, spot[0] - maxRadius);
            int top = Math.max(0, spot[1] - maxRadius);
            int right = Math.min(this.mapXSize - 1, spot[0] + maxRadius);
            int bottom = Math.min(this.mapYSize - 1, spot[1] + maxRadius);
            
            for (int x = left; x <= right; x++) {
                int dx = x - spot[0];
                for (int y = top; y <= bottom; y++) {
                    int dy = y - spot[1];
                    if (dx * dx + dy * dy <= maxRadius * maxRadius && this.passableMap[y][x]
                        && (visibleRobotMap[y][x] <= 0/* || getRobot(robotMap[y][x]).unit < 2*/)) {
                        if (from[y * this.mapYSize + x] != -1) {
                            continue;
                        }
                        int[] newSpot = new int[] { x, y };
                        from[y * this.mapYSize + x] = spot[1] * this.mapYSize + spot[0];
                        
                        if (occupied) {
                            if ((goalX - x) * (goalX - x) + (goalY - y) * (goalY - y) < closestDistance) {
                                closestDistance = (goalX - x) * (goalX - x) + (goalY - y) * (goalY - y);
                                closestSpot = newSpot;
                                continue;
                            }
                        }
                        spots.add(newSpot);
                    }
                }
            }
            
            if (occupied && closestSpot != null) {
                spot = closestSpot;
                break;
            }
            
            spot = spots.poll();
            if (spot == null) {
                // log("exhausted all options");
                return null;
            }
        }
        ArrayList<int[]> ans = new ArrayList<>();
        while (from[spot[1] * this.mapYSize + spot[0]] != -1) {
            ans.add(0, spot);
            int prevSpot = from[spot[1] * this.mapYSize + spot[0]];
            spot = new int[] { prevSpot % this.mapYSize, (int) (prevSpot / this.mapYSize) };
        }
        return ans;
    }
    
    public boolean spaceIsCootiesFree(int x, int y) {
        int[][] robomap = visibleRobotMap;
        for (int[] adj : adjacentSpaces) {
            if (y + adj[1] > -1 && y + adj[1] < robomap.length && x + adj[0] > -1 && x + adj[0] < robomap.length)
            {
                if (robomap[y + adj[1]][x + adj[0]] > 0) {
                    return false;
                }
            }
        }
        return true;
    }
    
    // mormons may be polygamists but no touchie touchie
    public ArrayList<int[]> bfsCooties(int goalX, int goalY) {
        locInPath = 0;
        
        boolean occupied = false;
        if (visibleRobotMap[goalY][goalX] > 0) {
            occupied = true;
        }
        // int fuelCost = SPECS.UNITS[me.unit].FUEL_PER_MOVE;
        int maxRadius = (int) Math.sqrt(SPECS.UNITS[me.unit].SPEED);
        LinkedList<int[]> spots = new LinkedList<>();
        int[] spot = new int[] { me.x, me.y };
        int[] from = new int[this.mapYSize * this.mapXSize];
        for (int i = 0; i < from.length; i++) {
            from[i] = -1;
        }
        // these two are only used if occupied == true
        int[] closestSpot = null;
        int closestDistance = (goalX - me.x) * (goalX - me.x) + (goalY - me.y) * (goalY - me.y);
        
        while (!(spot[0] == goalX && spot[1] == goalY)) {
            int left = Math.max(0, spot[0] - maxRadius);
            int top = Math.max(0, spot[1] - maxRadius);
            int right = Math.min(this.mapXSize - 1, spot[0] + maxRadius);
            int bottom = Math.min(this.mapYSize - 1, spot[1] + maxRadius);
            
            for (int x = left; x <= right; x++) {
                int dx = x - spot[0];
                for (int y = top; y <= bottom; y++) {
                    int dy = y - spot[1];
                    if (dx * dx + dy * dy <= maxRadius * maxRadius
                        && this.passableMap[y][x]
                        && visibleRobotMap[y][x] <= 0
                        && spaceIsCootiesFree(x, y)) {
                        if (from[y * this.mapYSize + x] != -1) {
                            continue;
                        }
                        int[] newSpot = new int[] { x, y };
                        from[y * this.mapYSize + x] = spot[1] * this.mapYSize + spot[0];
                        
                        if (occupied) {
                            if ((goalX - x) * (goalX - x) + (goalY - y) * (goalY - y) < closestDistance) {
                                closestDistance = (goalX - x) * (goalX - x) + (goalY - y) * (goalY - y);
                                closestSpot = newSpot;
                                continue;
                            }
                        }
                        spots.add(newSpot);
                    }
                }
            }
            if (occupied && closestSpot != null) {
                spot = closestSpot;
                break;
            }
            spot = spots.poll();
            if (spot == null) {
                //log("exhausted all options");
                return null;
            }
        }
        ArrayList<int[]> ans = new ArrayList<>();
        while (from[spot[1] * this.mapYSize + spot[0]] != -1) {
            ans.add(0, spot);
            int prevSpot = from[spot[1] * this.mapYSize + spot[0]];
            spot = new int[] { prevSpot % this.mapYSize, (int) (prevSpot / this.mapYSize) };
        }
        return ans;
    }
    
    public int[] availAdjSq(int[] target)
    {
        int i;
        if(target[0] == 0)
        {
            i = target[1] * -2 + 2;
        }
        else if(target[0] == -1)
        {
            i = target[1] * -1 + 2;
        }
        else if(target[0] == 1)
        {
            i = target[1] + 6;
        }
        else // NOTE: THIS WILL NOT CATCH ALL INVALID TARGETS, ONLY INVALID TARGET[0]S
        {
            log("That is not a valid target for availAdjSq(). Returning null.");
            return null;
        }
        
        int newX = me.x + adjacentSpaces[i][0];
        int newY = me.y + adjacentSpaces[i][1];
        
        int delta = 1;
        int sign = 1;
        
        while(newX < 0 || newX >= this.mapYSize || newY < 0 || newY >= this.mapYSize || !this.passableMap[newY][newX] || this.visibleRobotMap[newY][newX] > 0)
        {
            if(delta >= 8)
            {
                log("No adjacent movable spaces (from availAdjSq()).");
                return null;
            }
            
            i += delta * sign;
            i %= 8;
            
            newX = me.x + adjacentSpaces[i][0];
            newY = me.y + adjacentSpaces[i][1];
            
            delta += 1;
            sign *= -1;
        }
        
        return adjacentSpaces[i];
    }
    
    public int[] randomAdjSq()
    {
        int rand, newX, newY;
        rand = (int) (Math.random() * 8);
        int i = 0;
        do
        {
            rand += 1;
            rand %= 8;
            i++;
            newX = me.x + adjacentSpaces[rand][0];
            newY = me.y + adjacentSpaces[rand][1];
            
            if(i >= 8)
            {
                //                log("No adjacent movable spaces (from randomAdjSq()).");
                return null;
            }
        }
        while(newX < 0 || newX >= this.mapYSize || newY < 0 || newY >= this.mapYSize || !this.passableMap[newY][newX] || this.visibleRobotMap[newY][newX] > 0);
        
        return adjacentSpaces[rand];
    }
    
    public int[] randomOddAdjSq()
    {
        int rand, newX, newY;
        int pos = 1 - (me.x + me.y) % 2;
        
        rand = ((int) (Math.random() * 4)) * 2 + 1 + pos;
        int i = 0;
        do
        {
            i++;
            if(i > 4)
            {
                return null;
            }
            
            rand += 2;
            rand %= 8;
            newX = me.x + adjacentSpaces[rand][0];
            newY = me.y + adjacentSpaces[rand][1];
            
        }
        while(newX < 0 || newX >= this.mapYSize || newY < 0 || newY >= this.mapYSize || !this.passableMap[newY][newX] || this.visibleRobotMap[newY][newX] > 0);
        
        return adjacentSpaces[rand];
    }
    
    public void getCastleDir()
    {
        if(castleLocs[0][0] - me.x == 0)
        {
            castleDir = castleLocs[0][1] - me.y * -2 + 2;
        }
        else if(castleLocs[0][0] - me.x == -1)
        {
            castleDir = castleLocs[0][1] - me.y * -1 + 2;
        }
        else if(castleLocs[0][0] - me.x == 1)
        {
            castleDir = castleLocs[0][1] - me.y + 6;
        }
    }
    
    public int[] exploreLattice()
    {
        int[] fpoo;
        int newX, newY;
        
        if(castleDir % 2 == 0)
        {
            int chooseDir = (int) (Math.random() * 2);
            fpoo = (chooseDir == 0) ? (adjacentSpaces[(castleDir + 4) % 8]) : adjacentSpaces[sideDir];
            fpoo = new int[] {fpoo[0] * 2, fpoo[1] * 2};
            
            newX = me.x + fpoo[0];
            newY = me.y + fpoo[1];
            if(newX >= 0 && newX < this.mapYSize && newY >= 0 && newY < this.mapYSize && this.passableMap[newY][newX] && this.visibleRobotMap[newY][newX] <= 0)
            {
                return fpoo;
            }
            
            fpoo = (chooseDir != 0) ? (adjacentSpaces[(castleDir + 4) % 8]) : adjacentSpaces[sideDir];
            fpoo = new int[] {fpoo[0] * 2, fpoo[1] * 2};
            
            newX = me.x + fpoo[0];
            newY = me.y + fpoo[1];
            if(newX >= 0 && newX < this.mapYSize && newY >= 0 && newY < this.mapYSize && this.passableMap[newY][newX] && this.visibleRobotMap[newY][newX] <= 0)
            {
                return fpoo;
            }
        }
        else
        {
            int chooseDir = ((int) (Math.random() * 2)) * 2 - 1;
            fpoo = adjacentSpaces[(castleDir + 4 + chooseDir) % 8];
            fpoo = new int[] {fpoo[0] * 2, fpoo[1] * 2};
            
            newX = me.x + fpoo[0];
            newY = me.y + fpoo[1];
            if(newX >= 0 && newX < this.mapYSize && newY >= 0 && newY < this.mapYSize && this.passableMap[newY][newX] && this.visibleRobotMap[newY][newX] <= 0)
            {
                return fpoo;
            }
            
            fpoo = adjacentSpaces[(castleDir + 4 - chooseDir) % 8];
            fpoo = new int[] {fpoo[0] * 2, fpoo[1] * 2};
            
            newX = me.x + fpoo[0];
            newY = me.y + fpoo[1];
            if(newX >= 0 && newX < this.mapYSize && newY >= 0 && newY < this.mapYSize && this.passableMap[newY][newX] && this.visibleRobotMap[newY][newX] <= 0)
            {
                return fpoo;
            }
        }
        
        return null;
    }
    
    public boolean isNextToHome(int newX, int newY)
    {
        if(Math.abs(newX - castleLocs[0][0]) <= 1 && Math.abs(newY - castleLocs[0][1]) <= 1)
        {
            return true;
        }
        return false;
    }
    
    public ArrayList<int[]> goToLattice()
    {
        int newX, newY, rRange;
        for(int range = 1; range < 64; range++)
        {
            rRange = (int) (Math.floor(Math.sqrt(range)));
            for(int dx = -rRange; dx <= rRange; dx++)
            {
                for(int dy = -rRange; dy <= rRange; dy++)
                {
                    if(dx * dx + dy * dy == range)
                    {
                        newX = me.x + dx;
                        newY = me.y + dy;
                        if(isOnMap(newX, newY) && this.passableMap[newY][newX] && this.visibleRobotMap[newY][newX] <= 0 && (newX + newY) % 2 == 0 && !isNextToHome(newX, newY))
                        {
                            return bfs(newX, newY);
                        }
                    }
                }
            }
        }
        return null;
    }
    
    /*public int[] latticify()
     {
     int newX, newY;
     int dir = ((int) (Math.random() * 4)) * 2;
     int i = 0;
     
     do
     {
     i++;
     if(i > 4)
     {
     return null;
     }
     newX = me.x + adjacentSpaces[dir][0];
     newY = me.y + adjacentSpaces[dir][1];
     
     dir += 2;
     dir %= 8;
     
     if(me.unit == 5)
     {
     }
     }
     while(newX < 0 || newX >= this.mapYSize || newY < 0 || newY >= this.mapYSize || fullMap[newY][newX] != 0 || robotMap[newY][newX] > 0);
     
     return adjacentSpaces[(dir + 6) % 8];
     }*/
    
    public boolean moveAway()
    {
        for(int dx = -2; dx <= 2; dx++)
        {
            for(int dy = -2; dy <= 2; dy++)
            {
                if((Math.abs(dx) == 2 && Math.abs(dy) == 2) || (dx == 0 && dy == 0))
                {
                    continue;
                }
                
                int newX = me.x + dx;
                int newY = me.y + dy;
                if(!(newX < 0 || newX >= this.mapYSize || newY < 0 || newY >= this.mapYSize))
                {
                    int ID = this.visibleRobotMap[newY][newX];
                    if(ID > 0 && getRobot(ID).team == me.team && (getRobot(ID).unit == 4 || getRobot(ID).unit == 0  || getRobot(ID).unit == 1))
                    {
                        return true;
                    }
                }
            }
        }
        return false;
    }
    
    public boolean isOnMap(int x, int y)
    {
        return (x >= 0 && x < this.mapYSize && y >= 0 && y < this.mapYSize);
    }
    
    public void getNewUnit(int talk)
    {
        for(Robot r : getVisibleRobots())
        {
            if(r.unit == talk)
            {
                boolean n = true;
                for(Integer oldRobID : robots[talk])
                {
                    if(r.id == oldRobID)
                    {
                        n = false;
                        break;
                    }
                }
                if(n)
                {
                    robots[talk].add(r.id);
                    break;
                }
            }
        }
    }
    
    public Robot getCastObj(int num)
    {
        Robot[] visb = getVisibleRobots();
        
        for(Robot cast : visb)
        {
            if(cast.id == robots[0].get(num))
            {
                return cast;
            }
        }
        return null;
    }
}
