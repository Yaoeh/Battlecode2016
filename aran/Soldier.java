package aran;

import aran.Constants.InfoEnum;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends RobotPlayer{
    
    public static void run(RobotController rc) throws GameActionException {
        //sensor.goalLoc= rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
    	while (true) {
            try {
            	sensor.senseTrees(rc);
        		sensor.senseFriends(rc);
        		sensor.senseEnemies(rc);
        		sensor.senseBullets(rc);
            	if (sensor.goalLoc!= null){ //remove goals once you reach them
        			if (rc.senseRobotAtLocation(sensor.goalLoc)== null){
        				sensor.goalLoc= null;
        			}
            	}
            	if (rc.canShake() && sensor.nearbyNeutralTrees!=null && sensor.nearbyNeutralTrees.length > 0){
            		sensor.tryShakeTree(rc);
                } 
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
                    Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, Integer.MAX_VALUE, 10);
                    Vector2D friendVec= sensor.moveTowardsFriendVector(rc, Integer.MAX_VALUE, 2, 1, Constants.ignoreArchonGardenerScout);
                    Vector2D enemyVecStrong= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE, -3, Constants.ignoreNone);    
                    Vector2D enemyVecWeak= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE, 2, Constants.ignoreArchonGardener); 
                    //Vector2D treeVec= sensor.moveTowardsNeutralTreeVector(rc, rcLoc, 1, 9);
                    Vector2D goalVec= sensor.moveVecTowardsGoal(rc, 1);

                    Vector2D tryMoveVec= null;
                    if (dangerVec.length()> Constants.PERCENTAGE_UNTIL_DANGER_OVERRIDE){
                    	System.out.println("Danger vector: " + dangerVec.length());
                    	tryMoveVec= new Vector2D(rc.getLocation()).add(dangerVec); 
                    }else{
                    	tryMoveVec= new Vector2D(rc.getLocation()).add(goalVec).add(enemyVecStrong).add(enemyVecWeak).add(friendVec).add(dangerVec);
                    }

                	if (rc.getLocation().directionTo(tryMoveVec.getMapLoc())!= null){
                		Util.tryMove(rc.getLocation().directionTo(tryMoveVec.getMapLoc()));
                	}
            		
            	}
            	if(rc.getType().canAttack() && sensor.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
                	RobotInfo highPRobotInfo= (RobotInfo) Value.getHighestPriorityBody(rc, sensor.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
            		sensor.tryfireSingleShot(rc, highPRobotInfo.getLocation());
            	}
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


	public static void updateOwnInfo(RobotController rc) throws GameActionException {
		Info trackedInfo= InfoNet.unitInfoMap.get(rc.getType());
		int indexOffset= InfoNet.getFirstBehindRoundUpdateRobotIndex(rc); //starting index of an not updated robot type
		
		for (int i = 0; i < trackedInfo.reservedChannels.size(); i++){
			InfoEnum currentInfo = trackedInfo.getInfoEnum(i);
	        switch (currentInfo) {
		        case UPDATE_TIME:
		        	broadcastPrint(rc, indexOffset+ i, rc.getRoundNum());
		            break;

		        case LOCATION:
		        	broadcastPrint(rc, indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()));
		            break;
	        }
		}
	}
}