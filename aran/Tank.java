package aran;

import java.util.Random;

import battlecode.common.BodyInfo;
import battlecode.common.Clock;
import battlecode.common.Direction;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.RobotType;
import battlecode.common.Team;
import battlecode.common.TreeInfo;

public class Tank extends RobotPlayer{
	public static Team enemy;
	public static String mode = "getgoal";
	public static MapLocation goal;
	public static MapLocation myLoc;
	public static int seekRoundCount = 41;
	public static int seekRoundCountLimit = 40;
	public static Direction seekDir;
	public static String soldierType = "fighter";
	public static int randomRoundCount = 0;
	public static int randomRoundCountLimit = 15;
	public static int bulletwait = 0;
	public static RobotInfo[] robots;
	public static int broadcastRoundCount = 0;
	public static int broadcastRoundCountLimit = 10;
	public static int lastIndexSave = 0;
	public static int destroyRoundCount = 0;
	public static int destroyRoundCountLimit = 20;
	public static void run(RobotController rc) throws GameActionException {
        enemy = rc.getTeam().opponent();
        goal = rc.getLocation();
        myLoc = rc.getLocation();
        
        MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
    	MapLocation closestEnemyArchon= getClosestLoc(enemyArchonLocs, rc.getLocation());
    	
        int soldierN = rc.readBroadcast(2);
        rc.broadcast(2, soldierN+1);
        
        seekDir = Util.randomDirection();
        if(soldierN % 6 < 4)
        {
        	soldierType = "seeker";
        	mode = "seek";
        	//scouting soldier
        	if(soldierN % 20 == 0){
        		//first seek location is enemy archon location.
        		//seekDir = rc.getLocation().directionTo(closestEnemyArchon);
        		goal = closestEnemyArchon;
        		mode = "destroy";
        		destroyRoundCount = 0;
        	}
        	
        }
        
    	incrementCountOnSpawn(); 
        while (true) {
            try {
            	//infoUpdate();
            	
                myLoc = rc.getLocation();
                robots = rc.senseNearbyRobots(-1, enemy);
                boolean wasGetGoal = false;
                if(mode == "getgoal")
                {
                	getGoal();
                	wasGetGoal = true;
                }
                else if(mode == "random")
                {
                	doRandom();
                }
                else if(mode == "seek")
                {
                	doSeek();
                }
                
                if(mode=="destroy")
                {
                	doDestroy();
                	if(mode == "getgoal" && wasGetGoal)
                	{
                		mode = "random";
                		randomRoundCount = 0;
                		
                	}
		        }
                decrementCountOnLowHealth(Constants.LOW_HEALTH_DECREMENT_VALUE);
                Clock.yield();
            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
	
	public static void doDestroy() throws GameActionException
	{
		float rotateamount = 15.0f;
        if(rc.getRoundNum()%180<90){
        	rotateamount = -15.0f;
        }
        if(!Util.dodgeBullets(rc, rc.getLocation()))
        {
        	if(bulletwait < 1) ///change back to 0
            {
            	Direction dir = myLoc.directionTo(goal);
            	int count = 0;
            	while(!rc.canMove(dir) && count<24){
            		dir = dir.rotateLeftDegrees(rotateamount);
            		count+=1;
            	}
            	if(count <24)
            	{
            		rc.move(dir);
            	}
            }
        }
        
		bulletwait--;
		boolean firedFlag = false;
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
        	RobotInfo bestRobot = getBestRobot();
        	firedFlag = true;
        	goal = bestRobot.location;
        	Direction tdir = rc.getLocation().directionTo(bestRobot.location);
        	if(bestRobot.type == RobotType.GARDENER && rc.canFirePentadShot() && willShootTeammateOrNeutralTree(myLoc, tdir, "pentad"))
        	{
        		rc.firePentadShot(tdir);
        	}
        	else if(robots.length > 2 && rc.canFirePentadShot() && willShootTeammateOrNeutralTree(myLoc, tdir, "pentad"))
        	{
        		rc.firePentadShot(tdir);
        	}
        	else if(robots.length > 1 && rc.canFireTriadShot() && willShootTeammateOrNeutralTree(myLoc, tdir, "triad"))
        	{
        		rc.fireTriadShot(tdir);
        	}
        	else if (rc.canFireSingleShot() && willShootTeammateOrNeutralTree(myLoc, tdir, "single")) {
                rc.fireSingleShot(tdir);
            }
        	bulletwait = 0;
        	
        	//broadcast enemy location
        	broadcastRoundCount -= 1;
        	if(broadcastRoundCount < 0)
        	{
        		broadcastRoundCount = broadcastRoundCountLimit;
        		int lowestRoundCount = rc.getRoundLimit() + 10;
        		int channelToUse = 119;
        		int currentRound = rc.getRoundNum();
        		for(int i=100;i<120;i++){
        			int readRound = rc.readBroadcast(i);
        			if(readRound == -1 || currentRound - readRound > Constants.MessageValidTime)
        			{
        				channelToUse = i;
        				break;
        			}
        			if(readRound < lowestRoundCount)
        			{
        				lowestRoundCount = readRound;
        				channelToUse = i;
        			}
        		}
        		rc.broadcast(channelToUse, currentRound);
        		rc.broadcast(channelToUse+20, (int)bestRobot.location.x);
        		rc.broadcast(channelToUse+40, (int)bestRobot.location.y);
        		
        	}
        }
        else{
            if(myLoc.distanceTo(goal) < 5.0f){
            	//rc.broadcast(lastIndexSave, -9999);
            	if(soldierType == "fighter")
            	{
            		mode = "getgoal";
            	}
            	else{
            		mode = "seek";
            	}
            	
            	return;
            }
            else
            {
            	destroyRoundCount += 1;
            	if(destroyRoundCount > destroyRoundCountLimit)
            	{
            		if(soldierType == "fighter")
                	{
                		mode = "getgoal";
                	}
                	else{
                		mode = "seek";
                	}
            	}
            }
        }
        
	}
	
	public static void doSeek() throws GameActionException
	{
        if (robots.length > 0) {
            mode = "destroy";
            destroyRoundCount = 0;
            broadcastRoundCount = 0;
        }
        else{
        	seekRoundCount += 1;
        	if(seekRoundCount > seekRoundCountLimit)
        	{
        		seekRoundCount = 0;
        		seekDir = Util.randomDirection();
        	}
        	//dodge any bullets
            if(!Util.dodgeBullets(rc, rc.getLocation()))
            {
            	if(!Util.tryMove(seekDir, 30.0f, 4))
            	{
            		seekRoundCount = seekRoundCountLimit;
            	}
            }
        }
        
	}
	public static void doRandom() throws GameActionException
	{
        if (robots.length > 0) {
            mode = "destroy";
            broadcastRoundCount = 0;
            destroyRoundCount = 0;
        }
        else{
        	randomRoundCount += 1;
        	if(randomRoundCount > randomRoundCountLimit)
        	{
        		mode = "getgoal";
        	}
        }
        //dodge any bullets
        if(!Util.dodgeBullets(rc, rc.getLocation()))
        {
        	//Util.tryMove(Util.randomDirection());
        }
	}
	public static void getGoal() throws GameActionException{
    	int[] recent = new int[4];
    	recent[0] = -9999;
    	recent[1] = -9999;
    	recent[2] = -9999;
    	recent[3] = -9999;
		int[] indexSave = new int[4];
		int currentRound = rc.getRoundNum();
		for(int i=100;i<120;i++){
        	int round = rc.readBroadcast(i);
        	if(round != -1)
        	{
        		for(int j=0;j<4;j++){
        			if(round>recent[j] && currentRound - round < Constants.MessageValidTime)
        			{
        				recent[j] = round;
        				indexSave[j] = i;
        				break;
        			}
        				
        		}
        		
        	}
        	
        }
		//calculate shortest distance
    	float shortestDistance = 99999.0f;
    	
    	if(recent[0] > 0)
    	{
    		int recentCount = 0;
	    	for(int i=0;i<4;i++){
	    		if(recent[i] > 0)
	    		{
	    			recentCount+=1;
		    		MapLocation loc = new MapLocation(rc.readBroadcast(20+indexSave[i]), rc.readBroadcast(40+indexSave[i]));
		    		if(myLoc.distanceTo(loc) < shortestDistance){
		    			shortestDistance = myLoc.distanceTo(loc);
		    			goal = loc;
		    			lastIndexSave = indexSave[i];
		    		}
	    		}
	    	}
	    	if(Math.random() < 0.3)
	    	{
	    		Random a = new Random();
	    		int randomNum = a.nextInt(recentCount);
	    		goal = new MapLocation(rc.readBroadcast(20+indexSave[randomNum]), rc.readBroadcast(40+indexSave[randomNum]));
	    	}
	    	mode = "destroy";
	    	broadcastRoundCount = 0;
	    	destroyRoundCount = 0;
    	}
    	else
    	{
    		mode = "random";
    		randomRoundCount = 0;
    	}
	}
	public static RobotInfo getBestRobot() throws GameActionException
	{
		int bestrobot = 0;
    	int maxvalue = 0;
    	for(int i=0;i<robots.length;i++){
    		if(robots[i].type == RobotType.SOLDIER || robots[i].type == RobotType.TANK || robots[i].type == RobotType.LUMBERJACK)
    		{
    			if(maxvalue<4){
    				maxvalue = 4;
    				bestrobot = i;
    				break;
    			}
    		}
    		else if(robots[i].type == RobotType.SCOUT){
    			if(maxvalue<3){
    				maxvalue = 3;
    				bestrobot = i;
    			}
    		}
    		else if(robots[i].type == RobotType.GARDENER){
    			if(maxvalue<2){
    				maxvalue = 2;
    				bestrobot = i;
    			}
    		}
    		else if(robots[i].type == RobotType.ARCHON){
    			if(maxvalue<1){
    				maxvalue = 1;
    				bestrobot = i;
    			}
    		}
    		
    	}
    	return robots[bestrobot];
	}
	
	static boolean willShootTeammateOrNeutralTree(MapLocation bulletLocation, Direction dir, String shotType) throws GameActionException
	{
		bulletLocation = myLoc;
		RobotInfo[] nearbyFriends= rc.senseNearbyRobots(-1, rc.getTeam());
		TreeInfo[] nearbyNeutralTrees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
		float[] rotateAngles = new float[]{0.0f};
		if(shotType == "pentad")
		{
			rotateAngles = new float[]{-30.0f, -15.0f, 0.0f, 15.0f, 30.0f};
		}
		else if(shotType == "triad")
		{
			rotateAngles = new float[]{-20.0f, 0.0f, 20.0f};
		}
		for(int i=0;i<nearbyFriends.length;i++){
			for(int j=0;j<rotateAngles.length;j++){
				Direction tdir = dir.rotateLeftDegrees(rotateAngles[j]);
				if(willCollideWithBody(myLoc, tdir, nearbyFriends[i]))
				{
					return false;
				}
			}
		}
		for(int i=0;i<nearbyNeutralTrees.length;i++){
			for(int j=0;j<rotateAngles.length;j++){
				Direction tdir = dir.rotateLeftDegrees(rotateAngles[j]);
				if(willCollideWithBody(myLoc, tdir, nearbyNeutralTrees[i]))
				{
					return false;
				}
			}
		}
		
		return true;
	}
	
	static boolean willCollideWithBody(MapLocation bulletLocation, Direction propagationDirection,  BodyInfo bi) throws GameActionException 
	{
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
}