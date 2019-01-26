package bc19;

import java.util.ArrayList;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public ArrayList<ArrayList<int[]>> clumpList;
	//int[] location is done by y then x

	public Action turn() {
		if(turn==1) {
			clumpList=new ArrayList<ArrayList<int[]>>();
		}
		log(""+findClump());
		turn++;

		return null;

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
		boolean[][] karboniteMap=getKarboniteMap();
		boolean[][] fuelMap=getFuelMap();
		ArrayList<int[]> sortedResources=new ArrayList<int[]>();
		
		//Goes through the map
		//Checks to make sure I got my y's and x's correct
		for(int y=0;y<karboniteMap.length;y++) {
			for(int x=0;x<karboniteMap[y].length;x++) {
				if(karboniteMap[y][x]==true) {
					int[] location = new int[2];
					location[0]=y;
					location[1]=x;
					sortedResources.add(location);
				}
				if(fuelMap[y][x]==true) {
					int[] location = new int[2];
					location[0]=y;
					location[1]=x;
					sortedResources.add(location);
				}
			}
		}
		//At this point, sortedResources has all of the fuel and karbonite locations from the map
		return sortedResources;
	}
}
