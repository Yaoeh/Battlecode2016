package aran;

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

public class Lumberjack extends RobotPlayer {
	public static int lumberjackNum= -1;
    public static void run(RobotController rc) throws GameActionException {
        sensor.goalLoc= rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
    	while (true) {
            try {
            	Info unitCount= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
            	int archonCountIndex= unitCount.getStartIndex()+ unitCount.getIndex(InfoEnum.ARCHON_COUNT);
            	int numArchons= rc.readBroadcast(archonCountIndex);
            	if (numArchons > 0){
            		int closestArchonInfoIndex= InfoNet.getClosestRobotTypeIndex(rc, RobotType.ARCHON);
            		int dangerStatus= rc.readBroadcast(closestArchonInfoIndex + InfoNet.unitInfoMap.get(RobotType.ARCHON).getIndex(InfoEnum.STATUS));
            		if (dangerStatus- 2 > 0){
            			int mapInt= rc.readBroadcast(closestArchonInfoIndex + InfoNet.unitInfoMap.get(RobotType.ARCHON).getIndex(InfoEnum.LOCATION));
            			if (mapInt!= 0){
            				MapLocation archonLoc= InfoNet.extractMapLocation(mapInt);
                			sensor.goalLoc= archonLoc;
            			}
            		}
            		
            	}else{
            		if (lumberjackNum== InfoNet.NUM_LUMBERJACKS_TRACKED){ //if last guy tracked
            			updateTypeUnitCounts(rc);
            		}
            	}
            	
            	sensor.senseEnemies(rc);
            	if (sensor.goalLoc== null){
	            	if (sensor.nearbyEnemies.length <= 0){ //if no enemies nearby
	            		int readChannel = InfoNet.getClosestAddInfoTargetIndex(rc, AddInfo.BLACKLIST) + InfoNet.addInfoMap.get(AddInfo.BLACKLIST).getIndex(InfoEnum.LOCATION);
	            		//System.out.println("Read channel: " + readChannel);
	            		if (InfoNet.channelWithinBoradcastRange(readChannel)){
//		            		int hateLocIndex= rc.readBroadcast(readChannel);
//		            		if (hateLocIndex!= 0){
//		            			MapLocation hatecrimeLoc= InfoNet.extractMapLocation(hateLocIndex);
//		            			sensor.goalLoc= hatecrimeLoc;
//		            		}
	            		}else{
	            			//No stated enemies
	            		}
	            	}
            	}
            	
            	sensor.senseTrees(rc, rc.getType().strideRadius);
            	sensor.senseBullets(rc);
            	
            	move(rc);
				if (!rc.hasAttacked() && Value.shouldStrike(rc, sensor.nearbyEnemies, sensor.nearbyFriends,
						sensor.nearbyEnemyTrees, sensor.nearbyFriendlyTrees, sensor.nearbyNeutralTrees)) {
            		rc.strike();
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
			lumberjackNum= (indexOffset - trackedInfo.getStartIndex()) / trackedInfo.reservedChannels.size();
						
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
            Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, Integer.MAX_VALUE, 10);
            Vector2D friendVec= sensor.moveTowardsFriendVector(rc, Integer.MAX_VALUE, 2, 2, Constants.ignoreNone);
            Vector2D enemyVecStrong= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE, -3, Constants.ignoreNone);    
            Vector2D enemyVecWeak= sensor.moveTowardsEnemyVector(rc, Integer.MAX_VALUE, 2, Constants.ignoreArchonGardener); 
            //RobotController rc, MapLocation rcLoc, int maxConsidered, float multiplier		
            Vector2D treeVec= sensor.moveTowardsTreeVectorDisregardTastiness(rc, Integer.MAX_VALUE, -5);
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
	
    public static void updateTypeUnitCounts(RobotController rc) throws GameActionException{
		Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
    	for (RobotType rt : InfoNet.unitInfoMap.keySet()) {
    		int unitCount= InfoNet.countUnits(rc, rt);
    		int broadcastIndex= -1;
    		switch (rt) {
		        case LUMBERJACK:
		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.LUMBERJACK_COUNT);
		        	rc.broadcast(broadcastIndex, unitCount);
		        	break;
				default:
					break;
	        }
    	}
    }
    
}