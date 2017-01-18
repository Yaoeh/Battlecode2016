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
            case TANK:
            	runTank();
            	break;
        }
	}

    static void runArchon() throws GameActionException {
        System.out.println("I'm an archon!");
        double chance = 1.0;
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                // Generate a random direction
                Direction dir = randomDirection();

                // Randomly attempt to build a gardener in this direction
                if (rc.canHireGardener(dir) && Math.random() < chance) {
                    rc.hireGardener(dir);
                    chance = 0.01;
                }
                //chance += 0.0001;
                // Move randomly
                BulletInfo[] bulletInfo = rc.senseNearbyBullets();
            	for(int i=0;i<bulletInfo.length;i++){
            		if(willCollideWithMe(bulletInfo[i])){
            			Direction tdir = dodgeOneBullet(bulletInfo[i], rc.getLocation());
            			if(rc.canMove(tdir))
            			{
            				rc.move(tdir);
            				Clock.yield();
	            			break;
            			}
            			
            		}
            	}
                tryMove(randomDirection());

                // Broadcast archon's location for other robots on the team to know
               /* MapLocation myLocation = rc.getLocation();
                rc.broadcast(0,(int)myLocation.x);
                rc.broadcast(1,(int)myLocation.y);*/

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Archon Exception");
                e.printStackTrace();
            }
        }
    }
    
	static void runGardener() throws GameActionException {
        System.out.println("I'm a gardener!");
        int queue = 0;
        double chance = 0.10;
        Team myTeam = rc.getTeam();
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {

                /*// Listen for home archon's location
                int xPos = rc.readBroadcast(0);
                int yPos = rc.readBroadcast(1);
                MapLocation archonLoc = new MapLocation(xPos,yPos);*/

                // Generate a random direction
            	if(Math.random()<chance)
            	{
	                Direction dir = randomDirection();
	                if(queue == 0)
	                {
	                	if(rc.canPlantTree(dir)){
	                		rc.plantTree(dir);
	                		queue++;
	                	}
	                	
	                	
	                }
	                else if(queue< 2){
	                	if (rc.canBuildRobot(RobotType.SCOUT, dir)) {
	                        rc.buildRobot(RobotType.SCOUT, dir);
	                        queue++;
	                	}
	                }
	                else if(queue < 5){
	                	if (rc.canBuildRobot(RobotType.SOLDIER, dir)) {
	                        rc.buildRobot(RobotType.SOLDIER, dir);
	                        queue++;
	                	}
	                }
	                if(queue>4){
	                	queue = 0;
	                }
            	}
                // Randomly attempt to build a soldier or lumberjack in this direction
                /*if (rc.canBuildRobot(RobotType.SOLDIER, dir) && Math.random() < .01) {
                    rc.buildRobot(RobotType.SOLDIER, dir);
                } else if (rc.canBuildRobot(RobotType.LUMBERJACK, dir) && Math.random() < .01 && rc.isBuildReady()) {
                    rc.buildRobot(RobotType.LUMBERJACK, dir);
                }*/
            	
                // Move randomly
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
            	}
            	else
            	{
            		tryMove(randomDirection());
            	}

                // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Gardener Exception");
                e.printStackTrace();
            }
        }
    }
	static void runScout() throws GameActionException{
		MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
		MapLocation closestEnemyArchon= getClosestLoc(enemyArchonLocs, rc.getLocation());
		String mode = "seekinit";
		Team enemy = rc.getTeam().opponent();
		 MapLocation goal = rc.getLocation();
		int scoutID = rc.readBroadcast(0) + 1;
		rc.broadcast(0, scoutID);
		int broadcastCounter = 21;
		int scoutLength = 0;
		boolean seekerScout = false;
		Direction randomDir = goal.directionTo(closestEnemyArchon);
		if(Math.random() < 0.5)
		{
			seekerScout = true;
		}
		 while (true) {
	
	            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
	            try {
	            	MapLocation myLoc = rc.getLocation();
	            	if(mode == "seekinit")
	            	{
		            	Direction dir;
		            	
		            	
		            	dir = myLoc.directionTo(closestEnemyArchon);
		            	
		            	//dodge any bullets
		            	BulletInfo[] bulletInfo = rc.senseNearbyBullets();
		            	for(int i=0;i<bulletInfo.length;i++){
		            		if(willCollideWithMe(bulletInfo[i])){
		            			dir = dodgeOneBullet(bulletInfo[i], myLoc);
		            			break;
		            		}
		            	}
	
		            	// See if there are any nearby enemy robots
		                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
		
		                // If there are some...
		                if (robots.length > 0) {
		                    //broadcast robot location
		                	MapLocation robLocation = robots[0].getLocation();
		                	int hash = fastHash(rc.getRoundNum(), (int)robLocation.x, (int)robLocation.y);
		                	rc.broadcast(scoutID, hash);
		                	mode = "destroy";
		                }
		                int count = 0;
		                while(!rc.canMove(dir) && count<24){
		            		dir = dir.rotateLeftDegrees(15.0f);
		            		count+=1;
		            	}
		            	
		            	if(rc.canMove(dir)){
		            		rc.move(dir);

		            	}
		            	
		            	Clock.yield();
	            	}
	            	else if(mode == "seek")
	            	{
	            		RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
		                
		                // If there are some...
		                if (robots.length > 0) {
		                	broadcastCounter = 21;
		                	mode = "destroy";
		                }
	            		while(!rc.canMove(randomDir))
	            		{
	            			randomDir = randomDirection();
	            		}
	            		rc.move(randomDir);
	            		Clock.yield();
	            	}
	            	else if(mode == "destroy"){
	            		
	            		// See if there are any nearby enemy robots
		                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
		                broadcastCounter ++;
		                // If there are some...
		                if (robots.length > 0) {
		                	MapLocation robLocation = robots[0].getLocation();
		                	if(broadcastCounter>20)
		                	{
		                		
			                	int hash = fastHash(rc.getRoundNum(), (int)robLocation.x, (int)robLocation.y);
		                		rc.broadcast(scoutID, hash);
		                		broadcastCounter = 0;
		                	}
		                    // And we have enough bullets, and haven't attacked yet this turn...
		                    if (rc.canFireSingleShot()) {
		                        // ...Then fire a bullet in the direction of the enemy.
		                        rc.fireSingleShot(rc.getLocation().directionTo(robLocation));
		                    }
		                    goal = robLocation;
		                    
		                    for(int i=0;i<robots.length;i++){
		                    	if(robots[i].getLocation().distanceTo(myLoc) < 6.0f)
		                    	{
		                    		Direction tdir = robots[i].getLocation().directionTo(myLoc);
		                    		
		                    		if(rc.canMove(tdir)){
		                    			rc.move(tdir);
		                    			Clock.yield();
		                    		}
		                    	}
		                    }
		                }
		                else
		                {
		                	if(seekerScout){
		                		mode = "seek";
		                		randomDir = randomDirection();
		                	}
		                	else
		                	{
		                		mode = "getgoal";
		                	}
		                	scoutLength = rc.readBroadcast(0);
		                }
		                BulletInfo[] bulletInfo = rc.senseNearbyBullets();
		            	for(int i=0;i<bulletInfo.length;i++){
		            		if(willCollideWithMe(bulletInfo[i])){
		            			Direction tdir = dodgeOneBullet(bulletInfo[i], rc.getLocation());
		            			if(rc.canMove(tdir))
		            			{
		            				rc.move(tdir);
		            				Clock.yield();
			            			break;
		            			}
		            			
		            		}
		            	}
		            	//lock onto enemy robot
		            	Direction dir = myLoc.directionTo(goal);
	            		int count  = 0;
	            		while(!rc.canMove(dir) && count<24){
		            		dir = dir.rotateLeftDegrees(15.0f);
		            		count+=1;
		            	}
	                    if(rc.canMove(dir))
	                    {
	                    	rc.move(dir);
	                    }
		            	
		                //tryMove(randomDirection());
		                Clock.yield();
	            	}
	            	else if(mode == "getgoal")
	            	{
	            		scoutLength = rc.readBroadcast(0);
	            		int[] recent = new int[3];
	            		float[][] locations = new float[3][2];
	            		for(int i=1;i<scoutLength+1;i++){
	                    	int msg = rc.readBroadcast(i);
	                    	if(msg != 0)
	                    	{
	                    		int[] m = fastUnHash(msg);
	                    		for(int j=0;j<3;j++){
	                    			if(m[0]>recent[j])
	                    			{
	                    				recent[j] = m[0];
	                    				locations[j][0] = (float)m[1];
	                    				locations[j][1] = (float)m[2];
	                    				break;
	                    			}
	                    				
	                    		}
	                    		
	                    	}
	                    	
	                    }
	            		//calculate shortest distance
                    	float shortestDistance = myLoc.distanceTo(new MapLocation(locations[0][0],locations[0][1]));
                    	goal = new MapLocation((float)locations[0][0],(float)locations[0][1]);
                    	for(int i=1;i<3;i++){
                    		if(myLoc.distanceTo(new MapLocation(locations[i][0], locations[i][1])) < shortestDistance){
                    			shortestDistance = myLoc.distanceTo(new MapLocation(locations[i][0], locations[i][1]));
                    			goal = new MapLocation((float)locations[i][0],(float)locations[i][1]);
                    		}
                    	}
                    	mode = "seek2";
                    	Clock.yield();
	            	}
	            	else if(mode=="seek2")
	            	{
	            		Direction dir = myLoc.directionTo(goal);
	            		if(myLoc.distanceTo(goal) < 5.0f){
	                    	mode = "getgoal";
	                    	Clock.yield();
	                    }
	            		RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
		                
		                // If there are some...
		                if (robots.length > 0) {
		                	broadcastCounter = 21;
		                	mode = "destroy";
		                }
		                
	            		int count  = 0;
	            		while(!rc.canMove(dir) && count<24){
		            		dir = dir.rotateLeftDegrees(15.0f);
		            		count+=1;
		            	}
	                    if(rc.canMove(dir))
	                    {
	                    	rc.move(dir);
	                    }
	            		Clock.yield();
	            	}
	
	            } catch (Exception e) {
	                System.out.println("Soldier Exception");
	                e.printStackTrace();
	            }
	        }
	}
    static void runSoldier() throws GameActionException {
        System.out.println("I'm an soldier!");
        Team enemy = rc.getTeam().opponent();
        int scoutLength = rc.readBroadcast(0);
        String mode = "getgoal";
        MapLocation goal = rc.getLocation();
        
        // The code you want your robot to perform every round should be in this loop
        while (true) {

            // Try/catch blocks stop unhandled exceptions, which cause your robot to explode
            try {
                MapLocation myLoc = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                if(mode == "getgoal")
                {
                	scoutLength = rc.readBroadcast(0);
                	int[] recent = new int[3];
            		float[][] locations = new float[3][2];
            		for(int i=1;i<scoutLength+1;i++){
                    	int msg = rc.readBroadcast(i);
                    	if(msg != 0)
                    	{
                    		int[] m = fastUnHash(msg);
                    		for(int j=0;j<3;j++){
                    			if(m[0]>recent[j])
                    			{
                    				recent[j] = m[0];
                    				locations[j][0] = (float)m[1];
                    				locations[j][1] = (float)m[2];
                    				break;
                    			}
                    				
                    		}
                    		
                    	}
                    	
                    }
            		//calculate shortest distance
                	float shortestDistance = myLoc.distanceTo(new MapLocation(locations[0][0],locations[0][1]));
                	goal = new MapLocation((float)locations[0][0],(float)locations[0][1]);
                	for(int i=1;i<3;i++){
                		if(myLoc.distanceTo(new MapLocation(locations[i][0], locations[i][1])) < shortestDistance){
                			shortestDistance = myLoc.distanceTo(new MapLocation(locations[i][0], locations[i][1]));
                			goal = new MapLocation((float)locations[i][0],(float)locations[i][1]);
                		}
                	}
                	mode = "destroy";
                }
                else if(mode == "seek")
                {
                		
                	
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
	                    	mode = "getgoal";
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
	            		dir = dir.rotateLeftDegrees(15.0f);
	            		count+=1;
	            	}
                    rc.move(dir);
                    Clock.yield();
                }
                
                
                
                

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
    static void runTank() throws GameActionException{
    	System.out.println("I'm an soldier!");
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
        }
    }
    static void runLumberjack() throws GameActionException {
        System.out.println("I'm a lumberjack!");
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

    /**
     * A slightly more complicated example function, this returns true if the given bullet is on a collision
     * course with the current robot. Doesn't take into account objects between the bullet and this robot.
     *
     * @param bullet The bullet in question
     * @return True if the line of the bullet's path intersects with this robot's current position.
     */
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
    static Direction dodgeOneBullet(BulletInfo bulletinfo, MapLocation loc){
		Direction dir1 = new Direction(bulletinfo.location.x - loc.x, bulletinfo.location.y - loc.y);
		dir1 = dir1.rotateRightDegrees(90.0f);
		return dir1;
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
