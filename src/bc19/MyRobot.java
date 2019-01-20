package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public final int[] rotationTries = { 0, -1, 1, -2, 2, -3, 3 };
	public LinkedList<Point> path = new LinkedList<Point>();
	public boolean[][] passableMap;
	public int[][] visibleRobotMap;
	public boolean[][] karboniteMap;
	public boolean[][] fuelMap;
	public int mapYSize, mapXSize; //size of the map, length y and length x
	public HashSet<Point> karboLocations;
	public HashSet<Point> fuelLocations;
	public int karboDepositNum;
	public int fuelDepositNum;
	public int closeKarboNum, farKarboNum;
	public int closeFuelNum, farFuelNum;
	public final int CLOSE = 5, FAR = 10;
	public ArrayList<String> directions = new ArrayList<String>(Arrays.asList("NORTH", "NORTHEAST", "EAST", "SOUTHEAST", "SOUTH", "SOUTHWEST", "WEST", "NORTHWEST"));
	public ArrayList<Integer> previousLocations = new ArrayList<Integer>();
	public boolean haveCastle = false;
	public Point castleLocation = new Point(); //location of castle
	public Point crusaderTarget = new Point(); //location of crusader target
	public HashMap<String, Integer> bots = new HashMap<String, Integer>(); //castles know what bots they have created
	public boolean crusadeMode = false; //are we in full attack mode

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
            if((this.getX()==p.getX())&&(this.getY()==p.getY())) {
                return true;
            }
            return false;
        }
    }
    
    public class BFSPoint extends Point{
        public boolean visited,passable;
        public BFSPoint lastPoint;
        
        public BFSPoint() {
            super();
        }
        
        public BFSPoint (int x, int y, BFSPoint last) {
            super(x,y);
            setPassable();
            this.lastPoint = last;
        }
        
        public void setPassable() {
            int[][] visibleBots = getVisibleRobotMap();
            boolean[][] pmap = getPassableMap();
            passable = pmap[super.getY()][super.getX()]&&(visibleBots[super.getY()][super.getX()]<=0);
            //doesnt know if tile out of sight is occupied, so it assumes its passable
        }
        
        public boolean isFinalLoc(int[] targetLoc){
            visited = true;
            if(targetLoc[0]==super.getX()&&targetLoc[1]==super.getY()) {
                return true;
            }
        }
        
        public BFSPoint last() {
            return lastPoint;
        }
        
        
    }
	//finds axis of reflection
	public String reflectAxis(){
        for(int col=0;col<map.length/2+1;col++){
            for(int row = 0;row<map.length;row++){
                if(map[row][col]!=map[row][map.length-1-col]){
                    log(col+" "+row);
                    return "reflected across x-axis";
                }
            }
        }
        return "reflected across y-axis";
    }

	public Action turn() {
		turn++;
		passableMap = getPassableMap();

		

		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				return buildUnit(SPECS.PILGRIM,1,0);
			}
			
			
		}
		
		if (me.unit == SPECS.CHURCH) {
			
		}

		if (me.unit == SPECS.PILGRIM) {
			if (turn ==1) {
			}
			
			if(canMineKarbonite()||canMineFuel()) {
				return mine();
			}


			if(!fuelMap[me.y][me.x]&&!karboniteMap[me.y][me.x]){
				//methods return null if karb or fuel cannot be reached, or coords of reachable karb/fuel
				int[] karboniteLocationFind=searchForKarboniteLocation();
				int[] fuelLocationFind=searchForFuelLocation();
				//moves to mineable spot
				if(!karboniteLocationFind.equals(null)&&me.karbonite<20){
					return move (karboniteLocationFind[1]-me.x,karboniteLocationFind[0]-me.y);

				}
				if(!fuelLocationFind.equals(null)&&me.fuel<100){
					return move (fuelLocationFind[1]-me.x,fuelLocationFind[0]-me.y);
				}
			}

			int[][]visiMap = getVisibleRobotMap();

			//stuff for testing
			if(me.x+1<visiMap.length&&visiMap[me.y][me.x+1]==0&&passableMap[me.y][me.x+1]&&me.x+1<passableMap[0].length){
				if(!lastMove.equals("right")){
					lastMove = "right";
					return move(1,0);
				}
			}

			if(me.y+1<visiMap.length&&visiMap[me.y+1][me.x]==0&&me.y+1<passableMap.length&&passableMap[me.y+1][me.x]){
				if(!lastMove.equals("down")){
					lastMove = "down";
					return move(0,1);
				}
			}

			if(me.x-1>=0&&visiMap[me.y][me.x-1]==0&&me.x-1>=0&&passableMap[me.y][me.x-1]){
				if(!lastMove.equals("left")){
					lastMove = "left";
					return move(-1,0);
				}
			}

			if(me.y-1>=0&&visiMap[me.y-1][me.x]==0&&me.y-1>=0&&passableMap[me.y-1][me.x]){
				if(!lastMove.equals("up")){
					lastMove = "up";
					return move(0,-1);
				}
			}
			boolean t = true;

			if(me.x+1<visiMap.length&&visiMap[me.y][me.x+1]==0&&me.x+1<passableMap[0].length&&passableMap[me.y][me.x+1]){
				if(t){
					lastMove = "right";
					
					return move(1,0);
				}
			}

			if(me.y+1<visiMap.length&&visiMap[me.y+1][me.x]==0&&me.y+1<passableMap.length&&passableMap[me.y+1][me.x]){
				if(t){
					lastMove = "down";
					return move(0,1);
				}
			}

			if(me.x-1>=0&&visiMap[me.y][me.x-1]==0&&me.x-1>=0&&passableMap[me.y][me.x-1]){
				if(t){
					lastMove = "left";
					return move(-1,0);
				}
			}

			if(me.y-1>=0&&visiMap[me.y-1][me.x]==0&&me.y-1>=0&&passableMap[me.y-1][me.x]){
				if(t){
					lastMove = "up";
					return move(0,-1);
				}
			}
			
		}
		
		//sends signal max of 10 sqaure radius if damage was just taken
    	if(doAlertEnemiesNearby()){
    		int sigRad = 10;
    		if(sigRad>fuel) {
    			sigRad=fuel;
    		}
    		signal(696969,sigRad);
    	}
		
    		
    	
    	lastHealth = me.health;


		return null;

	}
	
	public MoveAction bfs(Point finalLocation) {
		log("Target: ("+finalLocation.getX()+","+finalLocation.getY()+")");
		log("Current: ("+me.x+","+me.y+")");
		log("Unit: "+me.unit);
		log("null x error? 0");
		//		this.log("moving toward x=" + finalLocation[0] + " y=" + finalLocation[1]);
		if (fuel <= 30 || finalLocation.getX()==-1) { //not enough fuel, or -1 b/c can't find karbo or fuel
			//			this.log("cannot move");
			return null;
		}
		if(!path.isEmpty()) {
			Point p = path.removeFirst();
			return move(p.getX()-me.x,p.getY()-me.y);
		}
		
		//One set for each layer, each set stores all points in layer
		//log("null x error? 1");
		ArrayList<HashSet<BFSPoint>> pointLayers = new ArrayList<HashSet<BFSPoint>>();
		pointLayers.add(new HashSet<BFSPoint>());
		BFSPoint home = new BFSPoint(this.me.x,this.me.y,null);
		pointLayers.get(0).add(home);
		//log("null x error? 2");
		int layer = 0;
		
		//removed all direction stuff
		
		//layer number: minumum number of moves to get to that tile
		//layer 0 is bot's current point, layer 1 is all points moveable from here, layer 2 is all possible points from all layer 1 points, etc
		//first, checks all points in layer to see if any are the final point
		//then, for each point in the layer, all possible moves are added to the next layer
		//repeats until final tile is found
		//each BFSPoint stores the last point, so from the final point, it backtracks to the first move
		//Problems:
		//no order of testing points
		//only finds least # of moves, usually taking longer distances
		
		boolean found = false;
		HashSet<Point> visitedPoints= new HashSet<Point>();
		visitedPoints.add(home);
		Iterator<BFSPoint> iter;
		BFSPoint nextMove;
		BFSPoint current;
		BFSPoint killme;
		//maxDist = square root of movement speed^2
		int maxDist=2;
		if (me.unit == SPECS.CRUSADER) {
			maxDist=3;
		}
		
		while(!found) {
			log("layer: "+layer);
			//iterates through current layer, ends if target is found
			iter=pointLayers.get(layer).iterator();
			while(iter.hasNext()) {
				current = iter.next();
				if(current.equals(finalLocation)) {
					nextMove=current;
					found = true;
					log("Found target, searching for move");
					break;
				}
			}
			if(found) {
				break;
			}
			
			//creates new layer from current layer
			pointLayers.add(new HashSet<BFSPoint>());
			iter=pointLayers.get(layer).iterator();
			while(iter.hasNext()) {
				current = iter.next();
				for(int x = -1*maxDist;x<=maxDist;x++) {
					for(int y = -1*maxDist;y<=maxDist;y++) {
						if(x*x+y*y<=maxDist*maxDist) {
							int newX = current.getX()+x;
							int newY = current.getY()+y;
							if((newX>=0&&newX<passableMap.length)&&(newY>=0&&newY<passableMap.length)) {
								if(passableMap[newY][newX]&&visibleRobotMap[newY][newX]<=0) {
									killme = new BFSPoint(newX,newY,current);
									Iterator<Point> iterPoint=visitedPoints.iterator();
									boolean old = false;
									while(iterPoint.hasNext()) {
										if(iterPoint.next().equals(killme)) {
											old = true;
										}
									}
									if(!old){
										pointLayers.get(layer+1).add(killme);
										visitedPoints.add(killme);
									}
								}
							}
						}
					}
				}
			}
			layer++;
		}
		//nextMove is set to the most recent point, until that point is the bot's current position
		while(!(nextMove.last().equals(home))) {
			path.add(0,new Point(nextMove.getX(),nextMove.getY()));
			nextMove = nextMove.last();
		}
		log("moving to ("+nextMove.getX()+","+nextMove.getY()+")");
		return move(nextMove.getX()-me.x,nextMove.getY()-me.y);
	}
	
	//if damage was taken, sends signal
    	//696969 is alert value :)
    	//broadcasts sq radius of 10 or, if less than 10 fuel, longest possible distance
    	public boolean doAlertEnemiesNearby() {
    		if(me.health<this.lastHealth) {
    			//log("i took damage");
    			
    			//log("sending alert");
    			return true;
    			
    		}
    		return false;
    	}
	
	//returns true if pilgrim can mine fuel
    	public boolean canMineFuel() {
    		
    		if (me.unit == SPECS.PILGRIM && (fuelMap[me.y][me.x])) {
    			if(me.fuel<100){
    				return true;
    			}
    		}
    		return false;
   	 }
		    
	//returns true if pilgrim can mine karbonite
    	public boolean canMineKarbonite() {
    	if (me.unit == SPECS.PILGRIM && (karboniteMap[me.y][me.x])) {
    		if(me.karbonite<20){
				return true;
    		}
    	}
    	return false;
    	}

    	//searches for karbonite within movement range
    	//can only move to one of 8 surrounding squares, should probably be fixed
    	public int[] searchForKarboniteLocation() {
			int[] location = new int[2];
			int[][] visible = getVisibleRobotMap();

			for(int row = -1;row<2;row++){
				for(int col = -1;col<2;col++){
					if((me.x+col>=0&&me.x+col<fuelMap[0].length)&&(me.y+row>=0&&me.y+row<fuelMap.length)){
						if((karboniteMap[row+me.y][col+me.x]&&map[row+me.y][col+me.x])&&visible[row+me.y][col+me.x]==0){
							int[] goTo = {row+me.y,col+me.x};
							
							return goTo;
						}
					}
				}
			}
			return null;
	}

	//searches for fuel within movement range
	//can only move to one of 8 surrounding squares, should probably be fixed
	public int[] searchForFuelLocation() {
			int[] location = new int[2];
			int[][] visible = getVisibleRobotMap();

			for(int row = -1;row<2;row++){
				for(int col = -1;col<2;col++){
					if((me.x+col>=0&&me.x+col<fuelMap[0].length)&&(me.y+row>=0&&me.y+row<fuelMap.length)){
						if((fuelMap[row+me.y][col+me.x]&&map[row+me.y][col+me.x])&&visible[row+me.y][col+me.x]==0){
							int[] goTo = {row+me.y,col+me.x};
							
							return goTo;
						}
					}
				}
			}
			return null;
	}
		    
	
}
