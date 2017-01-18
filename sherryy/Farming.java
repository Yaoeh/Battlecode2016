package sherryy;

import java.util.Random;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

public class Farming {
    
    static Random myRand = new Random();
    static int TREE_GARDENER_RATE = 70;
    static int GARDENER_SIGHT_RADIUS = 7;
    static Direction[] direction6 = new Direction[] {
            new Direction(0),
            new Direction((float) (Math.PI / 3)),
            new Direction((float) (2 * Math.PI / 3)),
            new Direction((float) (Math.PI)),
            new Direction((float) (4 * Math.PI / 3)),
            new Direction((float) (5 * Math.PI / 3)),
            };
    static MapLocation[] deltaLocs = new MapLocation[] {
            new MapLocation(3, (float)Math.sqrt(3)),
            new MapLocation(-3, (float)Math.sqrt(3)),
            new MapLocation(-3, -(float)Math.sqrt(3)),
            new MapLocation(3, -(float)Math.sqrt(3)),
            new MapLocation(0, (float)(2*Math.sqrt(3))),
            new MapLocation(0, (float)(-2*Math.sqrt(3)))
            };
    
    static int TREE_ONLY_CHANNEL = 10;
    static int NEXT_TREEBOT_X_CHANNEL = 11;
    static int NEXT_TREEBOT_Y_CHANNEL = 12;
    
    public static boolean hire(RobotController rc) throws GameActionException {
        Direction dir = RobotPlayer.randomDirection();
        if (rc.canHireGardener(dir)) {
            rc.hireGardener(dir);
            if (myRand.nextInt(100) < TREE_GARDENER_RATE) {
                System.out.println("broadcasting 1");
                RobotInfo[] bots= rc.senseNearbyRobots(3);
                for (RobotInfo bot:bots) {
                    rc.broadcast(bot.getID() % GameConstants.BROADCAST_MAX_CHANNELS, 1);
                }
            }
            return true;
        }
        return false;
    }
    
    public static void runGardener(RobotController rc) throws GameActionException {
        int treeOnly = rc.readBroadcast(rc.getID() % GameConstants.BROADCAST_MAX_CHANNELS);
        // reset channel
        System.out.println(treeOnly);
        if (treeOnly == 1) {
            runTreeGardener(rc);
        }
        else {
            runProduceGardener(rc);
        }
    }
    
    public static void runTreeGardener(RobotController rc) throws GameActionException {
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
            for (Direction d : direction6) {
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
                        for (Direction d : direction6) {
                            if (rc.canMove(tr.location.add(d, 2))) {
                                rc.move(tr.location.add(d, 2));
                                if (rc.getLocation().distanceTo(tr.location.add(d, 2)) < 0.1) {
                                    return;
                                }
                                Clock.yield();
                            }
                        }
                    }
                }
            } else {
                // continue planting trees in a circle
                for (Direction d : direction6) {
                    if (rc.canPlantTree(d)) {
                        rc.plantTree(d);
                        Clock.yield();
                    }
                }
            }
        }
    }
    
    public static void runProduceGardener(RobotController rc) throws GameActionException {
        RobotPlayer.dodge();
        if (rc.getRoundNum() < 500) {
            int prevNumGard = rc.readBroadcast(RobotPlayer.LUMBERJACK_CHANNEL);
            if (prevNumGard <= RobotPlayer.LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, RobotPlayer.randomDirection())) {
                rc.buildRobot(RobotType.LUMBERJACK, RobotPlayer.randomDirection());
                rc.broadcast(RobotPlayer.LUMBERJACK_CHANNEL, prevNumGard + 1);
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