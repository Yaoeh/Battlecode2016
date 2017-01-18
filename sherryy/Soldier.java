package sherryy;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;

public class Soldier extends RobotPlayer{
    
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                Util.dodge();
                Util.moveAwayFromMyTrees();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        Direction towards = rc.getLocation().directionTo(b.getLocation());
                        rc.fireSingleShot(towards);
                        break;
                    }
                }
                Util.wander();
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}