package aran;
import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import aran.Constants.SixAngle;
import battlecode.common.*;
public class Gardener extends RobotPlayer
{
	
	static int queue = 0;//first plants a tree, then a scout, then 3 soldiers, and repeats
    static int treesPlanted = 0;
    static double chance = 0.20;
    static Team myTeam;
    static int dirNum = 6; // number of directions this gardener can plant/build robots
    static int currentlyPlanted = 0;
    static int earlyGameIndex = 0;
    static int[] earlyGameQueue = {};
    
    static enum status {earlygame, looking, gardenCheck, midgame, ratiogame};
    //static String status = "looking";
    static status stat= status.looking;
    
    static int soldierCount= 0;
    static int tankCount= 0;
    static int scoutCount= 0;
    static int lumberjackCount= 0;
    static int gardenerCount = 0;
    
	public static void run(RobotController rc) throws GameActionException {
        
        myTeam = rc.getTeam();
        
        int lookingCount = 0;
        int lookingCountLimit = 7;
        Direction lookingDir = Util.randomDirection();
        incrementCountOnSpawn();
        earlyGameInit();
        while (true) {
            try {
            	//infoUpdate();
            	
            	//move away from other gardeners
            	if(stat== status.earlygame)//(status == "earlygame")
            	{
            		earlyGame();
            	}
            	if(stat== status.looking)//status == "looking")
            	{
            		//MapLocation myLocation = rc.getLocation();
            		RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);
            		int gardenerCount = 0;
            		for(int i=0;i<robots.length;i++){
            			if(robots[i].type == RobotType.GARDENER || robots[i].type == RobotType.ARCHON)
            			{
            				gardenerCount+=1;
            				break;
            			}
            		}
            		if(gardenerCount == 0)
            		{
            			//status = "gardenCheck";
            			stat= status.gardenCheck;
            		}
            		else
            		{
	            		boolean moved = Util.tryMove(lookingDir, 30.0f, 3);
	            		if(moved)
	            		{
	            			lookingCount += 1;
	            		}
	            		else
	            		{
	            			//cannot move, use perpendicular direction
	            			lookingDir = lookingDir.rotateLeftDegrees(90.0f);
	            			lookingCount = 0;
	            		}
	            		
	            		if(lookingCount > lookingCountLimit)
	            		{
	            			//find new direction
	            			lookingDir = Util.randomDirection();
	            			lookingCount = 0;
	            		}
            		}
            		
            	}
            	if(stat==status.gardenCheck)//status == "gardenCheck")
            	{
            		dirNum = 0;
            		for (SixAngle ra : Constants.SixAngle.values()) {
                        Direction d = new Direction(ra.getRadians());
                        if(rc.getTeamBullets() >= 50.0f){
                        	if (rc.canPlantTree(d)) {
                        		dirNum+=1;
                        	}
                        }
                        else
                        {
                        	if (rc.canMove(d)) {
                        		dirNum+=1;
                        	}
                        }
                    }
                    //status = "gardening";	
            		stat= status.midgame;
            	}
            	if(stat== status.midgame)//status == "gardening")
            	{
            		Util.waterLowestHealthTreeWithoutMoving(rc, myTeam);
            		ratioGame();
            	}
            	
            	decrementCountOnLowHealth(Constants.LOW_HEALTH_DECREMENT_VALUE);
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

	private static void ratioGame() throws GameActionException { 
		//spawn units based on the corresponding ratio
		//3 soldier to 1 lumberjack to 1 tank to 1 scout
		//Gardener first checks whether or not the unit count is accurate, if it is not then presume 0
		
		Info unitCountInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
		
//		if (!archonDead){ //all info is accurate
//			soldierCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.SOLDIER_COUNT));
//			tankCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.TANK_COUNT));
//			scoutCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.SCOUT_COUNT));
//			lumberjackCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.LUMBERJACK_COUNT));
//			gardenerCount = rc.readBroadcast(unitCountInfo.getStartIndex() + unitCountInfo.getIndex(InfoEnum.GARDENER_COUNT));
//		}else{ //need to check, if inaccurate then they all dead
//			soldierCount= getAccurateUnitCount(RobotType.SOLDIER);
//			tankCount= getAccurateUnitCount(RobotType.TANK);
//			scoutCount= getAccurateUnitCount(RobotType.SCOUT);
//			lumberjackCount= getAccurateUnitCount(RobotType.LUMBERJACK);
//			gardenerCount = getAccurateUnitCount(RobotType.GARDENER);
//		}
		soldierCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.SOLDIER_COUNT));
		tankCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.TANK_COUNT));
		scoutCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.SCOUT_COUNT));
		lumberjackCount= rc.readBroadcast(unitCountInfo.getStartIndex()+ unitCountInfo.getIndex(InfoEnum.LUMBERJACK_COUNT));
		gardenerCount = rc.readBroadcast(unitCountInfo.getStartIndex() + unitCountInfo.getIndex(InfoEnum.GARDENER_COUNT));
		
		System.out.println("soldier count in gardener: " + soldierCount);
		System.out.println("scout count in gardener: " + scoutCount);
		
		//tree cost is 50 bullets
		broadcastPrint(rc, 910, soldierCount);
		broadcastPrint(rc, 911, scoutCount);
		
		int totalUnitCount = soldierCount + tankCount + scoutCount;
		float farmingBulletCount = (float)(gardenerCount * RobotType.GARDENER.bulletCost +rc.getTreeCount() *50); 
		float combatBulletCount = (float)(soldierCount * RobotType.SOLDIER.bulletCost + tankCount * RobotType.TANK.bulletCost
				+ scoutCount * RobotType.SCOUT.bulletCost + lumberjackCount * RobotType.LUMBERJACK.bulletCost);
		float farmingToCombatRatio = ((float)rc.readBroadcast(501)) / 10000.0f;
		float safeBulletBank =(float)( totalUnitCount * 5 + Constants.SAFEMINIMUMBANK);
		if(rc.getRoundNum() < 200){
			safeBulletBank = Constants.SAFEMINIMUMBANK;
		}
		
		
		
		if(farmingBulletCount < 0.01f){
			//build tree
			buildTree(safeBulletBank);
		}
		else if(combatBulletCount < 0.01f)
		{
			buildRobot(safeBulletBank);
		}
		else if(farmingBulletCount/combatBulletCount < farmingToCombatRatio)
		{
			//build tree
			buildTree(safeBulletBank);
		}
		else
		{
			//build combat unit
			buildRobot(safeBulletBank);
		}
		
		//Util.tryBuildRobot(rtToBuild);
	}
	
	public static void buildTree(float safeBulletBank) throws GameActionException{
		if(currentlyPlanted < dirNum - 2)
		{
		
			boolean hasPlanted = plantCircleTrees();
			if(hasPlanted){
				currentlyPlanted+=1;
				//Clock.yield();
				return;
			}
			
		}
		
		if(rc.getTeamBullets() > safeBulletBank)
		{
			buildRobot(safeBulletBank);
		}
		
	}
	public static void buildRobot(float safeBulletBank) throws GameActionException{
		if(rc.getTeamBullets() > safeBulletBank)
		{
			float[] unitRatio = new float[4];
			float totalRatioN = 0.0f;
			for(int i=0;i<4;i++){
				unitRatio[i] = (float)rc.readBroadcast(503+i);
				totalRatioN += unitRatio[i];
			}
			for(int i=0;i<4;i++){
				unitRatio[i]/=totalRatioN;
			}
			float unitTotalCount = (float)(soldierCount + tankCount + scoutCount + lumberjackCount);
			if(unitTotalCount < 0.01f){
				Util.tryBuildRobot(RobotType.SOLDIER);
				return;
			}
			unitRatio[0] = unitRatio[0] - ((float)soldierCount)/unitTotalCount ;
			unitRatio[1] = unitRatio[1] - ((float)scoutCount)/unitTotalCount ;
			unitRatio[2] = unitRatio[2] - ((float)tankCount)/unitTotalCount ;
			unitRatio[3] = unitRatio[3] - ((float)lumberjackCount)/unitTotalCount ;
			
			broadcastPrint(rc, 900, soldierCount);
			broadcastPrint(rc, 901, scoutCount);
			
			for(int i=0;i<4;i++)
			{
				broadcastPrint(rc, 900+i, (int)(unitRatio[i]*100.0f));
			}
			
			float maxIndex = 0;
			float max = unitRatio[0];
			for(int i=1;i<4;i++){
				if(unitRatio[i] > max)
				{
					max = unitRatio[i];
					maxIndex = i;
				}
			}
			
			boolean flag = false;
			if(maxIndex==0) flag = Util.tryBuildRobot(RobotType.SOLDIER);
			if(maxIndex==1) flag = Util.tryBuildRobot(RobotType.SCOUT);
			if(maxIndex==2) flag = Util.tryBuildRobot(RobotType.TANK);
			if(maxIndex==3)
			{
				flag = Util.tryBuildRobot(RobotType.LUMBERJACK);
				System.out.println("lumberjack");
			}
			/*if(flag){
				Clock.yield();
			}*/
		}
	}
