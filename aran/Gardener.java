package aran;
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
	public static void run(RobotController rc) throws GameActionException {
        
        myTeam = rc.getTeam();
        String status = "looking";
        int lookingCount = 0;
        int lookingCountLimit = 7;
        Direction lookingDir = Util.randomDirection();
        while (true) {
            try {
            	//move away from other gardeners
            	if(status == "looking")
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
            			status = "gardenCheck";
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
            	if(status == "gardenCheck")
            	{
            		dirNum = 0;
            		for (SixAngle ra : Constants.SixAngle.values()) {
                        Direction d = new Direction(ra.getRadians());
                        if (rc.canMove(d)) {
                            dirNum+=1;
                        }
                    }
                    status = "gardening";		
            	}
            	if(status == "gardening")
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