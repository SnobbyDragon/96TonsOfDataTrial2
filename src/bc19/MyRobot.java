package bc19;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class MyRobot extends BCAbstractRobot {
	public int turn = 0;

	public Action turn() {
		turn++;

		if (me.unit == SPECS.CASTLE) {
			if (turn == 1) {
				log("Building a pilgrim.");
				return buildUnit(SPECS.PILGRIM,1,0);
			}
		}
		
		if (me.unit == SPECS.CHURCH) {
			
		}

		if (me.unit == SPECS.PILGRIM) {
			if (turn == 1) {
				log("I am a pilgrim.");

				//log(Integer.toString([0][getVisibleRobots()[0].castle_talk]));
			}
		}

		return null;

	}
}
