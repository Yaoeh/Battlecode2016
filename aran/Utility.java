package aran;

import java.util.ArrayList;
import java.util.Random;
import battlecode.common.*;

public class Utility implements GlobalConstants{
	public static Random randall = new Random();
	int[] sensedInfo= {0,0,0,0}; //0: bullets, 1: friends, 2: enemies, 3: trees
	BulletInfo[] nearbyBullets;
	RobotInfo[] nearbyFriends;
	RobotInfo[] nearbyEnemies;
	TreeInfo[] nearbyTrees;

	ArrayList<MapLocation> goalsLoc= new ArrayList<MapLocation>();
	boolean hasPurpose= false; //once you have a purpose, ignore standard procedure
	int broadcastRead= 0;
	
	public void refresh(RobotController rc, int[] profile) throws GameActionException{ //Refresh based on profile
		for (int i = 0; i < sensedInfo.length; i++){
			if (rc.getRoundNum()- sensedInfo[i] > profile[i]){ //if the information is too old
				updateSense(rc, i, rc.getType().sensorRadius/senseRadiusDivisor);
			}
		}
		
		if (broadcastRead!= 0){
			goalsLoc.add(Broadcast.expandInt(broadcastRead));
		}
	}
	
	public void updateSense(RobotController rc, int index, float r) throws GameActionException{
		//enum infoIndex {bullets, friends, enemies, trees};
		if (index== 0){
			senseBullets(rc,r);
		}else if (index == 1){
			senseFriends(rc,r);
		}else if (index == 2){
			senseEnemies(rc,r);
		}else{
			senseTrees(rc,r);
		}
	}
	
	public void senseBullets(RobotController rc, float r){ //Updates the freshness of the information?
		nearbyBullets= rc.senseNearbyBullets(r);
		sensedInfo[bulletIndex]= rc.getRoundNum();
	}
	
	public void senseFriends(RobotController rc, float r){
		nearbyFriends= rc.senseNearbyRobots(r, rc.getTeam());
		sensedInfo[friendIndex]= rc.getRoundNum();
	}
	
	public void senseEnemies(RobotController rc, float r) throws GameActionException{
		nearbyEnemies= rc.senseNearbyRobots(r , rc.getTeam().opponent());
		sensedInfo[enemyIndex]= rc.getRoundNum();		
	}
	
	public void senseTrees(RobotController rc, float r){
		nearbyTrees= rc.senseNearbyTrees(r);
		sensedInfo[treeIndex]= rc.getRoundNum();
	}
		
	public void tryfireSingleShot(RobotController rc) throws GameActionException{

        if (nearbyEnemies.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
            if (rc.canFireSingleShot()) {
                // ...Then fire a bullet in the direction of the enemy.
                //rc.fireSingleShot(rc.getLocation().directionTo(nearbyEnemies[0].location));
            	rc.fireSingleShot(rc.getLocation().directionTo(getMostDamageableBody(nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE).getLocation()));
            }
        }
		
	}
	
