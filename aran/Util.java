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
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
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
    
	public static Direction dodgeOneBullet(BulletInfo bulletinfo, MapLocation loc){
		Direction dir1 = new Direction(bulletinfo.location.x - loc.x, bulletinfo.location.y - loc.y);
		dir1 = dir1.rotateRightDegrees(90.0f);
		return dir1;
    }
	/**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    public static boolean willCollideWithMe(RobotController rc, MapLocation myLocation, BulletInfo bullet) {
        

        // Get relevant bullet information
        Direction propagationDirection = bullet.dir;
        MapLocation bulletLocation = bullet.location;

        // Calculate bullet relations to this robot
        Direction directionToRobot = bulletLocation.directionTo(myLocation);
        float distToRobot = bulletLocation.distanceTo(myLocation);
        float theta = propagationDirection.radiansBetween(directionToRobot);

        // If theta > 90 degrees, then the bullet is traveling away from us and we can break early
        if (Math.abs(theta) > Math.PI/2) {
            return false;
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return (perpendicularDist <= rc.getType().bodyRadius);
    }
    
	public static boolean dodgeBullets(RobotController rc, MapLocation myLocation) throws GameActionException{
		BulletInfo[] bulletInfo = rc.senseNearbyBullets();
    	for(int i=0;i<bulletInfo.length;i++){
    		if(willCollideWithMe(rc, myLocation, bulletInfo[i])){
    			Direction tdir = dodgeOneBullet(bulletInfo[i], myLocation);
    			if(rc.canMove(tdir))
    			{
    				rc.move(tdir);
    				return true;
    			}
    			
    		}
    	}
    	return false;
	}
    
    static void tryShoot() throws GameActionException{
    	if(rc.getType().canAttack() && sensor.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) Value.getHighestPriorityBody(rc, sensor.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		sensor.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
    }
    
    static void dodge() throws GameActionException {
        BulletInfo[] bullets = rc.senseNearbyBullets();
        for (BulletInfo bi : bullets) {
            if (willCollideWithMe(bi)) {
                trySidestep(bi);
            }
        }
    }
    
    static boolean isStuck() throws GameActionException{
    	boolean answer= true;
    	for (int i = 0; i < Constants.COORDINALDIRS.length; i++){
    		if (rc.canMove(rc.getLocation().add(Constants.COORDINALDIRS[i], rc.getType().bodyRadius))){
    			rc.setIndicatorDot(rc.getLocation(), 150, 150, 0);
    			answer= false;
    			break;
    		}
    	}
    	
    	return answer;
    }

	//Looks for lowest health tree and walks toward and waters it
	//Returns true if there are trees available
	public static boolean waterLowestHealthTree(RobotController rc, Team myTeam) throws GameActionException{
		TreeInfo[] trees = rc.senseNearbyTrees(-1, myTeam);
    	//get tree with lowest health and water
    	if(trees.length>0)
    	{
        	float minHealth = 9999.0f;
        	TreeInfo deadTree = trees[0];
        	for(int i=0;i<trees.length;i++){
        		if(trees[i].health< minHealth){
        			minHealth = trees[i].health;
        			deadTree = trees[i];
        		}
        	}
        	if(rc.canWater(deadTree.ID)){
        		rc.water(deadTree.ID);
        	}
        	else
        	{
        		int count = 0;
        		Direction dir = rc.getLocation().directionTo(deadTree.location);
        		while(!rc.canMove(dir) && count<24){
            		dir = dir.rotateLeftDegrees(15.0f);
            		count+=1;
            	}
                rc.move(dir);
        	}
        	return true;
    	}
    	return false;
	}
	public static boolean waterLowestHealthTreeWithoutMoving(RobotController rc, Team myTeam) throws GameActionException{
		TreeInfo[] trees = rc.senseNearbyTrees(2, myTeam);
    	//get tree with lowest health and water
    	if(trees.length>0)
    	{
        	float minHealth = 9999.0f;
        	TreeInfo deadTree = trees[0];
        	for(int i=0;i<trees.length;i++){
        		if(trees[i].health< minHealth){
        			minHealth = trees[i].health;
        			deadTree = trees[i];
        		}
        	}
        	if(rc.canWater(deadTree.ID)){
        		rc.water(deadTree.ID);
        	}
        	return true;
    	}
    	return false;
	}
	public static Direction getDodgeBulletDirection(RobotController rc, MapLocation myLocation, Direction dir) throws GameActionException
	{
		//dodge any bullets
    	BulletInfo[] bulletInfo = rc.senseNearbyBullets();
    	for(int i=0;i<bulletInfo.length;i++){
    		if(willCollideWithMe(rc, myLocation, bulletInfo[i])){
    			dir = dodgeOneBullet(bulletInfo[i], myLocation);
    			break;
    		}
    	}
    	return dir;
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
    
    public static boolean tryBuildRobot(RobotType rt) throws GameActionException{
    	if (rc.getType().equals(RobotType.GARDENER)){
    		for (int i = 0; i < Constants.SixAngle.values().length; i++){
    			Direction tryBuildDir= new Direction(Constants.SixAngle.values()[i].getRadians());
    			if (rc.isBuildReady() && rc.canBuildRobot(rt, tryBuildDir)){
    				rc.buildRobot(rt, tryBuildDir);
    				return true;
    			}
    		}
    	}
    	return false;
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
	
	public static float[] shuffleFloatArray(float[] ar){
	    for (int i = ar.length - 1; i > 0; i--)
	    {
	      int index = myRand.nextInt(i + 1);
	      // Simple swap
	      float a = ar[index];
	      ar[index] = ar[i];
	      ar[i] = a;
	    }
	    return ar;
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