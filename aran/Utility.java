package aran;

import java.util.Random;

import battlecode.common.*;


public class Utility {
	public static Random randall = new Random();
	public float getDamagePriority(RobotInfo ri){ //higher number, greater the priority
		//Greater the cost more valuable the damage
		//Greater the health, lower the priority
		//Greater the attack power, greater the priority
		
		return (float) (ri.getType().bulletCost- ri.getHealth()+ ri.getType().attackPower);
	}
	
	public float getHitChance(RobotController rc, RobotInfo ri){
		//further away, the lower the chance
		//slower the stride radius, larger the chance
		//closer the target larger the chance
		return rc.getType().bulletSpeed/(rc.getLocation().distanceTo(ri.location)+ri.getType().strideRadius);
	}
	
	public void attack(RobotController rc){
		if (rc.getType().canAttack()){
			
		}
	}
	
	public double getCharisma(RobotInfo ri){
		return ri.health*ri.getType().attackPower;
	}
	
	public double getTastiness(TreeInfo ti){
		return ti.health*ti.containedBullets;
	}
	
	public double getDanger(BulletInfo bi, RobotController rc){
		return bi.damage/rc.getHealth(); 
	}
	
    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
    static boolean willCollide(BulletInfo bullet, RobotController rc) {
        MapLocation myLocation = rc.getLocation();

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
	
	
	public void move(RobotController rc) throws GameActionException{
		//Each character is an island that attracts and detracts
		//Each friend attracts based on Charisma, which equals health*attackPower
		//Each tree attracts based on tastiness, which equals health*bulletsContained
		//Each enemy detracts based on Charisma, which equals health* attackPower
		//Each bullet detracts based on danger, which equals bulletDamage/CharacterHealth applied in path beyond rc's move range; 
		
		//Extra Points
		//when danger > 0.8, danger can override all other movements
		//Attraction is scaled by distance up to the sense range, basically math.clamp(0,distance,senseRange)/senseRange
		MapLocation rcLoc= rc.getLocation();
		Vector2D currentLoc= new Vector2D(rcLoc);
		
		BulletInfo[] nearbyBullets= rc.senseNearbyBullets();
		boolean dangerOverride= false;
		for (int i = 0; i < nearbyBullets.length; i++){
			BulletInfo bi= nearbyBullets[i];
			double danger= getDanger(bi, rc);
			if (willCollide(bi, rc)){
				double rotated90Rad= bi.dir.radians + ((randall.nextInt(2)-1)* Math.PI); //move perpendicular to the line of fire
				if (Math.abs(bi.location.distanceTo(rcLoc))!= 0){
					double scale = rc.getType().sensorRadius/Math.abs(bi.location.distanceTo(rcLoc)); 
					currentLoc.add(new Vector2D(rotated90Rad).normalize().scale(scale));
					if (danger> 0.8){
						dangerOverride= true;
					}
				}
			}
		}
		
		if (!dangerOverride){
			RobotInfo[] nearbyRobots= rc.senseNearbyRobots();
	
			TreeInfo[] nearbyTrees= rc.senseNearbyTrees();

			
			
			for (int i = 0; i < nearbyRobots.length; i++){
				RobotInfo ri= nearbyRobots[i];
				double charisma= getCharisma(ri);
				double scale= rc.getLocation().distanceTo(ri.location)/ rc.getType().sensorRadius;
				
				if (ri.team!= rc.getTeam()){ //If enemy
					scale*= -1;
				}
				currentLoc.add(new Vector2D(ri.location).normalize().scale(scale));
			}
			
	
			
			for (int i = 0; i < nearbyTrees.length; i++){
				TreeInfo ti= nearbyTrees[i];
				double scale= getTastiness(ti);
				if (ti.team!= rc.getTeam()){ //if enemy tree
					
				}else{
					currentLoc.add(new Vector2D(ti.location).normalize().scale(scale));
				}
			}
		}
		
		Vector2D moveLoc= currentLoc.normalize();
		Direction moveDir= rcLoc.directionTo(new MapLocation((float) (rcLoc.x+moveLoc.x), (float) (rcLoc.y+moveLoc.y)));
		if (rc.canMove(moveDir)){
			rc.move(moveDir);
		}
	}
	
	public static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}
