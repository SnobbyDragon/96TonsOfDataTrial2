package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Stack;

public class MyRobot extends BCAbstractRobot {
    public int turn;
    public final int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
    public boolean[][] passableMap;
    public int[][] visibleRobotMap;
    public boolean[][] karboniteMap;
    public boolean[][] fuelMap;
    public int mapYSize, mapXSize; //size of the map, length y and length x
    public String reflectAxis;
    public boolean didToggle;
    public HashSet<int[]> karboLocations;
    public HashSet<int[]> fuelLocations;
    public int karboDepositNum;
    public int fuelDepositNum;
    public int closeKarboNum, farKarboNum;
    public int closeFuelNum, farFuelNum;
    public final int CLOSE = 8;
    public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
    public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
    public boolean haveCastle = false;
    public int[] castleLocation = new int[2]; //location of castle
    public int[] crusaderTarget = new int[2]; //location of crusader target
    public HashMap<String, Integer> bots = new HashMap<String, Integer>(); //castles know what bots they have created
    public Stack<int[]> path = new Stack<int[]>();
    
    public Action turn() {
        turn++;
        if (turn == 1)
        {
            //gets maps
            passableMap = getPassableMap();
            karboniteMap = getKarboniteMap();
            fuelMap = getFuelMap();
            
            //reflectivity
            reflectAxis = this.reflectAxis();
            didToggle = false;
            
            //map size set
            mapYSize = passableMap.length;
            mapXSize = passableMap[0].length;
            
            //sets locations of deposits
            karboLocations = this.getKarboniteLocations();
            fuelLocations = this.getFuelLocations();
            //            this.log(karboLocations.toString());
            
            //sets number of deposits
            karboDepositNum = this.karboLocations.size();
            fuelDepositNum = this.fuelLocations.size();
            
            //close and far deposits
            closeKarboNum = this.findCloseKarboDepositNum(CLOSE); //closer than 5
            this.log("close karbos = " + this.closeKarboNum);
            closeFuelNum = this.findCloseFuelDepositNum(CLOSE); //closer than 5
            this.log("close fuel = " + this.closeFuelNum);
            
            //sets crusade target
            //            crusaderTarget.setint[](mapXSize/2, mapYSize/2);
//            this.setCrusadeTarget();
            
            //records number of robots
            bots.put("pilgrims", 0);
            bots.put("crusaders", 0);
            bots.put("preachers", 0);
            bots.put("prophets", 0);
            //this.log("x=" + this.me.x + " y=" + this.me.y);
            //this.logMap(this.karboniteMap);
        }
        visibleRobotMap = this.getVisibleRobotMap(); //get visible robots every turn
        if (me.unit == SPECS.CASTLE) { //castle
//            if (this.karbonite > 40 && this.turn <= 3) { //preachers to protect in the beginning
//                if (this.canBuild(SPECS.PREACHER)) {
//                    bots.put("preachers", bots.get("preachers") + 1);
//                    return this.makeUnit(SPECS.PREACHER);
//                }
//            }
            if (this.makeMorePilgrims()) { //|| (this.crusadeMode && this.makeEvenMorePilgrims())) { //build pilgrims //TODO too many rn
                if (this.canBuild(SPECS.PILGRIM))  {
                    //                    log("built pilgrim at x=" + this.checkAdjacentAvailable()[0] + " y=" + this.checkAdjacentAvailable()[1] + "\ncastle at x=" + this.me.x + " y=" + this.me.y);
                    bots.put("pilgrims", bots.get("pilgrims") + 1);
                    return this.makeUnit(SPECS.PILGRIM);
                }
            }
//            if (bots.get("preachers") < 2) { //build 2 preachers
//                if (this.canBuild(SPECS.PREACHER)) {
//                    //                    log("built preacher");
//                    bots.put("preachers", bots.get("preachers") + 1);
//                    return this.makeUnit(SPECS.PREACHER);
//                }
//            }
//            if (bots.get("crusaders") <= 5) { //build up to 5 crusaders per castle //TODO stop this nonsense
//                if (this.canBuild(SPECS.CRUSADER)) {
//                    //                    log("built crusader");
//                    bots.put("crusaders", bots.get("crusaders") + 1);
//                    return this.makeUnit(SPECS.CRUSADER);
//                }
//            }
//            if (this.karbonite >= 50 && bots.get("prophets") <= 5) {
//                if (this.canBuild(SPECS.PROPHET)) {
//                    //                    log("built prophet");
//                    bots.put("prophets", bots.get("prophets") + 1);
//                    return this.makeUnit(SPECS.PROPHET);
//                }
//            }
            //attacks enemies nearby
            HashSet<Robot> enemies = findBadGuys();
            Robot closeBadGuy = findPrimaryEnemyDistance(enemies);
            //log("Castle Health: " + me.health);
            if(closeBadGuy != null)
            {
                //log("found bad guy");
                //log("bad guy distance: " + closeBadGuy.x + ", " + closeBadGuy.y);
                
                try {
                    //log("Attacked");
                    return attack(closeBadGuy.x - me.x,closeBadGuy.y - me.y);
                } catch (Exception e) {
                    //log("Failed to attack");
                }
            }
        }
        if (me.unit == SPECS.PILGRIM) { //pilgrim
            //            log("I am a pilgrim");
            //            log("My karbonite: "+me.karbonite);
            //            log("My fuel: "+me.fuel);
            //            log("Have Castle: "+haveCastle);
            //            log("My Castle X: "+castleLocation.x);
            //            log("My Castle Y: "+castleLocation.y);
            //            log("My X position: "+me.x);
            //            log("My Y position: "+me.y);
            if (this.fuel < 500 && canMineFuel() || canMineKarbonite()) {
                //                this.log("mining");
                return mine();
            }
            if (!haveCastle) {
                if(locateNearbyCastle()) {
                    haveCastle = true;
                }
            }
            if (haveCastle && canGiveStuff()) {
                //                this.log("giving to castle, karbo=" + this.me.karbonite + " fuel=" + this.me.fuel);
                return give(castleLocation[1] - this.me.x, castleLocation[0] - this.me.y, me.karbonite, me.fuel);
            }
            if (haveCastle && (me.karbonite==20||me.fuel==100)) {
                //                this.log("returning to castle");
                return this.pathFind(castleLocation);
                //                path = this.bfs(castleLocation);
            }
            else { //TODO focus more on karbo than fuel
                int[] closestKarbonite = this.searchForKarboniteLocation();
                int[] closestFuel = this.searchForFuelLocation();
                //                this.log("karbo distance=" + findDistance(this.me, closestKarbonite[1], closestKarbonite[0]));
                //                this.log("fuel distance=" + findDistance(this.me, closestFuel[1], closestFuel[0]));
                //                this.log("pilgrim at x=" + this.me.x + " y=" + this.me.y + "\nkarbo at x=" + closestKarbonite[0] + " y=" + closestKarbonite[1] + "\nfuel at x=" + closestFuel[0] + " y=" + closestFuel[1]);
                if (this.fuel < 800 && findDistance(this.me, closestKarbonite[1], closestKarbonite[0]) > findDistance(this.me, closestFuel[1], closestFuel[0])) {
                    //                    this.log("getting fuel");
                    //                    return this.pathFind(closestFuel);
                    path = this.bfs(closestFuel);
                }
                else {
                    //                    this.log("getting karbo");
                    //                    return this.pathFind(closestKarbonite);
                    path = this.bfs(closestKarbonite);
                }
            }
            if (path == null) { //you ain't going nowhere
                //nothing for now
                this.log("no path");
            }
            else {
                int[] spot = this.path.pop();
                return this.move(spot[1] - this.me.x, spot[0] - this.me.y);
            }
        }
        if (me.unit == SPECS.CRUSADER) { //crusader
            //move crusade target every so turns
            //            this.log(crusaderTarget.x + " " + crusaderTarget.y);
            if (fuel >= 10) {
                HashSet<Robot> enemies = findBadGuys();
                if (enemies.isEmpty() && this.fuel > 100) {
                    return this.pathFind(crusaderTarget);
                }
                //                return this.crusaderAttack(enemies);
                //                log("Enemies size: "+enemies.size());
                Robot closeBadGuy = findPrimaryEnemyTypeDistance(enemies);
                try {
                    log("Other bad guy data " + closeBadGuy.x);
                    return attack(closeBadGuy.x - me.x,closeBadGuy.y - me.y);
                } catch (Exception e) {
                    log("Can't attack the man");
                    try {
                        return pathFind(new int[] {closeBadGuy.y, closeBadGuy.x});
                    } catch (Exception ef) {
                        log("Can't find the man");
                    }
                }
            }
        }
        if (me.unit==SPECS.PREACHER) { //preacher
            //            if (!this.haveCastle) {
            //                this.locateNearbyCastle();
            //            }
            //            if (this.haveCastle && this.isAdjacentToCastle()) { //get out of the way
            //
            //            }
            //            this.log(this.me.unit + " ");
            //            this.log("PREACHER=" + SPECS.PREACHER + "   PILGRIM=" + SPECS.PILGRIM);
            //            Iterator<int[]> iter = karboLocations.iterator();
            //            int[] p = iter.next();
            //            int[] p2 = new int[](p[1], p[0]);
            //            this.log((p.equals(p2)&&p.hashCode()==p2.hashCode()) + "");
            //            this.log(this.karboLocations.contains(p2) + " karbo");
            //            this.log(this.karboLocations.contains(new int[](this.me.x, this.me.y)) + " on a karbo");
            //            this.log(this.fuelLocations.contains(new int[](this.me.x, this.me.y)) + " on a fuel");
            //            if (this.karboLocations.contains(new int[](this.me.x, this.me.y)) || this.fuelLocations.contains(new int[](this.me.x, this.me.y))) { //TODO: doesn't work
            //                this.log("on a deposit");
            //            }
            
            if (this.isAdjacentToCastle() || this.onFuel() || this.onKarbo()) { //next to castle, or on a deposit
                //TODO
                return this.preacherMovesOutOfTheWay();
            }
            if (fuel >= 15) { //optimized attack
                //                //investigating AoE --> 3x3 area. it's the attacked square and all the adjacents to that square
                //                this.log(this.me.health + " health");
                //                return this.attack(1, 1);
                HashSet<Robot> potentialEnemies = this.findBadGuys();
                if (!potentialEnemies.isEmpty()) {
                    return this.preacherAttack();
                }
                //                AttackAction maybeSauce = preacherAttack(potentialEnemies);
                //                if (maybeSauce != null) {
                //                    return maybeSauce;
                //                }
                //                HashSet<Robot> enemies = findBadGuys();
                //                Robot targetBadGuy = this.findPrimaryEnemyDistance(enemies);
                //                try {
                //                    return attack(targetBadGuy.x-me.x,targetBadGuy.y-me.y);
                //                } catch (Exception e) {
                //                    this.log(e.getMessage());
                //                }
            }
            if (this.onFuel() || this.onKarbo()) {
                
            }
        }
        if (me.unit == SPECS.PROPHET) { //prophet
            HashSet<Robot> enemies = findBadGuys();
            if (enemies.isEmpty() && this.fuel > 100) {
                return this.pathFind(crusaderTarget);
            }
            Robot enemy = this.findPrimaryEnemyTypeDistance(enemies);
            if (this.canAttack(this.findDistance(this.me, enemy))) {
                //                this.log("prophet attacking");
                return this.attack(enemy.x - this.me.x, enemy.y - this.me.y);
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
    public void logMap(int[][] map) {
        for (int r = 0; r < mapYSize; r++) {
            for (int c = 0; c < mapXSize; c++) {
                this.log(r + ", " + c + " = " + map[r][c]);
            }
        }
    }
    
    //crusaders attack TODO clogging??
//    public Action crusaderAttack(HashSet<Robot> potentialEnemies) {
//    	//Create arraylist of preachers, prophets, crusaders, castles, churches, pilgrims
//    	ArrayList<Robot> preachers = new ArrayList<Robot>();
//    	ArrayList<Robot> prophets = new ArrayList<Robot>();
//    	ArrayList<Robot> crusaders = new ArrayList<Robot>();
//    	ArrayList<Robot> castles = new ArrayList<Robot>();
//    	ArrayList<Robot> churches = new ArrayList<Robot>();
//    	ArrayList<Robot> pilgrims = new ArrayList<Robot>();
//    	//Iterate through potentialEnemies and do the following
//    	Iterator<Robot> potentialEnemiesIterator = potentialEnemies.iterator();
//    	while(potentialEnemiesIterator.hasNext()) {
//    		Robot enemy = potentialEnemiesIterator.next();
//    		//Check the type
//    		//Based on the type, it would put it in the corresponding arraylist
//    		if(enemy.unit==SPECS.PREACHER) {
//    			preachers.add(enemy);
//    		} else if(enemy.unit==SPECS.PROPHET) {
//    			prophets.add(enemy);
//    		} else if(enemy.unit==SPECS.CRUSADER) {
//    			crusaders.add(enemy);
//    		} else if(enemy.unit==SPECS.CASTLE) {
//    			castles.add(enemy);
//    		} else if(enemy.unit==SPECS.CHURCH) {
//    			churches.add(enemy);
//    		} else if(enemy.unit==SPECS.PILGRIM) {
//    			pilgrims.add(enemy);
//    		}
//    	}
//    	//ArrayList sorts at very end
//    	preachers = sortArrayListByDistance(preachers);
//    	prophets = sortArrayListByDistance(prophets);
//    	crusaders = sortArrayListByDistance(crusaders);
//    	castles = sortArrayListByDistance(castles);
//    	churches = sortArrayListByDistance(churches);
//    	pilgrims = sortArrayListByDistance(pilgrims);
//
//    	//sortArrayListByDistance(ArrayList<Robot> robots)
//    	//Run through preacher arraylist
//    	for(int i=0;i<preachers.size();i++) {
//    		Robot thePreacher=preachers.get(i);
//    		int distanceX=thePreacher.x-me.x;
//    		int distanceY=thePreacher.y-me.y;
//    		AttackAction potentialAttack=attack(distanceX,distanceY);
//    		//Keep trying to attack them
//    		if(potentialAttack!=null) {
//    			return potentialAttack;
//    		}
//    	}
//
//    	//Run through prophet arraylist
//    	for (int i=0;i<prophets.size();i++) {
//    		Robot theProphet=prophets.get(i);
//    		int distanceX=theProphet.x-me.x;
//    		int distanceY=theProphet.y-me.y;
//    		AttackAction potentialAttack=attack(distanceX,distanceY);
//    		//Keep trying to attack them
//    		if(potentialAttack!=null) {
//    			return potentialAttack;
//    		}
//    	}
//
//    	//If attack fails, return move towards prophets
//    	for (int i = 0; i < prophets.size(); i++) {
//    		Robot theProphet=prophets.get(i);
//    		//            int distanceX=theProphet.x-me.x;
//    		//            int distanceY=theProphet.y-me.y;
//    		//pathfind here
//    		return this.pathFind(new int[](theProphet.x, theProphet.y));
//    	}
//
//    	//Run through crusader arraylist
//    	for (int i=0;i<crusaders.size();i++) {
//    		Robot theCrusader=crusaders.get(i);
//    		int distanceX=theCrusader.x-me.x;
//    		int distanceY=theCrusader.y-me.y;
//    		AttackAction potentialAttack=attack(distanceX,distanceY);
//    		//Keep trying to attack them
//    		if(potentialAttack!=null) {
//    			return potentialAttack;
//    		}
//    	}
//
//    	//Run through castle arraylist
//    	for (int i=0;i<castles.size();i++) {
//    		Robot theCastle=castles.get(i);
//    		int distanceX=theCastle.x-me.x;
//    		int distanceY=theCastle.y-me.y;
//    		AttackAction potentialAttack=attack(distanceX,distanceY);
//    		//Keep trying to attack them
//    		if(potentialAttack!=null) {
//    			return potentialAttack;
//    		}
//    	}
//
//    	//Run through church arraylist
//    	for(int i=0;i<churches.size();i++) {
//    		Robot theChurch=churches.get(i);
//    		int distanceX=theChurch.x-me.x;
//    		int distanceY=theChurch.y-me.y;
//    		AttackAction potentialAttack=attack(distanceX,distanceY);
//    		//Keep trying to attack them
//    		if(potentialAttack!=null) {
//    			return potentialAttack;
//    		}
//    	}
//
//    	//Run through pilgrim arraylist
//    	for(int i=0;i<pilgrims.size();i++) {
//    		Robot thePilgrim=pilgrims.get(i);
//    		int distanceX=thePilgrim.x-me.x;
//    		int distanceY=thePilgrim.y-me.y;
//    		AttackAction potentialAttack=attack(distanceX,distanceY);
//    		//Keep trying to attack them
//    		if(potentialAttack!=null) {
//    			return potentialAttack;
//    		}
//    	}
//
//    	//If attack fails, return move towards preachers
//    	for(int i=0;i<preachers.size();i++) {
//    		Robot thePreacher=preachers.get(i);
//    		//            int distanceX=thePreacher.x-me.x;
//    		//            int distanceY=thePreacher.y-me.y;
//    		//pathfind here
//    		return this.pathFind(new int[](thePreacher.x, thePreacher.y));
//    	}
//
//    	//If attack fails, return move towards crusaders
//    	for(int i=0;i<crusaders.size();i++) {
//    		Robot theCrusader=crusaders.get(i);
//    		//            int distanceX=theCrusader.x-me.x;
//    		//            int distanceY=theCrusader.y-me.y;
//    		//pathfind here
//    		return this.pathFind(new int[](theCrusader.x, theCrusader.y));
//    	}
//
//    	//If attack fails, return move towards castles
//    	for(int i=0;i<castles.size();i++) {
//    		Robot theCastles=castles.get(i);
//    		//            int distanceX=theCastles.x-me.x;
//    		//            int distanceY=theCastles.y-me.y;
//    		//pathfind here
//    		return this.pathFind(new int[](theCastles.x, theCastles.y));
//    	}
//
//    	//If attack fails, return move towards churches
//    	for(int i=0;i<churches.size();i++) {
//    		Robot theChurches=churches.get(i);
//    		//            int distanceX=theChurches.x-me.x;
//    		//            int distanceY=theChurches.y-me.y;
//    		//pathfind here
//    		return this.pathFind(new int[](theChurches.x, theChurches.y));
//    	}
//
//    	//If attack fails, return move towards pilgrims
//    	for(int i=0;i<pilgrims.size();i++) {
//    		Robot thePilgrim=pilgrims.get(i);
//    		//            int distanceX=thePilgrim.x-me.x;
//    		//            int distanceY=thePilgrim.y-me.y;
//    		//pathfind here
//    		return this.pathFind(new int[](thePilgrim.x, thePilgrim.y));
//    	}
//    	return null;
//    }
    
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
    
    public void swap(ArrayList<Robot> a, int i, int j) {
        Robot tmp = a.get(i);
        a.set(i, a.get(j));
        a.set(j, tmp);
    }
    
    //after destroying them, toggle reflection to find other castles
    public void toggleReflection() {
        if (!this.didToggle) { //only toggles once
            if (this.reflectAxis.equals("horizontal")) {
                this.reflectAxis = "vertical";
            }
            else {
                this.reflectAxis = "horizontal";
            }
            this.didToggle = true;
        }
    }
    
    //sets where we're attacking //USELESS
//    public void setCrusadeTarget() {
//        //        this.log(this.crusadeTurns + "");
//        if (this.reflectAxis.equals("horizontal")) {
//            this.crusaderTarget = new int[] {this.me.y, this.mapXSize - this.me.x};
//        }
//        else { //this.reflectAxis.equals("vertical")
//            this.crusaderTarget = new int[] {this.mapYSize - this.me.y, this.me.x};
//        }
//        //        this.log(this.crusaderTarget.toString());
//        //        else if (this.turn == 4*interval) { //up
//        //            //            this.log("up crusade target");
//        //            this.crusaderTarget.setint[](this.mapXSize/2, this.mapYSize/4);
//        //        }
//        //        else if (this.turn == 5*interval) { //up right
//        //            this.crusaderTarget.setint[](this.mapXSize*3/4, this.mapYSize/4);
//        //        }
//        //        else if (this.turn == 6*interval) { //right
//        //            this.crusaderTarget.setint[](this.mapXSize*3/4, this.mapYSize/2);
//        //        }
//        //        else if (this.turn == 7*interval) { //down right
//        //            this.crusaderTarget.setint[](this.mapXSize*3/4, this.mapYSize*3/4);
//        //        }
//        //        else if (this.turn == 8*interval) { //down
//        //            this.crusaderTarget.setint[](this.mapXSize/2, this.mapYSize*3/4);
//        //        }
//        //        else if (this.turn == 9*interval) { //down left
//        //            this.crusaderTarget.setint[](this.mapXSize/4, this.mapYSize*3/4);
//        //        }
//        //        else if (this.turn == 10*interval) { //left
//        //            this.crusaderTarget.setint[](this.mapXSize/4, this.mapYSize/2);
//        //        }
//        //        else if (this.turn == 11*interval) { //up left
//        //            this.crusaderTarget.setint[](this.mapXSize/4, this.mapYSize/4);
//        //        }
//    }
    
    //checks reflectivity of the map
    public String reflectAxis(){
        for(int col = 0; col < this.passableMap.length/2+1; col++){
            for(int row = 0; row < this.passableMap.length; row++){
                if (this.passableMap[row][col] != this.passableMap[row][this.passableMap.length-1-col]){
                    //                    log(col + " " + row);
                    return "vertical";
                }
            }
        }
        return "horizontal";
    }
    
    //Can this pilgrim give stuff to the castle
    public boolean canGiveStuff() {
        int absoluteXCastleDistance = Math.abs(castleLocation[1] - this.me.x);
        int absoluteYCastleDistance = Math.abs(castleLocation[0] - this.me.y);
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
                castleLocation[0] = goodGuy.y;
                castleLocation[1] = goodGuy.x;
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
    
    //breadth first search pathing
    public Stack<int[]> bfs(int[] finalLocation) {
        boolean blocked = this.visibleRobotMap[finalLocation[0]][finalLocation[1]] > 0; //can't go there because robot is in the way
        double shortestDistance = this.findDistance(this.me.x, this.me.y, finalLocation[1], finalLocation[0]);
        int closest = -1;
//        this.log("i am here (" + this.me.x + ", " + this.me.y + ") and going to " + finalLocation);
        //        this.log("d^2 from target " + this.findDistance(this.me, finalLocation.x, finalLocation.y));
        int speed = this.getMovementRangeRadius(this.me.unit); //movement speed
        int current = this.me.y*this.mapXSize + this.me.x; //current spot
        int finalLoc = finalLocation[0]*this.mapXSize + finalLocation[1]; //final location
        int[] tracer = new int[this.mapXSize*this.mapYSize]; //int[]s will be represented as a single integer -> row*row.length + column
        Arrays.fill(tracer, -1);
        LinkedList<Integer> toVisit = new LinkedList<Integer>();
        toVisit.add(current);
        
        int point;
        int currentC, currentR;
        double distanceCurrentToNeighbor, distanceCurrentToFinal;
        while (current != finalLoc) {
        	currentR = current/this.mapXSize;
        	currentC = current%this.mapXSize;
            for (int r = Math.max(currentR - speed, 0); r < Math.min(currentR + speed + 1, this.mapYSize); r++) {
                for (int c = Math.max(currentC - speed, 0); c < Math.min(currentC + speed + 1, this.mapXSize); c++) {
                    point = r*this.mapXSize + c;
                    distanceCurrentToNeighbor = this.findDistance(currentC, currentR, c, r);
                    if (distanceCurrentToNeighbor <= speed*speed && this.passableMap[r][c] && this.visibleRobotMap[r][c] <= 0) {
                        if (tracer[point] != -1) { //already checked this int[] before
                            continue;
                        }
                        tracer[point] = current; //tracks this int[] to the previous int[]
                        distanceCurrentToFinal = this.findDistance(c, r, finalLocation[1], finalLocation[0]);
                        if (blocked && distanceCurrentToFinal < shortestDistance) {
                            shortestDistance = distanceCurrentToFinal;
                            closest = point;
                            continue;
                        }
                        toVisit.add(point);
                    }
                }
            }
            
            if (blocked && closest != -1) {
                current = closest;
                break;
            }
            
            if (toVisit.isEmpty()) { //no path
            	return null;
            }
            current = toVisit.poll();
        }
//            this.log("checking " + new int[](x, y));
        Stack<int[]> results = new Stack<int[]>();
        while (tracer[current] != -1) {
            results.push(new int[] {current/this.mapXSize, current%this.mapXSize});
            this.log("" + results.peek());
            current = tracer[current];
        }
        return results;
    }
    
    //old path finding algorithm for moving
    public MoveAction pathFind(int[] finalLocation) {
        //        this.log("moving toward x=" + finalLocation[0] + " y=" + finalLocation[1]);
        if (fuel <= 30 || finalLocation[1]==-1) { //not enough fuel, or -1 b/c can't find karbo or fuel
            //            this.log("cannot move");
            return null;
        }
        int xDistance = finalLocation[1] - me.x;
        int yDistance = finalLocation[0] - me.y;
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
                    if (canMove(me.x, me.y + 1)) {
                        if(alreadyBeenHere(me,0,1)==false) {
                            return move(0,1);
                        }
                    } else if (canMove(me.x, me.y + 2)) {
                        if(alreadyBeenHere(me,0,2)==false) {
                            return move(0,2);
                        }
                    } else if (canMove(me.x, me.y + 3)) {
                        if(alreadyBeenHere(me,0,3)==false) {
                            return move(0,3);
                        }
                    }
                    
                } else if (index == 1) {
                    if (canMove(me.x + 1, me.y + 1)) {
                        if(alreadyBeenHere(me,1,1)==false) {
                            return move(1,1);
                        }
                    } else if (canMove(me.x + 2, me.y + 2)) {
                        if(alreadyBeenHere(me,2,2)==false) {
                            return move(2,2);
                        }
                    } else if (canMove(me.x + 2, me.y + 1)) {
                        if(alreadyBeenHere(me,2,1)==false) {
                            return move(2,1);
                        }
                    } else if (canMove(me.x + 1, me.y + 2)) {
                        if(alreadyBeenHere(me,1,2)==false) {
                            return move(1,2);
                        }
                    }
                } else if (index == 2) {
                    if (canMove(me.x + 1, me.y)) {
                        if(alreadyBeenHere(me,1,0)==false) {
                            return move(1,0);
                        }
                    } else if (canMove(me.x + 2, me.y)) {
                        if(alreadyBeenHere(me,2,0)==false) {
                            return move(2,0);
                        }
                    } else if (canMove(me.x + 3, me.y)) {
                        if(alreadyBeenHere(me,3,0)==false) {
                            return move(3,0);
                        }
                    }
                } else if (index == 3) {
                    if (canMove(me.x + 1, me.y - 1)) {
                        if(alreadyBeenHere(me,1,-1)==false) {
                            return move(1,-1);
                        }
                    } else if (canMove(me.x + 2, me.y - 2)) {
                        if(alreadyBeenHere(me,2,-2)==false) {
                            return move(2,-2);
                        }
                    } else if (canMove(me.x + 2, me.y - 1)) {
                        if(alreadyBeenHere(me,2,-1)==false) {
                            return move(2,-1);
                        }
                    } else if (canMove(me.x + 1, me.y - 2)) {
                        if(alreadyBeenHere(me,1,-2)==false) {
                            return move(1,-2);
                        }
                    }
                } else if (index == 4) {
                    if (canMove(me.x, me.y - 1)) {
                        if(alreadyBeenHere(me,0,-1)==false) {
                            return move(0,-1);
                        }
                    } else if (canMove(me.x, me.y - 2)) {
                        if(alreadyBeenHere(me,0,-2)==false) {
                            return move(0,-2);
                        }
                    } else if (canMove(me.x, me.y - 3)) {
                        if(alreadyBeenHere(me,0,-3)==false) {
                            return move(0,-3);
                        }
                    }
                } else if (index == 5) {
                    if (canMove(me.x - 1, me.y - 1)) {
                        if(alreadyBeenHere(me,-1,-1)==false) {
                            return move(-1,-1);
                        }
                    } else if (canMove(me.x - 2, me.y - 2)) {
                        if(alreadyBeenHere(me,-2,-2)==false) {
                            return move(-2,-2);
                        }
                    } else if (canMove(me.x - 2, me.y - 1)) {
                        if(alreadyBeenHere(me,-2,-1)==false) {
                            return move(-2,-1);
                        }
                    } else if (canMove(me.x - 1, me.y - 2)) {
                        if(alreadyBeenHere(me,-1,-2)==false) {
                            return move(-1,-2);
                        }
                    }
                } else if (index == 6) {
                    if (canMove(me.x - 1, me.y)) {
                        if(alreadyBeenHere(me,-1,0)==false) {
                            return move(-1,0);
                        }
                    } else if (canMove(me.x - 2, me.y)) {
                        if(alreadyBeenHere(me,-2,0)==false) {
                            return move(-2,0);
                        }
                    } else if (canMove(me.x - 3, me.y)) {
                        if(alreadyBeenHere(me,-3,0)==false) {
                            return move(-3,0);
                        }
                    }
                } else if (index == 7) {
                    if (canMove(me.x - 1, me.y + 1)) {
                        if(alreadyBeenHere(me,-1,1)==false) {
                            return move(-1,1);
                        }
                    } else if (canMove(me.x - 2, me.y + 2)) {
                        if(alreadyBeenHere(me,-2,2)==false) {
                            return move(-2,2);
                        }
                    } else if (canMove(me.x - 2, me.y + 1)) {
                        if(alreadyBeenHere(me,-2,1)==false) {
                            return move(-2,1);
                        }
                    } else if (canMove(me.x - 1, me.y + 2)) {
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
                    if (canMove(me.x, me.y + 1)) {
                        if(alreadyBeenHere(me,0,1)==false) {
                            return move(0,1);
                        }
                    } else if (canMove(me.x, me.y + 2)) {
                        if(alreadyBeenHere(me,0,2)==false) {
                            return move(0,2);
                        }
                    }
                    
                } else if (index == 1) {
                    if (canMove(me.x + 1, me.y + 1)) {
                        if(alreadyBeenHere(me,1,1)==false) {
                            return move(1,1);
                        }
                    }
                } else if (index == 2) {
                    if (canMove(me.x + 1, me.y)) {
                        if(alreadyBeenHere(me,1,0)==false) {
                            return move(1,0);
                        }
                    } else if (canMove(me.x + 2, me.y)) {
                        if(alreadyBeenHere(me,1,0)==false) {
                            return move(1,0);
                        }
                    }
                } else if (index == 3) {
                    if (canMove(me.x + 1, me.y - 1)) {
                        if(alreadyBeenHere(me,1,-1)==false) {
                            return move(1,-1);
                        }
                    }
                } else if (index == 4) {
                    if (canMove(me.x, me.y - 1)) {
                        if(alreadyBeenHere(me,0,-1)==false) {
                            return move(0,-1);
                        }
                    } else if (canMove(me.x, me.y - 2)) {
                        if(alreadyBeenHere(me,0,-2)==false) {
                            return move(0,-2);
                        }
                    }
                } else if (index == 5) {
                    if (canMove(me.x - 1, me.y - 1)) {
                        if(alreadyBeenHere(me,-1,-1)==false) {
                            return move(-1,-1);
                        }
                    }
                } else if (index == 6) {
                    if (canMove(me.x - 1, me.y)) {
                        if(alreadyBeenHere(me,-1,0)==false) {
                            return move(-1,0);
                        }
                    } else if (canMove(me.x - 2, me.y)) {
                        if(alreadyBeenHere(me,-2,0)==false) {
                            return move(-2,0);
                        }
                    }
                } else if (index == 7) {
                    if (canMove(me.x - 1, me.y + 1)) {
                        if(alreadyBeenHere(me,-1,1)==false) {
                            return move(-1,1);
                        }
                    }
                }
            }
        }
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
        return Math.abs(this.me.x-this.castleLocation[1])==1 && Math.abs(this.me.y-this.castleLocation[0])==1;
    }
    
    //checks if adjacent tiles are available. used for making units. checks tiles closer to the middle of the map first. //TODO build pilgrims on deposits, and other units not on deposits. if possible
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
    
    //returns a HashSet of all the karbo location coordinates
    public HashSet<int[]> getKarboniteLocations() {
        HashSet<int[]> karboniteLocations = new HashSet<int[]>();
        for (int r = 0; r < this.mapYSize; r++) {
            for (int c = 0; c < this.mapXSize; c++) {
                if (this.karboniteMap[r][c]) {
                    //                    this.log("adding karbo deposit location");
                    karboniteLocations.add(new int[] {r, c});
                }
            }
        }
        return karboniteLocations;
    }
    
    //returns a HashSet of all the fuel location coordinates
    public HashSet<int[]> getFuelLocations() {
        HashSet<int[]> fuelLocations = new HashSet<int[]>();
        for (int r = 0; r < this.mapYSize; r++) {
            for (int c = 0; c < this.mapXSize; c++) {
                if (this.fuelMap[r][c]) {
                    fuelLocations.add(new int[] {r, c});
                }
            }
        }
        return fuelLocations;
    }
    
    
    //gtfo preacher TODO: make this better
    public MoveAction preacherMovesOutOfTheWay() {
        MoveAction maybe=move(1,1);
        if(maybe!=null) {
            return maybe;
        }
        maybe = move(-1,-1);
        if(maybe!=null) {
            return maybe;
        }
        maybe=move(1,-1);
        if(maybe!=null) {
            return maybe;
        }
        maybe=move(-1,1);
        if(maybe!=null) {
            return maybe;
        }
        maybe = move(1,0);
        if(maybe!=null) {
            return maybe;
        }
        maybe = move(-1,0);
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
    
    //is this robot on a karbo deposit
    public boolean onKarbo() {
        int[] p = new int[] {this.me.y, this.me.x};
        Iterator<int[]> iter = this.karboLocations.iterator();
        int[] location;
        while (iter.hasNext()) {
            location = iter.next();
            if (location.equals(p)) {
                return true;
            }
        }
        return false;
    }
    
    //is this robot on a fuel deposit
    public boolean onFuel() {
    	int[] p = new int[] {this.me.y, this.me.x};
        Iterator<int[]> iter = this.fuelLocations.iterator();
        int[] location;
        while (iter.hasNext()) {
            location = iter.next();
            if (location.equals(p)) {
                return true;
            }
        }
        return false;
    }
    
    //searches for the closest karbonite
    public int[] searchForKarboniteLocation() {
        double minDistance = Double.MAX_VALUE;
        int minXCoordinate = -1;
        int minYCoordinate = -1;
        double distance;
        Iterator<int[]> iter = this.karboLocations.iterator();
        int[] location;
        while (iter.hasNext()) {
            location = iter.next();
            distance = findDistance(this.me, location[1], location[0]);
            if (this.visibleRobotMap[location[0]][location[1]]<=0 && distance > 0 && distance < minDistance) {
                minDistance = distance;
                minXCoordinate = location[1];
                minYCoordinate = location[0];
            }
        }
        //        for (int i = 0; i < mapYSize; i++) {
        //            for (int j = 0; j < mapXSize; j++) {
        //                if (karboniteMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
        //                    //this.log("i am here x=" + this.me.x + " y=" + this.me.y + "  could mine here x=" + j + " y=" + i);
        //                    distance = findDistance(me, j, i);
        //                    if (distance < minDistance) {
        //                        minDistance = distance;
        //                        minXCoordinate = j;
        //                        minYCoordinate = i;
        //                    }
        //                }
        //            }
        //        }
        return new int[] {minYCoordinate, minXCoordinate};
    }
    
    //searches for the closest fuel
    public int[] searchForFuelLocation() {
        double minDistance = Double.MAX_VALUE;
        int minXCoordinate = -1;
        int minYCoordinate = -1;
        double distance;
        Iterator<int[]> iter = this.fuelLocations.iterator();
        int[] location;
        while (iter.hasNext()) {
            location = iter.next();
            distance = findDistance(this.me, location[1], location[0]);
            if (this.visibleRobotMap[location[0]][location[1]]<=0 && distance > 0 && distance < minDistance) {
                minDistance = distance;
                minXCoordinate = location[1];
                minYCoordinate = location[0];
            }
        }
        //        for (int i = 0; i < mapYSize; i++) {
        //            for (int j = 0; j < mapXSize; j++) {
        //                if (fuelMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
        //                    distance = findDistance(me, j, i);
        //                    if (distance < minDistance) {
        //                        minDistance = distance;
        //                        minXCoordinate = j;
        //                        minYCoordinate = i;
        //                    }
        //                }
        //            }
        //        }
        return new int[] {minYCoordinate, minXCoordinate};
    }
    
    //builds a unit where available
    public Action makeUnit(int type) {
        int[] spot = this.checkAdjacentAvailable(); //TODO build pilgrims on deposits if possible, otherwise don't build on deposits if possible
        //this.log("x=" + spot[0] + " y=" + spot[1]);
        return this.buildUnit(type, spot[1] - this.me.x, spot[0] - this.me.y);
    }
    
    //should this castle make pilgrims (based on number of nearby deposits)
    public boolean makeMorePilgrims() {
        return this.bots.get("pilgrims") < this.closeFuelNum + this.closeKarboNum;
    }
    
    //finds the number of close fuel deposits (fuel <= x tiles away from castle)
    public int findCloseFuelDepositNum(int x) {
        int num = 0;
        Iterator<int[]> iter = this.fuelLocations.iterator();
        int[] location;
        while (iter.hasNext()) {
            location = iter.next();
            num += this.findDistance(this.me, location[1], location[0]) <= x ? 1 : 0;
        }
        return num;
    }
    
    //finds the number of close karbo deposits (karbo <= x tiles away from castle)
    public int findCloseKarboDepositNum(int x) {
        int num = 0;
        Iterator<int[]> iter = this.fuelLocations.iterator();
        int[] location;
        while (iter.hasNext()) {
            location = iter.next();
            num += this.findDistance(this.me, location[1], location[0]) <= x ? 1 : 0;
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
    
    // Finds distance squared between two int[]s
    public double findDistance(int[] one, int[] two) {
        int xDistance = one[1] - two[1];
        int yDistance = one[0] - two[0];
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
    }
    
    //Finds distance squared between two pairs of coordinates
    public double findDistance(int x1, int y1, int x2, int y2) {
        int xDistance = x1 - x2;
        int yDistance = y1 - y2;
        return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
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
            //            log("Distance: "+badGuyDistance);
            if(badGuyDistance < distance) {
                //                log("Found closer robot");
                distance = badGuyDistance;
                //                log("New closest distance: "+distance);
                closeBot = aBadGuy;
            }
            
        }
        //        if(closeBot==null) {
        //            log("Still null boo");
        //        } else {
        //            log("You the man");
        //        }
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
        if (!groupedEnemies.get(SPECS.PROPHET).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PROPHET));
        }
        if (!groupedEnemies.get(SPECS.CASTLE).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CASTLE));
        }
        if (!groupedEnemies.get(SPECS.CHURCH).isEmpty()) {
            return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.CHURCH));
        }
        return this.findPrimaryEnemyDistance(groupedEnemies.get(SPECS.PILGRIM));
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
        return this.getMinAttackRangeRadius(this.me.unit) <= Math.sqrt(distance) && Math.sqrt(distance) <= this.getMaxAttackRangeRadius(this.me.unit) && this.fuel >= this.getAttackFuel(this.me.unit);
    }
    
    //other robots can attack?
    public boolean canAttack(Robot r, double distance) {
        return this.getMinAttackRangeRadius(r.unit) <= Math.sqrt(distance) && Math.sqrt(distance) <= this.getMaxAttackRangeRadius(r.unit);
    }
    
    //finds the optimal place for preachers to attack (for AoE to be most effective)
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
        //        this.logMap(robotMap);
        //        int[][] weightedMap = new int[this.mapYSize][this.mapXSize]; //new int[this.getVisionRangeRadius(this.me.unit)*2+1][this.getVisionRangeRadius(this.me.unit)*2+1]; //TODO: size of vision radius. need to shift if I want to make this smaller matrix
        //        this.log((this.me.y - maxAttackRange) + " " + this.me.y + " " + (this.me.y + maxAttackRange));
        int value;
        for (int r = Math.max(this.me.y - maxAttackRange, 1); r < Math.min(this.me.y + maxAttackRange + 1, this.mapYSize-1); r++) {
            for (int c = Math.max(this.me.x - maxAttackRange, 1); c < Math.min(this.me.x + maxAttackRange + 1, this.mapXSize-1); c++) {
                //weightedMap[r][c] = this.averageAdjacent(robotMap, r, c);
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
    
    //can this unit be built? do we have enough fuel and karbonite? is there room?
    public boolean canBuild(int type) {
        return this.fuel >= SPECS.UNITS[type].CONSTRUCTION_FUEL && this.karbonite >= SPECS.UNITS[type].CONSTRUCTION_KARBONITE && this.checkAdjacentAvailable()!=null;
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
    
}
