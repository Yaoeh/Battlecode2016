package aran;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

public class Gardener extends RobotPlayer {
    
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
//            	sensor.senseFriends(rc);
//            	sensor.senseEnemies(rc);
//            	sensor.senseTrees(rc);
            	updateOwnInfo(rc);
//            	move(rc);
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
	public static void updateOwnInfo (RobotController rc) throws GameActionException {
		Info trackedInfo= InfoNet.unitInfoMap.get(rc.getType());
		int indexOffset= InfoNet.getFirstBehindRoundUpdateRobotIndex(rc); //starting index of an not updated robot type
		
		if (indexOffset!= Integer.MIN_VALUE){						
			for (int i = 0; i < trackedInfo.reservedChannels.size(); i++){
		        switch (trackedInfo.getInfoEnum(i)) {
			        case LOCATION:
			        	//rc.broadcast(indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()));
			        	broadcastPrint(rc, indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()), "loc");
			        	break;
			        case UPDATE_TIME:
			        	//rc.broadcast(indexOffset+ i, rc.getRoundNum());
			        	broadcastPrint(rc,indexOffset+i, rc.getRoundNum(), "time");
			            break;
					default:
						break;
		        }
			}
		}
	}
    
    public static void move(RobotController rc) throws GameActionException{
    	MapLocation rcLoc= rc.getLocation();
    	
    	if (!rc.hasMoved()){
            Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, rcLoc, Integer.MAX_VALUE, 10);
            Vector2D friendVec= sensor.moveTowardsFriendVector(rc, rcLoc, Integer.MAX_VALUE, 2, 0.1f, Constants.ignoreArchonGardener);
            Vector2D enemyVecStrong= sensor.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, -3, Constants.ignoreNone);    
            Vector2D enemyVecWeak= sensor.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, 2, Constants.ignoreArchonGardener); 
            Vector2D treeVec= sensor.moveTowardsTreeVectorDisregardTastiness(rc, rcLoc, 1, 1);
            Vector2D goalVec= sensor.moveVecTowardsGoal(rc, rcLoc,1, 10);

            Vector2D tryMoveVec= null;
            if (dangerVec.length()> Constants.PERCENTAGE_UNTIL_DANGER_OVERRIDE){
            	//System.out.println("Danger vector: " + dangerVec.length());
            	tryMoveVec= new Vector2D(rcLoc).add(treeVec).add(dangerVec); 
            }else{
            	tryMoveVec= new Vector2D(rcLoc).add(goalVec).add(enemyVecStrong).add(enemyVecWeak).add(friendVec).add(treeVec).add(dangerVec);
            }

        	if (rcLoc.directionTo(tryMoveVec.getMapLoc())!= null){
        		Util.tryMove(rcLoc.directionTo(tryMoveVec.getMapLoc()));
        	}
    	}
    }
	
    public static void runTreeGardener() throws GameActionException {
        // priority 1: water trees
        TreeInfo[] trees = rc.senseNearbyTrees();
        for (TreeInfo tree: trees) {
            if (tree.getTeam() == rc.getTeam()) {
                if (rc.canWater(tree.ID)) {
                    rc.water(tree.ID);
                    Clock.yield();
                }
            }
        }
        
        // priority 2: build trees
        if (trees.length == 0) {
            // no tree around. start a new cluster
            for (aran.Constants.SixAngle ra : Constants.SixAngle.values()) {
                Direction d = new Direction(ra.getRadians());
                if (rc.canPlantTree(d)) {
                    rc.plantTree(d);
                    Clock.yield();
                }
            }
        } else {
            if (rc.senseNearbyTrees(2).length == 0) {
                // move towards cluster
                for (TreeInfo tr: trees) {
                    if (tr.team == rc.getTeam()) {
                        for (aran.Constants.SixAngle ra : Constants.SixAngle.values()) {
                            if (rc.canMove(tr.location.add(new Direction(ra.getRadians()), 2))) {
                                rc.move(tr.location.add(new Direction(ra.getRadians()), 2));
                                if (rc.getLocation().distanceTo(tr.location.add(new Direction(ra.getRadians()), 2)) < 0.1) {
                                    return;
                                }
                                Clock.yield();
                            }
                        }
                    }
                }
            } else {
                // continue planting trees in a circle
                for (aran.Constants.SixAngle ra : Constants.SixAngle.values()) {
                    Direction d = new Direction(ra.getRadians());
                    if (rc.canPlantTree(d)) {
                        rc.plantTree(d);
                        Clock.yield();
                    }
                }
            }
        }
    }
    
    public static void runProduceGardener() throws GameActionException {
        Util.dodge();
        if (rc.getRoundNum() < 500) {
            int prevNumLj = rc.readBroadcast(Constants.Channel.LUMBERJACK_COUNTER);
            if (prevNumLj <= Constants.LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, Util.randomDirection())) {
                rc.buildRobot(RobotType.LUMBERJACK, Util.randomDirection());
                rc.broadcast(Constants.Channel.LUMBERJACK_COUNTER, prevNumLj + 1);
            }
        }
        else {
            if (rc.canBuildRobot(RobotType.SOLDIER, Direction.getEast())) {
                rc.buildRobot(RobotType.SOLDIER, Direction.getEast());
            }
        }
    }
    
    public static TreeInfo getClosestTree(TreeInfo[] trees, RobotController rc) {
        TreeInfo closest = null;
        float distMin = Math.max(GameConstants.MAP_MAX_HEIGHT, GameConstants.MAP_MAX_WIDTH);
        MapLocation tempLoc = rc.getLocation();
        for (TreeInfo t : trees) {
            if (tempLoc.distanceTo(t.location) < distMin) {
                closest = t;
                distMin = tempLoc.distanceTo(t.location);
            }
        }
        return closest;
    }
}