package aran;
import java.util.HashSet;

import battlecode.common.*;
import sherryy.Archon;
import sherryy.Gardener;
import sherryy.Lumberjack;
import sherryy.Soldier;
import sherryy.Tank;

public strictfp class RobotPlayer{
    static RobotController rc;
    static Sensor sensor= new Sensor();
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
    	
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
	        case ARCHON:
	            Archon.run(rc);
	            break;
	        case GARDENER:
	            Gardener.run(rc);
	            break;
	        case SOLDIER:
	            Soldier.run(rc);
	            break;
	        case LUMBERJACK:
	            Lumberjack.run(rc);
	            break;
	        case TANK:
	            Tank.run(rc);
	    }
	}

    public static void notMoveGeneric(int[] profile, float[] rads) throws GameActionException{ //incomplete, base on profiles
    	sensor.senseTrees(rc);
		sensor.senseFriends(rc);
		sensor.senseEnemies(rc);
    	if(rc.getType().canAttack() && sensor.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) Value.getHighestPriorityBody(rc, sensor.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		sensor.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
		sensor.senseBullets(rc);
    	if (sensor.goalLocs!= null){ //remove goals once you reach them
    		for (MapLocation loc : sensor.goalLocs) {
    			if (rc.canSenseLocation(loc)){
    				if (rc.senseRobotAtLocation(loc)== null){
    					sensor.goalLocs.remove(loc);
    					break;
    				}
    			}
    		}
    	}
    	if (rc.canShake() && sensor.nearbyNeutralTrees!=null && sensor.nearbyNeutralTrees.length > 0){
    		sensor.tryShakeTree(rc);
        }    	
    }
    
   
    public static void notMoveGeneric() throws GameActionException{
    	sensor.senseTrees(rc);
		sensor.senseFriends(rc);
		sensor.senseEnemies(rc, rc.getType().sensorRadius);
    	if(rc.getType().canAttack() && sensor.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) Value.getHighestPriorityBody(rc, sensor.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		sensor.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
		sensor.senseBullets(rc);
		sensor.senseEnemies(rc, rc.getType().sensorRadius-2); //this is how you move, not how you shoot
    	if (sensor.goalLocs!= null){ //remove goals once you reach them
    		for (MapLocation loc : sensor.goalLocs) {
    			if (rc.canSenseLocation(loc)){
					sensor.goalLocs.remove(loc);
					break;
    			}
    		}
    	}
    	if (rc.canShake() && sensor.nearbyNeutralTrees!=null && sensor.nearbyNeutralTrees.length > 0){
    		sensor.tryShakeTree(rc);
        }    	
    }
    
}
