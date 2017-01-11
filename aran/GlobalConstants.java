package aran;

public interface GlobalConstants {
	public int stepsUntilJiggle= 5; //jiggle so moving in a direction they don't get stuck
	public float percentageUntilDangerOverride= 0.8f;
	public float treeGardenMultiplier= 2;
	
	//enum infoIndex {bullets, friends, enemies, trees}; //enums apparently illegal
	int bulletIndex= 0;
	int friendIndex= 1;
	int enemyIndex= 2;
	int treeIndex= 3;
	
	int[] garProfil= {0, 10, 10, 0};
	int[] archProfil= {0, 0, 0, 10};
	int[] soldProfil= {0, 0, 0, 10};
	int[] lumbProfil= {0, 0, 0, 5};

	int senseRadiusDivisor= 1;
}
