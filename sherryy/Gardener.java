package sherryy;

import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;
import sherryy.Constants.SixAngle;

public class Gardener extends RobotPlayer {
    
    static int gardenerID;
    
    public static void run(RobotController rc) throws GameActionException {
        while (true) {
            try {
                System.out.println(rc.getID());
                int prev = rc.readBroadcast(Constants.Channel.GARDENER_COUNTER);
                gardenerID = prev + 1;
                rc.broadcast(Constants.Channel.GARDENER_COUNTER, prev+1);
                if (rc.getID() % 2 == 1) {
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
      // #1 water
      TreeInfo[] trees = rc.senseNearbyTrees(2);

      if (trees.length == 0) {
          RobotInfo[] bots = rc.senseNearbyRobots(3);
          if (bots.length > 0) {
              for (RobotInfo bot : bots) {
                  Util.tryMove(bot.getLocation().directionTo(curLoc));
                  Util.tryMove(bot.getLocation().directionTo(curLoc).rotateLeftDegrees(90));
                  Util.tryMove(bot.getLocation().directionTo(curLoc).rotateRightDegrees(90));
              }
          } else {
              plantCircleTrees();
              Clock.yield();
          }

      } else {
          for (TreeInfo tree: trees) {
              if (tree.getTeam() == team && rc.canWater(tree.ID)) {
                  System.out.println("watering");
                  rc.water(tree.ID);
                  Clock.yield();
              }
          }
          plantCircleTrees();
      }
    }
    
    private static void plantCircleTrees() throws GameActionException {
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
        RobotInfo[] bots = rc.senseNearbyRobots(3);
        if (bots.length > 0) {
            for (RobotInfo bot : bots) {
                Util.tryMove(bot.getLocation().directionTo(curLoc));
                Util.tryMove(bot.getLocation().directionTo(curLoc).rotateLeftDegrees(90));
                Util.tryMove(bot.getLocation().directionTo(curLoc).rotateRightDegrees(90));
            }
        } else {
            Direction dir = Util.randomDirection();
            if (rc.getRoundNum() < 500) {
                int prevNumLj = rc.readBroadcast(Constants.Channel.LUMBERJACK_COUNTER);
                if (prevNumLj <= Constants.LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, dir)) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                    rc.broadcast(Constants.Channel.LUMBERJACK_COUNTER, prevNumLj + 1);
                    Clock.yield();
                    Util.tryMove(dir.opposite());
                }
            }
            else {
                if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                    Clock.yield();
                    Util.tryMove(dir.opposite());
                }
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