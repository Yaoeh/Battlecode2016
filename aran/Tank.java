package aran;

import battlecode.common.BodyInfo;
import battlecode.common.Clock;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.TreeInfo;

public class Tank extends RobotPlayer{
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
            	//infoUpdate();	
        		incrementCountOnSpawn();
            	
            	sensor.senseTrees(rc);
            	if (sensor.nearbyNeutralTrees!= null && sensor.nearbyNeutralTrees.length> 0){
            		BodyInfo closetTree= Value.getClosestBody(sensor.nearbyNeutralTrees, rc.getLocation(), Integer.MAX_VALUE);
            		if (closetTree!= null){
            			sensor.goalLoc= closetTree.getLocation();
            		}
            		rc.setIndicatorLine(rc.getLocation(), sensor.goalLoc, 252, 258, 220);
            		sensor.tryShakeTree(rc);
            		Util.tryMove(rc.getLocation().directionTo(sensor.goalLoc));

            	}else{
            		Util.tryMove(Util.randomDirection());
            	}
            	
            	
            	decrementCountOnLowHealth();
            	Clock.yield();
            	
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}