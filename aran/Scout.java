package aran;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Scout extends RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric();
            	
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
                    Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, rcLoc, Integer.MAX_VALUE, 10);
                    Vector2D friendVec= sensor.moveTowardsFriendVector(rc, rcLoc, Integer.MAX_VALUE, 2, 0.1f, Constants.ignoreArchonGardener);
                    Vector2D enemyVecStrong= sensor.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, -3, Constants.ignoreNone);    
                    Vector2D enemyVecWeak= sensor.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, 2, Constants.ignoreArchonGardener); 
                    Vector2D treeVec= sensor.moveTowardsTreeVectorDisregardTastiness(rc, rcLoc, 1, 1);
                    Vector2D goalVec= sensor.moveVecTowardsGoal(rc, rcLoc,1, 10);

                    Vector2D tryMoveVec= null;
                    if (dangerVec.length()> Constants.percentageUntilDangerOverride){
                    	System.out.println("Danger vector: " + dangerVec.length());
                    	tryMoveVec= new Vector2D(rcLoc).add(treeVec).add(dangerVec); 
                    }else{
                    	tryMoveVec= new Vector2D(rcLoc).add(goalVec).add(enemyVecStrong).add(enemyVecWeak).add(friendVec).add(treeVec).add(dangerVec);
                    }

                	if (rcLoc.directionTo(tryMoveVec.getMapLoc())!= null){
                		//util.tryMove(rcLoc.directionTo(tryMoveVec.getMapLoc()));
                	}
            		
            	}
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
