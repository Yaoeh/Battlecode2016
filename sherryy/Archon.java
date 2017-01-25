package sherryy;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public class Archon extends RobotPlayer {
    
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                Util.dodge();
                Direction dir = Util.randomDirection();
                if (rc.isCircleOccupiedExceptByThisRobot(rc.getLocation(), 4)) {
                    Util.tryMove(dir.opposite());
                    Util.tryMove(dir.rotateLeftDegrees(90));
                    Util.tryMove(dir.rotateRightDegrees(90));
                }
                int prev = rc.readBroadcast(Constants.Channel.GARDENER_COUNTER);
                rc.broadcast(Constants.Channel.GARDENER_COUNTER, 0);
                if (prev < Constants.GARDENER_MAX && rc.canHireGardener(dir)) {
                    rc.hireGardener(dir);
                    Clock.yield();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}