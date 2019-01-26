/*
package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MyRobot extends BCAbstractRobot {
	public class Point {
		public int x;
		public int y;

		public Point() {
			
		}
		
		public Point(int x, int y) {
			setPoint(x, y);
		}

		public void setPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
		
		public boolean equals(Point p) {
			return this.getX()==p.getX() && this.getY()==p.getY();
		}
		
		@Override
		public String toString() {
			return "(" + this.getX() + ", " + this.getY() + ")";
		}
		
//		@Override
//		public boolean equals(Object obj) {
//			if (obj instanceof Point) {
//				Point p = (Point)obj;
//				return this.getX() == p.getX() && this.getY() == p.getY();	
//			}
//			return false;
//		}
//		
//		@Override
//		public int hashCode() {
//			return this.getX() + this.getY();
//		}
	}
	
	public class BFSPoint extends Point {
		
		List<BFSPoint> neighbors;
		BFSPoint parent;
		
		public BFSPoint() {
			// TODO Auto-generated constructor stub
			neighbors = new ArrayList<BFSPoint>();
			parent = null;
		}
		
		public BFSPoint(int x, int y) {
			super(x, y);
			neighbors = new LinkedList<BFSPoint>();
			parent = new BFSPoint();
		}
	}
	
	public int turn;
	public final int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public int mapYSize, mapXSize; //size of the map, length y and length x
	public String reflectAxis;
	public boolean didToggle;
	public HashSet<Point> karboLocations;
	public HashSet<Point> fuelLocations;
	public int karboDepositNum;
	public int fuelDepositNum;
	public int closeKarboNum, farKarboNum;
	public int closeFuelNum, farFuelNum;
	public final int CLOSE = 5, FAR = 10;
	public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
	public Point[] adjacents = new Point[] {new Point(1,0), new Point(1,1), new Point(0,1), new Point(-1,1), new Point(-1,0), new Point(-1,-1), new Point(0,-1), new Point(1,-1)};
	public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
	public boolean haveCastle = false;
	public Point castleLocation = new Point(); //location of castle
	public Point crusaderTarget = new Point(); //location of crusader target
	public HashMap<String, Integer> bots = new HashMap<String, Integer>(); //castles know what bots they have created
	public Queue<Point> path = new LinkedList<Point>();
	public int numberOfCastles;
	public ArrayList<Integer> castleRegions;
	public int myRegion;
	public int region16;
    public int region8;
    public boolean[][] theMap;
    public int mapXLength;
    public int mapYLength;
    public int mapXFirstEighth;
    public int mapXFirstQuarter;
    public int mapXThirdEighth;
    public int mapXSecondQuarter;
    public int mapXFifthEighth;
    public int mapXThirdQuarter;
    public int mapXSeventhEighth;
    public int mapYFirstEighth;
    public int mapYFirstQuarter;
    public int mapYThirdEighth;
    public int mapYSecondQuarter;
    public int mapYFifthEighth;
    public int mapYThirdQuarter;
    public int mapYSeventhEighth;
    //Determined by Tommy's method
    public boolean flippedAcrossX;
    public ArrayList<Integer> regionsOfCastles; 
    public ArrayList<Point> pointsOfCastles;
    public int signalForEverybody=-1;
    public int regionCode;
    public int otherCastleTalk;
	
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
			//			this.log(karboLocations.toString());

			//sets number of deposits
			karboDepositNum = this.karboLocations.size();
			fuelDepositNum = this.fuelLocations.size();
			//close and far deposits
			closeKarboNum = this.findCloseKarboDepositNum(CLOSE); //closer than 5
			farKarboNum = this.findFarKarboDepositNum(FAR); //farther than 10
			closeFuelNum = this.findCloseFuelDepositNum(CLOSE); //closer than 5
			farFuelNum = this.findFarFuelDepositNum(FAR); //farther than 10

			//sets crusade target
//			crusaderTarget.setPoint(mapXSize/2, mapYSize/2);
			this.setCrusadeTarget();

			//records number of robots
			bots.put("pilgrims", 0);
			bots.put("crusaders", 0);
			bots.put("preachers", 0);
			//this.log("x=" + this.me.x + " y=" + this.me.y);
			//this.logMap(this.karboniteMap);
			
			//stuff for finding enemy castles
			theMap=getPassableMap();
            mapYLength=theMap.length;
            mapXLength=theMap[0].length;
            mapXFirstEighth=mapXLength/8;
            mapXFirstQuarter=mapXLength/4;
            mapXThirdEighth=mapXLength*3/8;
            mapXSecondQuarter=mapXLength/2;
            mapXFifthEighth=mapXLength*5/8;
            mapXThirdQuarter=mapXLength*3/4;
            mapXSeventhEighth=mapXLength*7/8;
            
            mapYFirstEighth=mapYLength/8;
            mapYFirstQuarter=mapYLength/4;
            mapYThirdEighth=mapYLength*3/8;
            mapYSecondQuarter=mapYLength/2;
            mapYFifthEighth=mapYLength*5/8;
            mapYThirdQuarter=mapYLength*3/4;
            mapYSeventhEighth=mapYLength*7/8;
            regionsOfCastles=new ArrayList<Integer>();
            pointsOfCastles=new ArrayList<Point>();
            
		}
		
		visibleRobotMap = this.getVisibleRobotMap(); //get visible robots every turn
		if (me.unit != SPECS.CASTLE) { //dead??
			this.impendingDoom();
			
		}
		
		if (me.unit == SPECS.CASTLE) { 
			log("castle");
			
			//castle
			if (this.me.castle_talk > 0) { //something died
				int unit = this.me.castle_talk;
				this.log("DEATH " + unit);
				this.castleTalk(0);
				return this.makeUnit(unit);
			}
			if (this.karbonite > 40 && this.turn <= 3) { //preachers to protect in the beginning
				if (this.canBuild(SPECS.PREACHER)) {
					bots.put("preachers", bots.get("preachers") + 1);
					return this.makeUnit(SPECS.PREACHER);
				}
			}
			if (this.makeMorePilgrims()) { //|| (this.crusadeMode && this.makeEvenMorePilgrims())) { //build pilgrims //TODO too many rn
				if (this.canBuild(SPECS.PILGRIM))  {
//					log("built pilgrim at x=" + this.checkAdjacentAvailable()[0] + " y=" + this.checkAdjacentAvailable()[1] + "\ncastle at x=" + this.me.x + " y=" + this.me.y);
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
			if (bots.get("preachers") == 3 && (turn >= 100 || this.karbonite >= 50) && bots.get("crusaders") <= 5) { //build up to 5 crusaders per castle //TODO stop this nonsense
				if (this.canBuild(SPECS.CRUSADER)) {
//					log("built crusader");
					bots.put("crusaders", bots.get("crusaders") + 1);
					return this.makeUnit(SPECS.CRUSADER);
				}
			}
			if (bots.get("preachers") == 3 && (turn >= 100 || this.karbonite >= 50) && bots.get("prophets") <= 5) {
				if (this.canBuild(SPECS.PROPHET)) {
//					log("built prophet");
					bots.put("prophets", bots.get("prophets") + 1);
					return this.makeUnit(SPECS.PROPHET);
				}
			}
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
			
			
			//finding enemy castle stuff
			 if (turn == 1) {
	                int summingUpCastles=me.castle_talk+1;
	                castleTalk(summingUpCastles);
	            }
	            if(turn==2) {
	                numberOfCastles=me.castle_talk;
	                log("number of castles:" + numberOfCastles);
	            }
	            if(numberOfCastles==1) {
	                if(turn == 3) {
	                    region16=findMyRegion16();
	                    if(region16==0) {
	                        log("Couldn't find a region");
	                    }
	                    if(flippedAcrossX) {
	                        region8=findMyRegion8X(region16);
	                    } else {
	                        region8=findMyRegion8Y(region16);
	                    }
	                    regionsOfCastles.add(region8);
	                    pointsOfCastles=getEnemyCastlePoints(regionsOfCastles);
	                }
	                signal(region8,4);
	            } else if(numberOfCastles==2) {
	                if (turn==3) {
	                    region16=findMyRegion16();
	                    if(region16==0) {
	                        log("Couldn't find a region");
	                    }
	                    if(flippedAcrossX) {
	                        region8=findMyRegion8X(region16);
	                    } else {
	                        region8=findMyRegion8Y(region16);
	                    }
	                    regionsOfCastles.add(region8);
	                    int potentialSignal=me.castle_talk;
	                    if(potentialSignal>=10) {
	                        int remainderSignal=potentialSignal%10;
	                        if(regionsOfCastles.size()<numberOfCastles) {
	                            regionsOfCastles.add(remainderSignal);
	                        }
	                    }
	                    castleTalk(10+region8);
	                }
	                if(turn==4) {
	                    int potentialSignal=me.castle_talk;
	                    if(potentialSignal>=10) {
	                        int remainderSignal=potentialSignal%10;
	                        if(regionsOfCastles.size()<numberOfCastles) {
	                            regionsOfCastles.add(remainderSignal);
	                        }
	                    }
	                    String strSignalForEverybody="";
	                    for(int i=0;i<regionsOfCastles.size();i++) {
	                        strSignalForEverybody+=regionsOfCastles.get(i);
	                    }
	                    signalForEverybody=Integer.parseInt(strSignalForEverybody);
	                }
	                if(turn>=4) {
	                    signal(signalForEverybody,4);
	                }
	                
	            } else if(numberOfCastles==3) {
	                
	            }
			if (turn == 5)	
			{
				int myCastleIndex = castleRegions.indexOf(myRegion);
				int potentialSignal;
				if (myCastleIndex == 0)			
				{
					potentialSignal = castleRegions.get(1);
				}
				else
				{
					potentialSignal = castleRegions.get(0);
				}
				otherCastleTalk = me.castle_talk;
				if (otherCastleTalk > 20 && otherCastleTalk < 29)
				{
					int finalCastleRegion = otherCastleTalk-20;
					castleRegions.add(finalCastleRegion);
				}
				String strNewCastleTalk = "2" + potentialSignal;
				int newCastleTalk = Integer.parseInt(strNewCastleTalk);
				castleTalk(newCastleTalk);
			}
			if (turn == 6)
			{
				if(castleRegions.size()<numberOfCastles)
				{
					int finalCastleRegion = otherCastleTalk-20;
					castleRegions.add(finalCastleRegion);
				}
				castleRegions = intQuickSort(castleRegions, 0, castleRegions.size());
				String strRegionCode = "";
				for (int i = 0; i < castleRegions.size(); i++)
				{
					strRegionCode = castleRegions.get(i) + "";
				}
				//previously, regionCode was declared in here, but I took it out and made it an instance varibale
				regionCode = Integer.parseInt(strRegionCode);
			}
			if (turn >= 7)
			{
				signal(regionCode, 4);
			}
			
		}
		if (me.unit == SPECS.PILGRIM) { 
			
			//pilgrim
			//			log("I am a pilgrim");
			//			log("My karbonite: "+me.karbonite);
			//			log("My fuel: "+me.fuel);
			//			log("Have Castle: "+haveCastle);
			//			log("My Castle X: "+castleLocation.x);
			//			log("My Castle Y: "+castleLocation.y);
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
				return give(castleLocation.getX() - this.me.x, castleLocation.getY() - this.me.y, me.karbonite, me.fuel);
			}
			if (haveCastle && (me.karbonite==20||me.fuel==100)) {
				//				this.log("returning to castle");
				return this.pathFind(castleLocation);
			}
			else { //TODO focus more on karbo than fuel
				Point closestKarbonite = this.searchForKarboniteLocation();
				Point closestFuel = this.searchForFuelLocation();
//				this.log("karbo distance=" + findDistance(this.me, closestKarbonite.getX(), closestKarbonite.getY()));
//				this.log("fuel distance=" + findDistance(this.me, closestFuel.getX(), closestFuel.getY()));
//				this.log("pilgrim at x=" + this.me.x + " y=" + this.me.y + "\nkarbo at x=" + closestKarbonite[0] + " y=" + closestKarbonite[1] + "\nfuel at x=" + closestFuel[0] + " y=" + closestFuel[1]);
				if (this.karbonite > 20 && findDistance(this.me, closestKarbonite.getX(), closestKarbonite.getY()) > findDistance(this.me, closestFuel.getX(), closestFuel.getY())) {
//					this.log("getting fuel");
					return this.pathFind(closestFuel);
//					path = this.bfs(closestFuel);
				}
				else {
//					this.log("getting karbo");
					return this.pathFind(closestKarbonite);
//					path = this.bfs(closestKarbonite);
				}
//				if (path == null) { //you ain't going nowhere
					//nothing for now
//					this.log("no path");
//				}
//				else {
//					Point spot = this.path.poll();
//					return this.move(spot.getX() - this.me.x, spot.getY() - this.me.y);
				}
			
			}
//	}
		if (me.unit == SPECS.CRUSADER) { //crusader
			//stuff for finding enemy castles
			
			if(turn==1) {
                Robot[] visibleRobots=getVisibleRobots();
                for(int i=0;i<visibleRobots.length;i++) {
                    if(visibleRobots[i].unit==SPECS.CASTLE) {
                        if(isRadioing(visibleRobots[i])) {
                            int regionSignals=visibleRobots[i].signal;
                            while(regionSignals>0) {
                                int remainder=regionSignals%10;
                                regionsOfCastles.add(remainder);
                                regionSignals=regionSignals/10;
                            }
                            
                        }
                    }
                }
                regionsOfCastles=intQuickSort(regionsOfCastles,0,regionsOfCastles.size()-1);
            }
		
        }
			//move crusade target every so turns
			//			this.log(crusaderTarget.x + " " + crusaderTarget.y);
			if (this.enemyDestroyed()) {
				this.toggleReflection();
				this.setCrusadeTarget();
			}
			if (fuel >= 10) {
				HashSet<Robot> enemies = findBadGuys();
				if (enemies.isEmpty() && this.fuel > 100) {
					return this.pathFind(crusaderTarget);
				}
				return this.crusaderAttack(enemies);
//				log("Enemies size: "+enemies.size());
//				Robot closeBadGuy = findPrimaryEnemyTypeDistance(enemies);
//				try {
//					log("Other bad guy data " + closeBadGuy.x);
//					return attack(closeBadGuy.x - me.x,closeBadGuy.y - me.y);
//				} catch (Exception e) {
//					log("Can't attack the man");
//					try {
//						return pathFind(new Point(closeBadGuy.x, closeBadGuy.y));
//					} catch (Exception ef) {
//						log("Can't find the man");
//					}
//				}
				
			}
		
		if (me.unit==SPECS.PREACHER) {
			
			
			//preacher
		
//			if (!this.haveCastle) {
//				this.locateNearbyCastle();
//			}
//			if (this.haveCastle && this.isAdjacentToCastle()) { //get out of the way
//
//			}
//			this.log(this.me.unit + " ");
//			this.log("PREACHER=" + SPECS.PREACHER + "   PILGRIM=" + SPECS.PILGRIM);
//			Iterator<Point> iter = karboLocations.iterator();
//			Point p = iter.next();
//			Point p2 = new Point(p.getX(), p.getY());
//			this.log((p.equals(p2)&&p.hashCode()==p2.hashCode()) + "");
//			this.log(this.karboLocations.contains(p2) + " karbo");
//			this.log(this.karboLocations.contains(new Point(this.me.x, this.me.y)) + " on a karbo");
//			this.log(this.fuelLocations.contains(new Point(this.me.x, this.me.y)) + " on a fuel");
//			if (this.karboLocations.contains(new Point(this.me.x, this.me.y)) || this.fuelLocations.contains(new Point(this.me.x, this.me.y))) { //TODO: doesn't work
//				this.log("on a deposit");
//			}
			
			if (this.isAdjacentToCastle() || this.onFuel() || this.onKarbo()) { //next to castle, or on a deposit
				//TODO
				return this.preacherMovesOutOfTheWay();
			}
			if (fuel >= 15) { //optimized attack
				//				//investigating AoE --> 3x3 area. it's the attacked square and all the adjacents to that square
				//				this.log(this.me.health + " health");
				//				return this.attack(1, 1);
				HashSet<Robot> potentialEnemies = this.findBadGuys();
				if (!potentialEnemies.isEmpty()) {
					return this.preacherAttack();
				}
//				AttackAction maybeSauce = preacherAttack(potentialEnemies);
//				if (maybeSauce != null) {
//					return maybeSauce;
//				}
//				HashSet<Robot> enemies = findBadGuys();
//				Robot targetBadGuy = this.findPrimaryEnemyDistance(enemies);
//				try {
//					return attack(targetBadGuy.x-me.x,targetBadGuy.y-me.y);
//				} catch (Exception e) {
//					this.log(e.getMessage());
//				}
			}
			if (this.onFuel() || this.onKarbo()) {
				
			}
			
		}
		if (me.unit == SPECS.PROPHET) {
			
			//prophet
			//lead the way for crusaders. probably needs to use signaling near crusaders because vision isn't shared
			if (this.enemyDestroyed()) {
				this.toggleReflection();
				this.setCrusadeTarget();
			}
			HashSet<Robot> enemies = findBadGuys();
			if (enemies.isEmpty() && this.fuel > 100) {
				return this.pathFind(crusaderTarget);
			}
			Robot enemy = this.findPrimaryEnemyTypeDistance(enemies);
			if (this.canAttack(this.findDistance(this.me, enemy))) {
//				this.log("prophet attacking");
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
	public Action crusaderAttack(HashSet<Robot> potentialEnemies) {
		//Create arraylist of preachers, prophets, crusaders, castles, churches, pilgrims
		ArrayList<Robot> preachers = new ArrayList<Robot>();
		ArrayList<Robot> prophets = new ArrayList<Robot>();
		ArrayList<Robot> crusaders = new ArrayList<Robot>();
		ArrayList<Robot> castles = new ArrayList<Robot>();
		ArrayList<Robot> churches = new ArrayList<Robot>();
		ArrayList<Robot> pilgrims = new ArrayList<Robot>();
		//Iterate through potentialEnemies and do the following
		Iterator<Robot> potentialEnemiesIterator = potentialEnemies.iterator();
		while(potentialEnemiesIterator.hasNext()) {
			Robot enemy = potentialEnemiesIterator.next();
			//Check the type
			//Based on the type, it would put it in the corresponding arraylist
			if(enemy.unit==SPECS.PREACHER) {
				preachers.add(enemy);
			} else if(enemy.unit==SPECS.PROPHET) {
				prophets.add(enemy);
			} else if(enemy.unit==SPECS.CRUSADER) {
				crusaders.add(enemy);
			} else if(enemy.unit==SPECS.CASTLE) {
				castles.add(enemy);
			} else if(enemy.unit==SPECS.CHURCH) {
				churches.add(enemy);
			} else if(enemy.unit==SPECS.PILGRIM) {
				pilgrims.add(enemy);
			}
		}
		//ArrayList sorts at very end
		preachers = sortArrayListByDistance(preachers);
		prophets = sortArrayListByDistance(prophets);
		crusaders = sortArrayListByDistance(crusaders);
		castles = sortArrayListByDistance(castles);
		churches = sortArrayListByDistance(churches);
		pilgrims = sortArrayListByDistance(pilgrims);
		
		//sortArrayListByDistance(ArrayList<Robot> robots)
		//Run through preacher arraylist
		for(int i=0;i<preachers.size();i++) {
			Robot thePreacher=preachers.get(i);
			int distanceX=thePreacher.x-me.x;
			int distanceY=thePreacher.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through prophet arraylist
		for (int i=0;i<prophets.size();i++) {
			Robot theProphet=prophets.get(i);
			int distanceX=theProphet.x-me.x;
			int distanceY=theProphet.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//If attack fails, return move towards prophets
		for (int i = 0; i < prophets.size(); i++) {
			Robot theProphet=prophets.get(i);
//			int distanceX=theProphet.x-me.x;
//			int distanceY=theProphet.y-me.y;
			//pathfind here
			return this.pathFind(new Point(theProphet.x, theProphet.y));
		}
		
		//Run through crusader arraylist
		for (int i=0;i<crusaders.size();i++) {
			Robot theCrusader=crusaders.get(i);
			int distanceX=theCrusader.x-me.x;
			int distanceY=theCrusader.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through castle arraylist
		for (int i=0;i<castles.size();i++) {
			Robot theCastle=castles.get(i);
			int distanceX=theCastle.x-me.x;
			int distanceY=theCastle.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through church arraylist
		for(int i=0;i<churches.size();i++) {
			Robot theChurch=churches.get(i);
			int distanceX=theChurch.x-me.x;
			int distanceY=theChurch.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//Run through pilgrim arraylist
		for(int i=0;i<pilgrims.size();i++) {
			Robot thePilgrim=pilgrims.get(i);
			int distanceX=thePilgrim.x-me.x;
			int distanceY=thePilgrim.y-me.y;
			AttackAction potentialAttack=attack(distanceX,distanceY);
			//Keep trying to attack them
			if(potentialAttack!=null) {
				return potentialAttack;
			}
		}
		
		//If attack fails, return move towards preachers
		for(int i=0;i<preachers.size();i++) {
			Robot thePreacher=preachers.get(i);
//			int distanceX=thePreacher.x-me.x;
//			int distanceY=thePreacher.y-me.y;
			//pathfind here
			return this.pathFind(new Point(thePreacher.x, thePreacher.y));
		}
		
		//If attack fails, return move towards crusaders
		for(int i=0;i<crusaders.size();i++) {
			Robot theCrusader=crusaders.get(i);
//			int distanceX=theCrusader.x-me.x;
//			int distanceY=theCrusader.y-me.y;
			//pathfind here
			return this.pathFind(new Point(theCrusader.x, theCrusader.y));
		}
		
		//If attack fails, return move towards castles
		for(int i=0;i<castles.size();i++) {
			Robot theCastles=castles.get(i);
//			int distanceX=theCastles.x-me.x;
//			int distanceY=theCastles.y-me.y;
			//pathfind here
			return this.pathFind(new Point(theCastles.x, theCastles.y));
		}
		
		//If attack fails, return move towards churches
		for(int i=0;i<churches.size();i++) {
			Robot theChurches=churches.get(i);
//			int distanceX=theChurches.x-me.x;
//			int distanceY=theChurches.y-me.y;
			//pathfind here
			return this.pathFind(new Point(theChurches.x, theChurches.y));
		}
		
		//If attack fails, return move towards pilgrims
		for(int i=0;i<pilgrims.size();i++) {
			Robot thePilgrim=pilgrims.get(i);
//			int distanceX=thePilgrim.x-me.x;
//			int distanceY=thePilgrim.y-me.y;
			//pathfind here
			return this.pathFind(new Point(thePilgrim.x, thePilgrim.y));
		}
		return null;
	}
	
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

	//are they destroyed at the spot
	public boolean enemyDestroyed() {
		HashSet<Robot> enemies = this.findBadGuys();
		return enemies.isEmpty() && this.findDistance(me, this.crusaderTarget.getX(), this.crusaderTarget.getY()) < 5; //5 is arbitrary
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
	
	//sets where we're attacking
	public void setCrusadeTarget() {
		//		this.log(this.crusadeTurns + "");
		if (this.reflectAxis.equals("horizontal")) {
			this.crusaderTarget.setPoint(this.mapXSize - this.me.x, this.me.y);
		}
		else { //this.reflectAxis.equals("vertical")
			this.crusaderTarget.setPoint(this.me.x, this.mapYSize - this.me.y);
		}
//		this.log(this.crusaderTarget.toString());
//		else if (this.turn == 4*interval) { //up
//			//			this.log("up crusade target");
//			this.crusaderTarget.setPoint(this.mapXSize/2, this.mapYSize/4); 
//		}
//		else if (this.turn == 5*interval) { //up right
//			this.crusaderTarget.setPoint(this.mapXSize*3/4, this.mapYSize/4);
//		}
//		else if (this.turn == 6*interval) { //right
//			this.crusaderTarget.setPoint(this.mapXSize*3/4, this.mapYSize/2);
//		}
//		else if (this.turn == 7*interval) { //down right
//			this.crusaderTarget.setPoint(this.mapXSize*3/4, this.mapYSize*3/4);
//		}
//		else if (this.turn == 8*interval) { //down
//			this.crusaderTarget.setPoint(this.mapXSize/2, this.mapYSize*3/4);
//		}
//		else if (this.turn == 9*interval) { //down left
//			this.crusaderTarget.setPoint(this.mapXSize/4, this.mapYSize*3/4);
//		}
//		else if (this.turn == 10*interval) { //left
//			this.crusaderTarget.setPoint(this.mapXSize/4, this.mapYSize/2);
//		}
//		else if (this.turn == 11*interval) { //up left
//			this.crusaderTarget.setPoint(this.mapXSize/4, this.mapYSize/4);
//		}
	}
	
	//checks reflectivity of the map
	public String reflectAxis(){
		for(int col = 0; col < this.passableMap.length/2+1; col++){
			for(int row = 0; row < this.passableMap.length; row++){
				if (this.passableMap[row][col] != this.passableMap[row][this.passableMap.length-1-col]){
//					log(col + " " + row);
					return "vertical";
				}
			}
		}
		return "horizontal";
	}
	
	//signals castle it's gonna die, so castle can make a new one
	public void impendingDoom() {
		HashSet<Robot> enemies = this.findBadGuys();
		Iterator<Robot> iter = enemies.iterator();
		int totalDamage = 0;
		Robot enemy;
		while (iter.hasNext()) {
			enemy = iter.next();
			if (this.canAttack(enemy, this.findDistance(this.me, enemy))) {
				totalDamage += this.getAttackDamage(enemy.unit);
			}
		}
		if (totalDamage >= this.me.health) {
			this.castleTalk(this.me.unit);
		}
	}

	//Can this pilgrim give stuff to the castle
	public boolean canGiveStuff() {
		int absoluteXCastleDistance = Math.abs(castleLocation.getX() - this.me.x);
		int absoluteYCastleDistance = Math.abs(castleLocation.getY() - this.me.y);
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
				castleLocation.setPoint(goodGuy.x, goodGuy.y);
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
	
	//breadth first search pathing TODO finish this up
	public Queue<Point> bfs(Point finalLocation) {
		int speed = this.getMovementRangeRadius(this.me.unit); //movement speed
		BFSPoint start = new BFSPoint(this.me.x, this.me.y); //starting point
		LinkedList<BFSPoint> visited = new LinkedList<BFSPoint>(); //visited points
		LinkedList<BFSPoint> toVisit = new LinkedList<BFSPoint>(); //points to visit
		toVisit.add(start);
		start.parent = null;
		
		BFSPoint point;
		BFSPoint potentialNeighbor;
		Iterator<BFSPoint> iter;
		BFSPoint neighbor;
		while (!toVisit.isEmpty()) {
			point = toVisit.poll(); //gets and removes first element to visit
			
			if (point.equals(finalLocation)) { //found the final location
				this.log("found the location!");
				Queue<Point> path = new LinkedList<Point>();
				while (point.parent != null) { //retraces path
					path.add(point);
					point.parent = point;
				}
				return path;
			}
			else { //didn't find the final location
				visited.add(point); //add to visited points
				
				//gets neighbors
				for (int i = -1*speed; i <= speed; i++) {
					for (int j = -1*speed; j <= speed; j++) {
						potentialNeighbor = new BFSPoint(point.getX() + i, point.getY() + j);
						if (!(i==0&&j==0) && Math.sqrt(this.findDistance(point, potentialNeighbor)) <= speed && this.passableMap[potentialNeighbor.getY()][potentialNeighbor.getX()] && this.visibleRobotMap[potentialNeighbor.getY()][potentialNeighbor.getX()] <= 0) {
//							this.log(finalLocation + "   " + potentialNeighbor);
							point.neighbors.add(potentialNeighbor);
						}
					}
				}
				
				//add neighboring points to be visited
//				this.log("num neighbors " + point.neighbors.size());
				iter = point.neighbors.iterator();
				while (iter.hasNext()) {
//					this.log(neighbor + "");
//					this.log("currently at " + point + "    maybe visit " + neighbor + "    goal is " + finalLocation);
//					this.log(!visited.contains(neighbor) + " visited contains " + neighbor);
//					this.log(!toVisit.contains(neighbor) + " toVisit contains " + neighbor);
					neighbor = iter.next();
					if (!visited.contains(neighbor) && !toVisit.contains(neighbor)) { //if neighbor is neither visit, nor to be visited
						this.log("robot at " + new Point(this.me.x, this.me.y) + "    currently check " + point + "    going to visit " + neighbor + "    goal is " + finalLocation);
						neighbor.parent = point; //the neighbor's parent is the point we visited
						toVisit.add(neighbor); //we're going to visit neighbor
					}
				}
//				this.log("num visiting " + toVisit.size());
			}
		}
		//no path
		return null;
	}

	//old path finding algorithm for moving
	public MoveAction pathFind(Point finalLocation) {
		//		this.log("moving toward x=" + finalLocation[0] + " y=" + finalLocation[1]);
		if (fuel <= 30 || finalLocation.getX()==-1) { //not enough fuel, or -1 b/c can't find karbo or fuel
			//			this.log("cannot move");
			return null;
		}
		int xDistance = finalLocation.getX() - me.x;
		int yDistance = finalLocation.getY() - me.y;
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
		return Math.abs(this.me.x-this.castleLocation.getX())==1 && Math.abs(this.me.y-this.castleLocation.getY())==1;
	}

	//TODO implement with the adjacents[] points?
	//checks if adjacent tiles are available. used for making units. checks tiles closer to the middle of the map first. //TODO build pilgrims on deposits, and other units not on deposits. if possible
	public Point checkAdjacentAvailable() {
		int x = this.me.x;
		int y = this.me.y;
		int dx = x - this.mapXSize/2;
		int dy = y - this.mapYSize/2;
		if (dx > 0) { //robot is to the east of center, check left first, then up down, then right
			if (x > 0) { //can check left
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new Point(x-1, y-1);
						}
					}
					if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
						return new Point(x-1, y);
					}
					if (y < mapYSize - 1) { //can check down
						if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
							return new Point(x-1, y+1);
						}
					}
				}
				else { //robot is to the north or level of center, check down then middle then up
					if (y > 0) { //can check up
						if (y < mapYSize - 1) { //can check down
							if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
								return new Point(x-1, y+1);
							}
						}
						if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
							return new Point(x-1, y);
						}
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new Point(x-1, y-1);
						}
					}
				}
			}
			if (dy > 0) { //robot is to the south of center, check up then down
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new Point(x, y-1);
					}
				}
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new Point(x, y+1);
					}
				}
			}
			else { //robot is to the north or level of center, check down then up
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new Point(x, y+1);
					}
				}
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new Point(x, y-1);
					}
				}
			}
			if (x < mapXSize - 1) { //can check right
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new Point(x+1, y-1);
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new Point(x+1, y);
					}
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new Point(x+1, y+1);
						}
					}
				}
				else { //robot is north or level of center, check down then middle then up
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new Point(x+1, y+1);
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new Point(x+1, y);
					}
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new Point(x+1, y-1);
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
							return new Point(x+1, y-1);
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new Point(x+1, y);
					}
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new Point(x+1, y+1);
						}
					}
				}
				else { //robot is north or level of center, check down then middle then up
					if (y < mapYSize) { //can check down
						if (this.passableMap[y+1][x+1] && visibleRobotMap[y+1][x+1]==0) { //checks down right
							return new Point(x+1, y+1);
						}
					}
					if (this.passableMap[y][x+1] && visibleRobotMap[y][x+1]==0) { //checks middle right
						return new Point(x+1, y);
					}
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x+1] && visibleRobotMap[y-1][x+1]==0) { //checks up right
							return new Point(x+1, y-1);
						}
					}
				}
			}
			if (dy > 0) { //robot is to the south of center, check up then down
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new Point(x, y-1);
					}
				}
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new Point(x, y+1);
					}
				}
			}
			else { //robot is to the north or level of center, check down then up
				if (y < mapYSize - 1) { //can check down
					if (this.passableMap[y+1][x] && visibleRobotMap[y+1][x]==0) { //checks middle down
						return new Point(x, y+1);
					}
				}
				if (y > 0) { //can check up
					if (this.passableMap[y-1][x] && visibleRobotMap[y-1][x]==0) { //checks middle up
						return new Point(x, y-1);
					}
				}
			}
			if (x > 0) { //can check left
				if (dy > 0) { //robot is to the south of center, check up then middle then down
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new Point(x-1, y-1);
						}
					}
					if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
						return new Point(x-1, y);
					}
					if (y < mapYSize - 1) { //can check down
						if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
							return new Point(x-1, y+1);
						}
					}
				}
				else { //robot is north or level of center, check down then middle then up
					if (y < mapYSize - 1) { //can check down
						if (this.passableMap[y+1][x-1] && visibleRobotMap[y+1][x-1]==0) { //checks down left
							return new Point(x-1, y+1);
						}
					}
					if (this.passableMap[y][x-1] && visibleRobotMap[y][x-1]==0) { //checks middle left
						return new Point(x-1, y);
					}
					if (y > 0) { //can check up
						if (this.passableMap[y-1][x-1] && visibleRobotMap[y-1][x-1]==0) { //checks up left
							return new Point(x-1, y-1);
						}
					}
				}
			}
		}
		return null; //surrounded by impassable terrain
	}

	//returns a HashSet of all the karbo location coordinates
	public HashSet<Point> getKarboniteLocations() {
		HashSet<Point> karboniteLocations = new HashSet<Point>();
		for (int i = 0; i < this.mapYSize; i++) {
			for (int j = 0; j < this.mapXSize; j++) {
				if (this.karboniteMap[i][j]) {
					//					this.log("adding karbo deposit location");
					karboniteLocations.add(new Point(j, i));
				}
			}
		}
		return karboniteLocations;
	}

	//returns a HashSet of all the fuel location coordinates
	public HashSet<Point> getFuelLocations() {
		HashSet<Point> fuelLocations = new HashSet<Point>();
		for (int i = 0; i < this.mapYSize; i++) {
			for (int j = 0; j < this.mapXSize; j++) {
				if (this.fuelMap[i][j]) {
					fuelLocations.add(new Point(j, i));
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
		Point p = new Point(this.me.x, this.me.y);
		Iterator<Point> iter = this.karboLocations.iterator();
		Point location;
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
			Point p = new Point(this.me.x, this.me.y);
			Iterator<Point> iter = this.fuelLocations.iterator();
			Point location;
			while (iter.hasNext()) {
				location = iter.next();
				if (location.equals(p)) {
					return true;
				}
			}
			return false;
		}

	//searches for the closest karbonite
	public Point searchForKarboniteLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		double distance;
		Iterator<Point> iter = this.karboLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			distance = findDistance(this.me, location.getX(), location.getY());
			if (this.visibleRobotMap[location.getY()][location.getX()]<=0 && (location.getX()!=this.me.x&&location.getY()!=this.me.y) && distance < minDistance) {
				minDistance = distance;
				minXCoordinate = location.getX();
				minYCoordinate = location.getY();
			}
		}
		//		for (int i = 0; i < mapYSize; i++) {
		//			for (int j = 0; j < mapXSize; j++) {
		//				if (karboniteMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
		//					//this.log("i am here x=" + this.me.x + " y=" + this.me.y + "  could mine here x=" + j + " y=" + i);
		//					distance = findDistance(me, j, i);
		//					if (distance < minDistance) {
		//						minDistance = distance;
		//						minXCoordinate = j;
		//						minYCoordinate = i;
		//					}
		//				}
		//			}
		//		}
		return new Point(minXCoordinate, minYCoordinate);
	}

	//searches for the closest fuel
	public Point searchForFuelLocation() {
		double minDistance = Double.MAX_VALUE;
		int minXCoordinate = -1;
		int minYCoordinate = -1;
		double distance;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			distance = findDistance(this.me, location.getX(), location.getY());
			if (this.visibleRobotMap[location.getY()][location.getX()]<=0 && (location.getX()!=this.me.x&&location.getY()!=this.me.y) && distance < minDistance) {
				minDistance = distance;
				minXCoordinate = location.getX();
				minYCoordinate = location.getY();
			}
		}
		//		for (int i = 0; i < mapYSize; i++) {
		//			for (int j = 0; j < mapXSize; j++) {
		//				if (fuelMap[i][j] && visibleRobotMap[i][j]<=0 && (i!=this.me.y&&j!=this.me.x)) {
		//					distance = findDistance(me, j, i);
		//					if (distance < minDistance) {
		//						minDistance = distance;
		//						minXCoordinate = j;
		//						minYCoordinate = i;
		//					}
		//				}
		//			}
		//		}
		return new Point(minXCoordinate, minYCoordinate);
	}

	//builds a unit where available
	public Action makeUnit(int type) {
		Point spot = this.checkAdjacentAvailable(); //TODO build pilgrims on deposits if possible, otherwise don't build on deposits if possible
		//this.log("x=" + spot[0] + " y=" + spot[1]);
		return this.buildUnit(type, spot.getX() - this.me.x, spot.getY() - this.me.y);
	}

	//should this castle make pilgrims (based on number of nearby deposits)
	public boolean makeMorePilgrims() {
		return this.bots.get("pilgrims") < (this.closeFuelNum + this.closeKarboNum + 2)/2;
	}

	//should this castle make more pilgrims to sustain the crusade? TODO: finish this
	public boolean makeEvenMorePilgrims() {
		//pilgrims mine 10 fuel per turn, so takes 10 turns to get to 100 fuel full capacity
		//pilgrims can move 2 tiles per turn, so in 10 turns, pilgrims can move 20 tiles, thus max efficiency means a round trip of 20 tiles?
		return this.bots.get("pilgrims") < (this.closeFuelNum + this.closeKarboNum + 2)/2 + this.farFuelNum;
	}

	//finds the number of far away fuel deposits (fuel >= x tiles away from castle)
	public int findFarFuelDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) >= x ? 1 : 0;
		}
		return num;
	}

	//finds the number of close fuel deposits (fuel <= x tiles away from castle)
	public int findCloseFuelDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) <= x ? 1 : 0;
		}
		return num;
	}

	//finds the number of far away karbo deposits (karbo >= x tiles away from castle)
	public int findFarKarboDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) >= x ? 1 : 0;
		}
		return num;
	}

	//finds the number of close karbo deposits (karbo <= x tiles away from castle)
	public int findCloseKarboDepositNum(int x) {
		int num = 0;
		Iterator<Point> iter = this.fuelLocations.iterator();
		Point location;
		while (iter.hasNext()) {
			location = iter.next();
			num += this.findDistance(this.me, location.getX(), location.getY()) <= x ? 1 : 0;
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

	// Finds distance squared between two points
	public double findDistance(Point one, Point two) {
		int xDistance = one.getX() - two.getX();
		int yDistance = one.getY() - two.getY();
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}
	
	//Pilgrims run away when they see dangerous enemies TODO: fix this
	public Action pilgrimRunAway() {
		HashSet<Robot> nearbyEnemies = this.findBadGuys();
		Robot closestEnemy = this.findClosestThreat(nearbyEnemies);
		return this.move(this.me.x - closestEnemy.x, this.me.y - closestEnemy.y); //replace with our move/pathing method later
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
			//			log("Distance: "+badGuyDistance);
			if(badGuyDistance < distance) {
				//				log("Found closer robot");
				distance = badGuyDistance;
				//				log("New closest distance: "+distance);
				closeBot = aBadGuy;
			}

		}
		//		if(closeBot==null) {
		//			log("Still null boo");
		//		} else {
		//			log("You the man");
		//		}
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
//		this.log(maxAttackRange + "");
		int x = -1, y = -1, maxValue = 0;
		int[][] robotMap = new int[this.mapYSize][this.mapXSize];
		for (int[] row : robotMap) {
			Arrays.fill(row, 0);
		}
		for (Robot robot : robots) {
			robotMap[robot.y][robot.x] = this.getRobotValue(robot);
//			this.log(robotMap[robot.y][robot.x] + "");
		}
//		this.logMap(robotMap);
//		int[][] weightedMap = new int[this.mapYSize][this.mapXSize]; //new int[this.getVisionRangeRadius(this.me.unit)*2+1][this.getVisionRangeRadius(this.me.unit)*2+1]; //TODO: size of vision radius. need to shift if I want to make this smaller matrix
//		this.log((this.me.y - maxAttackRange) + " " + this.me.y + " " + (this.me.y + maxAttackRange));
		int value;
		for (int r = Math.max(this.me.y - maxAttackRange, 1); r < Math.min(this.me.y + maxAttackRange + 1, this.mapYSize-1); r++) {
			for (int c = Math.max(this.me.x - maxAttackRange, 1); c < Math.min(this.me.x + maxAttackRange + 1, this.mapXSize-1); c++) {
				//weightedMap[r][c] = this.averageAdjacent(robotMap, r, c);
				if (this.canAttack(Math.sqrt(this.findDistance(this.me, c, r)))) {
//					this.log("can attack");
					value = this.sumAdjacent(robotMap, r, c);
//					this.log(value + " x=" + c + " y=" + r + "   turn =" + this.turn);
					if (maxValue < value) {
						maxValue = value;
						x = c;
						y = r;
					}
				}
			}
		}
//		this.log("c=" + x + " r=" + y);
//		this.log("x=" + (x-this.me.x) + " y=" + (y-this.me.y));
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

//	//attacks as far as possible to still hit the enemy
//	public AttackAction preacherAttack(HashSet<Robot> potentialEnemies) {
//		Robot targetBadGuy = this.findPrimaryEnemyDistance(potentialEnemies);
//		while(targetBadGuy == null && potentialEnemies.size() > 0) {
//			potentialEnemies.remove(targetBadGuy);
//			targetBadGuy = findPrimaryEnemyDistance(potentialEnemies);
//		}
//		if(potentialEnemies.size() == 0) {
//			return null;
//		}
//		int xDistance = targetBadGuy.x - me.x;
//		int yDistance = targetBadGuy.y - me.y;
//		double absoluteXDistance = Math.abs(xDistance);
//		double absoluteYDistance = Math.abs(yDistance);
//		double radianAngle;
//		double piHalf = Math.PI / 2;
//		double piEight = Math.PI / 8;
//		double piThreeEight = piEight * 3;
//		String optimalDirection = "";
//		if (xDistance >= 0 && yDistance <= 0) {
//			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
//			if (radianAngle >= 0 && radianAngle <= piEight) {
//				optimalDirection = "EAST";
//			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
//				optimalDirection = "NORTH";
//			} else {
//				optimalDirection = "NORTHEAST";
//			}
//		} else if (xDistance <= 0 && yDistance <= 0) {
//			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
//			if (radianAngle >= 0 && radianAngle <= piEight) {
//				optimalDirection = "WEST";
//			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
//				optimalDirection = "NORTH";
//			} else {
//				optimalDirection = "NORTHWEST";
//			}
//		} else if (xDistance <= 0 && yDistance >= 0) {
//			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
//			if (radianAngle >= 0 && radianAngle <= piEight) {
//				optimalDirection = "WEST";
//			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
//				optimalDirection = "SOUTH";
//			} else {
//				optimalDirection = "SOUTHWEST";
//			}
//		} else if (xDistance >= 0 && yDistance >= 0) {
//			radianAngle = Math.atan(absoluteYDistance / absoluteXDistance);
//			if (radianAngle >= 0 && radianAngle <= piEight) {
//				optimalDirection = "EAST";
//			} else if (radianAngle >= piThreeEight && radianAngle <= piHalf) {
//				optimalDirection = "SOUTH";
//			} else {
//				optimalDirection = "SOUTHEAST";
//			}
//		}
//		//		int xDistance=targetBadGuy.x-me.x;
//		//		int yDistance=targetBadGuy.y-me.y;
//		AttackAction possibleAction;
//		if(optimalDirection=="NORTH") {
//			try {
//				possibleAction=attack(0, -4);
//				if(possibleAction != null) {
//					return attack(0,-4);
//				}
//			} catch (Exception e) {
//
//			}
//		} else if(optimalDirection=="NORTHEAST") {
//			try {
//				possibleAction=attack(2, -3);
//				if(possibleAction != null) {
//					return attack(2,-3);
//				}
//			} catch (Exception e) {
//
//			}
//
//		} else if(optimalDirection=="EAST") {
//			try {
//				possibleAction=attack(4, 0);
//				if(possibleAction != null) {
//					return attack(4,0);
//				}
//			} catch (Exception e) {
//
//			}
//		} else if(optimalDirection=="SOUTHEAST") {
//			try {
//				possibleAction=attack(3, 2);
//				if(possibleAction != null) {
//					return attack(3,2);
//				}
//			} catch (Exception e) {
//
//			}
//		} else if(optimalDirection=="SOUTH") {
//			try {
//				possibleAction=attack(0, 4);
//				if(possibleAction != null) {
//					return attack(0,4);
//				}
//			} catch (Exception e) {
//
//			}
//		} else if(optimalDirection=="SOUTHWEST") {
//			try {
//				possibleAction=attack(-2, 3);
//				if(possibleAction != null) {
//					return attack(-2, 3);
//				}
//			} catch (Exception e) {
//
//			}
//		} else if(optimalDirection=="WEST") {
//			try {
//				possibleAction=attack(-4, 0);
//				if(possibleAction != null) {
//					return attack(-4,0);
//				}
//			} catch (Exception e) {
//
//			}
//		} else if(optimalDirection=="NORTHWEST") {
//			try {
//				possibleAction=attack(-3, -2);
//				if(possibleAction != null) {
//					return attack(-3,-2);
//				}
//			} catch (Exception e) {
//
//			}
//		}
//		return null;
//	}

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
	 public int findMyRegion8X(int region16) {
	        if(region16==1||region16==13) {
	            return 1;
	        }
	        if(region16==2||region16==14) {
	            return 2;
	        }
	        if(region16==3||region16==15) {
	            return 3;
	        }
	        if(region16==4||region16==16) {
	            return 4;
	        }
	        if(region16==5||region16==9) {
	            return 5;
	        }
	        if(region16==6||region16==10) {
	            return 6;
	        }
	        if(region16==7||region16==11) {
	            return 7;
	        }
	        if(region16==8||region16==12) {
	            return 8;
	        }
	        return 0;
	    }
	 public int findMyRegion8Y(int region16) {
	        if(region16==1||region16==4) {
	            return 1;
	        }
	        if(region16==2||region16==3) {
	            return 2;
	        }
	        if(region16==5||region16==8) {
	            return 3;
	        }
	        if(region16==6||region16==7) {
	            return 4;
	        }
	        if(region16==9||region16==12) {
	            return 5;
	        }
	        if(region16==10||region16==11) {
	            return 6;
	        }
	        if(region16==13||region16==16) {
	            return 7;
	        }
	        if(region16==14||region16==15) {
	            return 8;
	        }
	        return 0;
	    }
	 public int findMyRegion16() {
	        if(me.x<=mapXFirstQuarter) {
	            if(me.y<=mapYFirstQuarter) {
	                return 1;
	            } else if(me.y<=mapYSecondQuarter) {
	                return 5;
	            } else if(me.y<=mapYThirdQuarter) {
	                return 9;
	            } else if(me.y<=mapYLength) {
	                return 13;
	            }
	        } else if(me.x<=mapXSecondQuarter) {
	            if(me.y<=mapYFirstQuarter) {
	                return 2;
	            } else if(me.y<=mapYSecondQuarter) {
	                return 6;
	            } else if(me.y<=mapYThirdQuarter) {
	                return 10;
	            } else if(me.y<=mapYLength) {
	                return 14;
	            }
	        } else if(me.x<=mapXThirdQuarter) {
	            if(me.y<=mapYFirstQuarter) {
	                return 3;
	            } else if(me.y<=mapYSecondQuarter) {
	                return 7;
	            } else if(me.y<=mapYThirdQuarter) {
	                return 11;
	            } else if(me.y<=mapYLength) {
	                return 15;
	            }
	        } else if(me.x<=mapXLength) {
	            if(me.y<=mapYFirstQuarter) {
	                return 4;
	            } else if(me.y<=mapYSecondQuarter) {
	                return 8;
	            } else if(me.y<=mapYThirdQuarter) {
	                return 12;
	            } else if(me.y<=mapYLength) {
	                return 16;
	            }
	        }
	        return 0;
	    }
	 public ArrayList<Point> getEnemyCastlePoints(ArrayList<Integer> regions) {
	        ArrayList<Point> enemyCastlePoints=new ArrayList<Point>();
	        for(int i=0;i<regions.size();i++) {
	            if(flippedAcrossX) {
	                if(me.y>=mapYSecondQuarter) {
	                    if(regions.get(i)==1) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==2) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==3) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==4) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==5) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==6) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==7) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==8) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYThirdEighth));
	                    }
	                } else {
	                    if(regions.get(i)==1) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYSeventhEighth));
	                    } else if(regions.get(i)==2) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYSeventhEighth));
	                    } else if(regions.get(i)==3) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYSeventhEighth));
	                    } else if(regions.get(i)==4) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYSeventhEighth));
	                    } else if(regions.get(i)==5) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==6) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==7) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==8) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYFifthEighth));
	                    }
	                }
	            } else {
	                if(me.x>=mapXSecondQuarter) {
	                    if(regions.get(i)==1) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==2) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==3) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==4) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==5) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==6) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==7) {
	                        enemyCastlePoints.add(new Point(mapXFirstEighth,mapYSeventhEighth));
	                    } else if(regions.get(i)==8) {
	                        enemyCastlePoints.add(new Point(mapXThirdEighth,mapYSeventhEighth));
	                    }
	                } else {
	                    if(regions.get(i)==1) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==2) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYFirstEighth));
	                    } else if(regions.get(i)==3) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==4) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYThirdEighth));
	                    } else if(regions.get(i)==5) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==6) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYFifthEighth));
	                    } else if(regions.get(i)==7) {
	                        enemyCastlePoints.add(new Point(mapXSeventhEighth,mapYSeventhEighth));
	                    } else if(regions.get(i)==8) {
	                        enemyCastlePoints.add(new Point(mapXFifthEighth,mapYSeventhEighth));
	                    }
	                }
	            }
	        }
	        return null;
	    }
	 
	 
	 public ArrayList<Integer> intQuickSort(ArrayList<Integer> a, int start, int end) {
	        if (start<end) { 
	            // general case 
	            int pivot = intPartition(a, start, end);
	              // sort left sublist
	              intQuickSort(a,start,pivot-1);
	              // sort the right sublist
	              intQuickSort(a,pivot+1,end);
	        }
	        return a;
	    }
	    
	    
	    public int intPartition(ArrayList<Integer> a, int start, int end) {
	        int pivot;
	        int endOfLeft;
	        int midIndex = (start+end)/2;
	        intSwap(a,start,midIndex);
	        pivot=a.get(start);
	        endOfLeft=start;
	        for (int i=start+1; i<=end; i++) {
	              if (a.get(i)<pivot) {
	                   endOfLeft=endOfLeft+1;
	                   intSwap(a,endOfLeft,i);
	              }
	        }
	        intSwap(a,start,endOfLeft); 
	        return endOfLeft;
	    }
	    
	    
	    public void intSwap(ArrayList<Integer> a, int i, int j) {
	        int tmp = a.get(i);
	            a.set(i, a.get(j));
	            a.set(j,tmp);

	    }
	    
	}
*/

