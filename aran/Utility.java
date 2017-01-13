package aran;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Random;
import battlecode.common.*;

public class Utility implements GlobalConstants{
	public static Random randall = new Random();
	int[] sensedInfo= {0,0,0,0}; //0: bullets, 1: friends, 2: enemies, 3: trees
	BulletInfo[] nearbyBullets;
	RobotInfo[] nearbyFriends;
	RobotInfo[] nearbyEnemies;
	TreeInfo[] nearbyTrees;
	ArrayList<MapLocation> goalLocs = new ArrayList<MapLocation>();

	boolean hasPurpose= false; //once you have a purpose, ignore standard procedure
//	int broadcastRead= 0;
	
	public void broadCastGoal_v1(RobotController rc) throws GameActionException{
		if (rc.getType()== RobotType.ARCHON){
			rc.broadcast(1, (int) rc.getLocation().x);
			rc.broadcast(2, (int) rc.getLocation().y);
			rc.broadcast(3, rc.getRoundNum());
		}
	}
	
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
    	
	public void setInitialEnemyArchonAsGoal(RobotController rc){
		MapLocation[] initArchonLocs= rc.getInitialArchonLocations(rc.getTeam().opponent());
		for (int i = 0; i< initArchonLocs.length; i++){
			goalLocs.add(initArchonLocs[i]);
		}
	}
	
	public Direction getDirToInitialArchon(RobotController rc){
		return rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
	}
	
	public void refresh(RobotController rc, int[] profile, float[] rads) throws GameActionException{ 
		//Refresh based on profile; i.e. does sense by how long until the next sense
		//Rads determines how far for each corresponding sense type to sense in
		
		for (int i = 0; i < sensedInfo.length; i++){
			if (rc.getRoundNum()- sensedInfo[i] > profile[i]){ //if the information is too old
				if (rads!=null && rads.length== 4){
					updateSense(rc, i, clamp(rads[i],0, rc.getType().sensorRadius));
				}else {
					updateSense(rc, i, rc.getType().sensorRadius);
				}
			}
		}
		
//		if (broadcastRead!= 0){
//			goalsLoc.add(Broadcast.expandInt(broadcastRead));
//		}
	}
	
