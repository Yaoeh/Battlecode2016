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
                Util.moveAwayFromMyTrees();
                int prevNumGard = rc.readBroadcast(Constants.Channel.GARDENER_COUNTER);
                rc.broadcast(Constants.Channel.GARDENER_COUNTER, 0);
                Direction dir = Util.randomDirection();
                if (rc.canHireGardener(dir)) {
                    rc.hireGardener(dir);
                    rc.broadcast(Constants.Channel.GARDENER_COUNTER, prevNumGard + 1);
                }

                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}