/*
 
 /*
package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

public class MyRobot extends BCAbstractRobot {
	public class Point {
		public int x;
		public int y;

		public Point() {
			
		}
		
		public Point(int x, int y) {
			setPoint(x, y);
		}

		public void setPoint(int x, int y) {
			this.x = x;
			this.y = y;
		}

		public int getX() {
			return this.x;
		}

		public int getY() {
			return this.y;
		}
		
		public boolean equals(Point p) {
			return this.getX()==p.getX() && this.getY()==p.getY();
		}
		
		@Override
		public String toString() {
			return "(" + this.getX() + ", " + this.getY() + ")";
		}
		
//		@Override
//		public boolean equals(Object obj) {
//			if (obj instanceof Point) {
//				Point p = (Point)obj;
//				return this.getX() == p.getX() && this.getY() == p.getY();	
//			}
//			return false;
//		}
//		
//		@Override
//		public int hashCode() {
//			return this.getX() + this.getY();
//		}
	}
	public int turn;
	public ArrayList<ArrayList<int[]>> clumpList;
	//int[] location is done by y then x
	public final int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public int mapYSize, mapXSize; //size of the map, length y and length x
	public String reflectAxis;
	public boolean didToggle;
	public HashSet<Point> karboLocations;
	public HashSet<Point> fuelLocations;
	public int karboDepositNum;
	public int fuelDepositNum;
	public int closeKarboNum, farKarboNum;
	public int closeFuelNum, farFuelNum;
	public final int CLOSE = 5, FAR = 10;
	public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
	public Point[] adjacents = new Point[] {new Point(1,0), new Point(1,1), new Point(0,1), new Point(-1,1), new Point(-1,0), new Point(-1,-1), new Point(0,-1), new Point(1,-1)};
	public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
	public boolean haveCastle = false;
	public Point castleLocation = new Point(); //location of castle
	public Point crusaderTarget = new Point(); //location of crusader target
	public HashMap<String, Integer> bots = new HashMap<String, Integer>(); //castles know what bots they have created
	public Queue<Point> path = new LinkedList<Point>();
	public int numberOfCastles;
	public ArrayList<Integer> castleRegions;
	public int myRegion;
	public int region16;
    public int region8;
    public boolean[][] theMap;
    public int mapXLength;
    public int mapYLength;
    public int mapXFirstEighth;
    public int mapXFirstQuarter;
    public int mapXThirdEighth;
    public int mapXSecondQuarter;
    public int mapXFifthEighth;
    public int mapXThirdQuarter;
    public int mapXSeventhEighth;
    public int mapYFirstEighth;
    public int mapYFirstQuarter;
    public int mapYThirdEighth;
    public int mapYSecondQuarter;
    public int mapYFifthEighth;
    public int mapYThirdQuarter;
    public int mapYSeventhEighth;
    //Determined by Tommy's method
    public boolean flippedAcrossX;
    public ArrayList<Integer> regionsOfCastles; 
    //public ArrayList<Point> pointsOfCastles;
    public int signalForEverybody=-1;
    public int regionCode;
    public int otherCastleTalk;
    
    

	public Action turn() {
		turn++;
		if(turn==1) {
			clumpList=new ArrayList<ArrayList<int[]>>();
		}
		log(""+findClump());
		log("My X location: "+me.x);
		log("My Y location: "+me.y);
		visibleRobotMap = this.getVisibleRobotMap(); //get visible robots every turn
		if (me.unit != SPECS.CASTLE) { //dead??
			//this.impendingDoom();
			
		}
		if(me.unit == SPECS.CASTLE)
		{
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
			
		/*	
			//finding enemy castle stuff
			 if (turn == 1) {
	                int summingUpCastles=me.castle_talk+1;
	                castleTalk(summingUpCastles);
	            }
	            if(turn==2) {
	                numberOfCastles=me.castle_talk;
	                log("number of castles:" + numberOfCastles);
	            }
			
		}
		

		return null;

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
	
	// Parse through karbonite map and fuel map and put locations in the same
	// Make ArrayList of locations sorted with everything by distance to the castle
	
	//Make ArrayList of clumps (which is an arraylist of arrays) technically
	
	//Repeat the following until the HashMap size is 0
	
		//Find the closest thing
	
		//Put that in clump
	
		//Go through ArrayList, and check if it was within 8 r^2 of any previous approved clump
	
		//Once done with making a full clump, remove locations from the initial sorted ArrayList
		public ArrayList<int[]> findClump() {
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
			quickSort(sortedResources,0,sortedResources.size());//causing prop 1 of undef error
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
		
		public double findDistance(int[] location) {
			int xDistance = location[1] - me.x;
			int yDistance = location[0] - me.y;
			return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}
		
		//Finds closest enemy robot
		public Robot findPrimaryEnemyDistance(HashSet<Robot> potentialEnemies) {
			double distance = Double.MAX_VALUE;
			Robot closeBot = null;
			Iterator<Robot> badGuyIter = potentialEnemies.iterator();
			while(badGuyIter.hasNext()) {
				Robot aBadGuy = badGuyIter.next();
				double badGuyDistance = findDistance(me, aBadGuy);
				//			log("Distance: "+badGuyDistance);
				if(badGuyDistance < distance) {
					//				log("Found closer robot");
					distance = badGuyDistance;
					//				log("New closest distance: "+distance);
					closeBot = aBadGuy;
				}

			}
			//		if(closeBot==null) {
			//			log("Still null boo");
			//		} else {
			//			log("You the man");
			//		}
			return closeBot;
		}
		
		// Finds distance squared between two robots
		public double findDistance(Robot me, Robot opponent) {
			int xDistance = opponent.x - me.x;
			int yDistance = opponent.y - me.y;
			return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
		}
		
		
		
	}
	
	*/
package bc19;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public ArrayList<ArrayList<int[]>> clumpList;
	// int[] location is done by y then x

	public Action turn() {
		turn++;
		if (turn == 1) {
			clumpList = new ArrayList<ArrayList<int[]>>();
		}
		log("My X location: " + me.x);
		log("My Y location: " + me.y);
		ArrayList<int[]> sortedResources = findSortedResources();
		log("Sorted Resources: " + sortedResources);
		displayAllClumps(findAllClumps(sortedResources));
		return null;

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

	public double findDistance(int[] location) {
		int xDistance = location[1] - me.x;
		int yDistance = location[0] - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}
	
	public double findDistance(int[] location1,int[] location2) {
		int xDistance = location2[1] - location1[1];
		int yDistance = location2[0] - location1[0];
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}
}
