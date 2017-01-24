package aran;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import java.util.Set;

import battlecode.common.BulletInfo;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

class Util extends RobotPlayer{
    static Random myRand = new Random(rc.getID());
    
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
    
    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }
    }

    static boolean trySidestep(BulletInfo bullet) throws GameActionException{

        Direction towards = bullet.getDir();
        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);

        return(tryMove(towards.rotateRightDegrees(90)) || tryMove(towards.rotateLeftDegrees(90)));
    }
    
    public static void wander() throws GameActionException {
        System.out.println("try to move");
        Direction dir = randomDirection();
        tryMove(dir);
    }
    
    public static void moveAwayFromMyTrees() throws GameActionException {
        TreeInfo[] trees = rc.senseNearbyTrees();
        float ra = 0;
        MapLocation rcLoc = rc.getLocation();
        Team myTeam = rc.getTeam();
        for (TreeInfo t : trees) {
            if (t.getTeam() == myTeam) {
                ra += t.location.directionTo(rc.getLocation()).radians;
            }
        }
        Util.tryMove(new Direction(ra));
    }
    
    public static Vector2D getMoveVec(RobotController rc, Vector2D[] influences){
    	Vector2D neturalVec= new Vector2D(rc.getLocation());
    	for (int i = 0; i< influences.length; i++){
    		neturalVec.add(influences[i]);
    	}
    	
    	return neturalVec;
    }
    
	public static void removeMapLocDuplicate(ArrayList<MapLocation> a){
		Set<MapLocation> s = new HashSet<MapLocation>(a);
		a.clear();
		a.addAll(s);
	}
	
	public static void shuffleDirArray(Direction[] ar){
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = myRand.nextInt(i + 1);
	      // Simple swap
	      Direction a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	}
	
    public static void drawSensorCircle(RobotController rc, int r, int g, int b) throws GameActionException{
		for (int d = 0; d < 360; d+= 30){
			Direction dir= new Direction((float) Math.toRadians(d));
			rc.setIndicatorLine(rc.getLocation(), (rc.getLocation().add(dir, rc.getType().sensorRadius)), r, g, b);
		}       
    }
	
	public static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}