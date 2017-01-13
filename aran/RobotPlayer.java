package aran;
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
                runSoldier();
                break;
            case LUMBERJACK:
                runLumberjack();
                break;
            case SCOUT:
            	runScout();
            	break;
        }
	}

    
    public static void notMoveGeneric(int[] profile, float[] rads) throws GameActionException{
    	ut.refresh(rc, profile, rads); //sense after you shoot?
    	
    	if(rc.getType().canAttack() && ut.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) ut.getHighestPriorityBody(rc, ut.nearbyEnemies,rc.getLocation(), 3);
    		ut.tryfireSingleShot(rc, highPRobotInfo.getLocation());
        	
    	}else if (rc.canShake() && ut.nearbyTrees!=null && ut.nearbyTrees.length > 0 && ! rc.hasAttacked()){
        	if (ut.nearbyTrees[0].containedBullets> 0 && rc.canShake(ut.nearbyTrees[0].getLocation())){
        		rc.shake(ut.nearbyTrees[0].getLocation());
        	}
        }
    }
    

    private static void runScout() {
        System.out.println("I'm an scout!");        
        // The code you want your robot to perform every round should be in this loop
        ut.setInitialEnemyArchonAsGoal(rc);
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric(scoutProfil, null);
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
                    Vector2D moveVec= new Vector2D(rcLoc);
                    double danger= ut.moveAwayFromBulletsVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 10);
                    if (danger< percentageUntilDangerOverride){
                        ut.moveVecTowardsGoal(rc, rcLoc, moveVec, 3, 5);
                        float friendScale= ut.moveTowardsFriendVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 3, 1.5f, ignoreNone);
                        ut.moveTowardsEnemyVecFlipOnMoreFriend(rc, rcLoc, moveVec, Integer.MAX_VALUE, (float) 2, friendScale,ignoreNone);
                        ut.moveTowardsTreeVector(rc, rcLoc, moveVec, 3, 1);
                    }
                    
                    tryMove(rcLoc.directionTo(moveVec.getMapLoc()));
            	}
                Clock.yield();

            } catch (Exception e) {
            	System.out.println("Scout Exception");
                e.printStackTrace();
            }
        }
	}

	static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        ut.setInitialEnemyArchonAsGoal(rc);
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                Direction dir = randomDirection();
                
                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < .01 && rc.getTeamBullets() > RobotType.GARDENER.bulletCost) {
                    rc.hireGardener(dir);
                }
                
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric(archProfil, null);
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
                    Vector2D moveVec= new Vector2D(rcLoc);
                    double danger= ut.moveAwayFromBulletsVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 10);
                    if (danger< percentageUntilDangerOverride){
                        ut.moveVecTowardsGoal(rc, rcLoc, moveVec, 3, 5);
                        float friendScale= ut.moveTowardsFriendVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 3, 0.25f, ignoreNone);
                        ut.moveTowardsEnemyVecFlipOnMoreFriend(rc, rcLoc, moveVec, Integer.MAX_VALUE, -2, friendScale,ignoreNone);
                        ut.moveTowardsTreeVector(rc, rcLoc, moveVec, 3, 1);
                    }
                    
                    tryMove(rcLoc.directionTo(moveVec.getMapLoc()));
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");
        ut.setInitialEnemyArchonAsGoal(rc);
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation rcLoc= rc.getLocation();        
                Direction dir = randomDirection();
   	            	
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SCOUT, dir) && Math.random() < .01 && rc.isBuildReady()) {
                	rc.buildRobot(RobotType.SCOUT, dir);
                }else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                } else if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                }  
            	notMoveGeneric(garProfil, null);
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
                    Vector2D moveVec= new Vector2D(rcLoc);
                    double danger= ut.moveAwayFromBulletsVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 10);
                    if (danger< percentageUntilDangerOverride){
                        ut.moveVecTowardsGoal(rc, rcLoc, moveVec, 3, 1);
                        ut.moveTowardsEnemyVector(rc, rcLoc, moveVec, Integer.MAX_VALUE,-4, ignoreArchon);
                        ut.moveTowardsFriendVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 3, 1.5f, ignoreNone);
                        ut.moveTowardsTreeVector(rc, rcLoc, moveVec, 3, 2);
                    }
                    
                    tryMove(rcLoc.directionTo(moveVec.getMapLoc()));
            	}
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!"); 
        
        ut.setInitialEnemyArchonAsGoal(rc);
        // The code you want your robot to perform every round should be in this loop
        while (true) {
            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric(soldProfil, null);
            	//Danger, goal, enemy, friend, tree
            	if (!rc.hasMoved()){
                    Vector2D moveVec= new Vector2D(rcLoc);
                    double danger= ut.moveAwayFromBulletsVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 10);
                    if (danger< percentageUntilDangerOverride){
                        ut.moveVecTowardsGoal(rc, rcLoc, moveVec, 5, 5);
                        float friendScale= ut.moveTowardsFriendVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 3, 1.5f, ignoreNone);
                        ut.moveTowardsEnemyVecFlipOnMoreFriend(rc, rcLoc, moveVec, Integer.MAX_VALUE, - 2, friendScale,ignoreNone);
                        ut.moveTowardsTreeVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 1);
                    }
                    
                    tryMove(rcLoc.directionTo(moveVec.getMapLoc()));
            	}
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        ut.setInitialEnemyArchonAsGoal(rc);
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	//bullets, friends, enemies, trees
            	MapLocation rcLoc= rc.getLocation();
            	notMoveGeneric(soldProfil, null);
            	//Danger, goal, enemy, friend, tree
       
                
                if(ut.nearbyEnemies.length > 0 && !rc.hasAttacked()) {
                    // Use strike() to hit all nearby robots!
                    rc.strike();
                }else if (ut.nearbyTrees.length > 0 && ! rc.hasAttacked()){
                	if (rc.canShake(ut.nearbyTrees[0].getLocation())){
                		rc.shake(ut.nearbyTrees[0].getLocation());
                	}else if (rc.canChop(ut.nearbyTrees[0].getLocation())){
                		rc.chop(ut.nearbyTrees[0].getLocation());
                	}
                }
                else {
                	//Danger, goal, enemy, friend, tree
                	if (!rc.hasMoved()){
                        Vector2D moveVec= new Vector2D(rcLoc);
                        double danger= ut.moveAwayFromBulletsVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 2);
                        if (danger< percentageUntilDangerOverride){
                            ut.moveVecTowardsGoal(rc, rcLoc, moveVec, 3, 1);
                            ut.moveTowardsEnemyVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 3, ignoreArchon);
                            ut.moveTowardsFriendVector(rc, rcLoc, moveVec, Integer.MAX_VALUE, 4,2, ignoreAllExceptArchon);
                            ut.moveTowardsTreeVector(rc, rcLoc, moveVec, 3, 5);
                        }
                        
                        tryMove(rcLoc.directionTo(moveVec.getMapLoc()));
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
