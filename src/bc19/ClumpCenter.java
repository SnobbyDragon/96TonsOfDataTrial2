package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

public class MyRobot extends BCAbstractRobot {

    //returns (y,x)
    //NOT (x,y)
    public int[][] clumpCenter(ArrayList<int[]> clump){
        double length = (double)(clump.size());
        int xsum = 0;
        int ysum = 0;
        for(int[] spot:clump){
            xsum+=spot[1];
            ysum+=spot[0];
        }
        int xAvg = Math.round(xsum/length);
        int yAvg = Math.round(ysum/length);
        
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[][] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        xAvg+=1.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[][] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        xAvg-=2.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[][] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        xAvg+=1.0;
        yAvg+=1.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[][] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        yAvg-=2.0;
        if(passableMap[yAvg][xAvg]&&visibleRobotMap[xAvg][yAvg]<=0){
            if((xAvg>=0&&xAvg<passableMap.length)&&(yAvg>=0&&yAvg<passableMap.length)){
                if(!(fuelMap[yAvg][xAvg]||karboniteMap[yAvg][xAvg])){
                  int[][] arr = {yAvg,xAvg};
                  return arr;
                }
            }
        }
        
        //if were here, oh god oh fuck
        return null;
        
    }

}
