package aran_v2;

import java.util.ArrayList;

import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TreeInfo;

public class Scout_test extends RobotPlayer {
    public static void run(RobotController rc) throws GameActionException {
        //sensor.goalLoc= rc.getInitialArchonLocations(rc.getTeam().opponent())[0];
    	while (true) {
            try {
            	Util.tryMove(Util.randomDirection());
            	checkOnMap(rc);

                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void checkOnMap(RobotController rc) throws GameActionException{
		for (int d = 0; d < 360; d+= 90){
			Direction dir= new Direction((float) Math.toRadians(d));
			MapLocation checkLoc= rc.getLocation().add(dir, rc.getType().sensorRadius-1);
			if (rc.onTheMap(checkLoc)){
				//rc.setIndicatorDot(checkLoc, 0, 255, 0);
				rc.setIndicatorLine(rc.getLocation(), (rc.getLocation().add(dir, rc.getType().sensorRadius)), 0, 255, 0);
			}else{
				rc.setIndicatorLine(rc.getLocation(), (rc.getLocation().add(dir, rc.getType().sensorRadius)), 255, 0, 0);
				//read: if loc.x < min x, replace?
				//read if loc.y < min y, replace?
				//read if loc.x > max x, replace?
				//read if loc.y > max y, replace?
			}

		} 
    }
    
    public static void drawSensorCircle(RobotController rc, int r, int g, int b) throws GameActionException{
		for (int d = 0; d < 360; d+= 30){
			Direction dir= new Direction((float) Math.toRadians(d));
			rc.setIndicatorLine(rc.getLocation(), (rc.getLocation().add(dir, rc.getType().sensorRadius)), r, g, b);
		}       
    }
}
