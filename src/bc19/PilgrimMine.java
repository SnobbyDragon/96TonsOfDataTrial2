package bc19;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

public class MyRobot extends BCAbstractRobot {
	public int turn;

	public Action turn() {
		turn++;
		if(me.unit==SPECS.CASTLE) {
			if(turn==1) {
				return buildUnit(SPECS.PILGRIM,1,0);
			}
		}
		if(me.unit==SPECS.PILGRIM) {
			try {
				return mine();
			} catch (Exception e) {
				log("Can't mine");
			}
		}
		log("Made the null");
		return null;

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
