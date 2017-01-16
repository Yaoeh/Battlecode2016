package aran;
import java.util.HashSet;

import battlecode.common.*;

public strictfp class RobotPlayer implements GlobalConstants {
    static RobotController rc;
    static Utility ut= new Utility();
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
    	
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
                //runSoldier();
            	runScout();
            	break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
            	runScout();
            	break;
        }
	}

    public static void notMoveGeneric(int[] profile, float[] rads) throws GameActionException{ //incomplete, base on profiles
    	ut.senseTrees(rc);
		ut.senseFriends(rc);
		ut.senseEnemies(rc);
    	if(rc.getType().canAttack() && ut.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) ut.getHighestPriorityBody(rc, ut.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		ut.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
		ut.senseBullets(rc);
    	if (ut.goalLocs!= null){ //remove goals once you reach them
    		for (MapLocation loc : ut.goalLocs) {
    			if (rc.canSenseLocation(loc)){
    				if (rc.senseRobotAtLocation(loc)== null){
    					ut.goalLocs.remove(loc);
    					break;
    				}
    			}
    		}
    	}
    	if (rc.canShake() && ut.nearbyTrees!=null && ut.nearbyTrees.length > 0){
    		ut.tryShakeTree(rc);
        }    	
    }
    
   
    public static void notMoveGeneric() throws GameActionException{
    	ut.senseTrees(rc);
		ut.senseFriends(rc);
		ut.senseEnemies(rc);
    	if(rc.getType().canAttack() && ut.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) ut.getHighestPriorityBody(rc, ut.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		ut.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
		ut.senseBullets(rc);
    	if (ut.goalLocs!= null){ //remove goals once you reach them
    		for (MapLocation loc : ut.goalLocs) {
    			if (rc.canSenseLocation(loc)){
    				if (rc.senseRobotAtLocation(loc)== null){
    					ut.goalLocs.remove(loc);
    					break;
    				}
    			}
    		}
    	}
    	if (rc.canShake() && ut.nearbyTrees!=null && ut.nearbyTrees.length > 0){
    		ut.tryShakeTree(rc);
        }    	
    }
    

    private static void runScout() {
       // System.out.println("I'm an scout!");        

        ut.setInitialEnemyArchonAsGoal(rc);
        //ut.setInitialScoutGoal(rc, 10); //make a spherical circle in expanding radiai away from location
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric(scoutProfil, null);
            	
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
            		
            		if (rc.getRoundNum()%stepsUntilJiggle== 0){
            			tryMove(randomDirection());
            		}else{
	            		
	                    Vector2D moveVec= new Vector2D(rcLoc);
	                    Vector2D dangerVec= ut.moveAwayFromBulletsVector(rc, rcLoc, Integer.MAX_VALUE, 10);
	                    Vector2D friendVec= ut.moveTowardsFriendVector(rc, rcLoc, Integer.MAX_VALUE, 2, 2, ignoreArchonGardener);
	                    Vector2D enemyVec= ut.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, -3, ignoreNone);    
	                    Vector2D treeVec= ut.moveTowardsTreeVectorDisregardTastiness(rc, rcLoc, 1, 1);
	                    Vector2D goalVec= ut.moveVecTowardsGoal(rc, rcLoc,1, 10);
//	                    if (dangerVec.length()> 0){
//	                    	MapLocation dangerDodge= new Vector2D(rcLoc).add(dangerVec).getMapLoc();
//	                    	if (rc.canMove(dangerDodge)){
//	                    		tryMove(rcLoc.directionTo(dangerDodge));
//	                    	}else{
//	                    		tryMove(randomDirection());
//	                    	}
//	                    }else{
//	                    	Vector2D tryMoveVec= new Vector2D(rcLoc).add(goalVec).add(enemyVec).add(treeVec).add(friendVec).add(dangerVec);
//	                    	if (rcLoc.directionTo(tryMoveVec.getMapLoc())!= null){
//	                    		tryMove(rcLoc.directionTo(tryMoveVec.getMapLoc()));
//	                    	}
//	                    }
                    	Vector2D tryMoveVec= new Vector2D(rcLoc).add(goalVec).add(enemyVec).add(treeVec).add(friendVec).add(dangerVec);
                    	if (rcLoc.directionTo(tryMoveVec.getMapLoc())!= null){
                    		tryMove(rcLoc.directionTo(tryMoveVec.getMapLoc()));
                    	}
            		}
            	}
                Clock.yield();

            } catch (Exception e) {
            	//System.out.println("Scout Exception");
                //e.printStackTrace();
            }
        }
	}

    static void runArchon() throws GameActionException {
       // System.out.println("I'm an archon!");

        ut.setInitialEnemyArchonAsGoal(rc);
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < .01) {
                    rc.hireGardener(dir);
                }
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric(scoutProfil, null);
            	
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
            		
            		if (rc.getRoundNum()%stepsUntilJiggle== 0){
            			tryMove(randomDirection());
            		}else{
	            		
	                    Vector2D moveVec= new Vector2D(rcLoc);
	                    Vector2D dangerVec= ut.moveAwayFromBulletsVector(rc, rcLoc, Integer.MAX_VALUE, 100);
	                    Vector2D friendVec= ut.moveTowardsFriendVector(rc, rcLoc, Integer.MAX_VALUE, 2, 2, ignoreArchonGardener);
	                    Vector2D enemyVec= ut.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, -3, ignoreNone);    
	                    Vector2D treeVec= ut.moveTowardsTreeVectorDisregardTastiness(rc, rcLoc, 1, 1);
	                    Vector2D goalVec= ut.moveVecTowardsGoal(rc, rcLoc,1, 10);
	                    if (dangerVec.length()> 0){
	                    	MapLocation dangerDodge= new Vector2D(rcLoc).add(dangerVec).getMapLoc();
	                    	if (rc.canMove(dangerDodge)){
	                    		tryMove(rcLoc.directionTo(dangerDodge));
	                    	}else{
	                    		tryMove(randomDirection());
	                    	}
	                    }else{
	                    	Vector2D tryMoveVec= new Vector2D(rcLoc).add(goalVec).add(enemyVec).add(treeVec).add(friendVec).add(dangerVec);
	                    	if (rcLoc.directionTo(tryMoveVec.getMapLoc())!= null){
	                    		tryMove(rcLoc.directionTo(tryMoveVec.getMapLoc()));
	                    	}
	                    }
            		}
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
       // System.out.println("I'm a gardener!");

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);

                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .01) {
                    rc.buildRobot(RobotType.SCOUT, dir);
                } else if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                }

                // Move randomly
                tryMove(randomDirection());

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
       // System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();
        ut.setInitialEnemyArchonAsGoal(rc);
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLocation = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);

                // If there are some...
                if (robots.length > 0) {
                    // And we have enough bullets, and haven't attacked yet this turn...
                    if (rc.canFireSingleShot()) {
                        // ...Then fire a bullet in the direction of the enemy.
                        rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                    }
                }
            	MapLocation rcLoc= rc.getLocation();

            	if (!rc.hasMoved()){
            		
            		if (rc.getRoundNum()%stepsUntilJiggle== 0){
            			tryMove(randomDirection());
            		}else{
	            		
	                    Vector2D moveVec= new Vector2D(rcLoc);
	                    Vector2D dangerVec= ut.moveAwayFromBulletsVector(rc, rcLoc, Integer.MAX_VALUE, 100);
	                    Vector2D friendVec= ut.moveTowardsFriendVector(rc, rcLoc, Integer.MAX_VALUE, 2, 2, ignoreArchonGardener);
	                    Vector2D enemyVec= ut.moveTowardsEnemyVector(rc, rcLoc, Integer.MAX_VALUE, -3, ignoreNone);    
	                    Vector2D treeVec= ut.moveTowardsTreeVectorDisregardTastiness(rc, rcLoc, 1, 1);
	                    Vector2D goalVec= ut.moveVecTowardsGoal(rc, rcLoc,1, 10);
	                    if (dangerVec.length()> 0){
	                    	MapLocation dangerDodge= new Vector2D(rcLoc).add(dangerVec).getMapLoc();
	                    	if (rc.canMove(dangerDodge)){
	                    		tryMove(rcLoc.directionTo(dangerDodge));
	                    	}else{
	                    		tryMove(randomDirection());
	                    	}
	                    }else{
	                    	Vector2D tryMoveVec= new Vector2D(rcLoc).add(goalVec).add(enemyVec).add(treeVec).add(friendVec).add(dangerVec);
	                    	if (rcLoc.directionTo(tryMoveVec.getMapLoc())!= null){
	                    		tryMove(rcLoc.directionTo(tryMoveVec.getMapLoc()));
	                    	}
	                    }
            		}
            	}

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
       // System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();

        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // See if there are any enemy robots within striking range (distance 1 from lumberjack's radius)
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);

                if(robots.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        tryMove(toEnemy);
                    } else {
                        // Move Randomly
                        tryMove(randomDirection());
                    }
                }

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }


    
    public static int getZeroEquivalent(float x){
    	if (x== 0){
    		return Integer.MAX_VALUE;
    	}else{
    		return (int) x;
    	}
    }

    
    /**
     * Returns a random Direction
     * @return a random Direction
     */
    static Direction randomDirection() {
        return new Direction((float)Math.random() * 2 * (float)Math.PI);
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
        if (rc.canMove(dir)) {
            rc.move(dir);
            return true;
        }

        // Now try a bunch of similar angles
        boolean moved = false;
        int currentCheck = 1;

        while(currentCheck<=checksPerSide) {
            // Try the offset of the left side
            if(rc.canMove(dir.rotateLeftDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateLeftDegrees(degreeOffset*currentCheck));
                return true;
            }
            // Try the offset on the right side
            if(rc.canMove(dir.rotateRightDegrees(degreeOffset*currentCheck))) {
                rc.move(dir.rotateRightDegrees(degreeOffset*currentCheck));
                return true;
            }
            // No move performed, try slightly further
            currentCheck++;
        }

        // A move never happened, so return false.
        return false;
    }
}
