package aran;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;

public class Broadcast { //Not tested hue
	//Map is 30x30 to 100x100, top left corner is random range[0,500]

	//code 0 = loneliness call
	//code 1 = reinforcement call etc.
	
//	public void broadcastLocation(RobotController rc, int channel) throws GameActionException{
//		if (rc.readBroadcast(channel)== 0){ //if zero not taken
//			if (condenseLocation(rc.getLocation())== 0){ //if zero is the answer
//				rc.broadcast(0, Integer.MAX_VALUE);
//			}else{
//				rc.broadcast(channel, condenseLocation(rc.getLocation()));
//			}
//		}else{
//			broadcastLocation(rc, channel+1);
//		}
//	}
//	
//	public static int condenseLocation(MapLocation loc){ //close enough is good
//		return (int) (loc.x*GameConstants.MAP_MAX_HEIGHT+loc.y);
//	}
//	
//	public static MapLocation expandInt(int coInt){
//		if (coInt!= Integer.MAX_VALUE){
//			return new MapLocation(coInt/GameConstants.MAP_MAX_HEIGHT, coInt%GameConstants.MAP_MAX_HEIGHT);
//		}else{
//			return new MapLocation(0,0); //if the whole thing is zero, max int is used instead
//		}
//	}
}
