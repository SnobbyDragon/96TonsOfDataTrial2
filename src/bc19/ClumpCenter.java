package bc19;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
    public boolean[][] passableMap;
    public int[][] visibleRobotMap;
    public boolean[][] karboniteMap;
    public boolean[][] fuelMap;
    public int turn;
    public ArrayList<HashSet<int[]>> clumpList;
    // int[] location is done by y then x

    public Action turn() {
        turn++;
        if (turn == 1) {
            passableMap = getPassableMap();
            karboniteMap = getKarboniteMap();
            fuelMap = getFuelMap();
            visibleRobotMap=getVisibleRobotMap();
            clumpList = new ArrayList<HashSet<int[]>>();
        }
        //log("My X location: " + me.x);
        //log("My Y location: " + me.y);
        ArrayList<int[]> sortedResources = findSortedResources();
        //log("Sorted Resources: " + sortedResources);
        clumpList = findAllClumps(sortedResources);
        displayAllClumps(clumpList);
        for(HashSet<int[]> set:clumpList){
            log("a");
            int[] currentCenter = clumpCenter(set);
            log("clump center: ("+currentCenter[1]+","+currentCenter[0]+")");
        }

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
            //log("Clump "+i+": "+allClumps.get(i));
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

    public int[] clumpCenter(HashSet<int[]> clump){
        
        double length = (double)(clump.size());
        int xsum = 0;
        int ysum = 0;
        Iterator<int[]> iter = clump.iterator();
        while(iter.hasNext()){
            int[] spot = iter.next();
            xsum+=spot[1];
            ysum+=spot[0];
        }
        int xAvg = (int)Math.round(xsum/length);
        int yAvg = (int)Math.round(ysum/length);

        //checks if (xAvg,yAvg) is available
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }

        //checks right
        xAvg+=1.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        //checks left
        xAvg-=2.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        //checks top
        xAvg+=1.0;
        yAvg+=1.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        //checks bottom
        yAvg-=2.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        //might wanna add more spots to test
        //might want to remove checking for robots and instead kill the bot occupying the spot
        return null;
        
    }

    public int partition(ArrayList<int[]> resources, int start, int end) {
        int[] pivot;
        int endOfLeft;
        int midIndex = (start + end) / 2;
        swap(resources, start, midIndex);
        pivot = resources.get(start);
        endOfLeft = start;
        for (int i = start + 1; i <= end; i++) {
            //log("Resources get i: " + resources.get(i));
            //log("Pivot: " + pivot);
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
