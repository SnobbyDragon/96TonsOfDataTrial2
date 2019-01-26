package bc19;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;
	public int[] castleLocation;
	public Action turn() {
		turn++;
		if(turn==1) {
			castleLocation=new int[2];
		}
		if(me.unit==SPECS.CASTLE) {
			if(turn==1) {
				return buildUnit(SPECS.PILGRIM,1,0);
			}
		}
		if(me.unit==SPECS.PILGRIM) {
			if(turn==1) {
				castleLocation=findMyCastle();
			}
			try {
				return mine();
			} catch (Exception e) {
				log("Can't mine");
				try {
					return give(me.x-castleLocation[1],me.y-castleLocation[0],me.karbonite,me.fuel);
				} catch (Exception f) {
					log("Didn't give");
				}
			}
			log("Castle Location: "+castleLocation);
		}
		log("Made the null");
		return null;
	}
	
	public int[] findMyCastle() {
		int[] castleLocation=new int[2];
		Robot[] visibleRobots=getVisibleRobots();
		for(int i=0;i<visibleRobots.length;i++) {
			if(visibleRobots[i].unit==SPECS.CASTLE) {
				castleLocation[0]=visibleRobots[i].y;
				castleLocation[1]=visibleRobots[i].x;
			}
		}
		return castleLocation;
	}
	public double findDistance(int[] location) {
		int xDistance = location[1] - me.x;
		int yDistance = location[0] - me.y;
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}

	public double findDistance(int[] location1, int[] location2) {
		int xDistance = location2[1] - location1[1];
		int yDistance = location2[0] - location1[0];
		return Math.pow(xDistance, 2) + Math.pow(yDistance, 2);
	}
}
