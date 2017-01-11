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
        }
	}

    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	ut.refresh(rc, archProfil);
            	//System.out.println("I'm retarded!");
                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < .01) {
                    rc.hireGardener(dir);
                }

                // Move randomly
                if (rc.getRoundNum()%stepsUntilJiggle== 0){ //jiggle so get out of stuck
                	tryMove(randomDirection());
                }else{
                	MapLocation myLoc= rc.getLocation();
                	Vector2D currentVec= new Vector2D(myLoc);
                	ut.move(rc, new Vector2D[] {
           	             ut.dodgeBulleteVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveAwayFromEnemyVector(rc, myLoc, currentVec, Integer.MAX_VALUE,0.5f),
           	             ut.moveTowardsFriendVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveTowardsTreeVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveTowardsGoal(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                	});
                }

                //ut.tryShakeTree(rc);
                
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }

	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	ut.refresh(rc, garProfil);
                if (rc.getRoundNum()%stepsUntilJiggle== 0){
                	tryMove(randomDirection());
                }else{
                	MapLocation myLoc= rc.getLocation();
                	Vector2D currentVec= new Vector2D(myLoc);
                	ut.move(rc, new Vector2D[] {
          	             ut.dodgeBulleteVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveAwayFromEnemyVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveTowardsFriendVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveTowardsTreeVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveTowardsGoal(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                	});
                }
                
                Direction dir = randomDirection();
                // Randomly attempt to build a soldier or lumberjack in this direction
                if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < 0.1) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                } 
//                else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
//                    rc.buildRobot(RobotType.LUMBERJACK, dir);
//                }
                
                //ut.tryShakeTree(rc);
   
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }

    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	ut.refresh(rc, soldProfil);
            	ut.tryfireSingleShot(rc);
                if (rc.getRoundNum()%stepsUntilJiggle== 0){
                	tryMove(randomDirection());
                }else{
                	MapLocation myLoc= rc.getLocation();
                	Vector2D currentVec= new Vector2D(myLoc);
                	ut.move(rc, new Vector2D[] {
          	             ut.dodgeBulleteVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveAwayFromEnemyVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             //ut.moveTowardsFriendVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             //ut.moveTowardsTreeVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
           	             ut.moveTowardsGoal(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                	});
                }
                
                //ut.tryShakeTree(rc);
                
                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }

    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
        Team enemy = rc.getTeam().opponent();
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
            	ut.refresh(rc, lumbProfil);
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
                        if (rc.getRoundNum()%stepsUntilJiggle== 0){
                        	tryMove(randomDirection());
                        }else{
                        	MapLocation myLoc= rc.getLocation();
                        	Vector2D currentVec= new Vector2D(myLoc);
                        	ut.move(rc, new Vector2D[] {
                  	             ut.dodgeBulleteVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                   	             ut.moveAwayFromEnemyVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                   	             ut.moveTowardsFriendVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                   	             ut.moveTowardsTreeVector(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                   	             ut.moveTowardsGoal(rc, myLoc, currentVec, Integer.MAX_VALUE,1),
                        	});
                        }
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
