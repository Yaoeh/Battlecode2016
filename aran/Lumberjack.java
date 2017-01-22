package aran;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TreeInfo;

public class Lumberjack {
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
            	
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}