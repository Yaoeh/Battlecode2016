package aran;
import java.util.HashSet;

import battlecode.common.*;

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
	        case SCOUT:
	        	Scout.run(rc);
	            break;
	        case TANK:
	            Tank.run(rc);
	        case LUMBERJACK:
	            Lumberjack.run(rc);
	            break;
	    }
	}
    
    public static void removeGoalOnFound() throws GameActionException{
    	if (sensor.goalLoc!= null){ //remove goals once you reach them
    		if (rc.canSenseLocation(sensor.goalLoc) && rc.getLocation().distanceTo(sensor.goalLoc) <= rc.getType().bodyRadius){
    			sensor.goalLoc= null;
    		}
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
		removeGoalOnFound();
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
		removeGoalOnFound();
    	if (rc.canShake() && sensor.nearbyNeutralTrees!=null && sensor.nearbyNeutralTrees.length > 0){
    		sensor.tryShakeTree(rc);
        }    	
    }
    
    public static void broadcastPrint(RobotController rc, int channel, int message) throws GameActionException{
    	System.out.println("\tChannel: " + channel + ": "+ message);
    	rc.broadcast(channel, message);
    }
    public static void broadcastPrint(RobotController rc, int channel, int message, String descript) throws GameActionException{
    	System.out.println("\t"+descript+ " Channel: " + channel + ": "+ message);
    	rc.broadcast(channel, message);
    }
}
