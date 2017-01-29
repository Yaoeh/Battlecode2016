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
    
    static enum status {earlygame, looking, gardenCheck, gardening, ratiogame};
    //static String status = "looking";
    static status stat= status.looking;
    
	public static void run(RobotController rc) throws GameActionException {
        
        myTeam = rc.getTeam();
        
        int lookingCount = 0;
        int lookingCountLimit = 7;
        Direction lookingDir = Util.randomDirection();
        earlyGameInit();
        while (true) {
            try {
            	infoUpdate();
            	
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
            			if(robots[i].type == RobotType.GARDENER)
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
                        if (rc.canMove(d)) {
                            dirNum+=1;
                        }
                    }
                    //status = "gardening";	
            		stat= status.gardening;
            	}
            	if(stat== status.gardening)//status == "gardening")
            	{
            		gardening();
            	}
            	
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

	private static void earlyGame() throws GameActionException {
		if (earlyGameQueue != null) {
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

				if (flag) {
					earlyGameIndex += 1;
					if (earlyGameIndex > earlyGameQueue.length) {
						rc.broadcast(500, 1);
						// status = "gardenCheck";
						stat = status.gardenCheck;
					}

				}
			}
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
	}
	
	private static void gardening() throws GameActionException{
		if(Math.random()<chance)
    	{
    		// :) ninja coding skills
            Direction dir = Util.randomDirection();
            if(queue == 0)
            {
            	if(currentlyPlanted < dirNum - 1)
            	{
            		plantCircleTrees();
            		currentlyPlanted += 1;
            		queue++;
            	}
            	else
            	{
            		queue++;
            	}
            	
            }
            else if(queue< 2){
            	if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
            		//rc.broadcast(999, 1);
                    rc.buildRobot(RobotType.SOLDIER, dir);
                    queue++;
            	}
            }
            else if(queue<3){
            	if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
                    rc.buildRobot(RobotType.SCOUT, dir);
                    queue++;
            	}
            }
            else if(queue < 5){
            	if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                    queue++;
            	}
            }
            else if(queue < 6){
            	if(rc.canBuildRobot(RobotType.LUMBERJACK, dir))
            	{
            		rc.buildRobot(RobotType.LUMBERJACK, dir);
                    queue++;
            	}
            }
            else if(queue < 7){
            	if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                    queue++;
            	}
            }
            if(queue>0){
            	queue = 0;
            }
    	}
		Util.waterLowestHealthTreeWithoutMoving(rc, myTeam);
    	/*if(!Util.dodgeBullets(rc, rc.getLocation()))
        {
        	if(!Util.waterLowestHealthTree(rc, myTeam))
        	{
            	Util.tryMove(Util.randomDirection());
            }
    	}*/
		Clock.yield();
	}
	
	
	private static void plantCircleTrees() throws GameActionException {
        for (SixAngle ra : Constants.SixAngle.values()) {
            Direction d = new Direction(ra.getRadians());
            if (rc.canPlantTree(d)) {
                rc.plantTree(d);
                Clock.yield();
            }
        }
    }
}