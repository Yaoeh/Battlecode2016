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
	public static void run(RobotController rc) throws GameActionException {
        
        myTeam = rc.getTeam();
        String status = "looking";
        int lookingCount = 0;
        int lookingCountLimit = 20;
        
        while (true) {
            try {
            	//move away from other gardeners
            	if(status == "looking")
            	{
            		MapLocation myLocation = rc.getLocation();
            		RobotInfo[] robots = rc.senseNearbyRobots(-1, myTeam);
            		boolean closeFlag = false;
            		for(int i=0;i<robots.length;i++){
            			if(myLocation.distanceTo(robots[i].getLocation()) < 2.0f)
            			{
            				Util.tryMove(robots[i].getLocation().directionTo(myLocation), 30.0f, 4);
            				closeFlag = true;
            				break;
            			}
            		}
            		
            		if(!closeFlag){
            			TreeInfo[] trees = rc.senseNearbyTrees(-1);
            			for(int i=0;i<trees.length;i++){
                			if(myLocation.distanceTo(trees[i].getLocation()) < 2.0f)
                			{
                				Util.tryMove(trees[i].getLocation().directionTo(myLocation), 30.0f, 4);
                				closeFlag = true;
                				break;
                			}
                		}
            		}
            		
            		if(!closeFlag){
            			status = "gardenCheck";
            		}
            		lookingCount += 1;
            		if(lookingCount > lookingCountLimit)
            		{
            			status = "gardenCheck";
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
            	if(rc.canPlantTree(dir)){
            		rc.plantTree(dir);
            		queue++;
            	}
            	
            	
            }
            else if(queue< 2){
            	if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
            		rc.broadcast(999, 1);
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
            if(queue>6){
            	queue = 0;
            }
    	}
    	if(!Util.dodgeBullets(rc, rc.getLocation()))
        {
        	if(!Util.waterLowestHealthTree(rc, myTeam))
        	{
            	Util.tryMove(Util.randomDirection());
            }
    	}
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