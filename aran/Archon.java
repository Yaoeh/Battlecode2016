package aran;

import aran.Constants.InfoEnum;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import sherryy.Gardener;
import sherryy.Lumberjack;
import sherryy.Soldier;
import sherryy.Tank;

public class Archon extends RobotPlayer implements InformationNetwork {
    
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
            	

                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

	@Override
	public void updateOwnInfo(RobotController rc) throws GameActionException {
		Info trackedInfo= unitInfoMap.get(rc.getType());
		int indexOffset= InformationNetwork.getFirstBehindRoundUpdateRobotIndex(rc); //starting index of an not updated robot type
		
		int dangerStatus= 0; //0 normal, 1 danger, 2 stuck, 3 danger and stuck
		if (sensor.nearbyEnemies.length > sensor.nearbyFriends.length){
			dangerStatus+= 1;
		}
		if (sensor.nearbyNeutralTrees.length> 1 || sensor.nearbyEnemyTrees.length> 1){ //ARCHON JUST SENSE SOMEWHERE CLOSE
			dangerStatus+= 2;
		}
		
		for (int i = 0; i < trackedInfo.getChannelSize(); i++){
			InfoEnum currentInfo = trackedInfo.getInfoEnum(i);
			
	        switch (currentInfo) {
		        case UPDATE_TIME:
		        	rc.broadcast(indexOffset+ i, rc.getRoundNum());
		            break;
		        case STATUS:
	            	rc.broadcast(indexOffset+i, dangerStatus);
		            break;
		        case LOCATION:
		        	rc.broadcast(indexOffset+i, InformationNetwork.condenseMapLocation(rc.getLocation()));
		            break;
	        }
		}
	}
}