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

public class ScoutingScout extends RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        Info trackedInfo= InfoNet.unitInfoMap.get(RobotType.SCOUT);
        incrementCountOnSpawn();
    	while (true) {
            try {
            	//infoUpdate();
            	
            	sensor.goalLoc= null;
            	sensor.senseBullets(rc);
            	sensor.senseEnemies(rc);
            	sensor.senseFriends(rc);
            	sensor.senseTrees(rc);
            	boolean hasTastyTree= overrideGoalWithTastyTreeIfExists();
            	//System.out.println("\tScount num: " + scoutNum);
            	boolean hasNormalGoal= ifNoGoalSetEdgeAsGoal();
            	
            	float goalAttraction= 1000;
            	
            	if (!hasTastyTree && !hasNormalGoal){
            		ifNoGoalSetDefaultGoal();
            		goalAttraction= 0.5f;
            	}
            	
            	
            	Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, 2, 10, 10);
            	Vector2D enemyVec= sensor.moveTowardsEnemyVector(rc, 10, 2, -5, Constants.ignoreArchonGardener);
//            	Vector2D friendVec= sensor.moveTowardsFriendVector(rc, 5, 3, 2, Constants.ignoreNone);
            	//Vector2D badGuyVec= sensor.moveTowardsEnemyVector(rc, 10 ,1, 1, Constants.ignoreDamaging);
//            	if (enemyVec.length() > 0){
//            		badGuyVec.scale(0);
//            	}
//            	
            	
            	Vector2D goalVec= sensor.moveVecTowardsGoal(rc, goalAttraction);
            	//Vector2D treeVec= sensor.moveTowardsNeutralTreeVector(rc, Integer.MAX_VALUE, 4);

            	Vector2D moveVec= Util.getMoveVec(rc,new Vector2D[] {
            		dangerVec,
            		enemyVec,
//            		friendVec,
            		//badGuyVec,
            		goalVec,
            		//treeVec,
            		
            	});
            	
            	Direction moveDir= rc.getLocation().directionTo(moveVec.getMapLoc());
            	if (moveDir != null){
            		Util.tryMove(moveDir);
            	}
            	
            	//rc.setIndicatorLine(rc.getLocation(), sensor.goalLoc, 255, 220, 0);
            	if (hasTastyTree)
            		sensor.tryShakeTree(rc);
            	else if (hasNormalGoal){
            		checkOnMap();
            	}else{
            		Util.tryShoot();
            	}
            	
            	decrementCountOnLowHealth(5);
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static boolean overrideGoalWithTastyTreeIfExists() throws GameActionException{
    	boolean answer= false;
    	if (sensor.nearbyNeutralTrees!= null && sensor.nearbyNeutralTrees.length> 0){
    		TreeInfo bestTree= Value.getTastiestBody(rc, sensor.nearbyNeutralTrees, rc.getLocation(), Integer.MAX_VALUE);
    		if (bestTree!= null){
    			sensor.goalLoc= bestTree.location;
        		rc.setIndicatorLine(rc.getLocation(), sensor.goalLoc, 255, 215, 0);
 
        		answer= true;
    		}
    	}
    	return answer;
    }
    
    public static boolean ifNoGoalSetEdgeAsGoal() throws GameActionException{
    	boolean answer= false;
    	if (sensor.goalLoc== null){
    		
//        	MapLocation[] correspondingGoals={ //min y, max x, max y, min x
//	    		new MapLocation(rc.getLocation().x, Integer.MIN_VALUE),
//	    		new MapLocation(Integer.MAX_VALUE, rc.getLocation().y),
//	    		new MapLocation(rc.getLocation().x, Integer.MAX_VALUE),
//	    		new MapLocation(Integer.MIN_VALUE, rc.getLocation().y)
//        	};
	    	MapLocation[] edgeGoalLocs= getRemainingMapEdgeLocGoals(); //correspondingGoals; //
	    	//System.out.println("Remaining map edge goals: " + edgeGoalLocs.length);
	    	if (unitNum>= 0 && unitNum < 4){
		    	if (edgeGoalLocs.length== 4){
		       		sensor.goalLoc= edgeGoalLocs[unitNum];
		       		System.out.println("setting new Goal loc: " + sensor.goalLoc);
		    		answer = true;
		    	}else if (edgeGoalLocs.length > 0){
		    		//don't set to the closest if you still have a job
		    		sensor.goalLoc= Value.getClosestLoc(edgeGoalLocs, rc.getLocation(), Integer.MAX_VALUE);//use the closest
		    		System.out.println("setting new Goal loc: " + sensor.goalLoc);
		    		answer= true;
		    	}
	    	}
    	}
    	
    	return answer;
    }
    
    public static boolean ifNoGoalSetDefaultGoal(){
    	sensor.goalLoc= Value.getClosestLoc(rc.getInitialArchonLocations(rc.getTeam().opponent()), rc.getLocation(), Integer.MAX_VALUE);
    	return true;
    }
    
    
    public static void checkOnMap() throws GameActionException{
		//for (int d = 0; d < 360; d+= 90){
			
		for (int i = 0 ; i < Constants.COORDINALDIRS.length; i++){
			Direction dir= Constants.COORDINALDIRS[i];
			MapLocation checkLoc= rc.getLocation().add(dir, rc.getType().sensorRadius-1);
			if (rc.onTheMap(checkLoc)){
				//rc.setIndicatorDot(checkLoc, 0, 255, 0);
				rc.setIndicatorLine(rc.getLocation(), (rc.getLocation().add(dir, rc.getType().sensorRadius)), 0, 255, 0);
			}else{
				rc.setIndicatorLine(rc.getLocation(), (rc.getLocation().add(dir, rc.getType().sensorRadius)), 255, 0, 0);
				trySetMapEdge(dir,(rc.getLocation().add(dir, rc.getType().sensorRadius)));
			}

		} 
    }
    
    public static void trySetMapEdge(Direction d, MapLocation loc) throws GameActionException{
    	Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.MAP_EDGE);
    	Direction[] checkedDIr= {
    			Direction.getNorth(),
    			Direction.getEast(),
    			Direction.getSouth(),
    			Direction.getWest()
    	};
    	InfoEnum[] correspondingEnums= {InfoEnum.MIN_Y, InfoEnum.MAX_X, InfoEnum.MAX_Y, InfoEnum.MIN_X};
    	int[] correspondingInfo= {(int) loc.y, (int) loc.x, (int) loc.y, (int) loc.x};
//    	boolean[] correspondingFlip= {false, true, true, false};
    	
    	for (int i= 0; i< checkedDIr.length; i++){
    		if (d.equals(checkedDIr[i])){
    			int index= trackedInfo.getNextInfoStartIndex()+ trackedInfo.getIndex(correspondingEnums[i]); //there is only one
    			int readData= rc.readBroadcast(index);
    			System.out.println(d.toString()+ " Read data: " + readData );
    			
    			if (readData== 0){
    				if (correspondingInfo[i]== 0){
    					correspondingInfo[i]= 1;
    				}
    				broadcastPrint(rc, index, correspondingInfo[i], "edge placed");
    				Util.drawSensorCircle(rc, 0, 255, 255);
    				System.out.println("Found Edge "+ checkedDIr[i].toString()+ ": " + correspondingInfo[i]);
    			}
//    			else if (correspondingFlip[i] == (correspondingInfo[i] > readData)){
//					rc.broadcast(index, correspondingInfo[i]);
//					Util.drawSensorCircle(rc, 0, 255, 0);
//					System.out.println("Found Edge "+ checkedDIr[i].toString()+ ": " + correspondingInfo[i]);
//    			}
    			break;
    		}
    	}
    }
    
    public static MapLocation[] getRemainingMapEdgeLocGoals() throws GameActionException{
    	ArrayList<MapLocation> edgeGoalLoc= new ArrayList<MapLocation>();
    	MapLocation[] correspondingGoals={ //min y, max x, max y, min x
	    		rc.getLocation().add(Direction.getNorth(), rc.getType().sensorRadius),
	    		rc.getLocation().add(Direction.getEast(), rc.getType().sensorRadius),
	    		rc.getLocation().add(Direction.getSouth(), rc.getType().sensorRadius),
	    		rc.getLocation().add(Direction.getWest(), rc.getType().sensorRadius),
	    	};
    	
    	for (int i = 0; i < correspondingGoals.length; i++){
    		boolean stillGoal= isEdgeMissionCompleted(InfoNet.addInfoMap.get(AddInfo.MAP_EDGE).getInfoEnum(i));
    		//System.out.println("Edge mission completed: " + stillGoal);
    		if (!stillGoal){
    			//System.out.println("\t\tRemaining goal:  "+ correspondingGoals[i]);
    			edgeGoalLoc.add(correspondingGoals[i]);
    		}
    	}
    	
    	MapLocation[] answer= new MapLocation[edgeGoalLoc.size()];
    	return edgeGoalLoc.toArray(answer);
    }
    
    public static int getNextMissionInt() throws GameActionException{
    	int answer= -1;
    	//returns -1 on all mission complete
    	for (int i = 0; i < 4; i++){
    		if (!isEdgeMissionCompleted(InfoNet.addInfoMap.get(AddInfo.MAP_EDGE).getInfoEnum(i))){
    			answer= i;
    			//rc.setIndicatorDot(rc.getLocation(), 50*4, 123, 123);
    			break;
    		}
    	}
    	System.out.println("\tGetting next mission: " + answer);
    	return answer;
    }
    
    public static boolean isEdgeMissionCompleted(InfoEnum ie) throws GameActionException{
    	boolean answer= false;
    	Info trackedInfo = InfoNet.addInfoMap.get(AddInfo.MAP_EDGE);
		int infoIndex= trackedInfo.getNextInfoStartIndex() + trackedInfo.getIndex(ie);
		int checkLoc= rc.readBroadcast(infoIndex);
		if (checkLoc!= 0){
			answer= true;
		}
    	return answer;
    }
}
