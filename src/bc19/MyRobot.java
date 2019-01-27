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
    public HashSet<int[]> karboLocations;
    public HashSet<int[]> fuelLocations;
    public int karboDepositNum;
    public int fuelDepositNum;
    public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
    public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
    public boolean haveCastle = false;
    public int numberOfCastles = 0;
    public int[] castleIDs = new int[3]; //castle IDs
    public int[][] castleLocations = new int[3][2]; //locations of castles
    public HashMap<String, Integer> bots = new HashMap<String, Integer>(); //castles know what bots they have created
    public Stack<int[]> path = new Stack<int[]>(); //pathing with bfs
    public ArrayList<HashSet<int[]>> clumpList;
    public int clumpIndex = 0, depositIndex = 0;
    public boolean settled = false; //can this pilgrim change clumps?? if settled, cant
    public int[] clumpCenter;
    public boolean foundClumpCenter = false;
    
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
            
            //records number of robots
            bots.put("pilgrims", 0);
            bots.put("preachers", 0);
            bots.put("prophets", 0);
            
            //finding clumps
            ArrayList<int[]> sortedResources = findSortedResources();
//    		log("Sorted Resources: " + sortedResources);
            clumpList = findAllClumps(sortedResources);
//    		displayAllClumps(this.clumpList);
        }
        visibleRobotMap = this.getVisibleRobotMap(); //get visible robots every turn
        if (me.unit == SPECS.CASTLE) { //castle
        	if (me.turn == 1) {
        		
        		this.castleIDs[numberOfCastles] = this.me.id;
        		this.castleLocations[numberOfCastles] = new int[] {me.y, me.x};
        		numberOfCastles++;
        		
        		for (Robot r : getVisibleRobots()) { //tries to find friendly castles visible
        			if (r.team == me.team && r.id != me.id) {
        				castleIDs[numberOfCastles] = r.id;
        				this.castleLocations[numberOfCastles] = new int[] {r.y, r.x};
        				numberOfCastles += 1;
        			}
        		}

        		if (this.numberOfCastles > 1)
        		{
        			castleTalk(me.x);
        			for(int i = 1; i < this.numberOfCastles; i++)
        			{
        				Robot r = getRobot(this.castleIDs[i]);
        				if(r.turn == 1) {
        					this.castleLocations[i][1] = r.castle_talk;
        				}
        			}
        		}
        		
        		return null; //can't make pilgrims or else will mess up count
        	}
        	
        	else if (me.turn == 2) {
        		if (this.numberOfCastles > 1)
        		{
        			castleTalk(me.y);

        			for(int i = 1; i < this.numberOfCastles; i++) {
        				Robot r = getRobot(this.castleIDs[i]);
        				if(r.turn == 2) {
        					this.castleLocations[i][0] = r.castle_talk;
        				}
        				else {
        					this.castleLocations[i][1] = r.castle_talk;
        				}
        			}
        		}
        	}
        	else if (me.turn == 3) {
        		if (this.numberOfCastles > 1)
        		{
        			for(int i = 1; i < this.numberOfCastles; i++) {
        				Robot r = getRobot(this.castleIDs[i]);
        				if(r.turn == 2) {
        					this.castleLocations[i][0] = r.castle_talk;
        				}
        			}
        		}
//        		this.log("number of castles = " + this.numberOfCastles);
//        		this.log("castle location 1: x=" + this.castleLocations[0][1] + " y=" + this.castleLocations[0][0]);
//        		this.log("castle location 2: x=" + this.castleLocations[1][1] + " y=" + this.castleLocations[1][0]);
//        		this.log("castle location 3: x=" + this.castleLocations[2][1] + " y=" + this.castleLocations[2][0]);
        	}
        	if (this.bots.get("pilgrims") <= this.clumpList.get(this.clumpIndex).size()) { //makes 1 more pilgrim than necessary (to venture to another clump)
        		if (this.canBuild(SPECS.PILGRIM))  {
        			bots.put("pilgrims", bots.get("pilgrims") + 1);
        			return this.makeUnit(SPECS.PILGRIM);
        		}
        	}
        	//attacks enemies nearby
        	HashSet<Robot> enemies = findBadGuys();
        	Robot closeBadGuy = findPrimaryEnemyDistance(enemies);
        	//log("Castle Health: " + me.health);
        	if (closeBadGuy != null)
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
        if (me.unit == SPECS.CHURCH) {
        	if (this.bots.get("pilgrims") <= this.clumpList.get(this.clumpIndex).size()) { //makes 1 more pilgrim than necessary (to venture to another clump)
                if (this.canBuild(SPECS.PILGRIM))  {
                    bots.put("pilgrims", bots.get("pilgrims") + 1);
                    return this.makeUnit(SPECS.PILGRIM);
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
            if (settled && (canMineFuel() || canMineKarbonite())) { //mining
                //                this.log("mining");
                return mine();
            }
            if (!haveCastle && settled) { //need to find the castle/church
            	this.castleLocations[0] = this.findMyCastle();
            	if (this.castleLocations[0] != null) { //castle found
            		haveCastle = true;
            	}
            }
            if (settled && haveCastle && canGiveStuff()) { //give stuff to the castle
                //                this.log("giving to castle, karbo=" + this.me.karbonite + " fuel=" + this.me.fuel);
                return give(castleLocations[0][1] - this.me.x, castleLocations[0][0] - this.me.y, me.karbonite, me.fuel);
            }
            if (this.foundClumpCenter && this.canBuild(SPECS.CHURCH) && this.findDistance(this.clumpCenter) <= 2 && this.findNearbyCastleChurchNum() == 0) { //can build church and adjacent to clump center and no nearby establishments
            	this.settled = true;
            	return this.buildUnit(SPECS.CHURCH, this.clumpCenter[1] - this.me.x, this.clumpCenter[0] - this.me.y);
            }
            if (settled && haveCastle && (me.karbonite==20||me.fuel==100)) { //return to the castle
                //                this.log("returning to castle");
            	path = this.bfs(castleLocations[0]);
            }
            else if (!settled) { //need to find a clump
            	int nearbyPilgrimNum = this.findNearbyPilgrimNum();
            	if (nearbyPilgrimNum == this.clumpList.get(this.clumpIndex).size()) { //there are enough pilgrims for this clump, so move to next clump
            		if (!this.foundClumpCenter) {
            			this.clumpIndex++;
            			this.clumpCenter = this.findClumpCenter(this.clumpList.get(this.clumpIndex));
            			this.foundClumpCenter = this.clumpCenter != null;
            		}
            		this.path = this.bfs(this.clumpCenter);
            	}
            	else { //not enough pilgrims for this clump
            		this.settled = true; //stay at this clump
            		this.depositIndex = nearbyPilgrimNum; //assigned to the next open deposit
            	}
            }
            else { //settled in a clump, finding deposits
                int[] closestKarbonite = this.searchForKarboniteLocation();
                int[] closestFuel = this.searchForFuelLocation();
                //                this.log("karbo distance=" + findDistance(this.me, closestKarbonite[1], closestKarbonite[0]));
                //                this.log("fuel distance=" + findDistance(this.me, closestFuel[1], closestFuel[0]));
                //                this.log("pilgrim at x=" + this.me.x + " y=" + this.me.y + "\nkarbo at x=" + closestKarbonite[0] + " y=" + closestKarbonite[1] + "\nfuel at x=" + closestFuel[0] + " y=" + closestFuel[1]);
                if (findDistance(this.me, closestKarbonite[1], closestKarbonite[0]) > findDistance(this.me, closestFuel[1], closestFuel[0])) {
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
        if (me.unit==SPECS.PREACHER) { //preacher
            if (fuel >= 15) { //optimized attack
                //                //investigating AoE --> 3x3 area. it's the attacked square and all the adjacents to that square
                //                this.log(this.me.health + " health");
                //                return this.attack(1, 1);
                HashSet<Robot> potentialEnemies = this.findBadGuys();
                if (!potentialEnemies.isEmpty()) {
                    return this.preacherAttack();
                }
            }
        }
        if (me.unit == SPECS.PROPHET) { //prophet
            
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
    
 // Parse through karbonite map and fuel map and put locations in the same
 	// Make ArrayList of locations sorted with everything by distance to the castle

 	// Make ArrayList of clumps (which is an arraylist of arrays) technically

 	// Repeat the following until the HashMap size is 0

 	// Find the closest thing

 	// Put that in clump

 	// Go through ArrayList, and check if it was within 8 r^2 of any previous
 	// approved clump

 	// Once done with making a full clump, remove locations from the initial sorted
 	// ArrayList
 	public void displayAllClumps(ArrayList<HashSet<int[]>> allClumps) {
 		for(int i=0;i<allClumps.size();i++) {
 			log("Clump "+i+": "+allClumps.get(i));
 		}
 	}
 	
 	public ArrayList<HashSet<int[]>> findAllClumps(ArrayList<int[]> sortedResources) {
 		ArrayList<HashSet<int[]>> everyClump=new ArrayList<HashSet<int[]>>();
 		while(sortedResources.size()>0) {
 			everyClump.add(findClump(sortedResources));
 			int lastIndex=everyClump.size()-1;
 			Iterator<int[]> clumpRemovalFromArrayListIterator=everyClump.get(lastIndex).iterator();
 			while(clumpRemovalFromArrayListIterator.hasNext()) {
 				sortedResources.remove(clumpRemovalFromArrayListIterator.next());
 			}
 		}
 		return everyClump;
 	}
 	
 	
 	public HashSet<int[]> findClump(ArrayList<int[]> sortedResources) {
 		HashSet<int[]> clump = new HashSet<int[]>();
 		clump.add(sortedResources.get(0));
 		for (int i = 1; i < sortedResources.size(); i++) {
 			Iterator<int[]> clumpIterator=clump.iterator();
 			while(clumpIterator.hasNext()) {
 				int[] aClumpLocation=clumpIterator.next();
 				if(findDistance(sortedResources.get(i),aClumpLocation)<=9) {
 					clump.add(sortedResources.get(i));
 				}
 			}
 		}
 		return clump;
 	}

 	public ArrayList<int[]> findSortedResources() {
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
// 			log("Resources get i: " + resources.get(i));
// 			log("Pivot: " + pivot);
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
 	
 	//returns (y,x)
    //NOT (x,y)
    public int[] findClumpCenter(HashSet<int[]> clump){
        double length = (double)(clump.size());
        int x = 0;
        int y = 0;
        for (int[] spot : clump){
            x += spot[1];
            y += spot[0];
        }
        x = (int)Math.round(x/length);
        y = (int)Math.round(y/length);
        
        if (this.passableMap[y][x] && !this.karboniteMap[y][x] && !this.fuelMap[y][x]) { //main clump center is free to build on
			return new int[] {y, x};
		}
        //otherwise find distance squared and pick the smallest one that is also free to build on
        int distance, minDistance1 = Integer.MAX_VALUE, minDistance2 = Integer.MAX_VALUE;
        int[] spot, bestSpot1 = null, bestSpot2 = null;
        for (int r = Math.max(0, y - 1); r < Math.min(this.mapYSize, y + 2); r++) {
        	for (int c = Math.max(0, x - 1); c < Math.min(this.mapXSize, x + 2); c++) {
        		if (this.passableMap[r][c]) { //passable
        			spot = new int[] {r, c};
        			distance = 0;
        			for (int[] deposit : clump) { //calculate the total distance squared
        				distance += this.findDistance(spot, deposit);
        			}
        			if (distance < minDistance1 && !this.karboniteMap[r][c] && !this.fuelMap[r][c]) { //better option
        				minDistance1 = distance;
        				bestSpot1 = spot;
        			}
        			if (distance < minDistance2) { //last resort
        				minDistance2 = distance;
        				bestSpot2 = spot;
        			}
        		}
        	}
        }
        if (bestSpot1 != null) {
        	return bestSpot1;
        }
        //otherwise pick the smallest distance squared that is on a deposit
        if (bestSpot2 != null) {
        	return bestSpot2;
        }
        this.log("clump center null");
        return null; //this is really bad
    }

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
        int absoluteXCastleDistance = Math.abs(castleLocations[0][1] - this.me.x);
        int absoluteYCastleDistance = Math.abs(castleLocations[0][0] - this.me.y);
        if(absoluteXCastleDistance == 0 || absoluteXCastleDistance == 1) {
            if(absoluteYCastleDistance == 0 || absoluteYCastleDistance==1) {
                return this.me.karbonite > 0 || this.me.fuel > 0;
            }
        }
        return false;
    }
    
    public int[] findMyCastle() {
		int[] castleLocation = new int[2];
		Robot[] visibleRobots = getVisibleRobots();
		for(int i=0; i < visibleRobots.length; i++) {
			if(visibleRobots[i].unit == SPECS.CASTLE) {
				castleLocation[0] = visibleRobots[i].y;
				castleLocation[1] = visibleRobots[i].x;
			}
		}
		return castleLocation;
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
//            this.log("" + results.peek());
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
        return new int[] {minYCoordinate, minXCoordinate};
    }
    
    //builds a unit where available
    public Action makeUnit(int type) {
        int[] spot = this.checkAdjacentBuildAvailable(); //TODO build pilgrims on deposits if possible, otherwise don't build on deposits if possible
        //this.log("x=" + spot[0] + " y=" + spot[1]);
        return this.buildUnit(type, spot[1] - this.me.x, spot[0] - this.me.y);
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
    
    //finds all ally pilgrims nearby
    public int findNearbyPilgrimNum() {
    	int pilgrims = 0;
        Robot[] visibleBots = getVisibleRobots();
        for (int i = 0; i < visibleBots.length; i++) {
            if (me.team == visibleBots[i].team && visibleBots[i].unit == SPECS.PILGRIM) {
                pilgrims++;
            }
        }
        return pilgrims;
    }
    
    //find nearby castles/church num
    public int findNearbyCastleChurchNum() {
    	int x = 0;
        Robot[] visibleBots = getVisibleRobots();
        for (int i = 0; i < visibleBots.length; i++) {
            if (me.team == visibleBots[i].team && (visibleBots[i].unit == SPECS.CASTLE || visibleBots[i].unit == SPECS.CHURCH)) {
                x++;
            }
        }
        return x;
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
        return this.fuel >= SPECS.UNITS[type].CONSTRUCTION_FUEL && this.karbonite >= SPECS.UNITS[type].CONSTRUCTION_KARBONITE && this.checkAdjacentBuildAvailable()!=null;
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
