package aran_v2;
import battlecode.common.*;
public class Gardener extends RobotPlayer
{
	public static void run(RobotController rc) throws GameActionException {
        int queue = 0;//first plants a tree, then a scout, then 3 soldiers, and repeats
        
        double chance = 0.20;
        Team myTeam = rc.getTeam();
        while (true) {
            try {
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
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
}