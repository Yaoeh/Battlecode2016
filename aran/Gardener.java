package aran;

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
        // priority 1: water trees
        TreeInfo[] trees = rc.senseNearbyTrees();
        for (TreeInfo tree: trees) {
            if (tree.getTeam() == rc.getTeam()) {
                if (rc.canWater(tree.ID)) {
                    rc.water(tree.ID);
                    Clock.yield();
                }
            }
        }
        
        // priority 2: build trees
        if (trees.length == 0) {
            // no tree around. start a new cluster
            for (aran.Constants.SixAngle ra : Constants.SixAngle.values()) {
                Direction d = new Direction(ra.getRadians());
                if (rc.canPlantTree(d)) {
                    rc.plantTree(d);
                    Clock.yield();
                }
            }
        } else {
            if (rc.senseNearbyTrees(2).length == 0) {
                // move towards cluster
                for (TreeInfo tr: trees) {
                    if (tr.team == rc.getTeam()) {
                        for (aran.Constants.SixAngle ra : Constants.SixAngle.values()) {
                            if (rc.canMove(tr.location.add(new Direction(ra.getRadians()), 2))) {
                                rc.move(tr.location.add(new Direction(ra.getRadians()), 2));
                                if (rc.getLocation().distanceTo(tr.location.add(new Direction(ra.getRadians()), 2)) < 0.1) {
                                    return;
                                }
                                Clock.yield();
                            }
                        }
                    }
                }
            } else {
                // continue planting trees in a circle
                for (aran.Constants.SixAngle ra : Constants.SixAngle.values()) {
                    Direction d = new Direction(ra.getRadians());
                    if (rc.canPlantTree(d)) {
                        rc.plantTree(d);
                        Clock.yield();
                    }
                }
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