	public void updateSense(RobotController rc, int index, float r) throws GameActionException{
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
		
	
    static boolean willCollideWithBody(MapLocation bulletLocation, Direction propagationDirection,  BodyInfo bi) {
        MapLocation myLocation = bi.getLocation();

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

        return (perpendicularDist <= bi.getRadius());
    }
	
	public void tryfireSingleShot(RobotController rc, MapLocation target) throws GameActionException{

        // And we have enough bullets, and haven't attacked yet this turn...
        if (rc.canFireSingleShot()) {
            // ...Then fire a bullet in the direction of the enemy.
            //rc.fireSingleShot(rc.getLocation().directionTo(nearbyEnemies[0].location));
        	
        	boolean shouldShoot= true;
        	if (nearbyFriends!= null){
	    		for (int i = 0; i < nearbyFriends.length; i++){
	    			if (willCollideWithBody(rc.getLocation(), rc.getLocation().directionTo(target), nearbyFriends[i])){
	    				shouldShoot= false;
	    				break;
	    			}
	    		}
        	}
        	if (nearbyTrees!= null){
	    		for (int i = 0; i < nearbyTrees.length; i++){
	    			if (willCollideWithBody(rc.getLocation(), rc.getLocation().directionTo(target), nearbyTrees[i])){
	    				shouldShoot= false;
	    				break;
	    			}
	    		}
        	}
    		
    		if (shouldShoot){
    			rc.fireSingleShot(rc.getLocation().directionTo(target));
    			rc.setIndicatorLine(rc.getLocation(), target, 255, 255, 255);
    			rc.setIndicatorDot(target, 255, 255, 255);
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
	
	
	
	public BodyInfo getHighestPriorityBody(RobotController rc, RobotInfo[] bodies, MapLocation ref, int maxConsidered){
		RobotInfo priorityBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				priorityBody= bodies[0];
				float largestPriority= getDamagePriority(priorityBody);
				for (int i = 1; i< clamp(bodies.length,0 , maxConsidered); i++){
					float candidatePriority = getDamagePriority(bodies[i]) *getHitChance(rc, bodies[i]);
					if (candidatePriority > largestPriority){
						largestPriority= candidatePriority;
						priorityBody= bodies[i];
					}
				}
			}
		}
		return priorityBody;
	}
	
	public TreeInfo getTastiestBody(RobotController rc, TreeInfo[] bodies, MapLocation ref, int maxConsidered) throws GameActionException{
		TreeInfo priorityBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				priorityBody= bodies[0];
				int largestPriority= (int) getTastiness(priorityBody,rc);
				for (int i = 1; i< clamp(bodies.length,0 , maxConsidered); i++){
					int candidatePriority = (int) getTastiness(bodies[i],rc);
					rc.setIndicatorDot(bodies[i].location, (int) clamp(candidatePriority,0,255) , 0, 0);
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
		if (ti.containedBullets> 0){
			return ti.containedBullets + ti.health/50;
		}else{
			return 0;
		}
	}
	
	public double getDanger(BulletInfo bi, RobotController rc){
		return bi.damage/rc.getHealth(); 
	}
	
    static boolean willCollide(BulletInfo bullet, MapLocation robotLoc, RobotType rt) {
        MapLocation myLocation = robotLoc;

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

        return (perpendicularDist <= rt.bodyRadius);
    }
	
    public double moveAwayFromBulletsVector(RobotController rc, MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier) throws GameActionException{
    	double danger= 0;
    	if (nearbyBullets!= null){
	    	for (int i = 0; i < clamp(maxConsidered,0,nearbyBullets.length); i++){
				BulletInfo bi= nearbyBullets[i];
				danger= getDanger(bi, rc);
				moveVec.add(new Vector2D(rcLoc.directionTo(rcLoc).radians).scale(danger*multiplier));
				rc.setIndicatorLine(rcLoc, bi.location, 0, 200, 20);
			}
		}
    	return danger;
    }
    
    public float moveTowardsFriendVector(RobotController rc, MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier, float bodyRadiusMultiplier, HashSet<RobotType> ignoreType) throws GameActionException{
    	float friendScale= 0;
    	float friends= 0;
    	if (nearbyFriends!= null){
	    	double charisma= 0;
			double scale= 0;
	
			for (int i = 0; i <clamp(maxConsidered,0,nearbyFriends.length) ; i++){
				RobotInfo ri= nearbyFriends[i];
				if (!ignoreType.contains(ri.getType())){
					charisma= getCharisma(ri);
					
					//Scale up until you are too close; should always be between 0 and 1
					scale= (rc.getLocation().distanceTo(ri.location)- (ri.getRadius()+rc.getType().bodyRadius)/ rc.getType().sensorRadius) / GameConstants.MAP_MAX_WIDTH;
					friendScale+= charisma*scale*multiplier;
					
					//if you're too close to your friend
					if (ri.getLocation().distanceTo(rc.getLocation()) - bodyRadiusMultiplier*rc.getType().bodyRadius <= 0){ 
						scale*= -1;
					}
	
					moveVec.add(new Vector2D(rcLoc.directionTo(ri.location).radians).scale(charisma*scale*multiplier));
					rc.setIndicatorLine(rcLoc, ri.location, 0, 0, 255);
					friends+= 1;
				}
			}
		}
    	return friendScale/friends;
    }
    
    public void moveTowardsEnemyVector(RobotController rc,  MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier, HashSet<RobotType> ignoreType) throws GameActionException{
    	if (nearbyEnemies!= null){
	    	double charisma= 0;
			double scale= 0;
			for (int i = 0; i <  clamp(maxConsidered, 0,nearbyEnemies.length); i++){
				RobotInfo ri= nearbyEnemies[i];
				if (!ignoreType.contains(ri.getType())){
					charisma= getCharisma(ri);
					scale= rc.getLocation().distanceTo(ri.location)/ rc.getType().sensorRadius/ GameConstants.MAP_MAX_HEIGHT;
					moveVec.add(new Vector2D(rcLoc.directionTo(ri.location).radians).scale(charisma*scale*multiplier));
					rc.setIndicatorLine(rcLoc, ri.location, 255, 0, 0);
				}
			}
		}
    }
    public void moveTowardsEnemyVecFlipOnMoreFriend(RobotController rc,  MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier, float friendScale, HashSet<RobotType> ignoreType) throws GameActionException{
    	boolean flipped= false;
    	if (nearbyEnemies!= null){
	    	double charisma= 0;
			double scale= 0;
			for (int i = 0; i <  clamp(maxConsidered, 0,nearbyEnemies.length); i++){
				RobotInfo ri= nearbyEnemies[i];
				if (!ignoreType.contains(ri.getType())){
					charisma= getCharisma(ri);
					scale= rc.getLocation().distanceTo(ri.location)/ rc.getType().sensorRadius/ GameConstants.MAP_MAX_HEIGHT;
					
					if (friendScale > scale){
						scale*= -1;
						flipped= true;
					}
					
					moveVec.add(new Vector2D(rcLoc.directionTo(ri.location).radians).scale(charisma*scale*multiplier));
					if (!flipped){
						rc.setIndicatorLine(rcLoc, ri.location, 255, 0, 0);
					}else{
						rc.setIndicatorLine(rcLoc, ri.location, 0, 255, 0);
					}
				}
			}
		}
    }
    
    public void moveTowardsTreeVector(RobotController rc, MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier) throws GameActionException{		
    	if (nearbyTrees!= null){;
			double scale= 0;
			for (int i = 0; i < clamp(maxConsidered,0,nearbyTrees.length); i++){
				TreeInfo ti= nearbyTrees[i];
				scale= getTastiness(ti, rc);
				moveVec.add(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(scale*multiplier));
				rc.setIndicatorLine(rcLoc, ti.location, 0, 150, 0);
			}

		}
    }
    
    public void moveTowardsTreeVectorFlipOnNoBullet(RobotController rc, MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier) throws GameActionException{		
    	if (nearbyTrees!= null){;
			double scale= 0;
			for (int i = 0; i < clamp(maxConsidered,0,nearbyTrees.length); i++){
				TreeInfo ti= nearbyTrees[i];
				scale= getTastiness(ti, rc);
				if (ti.containedBullets<= 0){
					moveVec.minus(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(scale*multiplier));
				}else{
					moveVec.add(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(scale*multiplier));
				}
				rc.setIndicatorLine(rcLoc, ti.location, 0, 150, 0);
			}

		}
    }
    
    public void moveTowardsTreeVectorIgnoreOnNoBullet(RobotController rc, MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier) throws GameActionException{		
    	if (nearbyTrees!= null){;
			double scale= 0;
			for (int i = 0; i < clamp(maxConsidered,0,nearbyTrees.length); i++){
				TreeInfo ti= nearbyTrees[i];
				scale= getTastiness(ti, rc);
				if (ti.containedBullets> 0){
					moveVec.add(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(scale*multiplier));
				}
				rc.setIndicatorLine(rcLoc, ti.location, 0, 150, 0);
			}

		}
    }
    
    public void moveVecTowardsGoal(RobotController rc, MapLocation rcLoc, Vector2D moveVec, int maxConsidered, float multiplier) throws GameActionException{
    	//System.out.println("Current Loc Vec start: "+ currentLoc.toStr());

    	if (goalLocs!= null){
    		for (int i =0 ; i < clamp(maxConsidered,0,goalLocs.size()); i++){
	    		float distance= 0;
				distance= rc.getLocation().distanceTo(goalLocs.get(i));
				moveVec.add(new Vector2D(rcLoc.directionTo(goalLocs.get(i)).radians).scale(distance/GameConstants.MAP_MAX_HEIGHT*multiplier)); //.normalize().scale(distance*multiplier));
				rc.setIndicatorLine(rcLoc, goalLocs.get(i), 255, 210, 0);
    		}
		}else{
			System.out.println("Goals Loc Array is null");
		}
    }
    	
	public void tryShakeTree(RobotController rc) throws GameActionException{
		// getTastiestBody(RobotController rc, TreeInfo[] bodies, MapLocation ref, int maxConsidered)
		TreeInfo closestBody= getTastiestBody(rc, nearbyTrees, rc.getLocation(), Integer.MAX_VALUE);
		if (closestBody!= null){
	        MapLocation possibleTreeLoc= closestBody.getLocation();
	        if (possibleTreeLoc!= null){
		        if (rc.canShake(possibleTreeLoc) && closestBody.containedBullets> 0){
		        	rc.shake(possibleTreeLoc);
		        	rc.setIndicatorLine(rc.getLocation(), possibleTreeLoc, 255, 20, 147);
		        }
	        }
		}
	}
	
	public MapLocation getMapLoc(Vector2D vec){
		return new MapLocation((float) vec.x, (float) vec.y);
	}
	
	public MapLocation clampMapLoc(MapLocation loc){//clamp it between the map constraints
		return new MapLocation(clamp(loc.x, GameConstants.MAP_MIN_WIDTH, GameConstants.MAP_MAX_WIDTH), clamp(loc.y, GameConstants.MAP_MIN_HEIGHT, GameConstants.MAP_MAX_HEIGHT));
	}
	
	public boolean withinMap(MapLocation loc){
		boolean answer= false;
		if (GameConstants.MAP_MAX_HEIGHT > loc.y){
			if (GameConstants.MAP_MIN_HEIGHT < loc.y){
				if (GameConstants.MAP_MAX_WIDTH > loc.x){
					if (GameConstants.MAP_MIN_HEIGHT < loc.y){
						answer= true;
					}
				}
			}
		}
		
		return answer;
	}
	
	public static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}
