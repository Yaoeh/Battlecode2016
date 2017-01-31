package aran;

import java.util.ArrayList;
import java.util.Arrays;

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
	public static boolean missionComplete= false;
	public static boolean neutralTreeBarrierScouted= false;
    public static ArrayList<MapLocation> enemArchonLocs;
    public static ArrayList<MapLocation> neutralLocs;
    public static void setupEnemArchonLocs(){
    	if (enemArchonLocs== null){
    		enemArchonLocs= new ArrayList<MapLocation>(Arrays.asList(rc.getInitialArchonLocations(rc.getTeam().opponent())));	
    		System.out.println("enemy archon location setup size: " +enemArchonLocs.size() );
    	}
    	
    }
    
    public static void setUpBarrierScoutPoints(){
    	if (neutralLocs== null){
    		neutralLocs= new ArrayList<MapLocation>();
	    	for (int i = (int) (rc.getType().sensorRadius*2); i < rc.getLocation().distanceTo(enemArchonLocs.get(0)); i+= rc.getType().sensorRadius*2){
	    		neutralLocs.add(rc.getLocation().add(rc.getLocation().directionTo(enemArchonLocs.get(0)), i));
	    	}
    	}
    }
    
    public static void setupGoal(){
    	if (neutralTreeBarrierScouted){    	
			if (enemArchonLocs.size()> 0){
				sensor.goalLoc= enemArchonLocs.get(0);
			}
    	}else{
			if (neutralLocs.size()> 0){
				sensor.goalLoc= neutralLocs.get(0);
			}
    	}
    }
    
    
	public static void removeTreeBarrierOnClose(float radius) throws GameActionException {
		if (sensor.goalLoc != null && neutralLocs != null) {
			if (rc.getLocation().distanceTo(sensor.goalLoc) <= radius) {
				neutralLocs.remove(sensor.goalLoc);
				if (neutralLocs.size() <= 0) {
					neutralTreeBarrierScouted = true;
				}
			}
			
			Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.SCOUTED_INFO);
			int treeCountIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.NUM_NEUTRAL_TREE_SPIED);
			int treeCount= rc.readBroadcast(treeCountIndex);
			//rc.broadcast(gardenerCountIndex, currentGarCount+ gardenerCount);
				broadcastPrint(rc,treeCountIndex, treeCount+ sensor.nearbyNeutralTrees.length, "neutral tree count count by spy");

		}
	}
    
    public static void removeCleanUpDotOnClose(float radius) throws GameActionException{
    	if (sensor.goalLoc!= null && enemArchonLocs!= null){
    		if (rc.getLocation().distanceTo(sensor.goalLoc) <= radius){
    			enemArchonLocs.remove(sensor.goalLoc); 
    			if (enemArchonLocs.size()<= 0){
    				missionComplete = true;
    			}
    			
    			
    			//senses here
    			Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.SCOUTED_INFO);
    			int finalgardenerCount = 0;
    			int finaldamagingCount = 0;
    			int finaltreeCount = 0;
    			if (sensor.nearbyEnemies!= null && sensor.nearbyEnemies.length> 0){
    				int gardenerCount= 0;
    				int damagingUnitCount= 0;
	    			for (int i = 0; i < sensor.nearbyEnemies.length; i++){
	    				RobotInfo enemyInQuestion= sensor.nearbyEnemies[i];
	    				if (enemyInQuestion.getType()== RobotType.GARDENER){
	    					gardenerCount+= 1;
	    				}else if (enemyInQuestion.getType()!= RobotType.ARCHON){
	    					damagingUnitCount+= 1;
	    				}
	    			}
	    			
	    			int gardenerCountIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.NUM_GARDENERS_SPIED);
	    			int currentGarCount= rc.readBroadcast(gardenerCountIndex);
	    			//rc.broadcast(gardenerCountIndex, currentGarCount+ gardenerCount);
      				broadcastPrint(rc,gardenerCountIndex, currentGarCount+ gardenerCount, "gardener count by spy");
	    			finalgardenerCount = currentGarCount+ gardenerCount;
	    			int damagingCountIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.NUM_DAMAGE_SPIED);
	    			int currentDamageCount= rc.readBroadcast(damagingUnitCount);
	    			//rc.broadcast(damagingCountIndex, currentDamageCount+ damagingUnitCount);
	    			broadcastPrint(rc,damagingCountIndex,  currentDamageCount+ damagingUnitCount, "damaging unit count by spy");
	    			finaldamagingCount = currentDamageCount+ damagingUnitCount;
    			}
    			
    			if (sensor.nearbyEnemyTrees!= null && sensor.nearbyEnemyTrees.length> 0){
      				int enemyTreeCount= sensor.nearbyEnemyTrees.length;
      				int treeCountIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.NUM_ENEMY_TREES_SPIED);
      				int originalTreeCount= rc.readBroadcast(treeCountIndex);
      				//rc.broadcast(treeCountIndex, originalTreeCount+ enemyTreeCount);
      				broadcastPrint(rc,treeCountIndex, originalTreeCount+ enemyTreeCount, "tree count by spy");
      				finaltreeCount = originalTreeCount+ enemyTreeCount;
    			}
    			
    			if(finaltreeCount > 1){
    				rc.broadcast(501, 20000);
    				broadcastPrint(rc, 960, 22000, "treecombatratio");
    			}
    			else if(finaltreeCount > 0){
    				rc.broadcast(501, 14000);
    				broadcastPrint(rc, 960, 16000, "treecombatratio");
    			}
    			else if(finaldamagingCount > 1){
    				rc.broadcast(501, 5000);
    				broadcastPrint(rc, 960, 6000, "treecombatratio");
    			}
    			else{
    				rc.broadcast(501, 10000);
    				broadcastPrint(rc, 960, 11000, "treecombatratio");
    			}
			}
    	}
    }
    
    public static void carelessMove() throws GameActionException{
    	Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, 2, 10, 10);
    	Vector2D enemyVec= sensor.moveTowardsEnemyVector(rc, 10, 2, -5, Constants.ignoreNone);

    	
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
    	
    }
    
	public static void virtual_run(RobotController rc) throws GameActionException {
		if (!missionComplete){
			if (enemArchonLocs == null) {
				setupEnemArchonLocs();
			}
			
			if (neutralLocs== null){
				setUpBarrierScoutPoints();
			}

			if (enemArchonLocs.size() > 0) {
				setupGoal();
				sensor.senseBullets(rc);
				sensor.senseEnemies(rc);
				sensor.senseTrees(rc);
	
				carelessMove();
				
				if (!neutralTreeBarrierScouted){
					removeTreeBarrierOnClose(rc.getType().sensorRadius / 3);
				}else{
					removeCleanUpDotOnClose(rc.getType().sensorRadius / 3);
				}
			}else{
				missionComplete= true;
			}
		}else{
			ScoutingScout.run(rc);
		}
	}
}

