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
            	sensor.goalLoc= null;
            	updateOwnInfo();
            	sensor.senseBullets(rc);
            	sensor.senseEnemies(rc);
            	sensor.senseTrees(rc);
            	ifNoGoalSetDefaultGoal();
            	
            	Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, 2, Integer.MAX_VALUE, 10);
            	Vector2D enemyVec= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE, 2, -5, Constants.ignoreArchonGardener);
            	Vector2D friendVec= sensor.moveTowardsFriendVector(rc, Integer.MAX_VALUE, 3, 2, Constants.ignoreNone);
            	Vector2D badGuyVec= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE,1, 1, Constants.ignoreDamaging);
            	if (enemyVec.length() > 0){
            		badGuyVec.scale(0);
            	}
            	
            	Vector2D goalVec= sensor.moveVecTowardsGoal(rc, 0.5f);
            	Vector2D moveVec= Util.getMoveVec(rc,new Vector2D[] {
            		dangerVec,
            		enemyVec,
            		friendVec,
            		badGuyVec,
            		goalVec,
            		//treeVec,
            		
            	});
            	
            	Direction moveDir= rc.getLocation().directionTo(moveVec.getMapLoc());
            	if (moveDir != null){
            		Util.tryMove(moveDir);
            	}
            	
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


	public static void updateOwnInfo() throws GameActionException {
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
	
    
    public static boolean ifNoGoalSetDefaultGoal(){
    	sensor.goalLoc= Value.getClosestLoc(rc.getInitialArchonLocations(rc.getTeam().opponent()), rc.getLocation(), Integer.MAX_VALUE);
    	return true;
    }
    
}