package AndrewBot;
import battlecode.common.*;
public class Utility{
	
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
	
	//dodges bullets perpendicular
	//returns true if a move to dodge was made
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
}