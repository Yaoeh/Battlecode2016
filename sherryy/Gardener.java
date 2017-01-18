package sherryy;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
import sherryy.Constants.SixAngle;

public class Gardener extends RobotPlayer {
    
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                int prev = rc.readBroadcast(Constants.Channel.GARDENER_COUNTER);
                rc.broadcast(Constants.Channel.GARDENER_COUNTER, prev+1);
                int treeOnly = (prev + 1) % 2;
                if (treeOnly == 1) {
                    runTreeGardener();
                }
                else {
                    runProduceGardener();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    public static void runTreeGardener() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees(2, rc.getTeam());
        if (trees.length > 0) {
            // water trees
            for (TreeInfo tree: trees) {
                
                if (rc.canWater(tree.ID)) {
                    System.out.println("Trying to water");
                    rc.water(tree.ID);
                }
            }
        }

        for (SixAngle ra : Constants.SixAngle.values()) {
            Direction d = new Direction(ra.getRadians());
            if (rc.canPlantTree(d)) {
                rc.plantTree(d);
                Clock.yield();
            }
        }
    }
    
    public static void runProduceGardener() throws GameActionException {
        Util.dodge();
        if (rc.getRoundNum() < 500) {
            int prevNumLj = rc.readBroadcast(Constants.Channel.LUMBERJACK_COUNTER);
            if (prevNumLj <= Constants.LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, Util.randomDirection())) {
                rc.buildRobot(RobotType.LUMBERJACK, Util.randomDirection());
                rc.broadcast(Constants.Channel.LUMBERJACK_COUNTER, prevNumLj + 1);
            }
        }
        else {
            if (rc.canBuildRobot(RobotType.SOLDIER, Direction.getEast())) {
                rc.buildRobot(RobotType.SOLDIER, Direction.getEast());
            }
        }
    }
    
    public static TreeInfo getClosestTree(TreeInfo[] trees, RobotController rc) {
        TreeInfo closest = null;
        float distMin = Math.max(GameConstants.MAP_MAX_HEIGHT, GameConstants.MAP_MAX_WIDTH);
        MapLocation tempLoc = rc.getLocation();
        for (TreeInfo t : trees) {
            if (tempLoc.distanceTo(t.location) < distMin) {
                closest = t;
                distMin = tempLoc.distanceTo(t.location);
            }
        }
        return closest;
    }
}