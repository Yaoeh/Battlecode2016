package AndrewBot;
import battlecode.common.*;

public strictfp class RobotPlayer {
    static RobotController rc;

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
                Archon.run(rc);
                break;
            case GARDENER:
                Gardener.run(rc);
                break;
            case SOLDIER:
                Soldier.run(rc);
                break;
            case LUMBERJACK:
                Lumberjack.run(rc);
                break;
            case SCOUT:
            	Scout.run(rc);;
            	break;
            case TANK:
            	runTank();
            	break;
        }
	}

    
    
	
	
    
    static void runTank() throws GameActionException{
    	/*System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();
        int scoutLength = rc.readBroadcast(0);
        String mode = "seek";
        MapLocation goal = rc.getLocation();
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLoc = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                if(mode == "seek")
                {
                	for(int i=1;i<scoutLength+1;i++){
                    	int msg = rc.readBroadcast(i);
                    	if(msg != 0)
                    	{
                    		int[] m = fastUnHash(msg);
                    		goal = new MapLocation((float)m[1],(float)m[2]);
                    		mode = "destroy";
                    	}
                    }
                	// If there are some...
                    if (robots.length > 0) {
                        // And we have enough bullets, and haven't attacked yet this turn...
                    	goal = robots[0].location;
                        if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                        }
                        mode = "destroy";
                    }
                  //dodge any bullets
	            	
                	// Move randomly
                    tryMove(randomDirection());

                    // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                    Clock.yield();
                }
                else if(mode=="destroy")
                {
                	// If there are some...
                    if (robots.length > 0) {
                        // And we have enough bullets, and haven't attacked yet this turn...
                    	goal = robots[0].location;
                        if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(rc.getLocation().directionTo(robots[0].location));
                        }
                    }
                    else{
	                    if(myLoc.distanceTo(goal) < 5.0f){
	                    	mode = "seek";
	                    	Clock.yield();
	                    }
                    }
                    Direction dir = myLoc.directionTo(goal);
                    BulletInfo[] bulletInfo = rc.senseNearbyBullets();
	            	for(int i=0;i<bulletInfo.length;i++){
	            		if(willCollideWithMe(bulletInfo[i])){
	            			Direction tdir = dodgeOneBullet(bulletInfo[i], myLoc);
	            			if(rc.canMove(tdir))
	            			{
	            				rc.move(tdir);
	            				Clock.yield();
		            			break;
	            			}
	            			
	            		}
	            	}
	            	int count = 0;
	            	while(!rc.canMove(dir) && count<24){
	            		dir.rotateLeftDegrees(15.0f);
	            		count+=1;
	            	}
                    rc.move(dir);
                    Clock.yield();
                }
                
                
                
                

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }*/
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

    
    public static MapLocation getClosestLoc(MapLocation[] locs, MapLocation ref){
		MapLocation cloestLoc= locs[0];

		float shortestLength= ref.distanceTo(cloestLoc);
		for (int i = 1; i< locs.length; i++){
			float candidateDis= ref.distanceTo(locs[i]);
			if (candidateDis < shortestLength){
				shortestLength= candidateDis;
				cloestLoc= locs[i];
			}
		}

		return cloestLoc;
	}
    
    static int fastHash(int rounds, int x1, int y1){
    	return rounds*600*600 + x1*600 + y1;
    }
    static int[] fastUnHash(int m){
    	int[] ans = new int[3];
    	
    	ans[0] = m/600/600;
    	m -= ans[0]*600*600;
    	ans[1] = m/600;
    	ans[2] = m%600;
    	
    	
    	return ans;
    }
}
