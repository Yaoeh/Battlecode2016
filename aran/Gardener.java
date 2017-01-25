package aran;

import java.util.HashSet;

import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

public class Gardener extends RobotPlayer {
    public static int gardenerNum = -1;
    public static void run(RobotController rc) throws GameActionException {
    	while (true) {
            try {
            	sensor.senseFriends(rc);
            	sensor.senseEnemies(rc);
            	sensor.senseTrees(rc);
            	move(rc);
            	updateOwnInfo(rc);
            	
            	if (gardenerNum < 2){
            		runProduceGardener();
            	}else{
            		runTreeGardener();
            	}
            	
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
			gardenerNum= (indexOffset - trackedInfo.getStartIndex()) / trackedInfo.reservedChannels.size();
						
			for (int i = 0; i < trackedInfo.reservedChannels.size(); i++){
				InfoEnum state= trackedInfo.getInfoEnum(i);
				
				switch (state) {
					case UPDATE_TIME:
						rc.broadcast(indexOffset+ i, rc.getRoundNum());
						break;
					case LOCATION:
						rc.broadcast(indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()));
					case ID:
						rc.broadcast(indexOffset+i, rc.getID());
					default:
						break;
						
				}
			}
		}else{
			//System.out.println("Index offset returning a failed number: " + indexOffset);
			//This can happen if there are more than the enough tracked gardeners
		}
	}
    
    public static void move(RobotController rc) throws GameActionException{
    	if (!rc.hasMoved()){
            Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc,2, Integer.MAX_VALUE, 10);
            //RobotController rc, MapLocation rcLoc, int maxConsidered, float multiplier, float bodyRadiusMultiplier, HashSet<RobotType> ignoreType)
            Vector2D friendVec= sensor.moveTowardsFriendVector(rc, Integer.MAX_VALUE, 7, 5, Constants.ignoreNone);
            Vector2D enemyVecStrong= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE,1, -3, Constants.ignoreNone);    
            Vector2D enemyVecWeak= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE,1, 2, Constants.ignoreArchonGardener); 
            Vector2D treeVec= sensor.moveTowardsTreeVectorDisregardTastiness(rc, -3, 1);
            Vector2D goalVec= sensor.moveVecTowardsGoal(rc, 10);

            Vector2D tryMoveVec= null;
            if (dangerVec.length()> Constants.PERCENTAGE_UNTIL_DANGER_OVERRIDE){
            	//System.out.println("Danger vector: " + dangerVec.length());
            	tryMoveVec= new Vector2D(rc.getLocation()).add(treeVec).add(dangerVec); 
            }else{
            	tryMoveVec= new Vector2D(rc.getLocation()).add(goalVec).add(enemyVecStrong).add(enemyVecWeak).add(friendVec).add(treeVec).add(dangerVec);
            }

        	if (rc.getLocation().directionTo(tryMoveVec.getMapLoc())!= null){
        		Util.tryMove(rc.getLocation().directionTo(tryMoveVec.getMapLoc()));
        	}
    	}
    }
	
    public static void runTreeGardener() throws GameActionException {
        // priority 1: water trees
        TreeInfo[] trees = sensor.nearbyFriendlyTrees;
        for (TreeInfo tree: trees) {
//            if (tree.getTeam() == rc.getTeam()) {
                if (rc.canWater(tree.ID)) {
                    rc.water(tree.ID);
                    Clock.yield();
                }
//            }
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
                            if (!rc.hasMoved() && rc.canMove(tr.location.add(new Direction(ra.getRadians()), 2))) {
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
        //Util.dodge();
    	//move(rc);
        if (rc.getRoundNum() < 500) {
//            int prevNumLj = rc.readBroadcast(Constants.Channel.LUMBERJACK_COUNTER);
//            if (prevNumLj <= Constants.LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, Util.randomDirection())) {
//                rc.buildRobot(RobotType.LUMBERJACK, Util.randomDirection());
//                rc.broadcast(Constants.Channel.LUMBERJACK_COUNTER, prevNumLj + 1);
//            }
        	Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
//        	int lumberjackCountIndex= trackedInfo.getStartIndex() + trackedInfo.getIndex(InfoEnum.LUMBERJACK_COUNT);
//        	int lumberjackCount= rc.readBroadcast(lumberjackCountIndex);
//        	if (lumberjackCount < Constants.LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, Util.randomDirection())){
//        		rc.buildRobot(RobotType.LUMBERJACK, Util.randomDirection());
//        	}
        	int scoutCountIndex= trackedInfo.getStartIndex() + trackedInfo.getIndex(InfoEnum.SCOUT_COUNT);
        	int scoutCount= rc.readBroadcast(scoutCountIndex);
        	Direction randomDir= Util.randomDirection();
        	if (scoutCount < Constants.SCOUT_MAX && rc.canBuildRobot(RobotType.SCOUT, Util.randomDirection()) && rc.getTeamBullets() > RobotType.SCOUT.bulletCost){
        		rc.buildRobot(RobotType.SCOUT, randomDir);
        	}else{
            	int tankCountIndex= trackedInfo.getStartIndex() + trackedInfo.getIndex(InfoEnum.TANK_COUNT);
            	int tankCount= rc.readBroadcast(tankCountIndex);
            	if (tankCount < Constants.TANK_MAX && rc.canBuildRobot(RobotType.TANK, Util.randomDirection()) && rc.getTeamBullets() > RobotType.TANK.bulletCost){
            		rc.buildRobot(RobotType.TANK, Util.randomDirection());
            	}
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