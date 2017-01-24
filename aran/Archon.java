package aran;

import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

public class Archon extends RobotPlayer {
	static boolean firstArchon= false;
	
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
            	if (firstArchon){
            		if (rc.getRoundNum()%Constants.UNIT_COUNT_UPDATE_MOD== 0){
            			updateUnitCounts(rc);
            		}
                	//System.out.println("First Archon!");
            	}else{
            		//System.out.println("not first archon!");
            	}
            	sensor.senseFriends(rc);
            	sensor.senseEnemies(rc);
            	sensor.senseTrees(rc);
            	updateOwnInfo(rc);
            	spawn(rc);
            	move(rc);
            	targetNearbyTree(rc);
            	
                Clock.yield();
            } catch (Exception e) {
            	System.out.println("Archon Error!");
                e.printStackTrace();
            }
        }
    }
    
    public static void targetNearbyTree(RobotController rc) throws GameActionException{
    	sensor.senseTrees(rc, rc.getType().bodyRadius);
    	for (int i = 0; i < sensor.nearbyNeutralTrees.length; i++){
    		Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.BLACKLIST);
    		int indexOffset= InfoNet.getClosestAddInfoTargetIndex(rc, AddInfo.BLACKLIST);
    		
    		for (int j = 0; j< trackedInfo.reservedChannels.size(); j++){
    			InfoEnum info= trackedInfo.reservedChannels.get(j);
				switch (info) {
					case UPDATE_TIME:
						rc.broadcast(indexOffset+j, rc.getRoundNum());
						break;
					case LOCATION:
						rc.broadcast(indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()));
					case PRIORITY:
						rc.broadcast(indexOffset+i, (int) Value.getTastiness(sensor.nearbyNeutralTrees[i], rc));
					default:
						break;
						
				}
    		}
    	}
    	
    	for (int i = 0; i < sensor.nearbyEnemyTrees.length; i++){
    		Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.BLACKLIST);
    		int indexOffset= InfoNet.getClosestAddInfoTargetIndex(rc, AddInfo.BLACKLIST);
    		
    		for (int j = 0; j< trackedInfo.reservedChannels.size(); j++){
    			InfoEnum info= trackedInfo.reservedChannels.get(j);
				switch (info) {
					case UPDATE_TIME:
						rc.broadcast(indexOffset+j, rc.getRoundNum());
						break;
					case LOCATION:
						rc.broadcast(indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()));
					case PRIORITY:
						rc.broadcast(indexOffset+i, (int) Value.getTastiness(sensor.nearbyEnemyTrees[i], rc));
					default:
						break;
						
				}
    		}
    	}
    }
    
    public static void spawn(RobotController rc) throws GameActionException{
    	Direction dir = Util.randomDirection();
    	Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
    	int gardenerCountIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.GARDENER_COUNT);
    	
    	if (rc.readBroadcast(gardenerCountIndex) < InfoNet.NUM_GARDENERS_TRACKED){
            if (rc.canHireGardener(dir) && rc.getTeamBullets() > RobotType.GARDENER.bulletCost) {
                rc.hireGardener(dir);
            }
    	}
    }
    
    public static void move(RobotController rc) throws GameActionException{
    	if (!rc.hasMoved()){
            Vector2D dangerVec= sensor.moveAwayFromBulletsVector(rc, Integer.MAX_VALUE, 10);
            Vector2D friendVec= sensor.moveTowardsFriendVector(rc, Integer.MAX_VALUE, 2, 0.1f, Constants.ignoreArchonGardener);
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
    
    public static void updateUnitCounts(RobotController rc) throws GameActionException{
		Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
    	for (RobotType rt : InfoNet.unitInfoMap.keySet()) {
    		int unitCount= InfoNet.countUnits(rc, rt);
    		int broadcastIndex= -1;
    		switch (rt) {
		        case ARCHON:
		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.ARCHON_COUNT);
		    		//rc.broadcast(broadcastIndex, unitCount);
		            broadcastPrint(rc, broadcastIndex, unitCount, "ArchonCount");
		        	break;
		        case GARDENER:
		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.GARDENER_COUNT);
		    		//rc.broadcast(broadcastIndex, unitCount);
		        	broadcastPrint(rc, broadcastIndex, unitCount, "GardenerCount");
		        	break;
		        case SOLDIER:
		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.SOLDIER_COUNT);
		    		//rc.broadcast(broadcastIndex, unitCount);
		        	broadcastPrint(rc, broadcastIndex, unitCount, "SoldierCount");
		        	break;
//		        case SCOUT:
//		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.SCOUT_COUNT);
//		    		//rc.broadcast(broadcastIndex, unitCount);
//		        	broadcastPrint(rc, broadcastIndex, unitCount, "ScoutCount");
//		        	break;
		        case TANK:
		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.TANK_COUNT);
		    		//rc.broadcast(broadcastIndex, unitCount);
		        	broadcastPrint(rc, broadcastIndex, unitCount, "TankCount");
		        	break;
		        case LUMBERJACK:
		        	broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.LUMBERJACK_COUNT);
		    		//rc.broadcast(broadcastIndex, unitCount);
		        	broadcastPrint(rc, broadcastIndex, unitCount, "LumberJackCount");
		        	break;
				default:
					break;
	        }
    	}
    	int broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.UPDATE_TIME); //knowing the Unit Count Info only has one unit of itself tracked
    	rc.broadcast(broadcastIndex, rc.getRoundNum());
    }

	public static void updateOwnInfo (RobotController rc) throws GameActionException {
		Info trackedInfo= InfoNet.unitInfoMap.get(rc.getType());
		int indexOffset= InfoNet.getFirstBehindRoundUpdateRobotIndex(rc); //starting index of an not updated robot type
		
		if (indexOffset!= Integer.MIN_VALUE){
			if (indexOffset== InfoNet.unitInfoMap.get(rc.getType()).getStartIndex()){
				firstArchon= true; //first archon
			}
			
			int dangerStatus= 0; //0 normal, 1 danger, 2 stuck, 3 danger and stuck
			if (sensor.nearbyEnemies.length > sensor.nearbyFriends.length){
				dangerStatus+= 1;
			}
			if (sensor.nearbyNeutralTrees.length> 1 || sensor.nearbyEnemyTrees.length> 1){ //ARCHON JUST SENSE SOMEWHERE CLOSE
				dangerStatus+= 2;
			}
			
			for (int i = 0; i < trackedInfo.reservedChannels.size(); i++){
				InfoEnum state= trackedInfo.getInfoEnum(i);
				
				switch (state) {
					case UPDATE_TIME:
						broadcastPrint(rc,indexOffset+ i, rc.getRoundNum(), "time");
						break;
					case STATUS:
						broadcastPrint(rc, indexOffset+i, dangerStatus, "stat");
					case LOCATION:
						broadcastPrint(rc, indexOffset+i, InfoNet.condenseMapLocation(rc.getLocation()), "loc");
					case ID:
						broadcastPrint(rc, indexOffset+i, rc.getID());
					default:
						break;
						
				}
			}
		}else{
			System.out.println("Index offset returning a failed number: " + indexOffset);
		}
	}
}