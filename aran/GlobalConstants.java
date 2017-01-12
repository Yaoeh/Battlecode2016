package aran;

import java.util.Arrays;
import java.util.HashSet;

import battlecode.common.RobotType;

public interface GlobalConstants {
	public int stepsUntilJiggle= 5; //jiggle so moving in a direction they don't get stuck
	public float percentageUntilDangerOverride= 0.8f;
	public float treeGardenMultiplier= 2;
	
	public float archonGoalRefreshRate= 10;
	public float targetRefreshRate= 3;
	
	int bulletIndex= 0;
	int friendIndex= 1;
	int enemyIndex= 2;
	int treeIndex= 3;
	
	//enum infoIndex {bullets, friends, enemies, trees}; //enums apparently illegal
	int[] garProfil= {0, 3, 0, 0};
	int[] archProfil= {0, 3, 0, 10};
	int[] soldProfil= {0, 3, 0, 10};
	int[] lumbProfil= {0, 3, 0, 5};
	int[] scoutProfil= {0,2,0,10};
	
	HashSet<RobotType> ignoreNone= new HashSet<RobotType>();
	HashSet<RobotType> ignoreScout= new HashSet<RobotType>(Arrays.asList(RobotType.SCOUT));
	HashSet<RobotType> ignoreArchon= new HashSet<RobotType>(Arrays.asList(RobotType.ARCHON));
	HashSet<RobotType> ignoreAllExceptArchon= new HashSet<RobotType>(Arrays.asList(RobotType.GARDENER, RobotType.LUMBERJACK, RobotType.SCOUT, RobotType.SOLDIER, RobotType.TANK));
}
