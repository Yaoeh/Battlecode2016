package aran;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashSet;
import java.util.Random;
import battlecode.common.*;

public class Sensor {
	public static Random randall = new Random();
	//int[] sensedInfo= {0,0,0,0}; //0: bullets, 1: friends, 2: enemies, 3: trees
	BulletInfo[] nearbyBullets;
	RobotInfo[] nearbyFriends;
	RobotInfo[] nearbyEnemies;
	TreeInfo[] nearbyNeutralTrees;
	TreeInfo[] nearbyFriendlyTrees;
	TreeInfo[] nearbyEnemyTrees;
	MapLocation goalLoc;
			
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
    }
    		
	public void setInitialScoutGoal(RobotController rc, float numLayer){
		MapLocation[] initArchonLocs= rc.getInitialArchonLocations(rc.getTeam());
		MapLocation yourArchonLoc= Value.getClosestLoc(initArchonLocs, rc.getLocation(), Integer.MAX_VALUE);
		
		for (int n = 0; n< Value.clamp(numLayer, 0, GameConstants.MAP_MAX_WIDTH/rc.getType().sensorRadius); n++){
			//bleh
			
		}
		
	}
	
	public Direction getDirToInitialArchon(RobotController rc){
		return rc.getLocation().directionTo(rc.getInitialArchonLocations(rc.getTeam().opponent())[0]);
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
	
	public void senseBullets(RobotController rc){ //Updates the freshness of the information?
		nearbyBullets= rc.senseNearbyBullets(-1);
		//System.out.println("Bullet sensed: " + nearbyBullets.length);
		//sensedInfo[Constants.SenseRefresh.BULLET.getIndex()]= rc.getRoundNum();
	}
	
	public void senseFriends(RobotController rc){
		nearbyFriends= rc.senseNearbyRobots(-1, rc.getTeam());
		//sensedInfo[Constants.SenseRefresh.FRIEND.getIndex()]= rc.getRoundNum();
	}
	
	public void senseEnemies(RobotController rc) throws GameActionException{
		nearbyEnemies= rc.senseNearbyRobots(-1 , rc.getTeam().opponent());
		//sensedInfo[Constants.SenseRefresh.ENEMY.getIndex()]= rc.getRoundNum();		
	}
	
	public void senseTrees(RobotController rc){
		nearbyNeutralTrees= rc.senseNearbyTrees(-1, rc.getTeam().NEUTRAL);
		nearbyFriendlyTrees= rc.senseNearbyTrees(-1, rc.getTeam());
		nearbyEnemyTrees= rc.senseNearbyTrees(-1, rc.getTeam().opponent());
		//sensedInfo[Constants.SenseRefresh.TREE.getIndex()]= rc.getRoundNum();
	}
		
	
	
	public void senseBullets(RobotController rc, float r){ //Updates the freshness of the information?
		nearbyBullets= rc.senseNearbyBullets(r);
		//sensedInfo[Constants.SenseRefresh.BULLET.getIndex()]= rc.getRoundNum();
	}
	
	public void senseFriends(RobotController rc, float r){
		nearbyFriends= rc.senseNearbyRobots(r, rc.getTeam());
		//sensedInfo[Constants.SenseRefresh.FRIEND.getIndex()]= rc.getRoundNum();
	}
	
	public void senseEnemies(RobotController rc, float r) throws GameActionException{
		nearbyEnemies= rc.senseNearbyRobots(r , rc.getTeam().opponent());
		//sensedInfo[Constants.SenseRefresh.ENEMY.getIndex()]= rc.getRoundNum();		
	}
	
	public void senseTrees(RobotController rc, float r){
		nearbyNeutralTrees= rc.senseNearbyTrees(Value.clamp(r,1, rc.getType().sensorRadius), rc.getTeam().NEUTRAL);
		nearbyFriendlyTrees= rc.senseNearbyTrees(Value.clamp(r,1, rc.getType().sensorRadius), rc.getTeam());
		nearbyEnemyTrees= rc.senseNearbyTrees(Value.clamp(r,1, rc.getType().sensorRadius), rc.getTeam().opponent());
		//sensedInfo[Constants.SenseRefresh.TREE.getIndex()]= rc.getRoundNum();
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
	    				//rc.setIndicatorLine(rc.getLocation(), nearbyFriends[i].getLocation(), 0, 120, 0);
	    				rc.setIndicatorDot(nearbyFriends[i].getLocation(), 0, 120, 0);
	    				shouldShoot= false;
	    				break;
	    			}
	    		}
        	}else{
        		shouldShoot= false; //if you didn't look out for your friends you shouldn't shoot
        	}
        	if (shouldShoot){
	        	if (nearbyFriendlyTrees!= null){
		    		for (int i = 0; i < nearbyFriendlyTrees.length; i++){
		    			if (willCollideWithBody(rc.getLocation(), rc.getLocation().directionTo(target), nearbyFriendlyTrees[i])){
		    				rc.setIndicatorLine(rc.getLocation(), nearbyFriendlyTrees[i].getLocation(), 0, 120, 0);
		    				shouldShoot= false;
		    				break;
		    			}
		    		}
	        	}
        	}
        	if (shouldShoot){
	        	if (nearbyNeutralTrees!= null){
		    		for (int i = 0; i < nearbyNeutralTrees.length; i++){
		    			if (willCollideWithBody(rc.getLocation(), rc.getLocation().directionTo(target), nearbyNeutralTrees[i])){
		    				rc.setIndicatorLine(rc.getLocation(), nearbyNeutralTrees[i].getLocation(), 0, 120, 0);
		    				shouldShoot= false;
		    				break;
		    			}
		    		}
	        	}
        	}
    		
    		if (shouldShoot){
    			rc.fireSingleShot(rc.getLocation().directionTo(target));
    			//rc.setIndicatorLine(rc.getLocation(), target, 255, 255, 255);
    			rc.setIndicatorDot(target, 255, 255, 255);
    			//rc.setIndicatorDot(rc.getLocation(), 255, 255, 255);
    		}else{
    			//rc.fireSingleShot(rc.getLocation().directionTo(target));
    			//rc.setIndicatorLine(rc.getLocation(), target, 0, 0, 0);
    			rc.setIndicatorDot(target, 0, 0, 0);
    			//rc.setIndicatorDot(rc.getLocation(), 0, 0, 0);
    		}
        }
    
		
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
    
    static float[] willCollide(BulletInfo bullet, MapLocation robotLoc) {
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
            return new float[]{0, 0};
        }

        // distToRobot is our hypotenuse, theta is our angle, and we want to know this length of the opposite leg.
        // This is the distance of a line that goes from myLocation and intersects perpendicularly with propagationDirection.
        // This corresponds to the smallest radius circle centered at our location that would intersect with the
        // line that is the path of the bullet.
        float perpendicularDist = (float)Math.abs(distToRobot * Math.sin(theta)); // soh cah toa :)

        return new float[]{1, perpendicularDist};
    }
       
    
    public Vector2D getSideStepVector(RobotController rc, BulletInfo bullet){ //not tested
        Vector2D answer= null;
        float[] collideAnswer= willCollide(bullet, rc.getLocation());
        if (collideAnswer[0]== 1){
	    	Direction towards = bullet.getDir();
	        MapLocation leftGoal = rc.getLocation().add(towards.rotateLeftDegrees(90), rc.getType().bodyRadius);
	        MapLocation rightGoal = rc.getLocation().add(towards.rotateRightDegrees(90), rc.getType().bodyRadius);
	        
	        ArrayList<MapLocation> possLocs= new ArrayList<MapLocation>();
	        if (collideAnswer[1]== 0){
		        if (rc.canMove(rc.getLocation().directionTo(leftGoal), rc.getType().strideRadius)){
		        	possLocs.add(leftGoal);
		        }
		        if (rc.canMove(rc.getLocation().directionTo(rightGoal), rc.getType().strideRadius)){
		        	possLocs.add(rightGoal);
		        }
		        
		        if (possLocs.size() > 0){
		        	answer= new Vector2D(rc.getLocation().directionTo(possLocs.get(randall.nextInt(possLocs.size()))).radians).normalize();
		        }
	        }else{
	        	answer= new Vector2D(rc.getLocation()).add(new Vector2D(towards.rotateRightDegrees(90 * collideAnswer[1]/Math.abs(collideAnswer[1])).radians));
	        }
        }
        
		return answer;
    }
	
    public Vector2D moveAwayFromBulletsVector(RobotController rc, float consideredRadius, int maxConsidered, float multiplier) throws GameActionException{
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D();
    	double danger= 0;
    	if (nearbyBullets!= null){
    		int iteratedValues= (int) Value.clamp(maxConsidered,0,nearbyBullets.length);
	    	for (int i = 0; i <iteratedValues ; i++){
				BulletInfo bi= nearbyBullets[i];
				if (rc.getLocation().distanceTo(bi.location) < consideredRadius){
					if (bi!= null){
						danger= Value.getDanger(bi, rc);
						Vector2D sideStepVec= getSideStepVector(rc, bi);
						if (sideStepVec!= null)
							neutralVec.add(sideStepVec.scale(danger*multiplier));
						rc.setIndicatorLine(rcLoc, bi.location, 0, 200, 20);
					}
				}
			}
		}
    	return neutralVec;
    }
    
    public Vector2D moveTowardsFriendVector(RobotController rc, int maxConsidered, float multiplier, float bodyRadiusMultiplier, HashSet<RobotType> ignoreType) throws GameActionException{
    	//Body Radius Multiplier dictates how far away to be from a friend, where the distance away is multiplied by this value
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D(0,0);
    	if (nearbyFriends!= null){
	    	double charisma= 0;
			double scale= 0;
	
			for (int i = 0; i <Value.clamp(maxConsidered,0,nearbyFriends.length) ; i++){
				RobotInfo ri= nearbyFriends[i];
				if (!ignoreType.contains(ri.getType())){
					charisma= Value.getCharisma(ri);
					
					//Scale up until you are too close; should always be between 0 and 1
					scale= (rc.getLocation().distanceTo(ri.location)- (ri.getRadius()+rc.getType().bodyRadius)/ rc.getType().sensorRadius) / GameConstants.MAP_MAX_WIDTH;

					
					//if you're too close to your friend
					if (ri.getLocation().distanceTo(rc.getLocation()) - bodyRadiusMultiplier*(rc.getType().bodyRadius+ri.getType().bodyRadius) <= 0){ 
						scale*= -1;
						rc.setIndicatorLine(rcLoc, ri.location, 255, 0, 255);
					}else{
						rc.setIndicatorLine(rcLoc, ri.location, 0, 0, 255);
					}
	
					neutralVec.add(new Vector2D(rcLoc.directionTo(ri.location).radians).scale(charisma*scale*multiplier));

				}
			}
		}
    	return neutralVec;
    }
    
    public Vector2D moveTowardsEnemyVector(RobotController rc, int maxConsidered, float consideredDivison, float multiplier, HashSet<RobotType> ignoreType) throws GameActionException{
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D(0,0);
    	if (nearbyEnemies!= null){
	    	double charisma= 0;
			double scale= 0;
			for (int i = 0; i <  Value.clamp(maxConsidered, 0,nearbyEnemies.length); i++){
				RobotInfo ri= nearbyEnemies[i];
				if (!ignoreType.contains(ri.getType())){
					if (rc.getLocation().distanceTo(ri.getLocation()) < Value.clamp(rc.getType().sensorRadius/consideredDivison, rc.getType().strideRadius*3, rc.getType().sensorRadius)){
						charisma= Value.getCharisma(ri);
						scale= rc.getLocation().distanceTo(ri.location)/ GameConstants.MAP_MAX_HEIGHT;
						neutralVec.add(new Vector2D(rcLoc.directionTo(ri.location).radians).scale(charisma*scale*multiplier));
						rc.setIndicatorLine(rcLoc, ri.location, 255, 0, 0);
					}else{
						charisma= Value.getCharisma(ri);
						scale= rc.getLocation().distanceTo(ri.location)/ GameConstants.MAP_MAX_HEIGHT;
						if (rc.getRoundNum()%50 < 25){
							neutralVec.add(new Vector2D(rcLoc.directionTo(ri.location).rotateLeftDegrees(90).radians).scale(charisma*scale*multiplier));
							rc.setIndicatorLine(rcLoc, ri.location, 255, 0, 0);
						}else{
							neutralVec.add(new Vector2D(rcLoc.directionTo(ri.location).rotateRightDegrees(90).radians).scale(charisma*scale*multiplier));
							rc.setIndicatorLine(rcLoc, ri.location, 255, 0, 0);
						}
					}
				}
			}
		}
    	return neutralVec;
    }

    public Vector2D moveTowardsAllTreeVector(RobotController rc, int maxConsidered, float multiplier) throws GameActionException{		
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D(0,0);
    	if (nearbyFriendlyTrees!= null){;
			double scale= 0;
			for (int i = 0; i < Value.clamp(maxConsidered,0,nearbyFriendlyTrees.length); i++){
				TreeInfo ti= nearbyFriendlyTrees[i];
				scale= Value.getDistanceToTree(ti, rc);
				neutralVec.add(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(scale*multiplier));
				if (scale> 0)
					rc.setIndicatorLine(rcLoc, ti.location, 0, 150, 0);
			}

		}
    	return neutralVec;
    }
    
    public Vector2D moveTowardsNeutralTreeVector(RobotController rc, int maxConsidered, float multiplier) throws GameActionException{		
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D(0,0);
    	if (nearbyNeutralTrees!= null){;
			double scale= 0;
			for (int i = 0; i < Value.clamp(maxConsidered,0,nearbyNeutralTrees.length); i++){
				TreeInfo ti= nearbyNeutralTrees[i];
				scale= Value.getDistanceToTree(ti, rc);
				neutralVec.add(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(scale*multiplier));
				if (scale> 0)
					rc.setIndicatorLine(rcLoc, ti.location, 0, 150, 0);
			}

		}
    	return neutralVec;
    }
    
    public Vector2D moveTowardsTreeVectorDisregardTastiness(RobotController rc, int maxConsidered, float multiplier) throws GameActionException{		
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D(0,0);
    	if (nearbyFriendlyTrees!= null){;
			double scale= 0;
			for (int i = 0; i < Value.clamp(maxConsidered,0,nearbyFriendlyTrees.length); i++){
				TreeInfo ti= nearbyFriendlyTrees[i];
				//scale= getTastiness(ti, rc);
				neutralVec.add(new Vector2D(rcLoc.directionTo(ti.location).radians).scale(multiplier));
				if (scale> 0)
					rc.setIndicatorLine(rcLoc, ti.location, 0, 150, 0);
			}

		}
    	return neutralVec;
    }
            
    public Vector2D moveVecTowardsGoal(RobotController rc, float multiplier) throws GameActionException{
    	//System.out.println("Current Loc Vec start: "+ currentLoc.toStr());
    	MapLocation rcLoc= rc.getLocation();
    	Vector2D neutralVec= new Vector2D(0,0);
    	if (goalLoc!= null){
			float distance= 0;
			distance= rc.getLocation().distanceTo(goalLoc);
			neutralVec.add(new Vector2D(rcLoc.directionTo(goalLoc).radians).scale(distance/GameConstants.MAP_MAX_HEIGHT*multiplier)); //.normalize().scale(distance*multiplier));
			rc.setIndicatorLine(rcLoc, goalLoc, 255, 210, 0);
    	}
    	return neutralVec;
    }
    	
	public void tryShakeTree(RobotController rc) throws GameActionException{
		// getTastiestBody(RobotController rc, TreeInfo[] bodies, MapLocation ref, int maxConsidered)
		TreeInfo closestBody= Value.getTastiestBody(rc, rc.senseNearbyTrees(1, rc.getTeam().NEUTRAL), rc.getLocation(), Integer.MAX_VALUE);
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
	
	public MapLocation clampMapLoc(MapLocation loc, int minY, int maxX, int maxY, int minX){//clamp it between the map constraints
		return new MapLocation(Value.clamp(loc.x, minX, maxX), Value.clamp(loc.y,minY, maxY));
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

}