	public BodyInfo getClosestBody(BodyInfo[] bodies, MapLocation ref, int maxConsidered){
		BodyInfo closestBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				closestBody= bodies[0];
				float shortestLength= ref.distanceTo(closestBody.getLocation());
				for (int i = 1; i< clamp(bodies.length,0,maxConsidered); i++){
					float candidateDis= ref.distanceTo(bodies[i].getLocation());
					if (candidateDis < shortestLength){
						shortestLength= candidateDis;
						closestBody= bodies[i];
					}
				}
			}
		}
		return closestBody;
	}
	
	public BodyInfo getMostDamageableBody(RobotInfo[] bodies, MapLocation ref, int maxConsidered){
		RobotInfo priorityBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				priorityBody= bodies[0];
				float largestPriority= getDamagePriority(priorityBody);
				for (int i = 1; i< clamp(bodies.length,0 , maxConsidered); i++){
					float candidatePriority = getDamagePriority(bodies[i]);
					if (candidatePriority > largestPriority){
						largestPriority= candidatePriority;
						priorityBody= bodies[i];
					}
				}
			}
		}
		return priorityBody;
	}
		
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
	
	public double getCharisma(RobotInfo ri){
		return ri.health*ri.getType().attackPower;
	}
	
	public double getTastiness(TreeInfo ti, RobotController rc){
		double multiplier= 1;
		if (rc.getType()== RobotType.GARDENER){
			multiplier= treeGardenMultiplier;
		}
		return ti.health*ti.containedBullets* multiplier;
	}
	
	public double getDanger(BulletInfo bi, RobotController rc){
		return bi.damage/rc.getHealth(); 
	}
	
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
	
    public Vector2D dodgeBulleteVector(RobotController rc, MapLocation rcLoc, Vector2D currentLoc, int maxConsidered, float multiplier){
		if (nearbyBullets!= null){
	    	for (int i = 0; i < clamp(maxConsidered,0,nearbyBullets.length); i++){
				BulletInfo bi= nearbyBullets[i];
				double danger= getDanger(bi, rc);
				if (willCollide(bi, rc)){
					double rotated90Rad= bi.dir.radians + ((randall.nextInt(2)-1)* Math.PI); //move perpendicular to the line of fire
					if (Math.abs(bi.location.distanceTo(rcLoc))!= 0){
						double scale = rc.getType().sensorRadius/Math.abs(bi.location.distanceTo(rcLoc)); 
						currentLoc.add(new Vector2D(rotated90Rad).normalize().scale(danger*scale*multiplier));
					}
				}
			}
		}
		return currentLoc;
    }
    
    public Vector2D moveTowardsFriendVector(RobotController rc, MapLocation rcLoc, Vector2D currentLoc, int maxConsidered, float multiplier) throws GameActionException{
		if (nearbyFriends!= null){
	    	double charisma= 0;
			double scale= 0;
	
			for (int i = 0; i <clamp(maxConsidered,0,nearbyFriends.length) ; i++){
				RobotInfo ri= nearbyFriends[i];
				charisma= getCharisma(ri);
				
				//Scale up until you are too close
				scale= rc.getLocation().distanceTo(ri.location)- (ri.getRadius()+rc.getType().bodyRadius)/ rc.getType().sensorRadius;
				
				//if you're too close to your friend
				if (ri.getLocation().distanceTo(rc.getLocation()) - rc.getType().bodyRadius - ri.getRadius() <= 0){ 
					scale*= -2;
				}
	
				currentLoc.add(new Vector2D(ri.location).normalize().scale(charisma*scale*multiplier));
			}
			
			if (scale< 0){
				rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 255, 0, 0);
			}else{
				rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 0, 255, 0);
			}
		}
			
		return currentLoc;
    }
    
    public Vector2D moveAwayFromEnemyVector(RobotController rc, MapLocation rcLoc, Vector2D currentLoc, int maxConsidered, float multiplier) throws GameActionException{
		if (nearbyEnemies!= null){
	    	double charisma= 0;
			double scale= 0;
			for (int i = 0; i <  clamp(maxConsidered, 0,nearbyEnemies.length); i++){
				RobotInfo ri= nearbyEnemies[i];
				charisma= getCharisma(ri);
				scale= - (rc.getLocation().distanceTo(ri.location)/ rc.getType().sensorRadius);
	
				currentLoc.add(new Vector2D(ri.location).normalize().scale(scale*charisma*multiplier));
				
				//ADDITIONS
				if (ri.getType()== RobotType.ARCHON){//let everyone know where enemy archon is at
					rc.broadcast(0, Broadcast.condenseLocation(rc.getLocation()));
				}
//				if (broadcastRead== 0){
//					BodyInfo badBoy = getMostDamageableBody(nearbyEnemies, rcLoc, 3);
//					if (badBoy!= null)
//						rc.broadcast(0, Broadcast.condenseLocation(rc.getLocation()));
//				}
			}
			
			if (scale< 0){
				rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 255, 0, 0);
			}else{
				rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 0, 255, 0);
			}
		}
		
		return currentLoc;
    }
    
    public Vector2D moveTowardsTreeVector(RobotController rc, MapLocation rcLoc, Vector2D currentLoc, int maxConsidered, float multiplier) throws GameActionException{
		if (nearbyTrees!= null){
	    	double charisma= 0;
			double scale= 0;
			for (int i = 0; i < clamp(maxConsidered,0,nearbyTrees.length); i++){
				TreeInfo ti= nearbyTrees[i];
				scale= getTastiness(ti, rc);
				if (ti.team== rc.getTeam().opponent()){ //if enemy tree
					
				}else{
					currentLoc.add(new Vector2D(ti.location).normalize().scale(charisma*scale*multiplier));
				}
			}
			
			if (scale< 0){
				rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 255, 0, 0);
			}else{
				rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 0, 255, 0);
			}
		}
		return currentLoc;
    }
    
    public Vector2D moveTowardsGoal(RobotController rc, MapLocation rcLoc, Vector2D currentLoc, int maxConsidered, float multiplier) throws GameActionException{
		if (goalsLoc!= null){
	    	double charisma= 0;
			double scale= 0;
			Vector2D additionVec= null;
			for (int i = 0; i < clamp(goalsLoc.size(), 0, maxConsidered); i++){
				TreeInfo ti= nearbyTrees[i];
				scale= getTastiness(ti, rc);
				if (ti.team== rc.getTeam().opponent()){ //if enemy tree
					
				}else{
					currentLoc.add(new Vector2D(ti.location).normalize().scale(charisma*scale*multiplier));
				}
				if (scale< 0){
					rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 255, 0, 0);
				}else{
					rc.setIndicatorLine(rcLoc, currentLoc.getMapLoc(), 0, 255, 0);
				}
			}
		}
		return currentLoc;
    }
    
	public void move(RobotController rc, Vector2D[] considered) throws GameActionException{
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
		for (int i = 0; i< considered.length; i++){
			currentLoc.add(considered[i]);
		}

		Direction moveDir= rcLoc.directionTo(new MapLocation((float) (rcLoc.x+currentLoc.x), (float) (rcLoc.y+currentLoc.y)));
		if (rc.canMove(moveDir)){
			rc.move(moveDir);
		}
	}
	
	
	public void tryShakeTree(RobotController rc) throws GameActionException{
		BodyInfo closestBody= getClosestBody(nearbyTrees, rc.getLocation(), Integer.MAX_VALUE);
		if (closestBody!= null){
	        MapLocation possibleTreeLoc= closestBody.getLocation();
	        if (possibleTreeLoc!= null){
		        if (rc.canShake(possibleTreeLoc)){
		        	rc.shake(possibleTreeLoc);
		        }
	        }
		}
	}
	
	public static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}
