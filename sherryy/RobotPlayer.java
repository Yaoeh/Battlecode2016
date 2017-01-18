package sherryy;
import java.util.Random;

import battlecode.common.BulletInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.TreeInfo;

/**
 * Created by Max_Inspiron15 on 1/10/2017.
 */
public strictfp class RobotPlayer {
    static RobotController rc;
    static Random myRand;
    @SuppressWarnings("unused")
    // Keep broadcast channels
    static int ARCHON_CHANNEL = 0;
    static int GARDENER_CHANNEL = 1;
    static int LUMBERJACK_CHANNEL = 2;
    static int NUMTREE_CHANNEL = 3;

    // Keep important numbers here
    static int GARDENER_MAX = 4;
    static int LUMBERJACK_MAX = 10;

    public static void run(RobotController rc) throws GameActionException {
        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
        myRand = new Random(rc.getID());
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
            case ARCHON:
                runArchon();
                break;
            case GARDENER:
                runGardener();
                break;
            case SOLDIER:
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
        }
    }

    static Direction randomPassiveDir(MapLocation currentLoc) {
        RobotInfo[] bots = rc.senseNearbyRobots();
        float dirRadius = 0;
        for (RobotInfo b : bots) {
            if (b.getTeam() != rc.getTeam()) {
                dirRadius += b.getLocation().directionTo(currentLoc).radians;
            }
        }
        return dirRadius == 0 ? randomDirection() : new Direction(dirRadius);
    }

    static void runArchon() throws GameActionException {
        while (true) {
            try {
                dodge();
                Direction dir = randomPassiveDir(rc.getLocation());
                tryMove(randomPassiveDir(rc.getLocation()));
                
                int prevNumGard = rc.readBroadcast(GARDENER_CHANNEL);
                
                if (prevNumGard < GARDENER_MAX && rc.canHireGardener(dir)) {
                    if (Farming.hire(rc)) {
                        rc.broadcast(GARDENER_CHANNEL, prevNumGard + 1);
                    }
                }
                
                // clear gardener count
                rc.broadcast(GARDENER_CHANNEL, 0);
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    
    static boolean isFreeWithin(int radius) {
        RobotInfo[] bot = rc.senseNearbyRobots(radius);
        TreeInfo[] trees = rc.senseNearbyTrees(radius);
        // magic number 2
        return (bot.length + trees.length < 2);
    }
    

    static void runGardener() throws GameActionException {
        while (true) {
            try {
                Farming.runGardener(rc);
//                int prev = rc.readBroadcast(GARDENER_CHANNEL);
//                rc.broadcast(GARDENER_CHANNEL, prev+1);
//
//                // priority 1: water trees
//                TreeInfo[] trees = rc.senseNearbyTrees();
//                for (TreeInfo tree: trees) {
//                    if (tree.getTeam() == rc.getTeam() && tree.health <= 45) {
//                        if (rc.canWater(tree.ID)) {
//                            rc.water(tree.ID);
//                            System.out.println("round number is: " + rc.getRoundNum());
//                        }
//                    }
//                }
//
//                // priority 2: plant new tree
//                if (rc.getTreeCount() < 10) {
//                    Direction dir = randomDirection();
//                        if (isFreeWithin(3) && rc.canPlantTree(dir)) {
//                            rc.plantTree(dir);
//                            System.out.println("round number is: " + rc.getRoundNum());
//                        } else {
//                            wander();
//                        }
//                }
//                dodge();
//                System.out.println("round number is: " + rc.getRoundNum());
//                // priority 3: spawn army
//                if (rc.getRoundNum() < 500) {
//                    int prevNumGard = rc.readBroadcast(LUMBERJACK_CHANNEL);
//                    if (prevNumGard <= LUMBERJACK_MAX && rc.canBuildRobot(RobotType.LUMBERJACK, randomDirection())) {
//                        rc.buildRobot(RobotType.LUMBERJACK, randomDirection());
//                        rc.broadcast(LUMBERJACK_CHANNEL, prevNumGard + 1);
//                        System.out.println("round number is: " + rc.getRoundNum());
//                    }
//                }
//                else {
//                    if (rc.canBuildRobot(RobotType.SOLDIER, Direction.getEast())) {
//                        rc.buildRobot(RobotType.SOLDIER, Direction.getEast());
//                        System.out.println("round number is: " + rc.getRoundNum());
//                    }
//                }
//
//                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        while (true) {
            try {
                dodge();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam()) {
                        Direction towards = rc.getLocation().directionTo(b.getLocation());
                        rc.fireSingleShot(towards);
                        break;
                    }
                }
                wander();
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
    static void runLumberjack() throws GameActionException {
        while (true) {
            try {
                dodge();
                RobotInfo[] bots = rc.senseNearbyRobots();
                for (RobotInfo b : bots) {
                    if (b.getTeam() != rc.getTeam() && rc.canStrike()) {
                        rc.strike();
                        Direction chase = rc.getLocation().directionTo(b.getLocation());
                        tryMove(chase);
                        break;
                    }
                }
                TreeInfo[] trees = rc.senseNearbyTrees();
                for (TreeInfo t : trees) {
                    if (t.getTeam() != rc.getTeam() && rc.canChop(t.getLocation())) {
                        rc.chop(t.getLocation());
                        break;
                    }
                }
                if (! rc.hasAttacked()) {
                    wander();
                }
                Clock.yield();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    public static void wander() throws GameActionException {
        Direction dir = randomDirection();
        tryMove(dir);
    }


    public static Direction randomDirection() {
        return(new Direction(myRand.nextFloat()*2*(float)Math.PI));
    }

    static boolean willCollideWithMe(BulletInfo bullet) {
        MapLocation myLocation = rc.getLocation();

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI / 2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float) Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }

    /**
     * Attempts to move in a given direction, while avoiding small obstacles directly in the path.
     *
     * @param dir The intended direction of movement
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir) throws GameActionException {
        return tryMove(dir,20,3);
    }
    
    static boolean tryHireGardener(Direction dir) throws GameActionException {
        return tryHireGardener(dir, 20, 3);
    }
    
    static boolean tryHireGardener(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        // First, try intended direction
        if (rc.canHireGardener(dir)) {
            rc.hireGardener(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(!rc.canHireGardener(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.hireGardener(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canHireGardener(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.hireGardener(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
    
    static boolean tryBuildRobot(RobotType type, Direction dir) throws GameActionException {
        return tryBuildRobot(type, dir, 20, 3);
    }
    
    static boolean tryBuildRobot(RobotType type, Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {
        // First, try intended direction
        if (rc.canBuildRobot(type, dir)) {
            rc.buildRobot(type, dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(!rc.canBuildRobot(type, dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.buildRobot(type, dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canBuildRobot(type, dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.buildRobot(type, dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }


    /**
     * Attempts to move in a given direction, while avoiding small obstacles direction in the path.
     *
     * @param dir The intended direction of movement
     * @param degreeOffset Spacing between checked directions (degrees)
     * @param checksPerSide Number of extra directions checked on each side, if intended direction was unavailable
     * @return true if a move was performed
     * @throws GameActionException
     */
    static boolean tryMove(Direction dir, float degreeOffset, int checksPerSide) throws GameActionException {

        // First, try intended direction
        if (!rc.hasMoved() && rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        //boolean moved = rc.hasMoved();
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(!rc.hasMoved() && rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(! rc.hasMoved() && rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }

    static boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }

    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }

    }
}