//	public static RobotType needMore(int soldierCount, tankCount, scoutCount, lumberjackCount){
//		
//	}
	
//	public static int getAccurateUnitCount(RobotType rt) throws GameActionException{ //this is only accurate if you call infoUpdate first
//		int answer= 0;
//		Info unitCountInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
//
//		if (archonDead){
//				Info unitInfo= InfoNet.unitInfoMap.get(rt);
//				int firstOfUnitUpdateIndex= unitInfo.getStartIndex()+ unitInfo.getIndex(InfoEnum.UPDATE_TIME);
//				int lastUpdateTime= rc.readBroadcast(firstOfUnitUpdateIndex);
//				if (rc.getRoundNum() - lastUpdateTime > Constants.DEAD_TOLERANCE_ROUNDNUM){
//					int checkIndex= InfoNet.getFirstBehindRoundUpdateOtherRobotIndex(rc, rt);
//					if (checkIndex== Integer.MIN_VALUE){
//						answer= InfoNet.getNumTypeTracked(rt);
//					}else{
//						answer= (checkIndex - unitInfo.getStartIndex()) / unitInfo.reservedChannels.size();
//					}
//				}
//		}
//		return answer;
//	}

	private static void earlyGame() throws GameActionException {
		if (rc.getRoundNum()< 500) {
			boolean flag = false;
			Direction dir = new Direction(0.0f);
			for (int i = 0; i < 12; i++) {
				if (i >= 0 && i < earlyGameQueue.length) { //added check because I found Array Index out of bound error
					if (earlyGameQueue[earlyGameIndex] == 0) {
						if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
							rc.buildRobot(RobotType.SCOUT, dir);
							flag = true;
							break;
						}
					} else if (earlyGameQueue[earlyGameIndex] == 1) {
						if (rc.canPlantTree(dir)) {
							rc.plantTree(dir);
							flag = true;
							break;
						}
					} else if (earlyGameQueue[earlyGameIndex] == 2) {
						if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
							rc.buildRobot(RobotType.SOLDIER, dir);
							flag = true;
							break;
						}
					}
					dir = dir.rotateLeftDegrees(30.0f);
				}
			}
			
			if (flag) {
				earlyGameIndex += 1;
				if (earlyGameIndex >= earlyGameQueue.length) {
					rc.broadcast(500, 1);
					// status = "gardenCheck";
					stat = status.looking;
				}
			}
		}else{
			stat= status.looking;
		}
	}
	private static void earlyGameInit() throws GameActionException{
		int isEarlyGame = rc.readBroadcast(500);
        
        if(isEarlyGame == 0){
        	//status = "earlygame";
        	stat=stat.earlygame;
        	int earlyGameType = rc.readBroadcast(507);
        	if(earlyGameType == 0)
        	{
        		earlyGameQueue = Constants.EARLYGAME_SCOUTFIRST_SPAWNORDER;
        	}
        	else if(earlyGameType == 1)
        	{
        		earlyGameQueue = Constants.EARLYGAME_TREEFIRST_SPAWNORDER;
        	}
        	else if(earlyGameType == 2)
        	{
        		earlyGameQueue = Constants.EARLYGAME_SOLDIERFIRST_SPAWNORDER;
        	}
        }
        else if(isEarlyGame == 1){
        	stat = stat.looking;
        }
	}
	
	
	private static boolean plantCircleTrees() throws GameActionException {
        for (SixAngle ra : Constants.SixAngle.values()) {
            Direction d = new Direction(ra.getRadians());
            if (rc.canPlantTree(d)) {
                rc.plantTree(d);
                return true;
            }
        }
        return false;
    }
}