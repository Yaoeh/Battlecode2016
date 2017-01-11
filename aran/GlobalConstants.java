package aran;

import java.util.Dictionary;

import battlecode.common.RobotType;

public interface GlobalConstants {
	public int stepsUntilJiggle= 5; //jiggle so moving in a direction they don't get stuck
	public float percentageUntilDangerOverride= 0.8f;
	public float treeGardenMultiplier= 2;
	
	enum infoIndex {bullets, friends, enemies, trees};
	int[] garProfil= {0, 10, 10, 0};
	int[] archProfil= {0, 10, 10, Integer.MAX_VALUE};
	int[] soldProfil= {0, 2, 0, 10};
	int[] lumbProfil= {0, 10, 0, 5};

	
}
