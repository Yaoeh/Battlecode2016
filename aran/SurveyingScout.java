package aran;

import java.util.ArrayList;

import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

public class SurveyingScout extends RobotPlayer {
	public static int scoutNum= -1;
    public static void run(RobotController rc) throws GameActionException {
        Info trackedInfo= InfoNet.unitInfoMap.get(RobotType.SCOUT);
    	while (true) {
            try {
            	sensor.goalLoc= null;
            	updateOwnInfo();
            	sensor.senseBullets(rc);
            	sensor.senseEnemies(rc);
            	sensor.senseTrees(rc);
            	
            	Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, 2, 10, 10);
            	Vector2D enemyVec= sensor.moveTowardsEnemyVector(rc, 10, 2, -5, Constants.ignoreArchonGardener);

            	
            	Vector2D goalVec= sensor.moveVecTowardsGoal(rc, 1000);

            	Vector2D moveVec= Util.getMoveVec(rc,new Vector2D[] {
            		dangerVec,
            		enemyVec,
            		goalVec,
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

 
	public static void updateOwnInfo () throws GameActionException {
		Info trackedInfo= InfoNet.unitInfoMap.get(rc.getType());
		int indexOffset= InfoNet.getFirstBehindRoundUpdateRobotIndex(rc); //starting index of an not updated robot type
				
		if (indexOffset!= Integer.MIN_VALUE){
			scoutNum= (indexOffset - trackedInfo.getStartIndex()) / trackedInfo.reservedChannels.size(); //number in the info net slot
			
			for (int i = 0; i < trackedInfo.reservedChannels.size(); i++){ //Iterate through all needed channels
				InfoEnum state= trackedInfo.getInfoEnum(i);
				
				switch (state) {
					case UPDATE_TIME:
						broadcastPrint(rc,indexOffset+ i, rc.getRoundNum(), "time");
						break;
					case ID:
						broadcastPrint(rc, indexOffset+i, rc.getID());
					default:
						break;
				}
			}
		}else{
			//System.out.println("Index offset returning a failed number: " + indexOffset);
		}
	}
}
