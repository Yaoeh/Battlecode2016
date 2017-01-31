package aran;
import battlecode.common.*;
public class Lumberjack extends RobotPlayer
{
	public static RobotInfo[] robots;
	public static MapLocation myLoc;
	public static Team enemy;
	public static MapLocation goal;
	public static String mode = "seek";
	public static void run(RobotController rc) throws GameActionException {
        enemy = rc.getTeam().opponent();

        MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
    	goal = getClosestLoc(enemyArchonLocs, rc.getLocation());
        
    	int lNum = rc.readBroadcast(4);
    	rc.broadcast(4, lNum+1);
    	
        if(lNum % 2 == 1 && rc.getRoundNum() > 200)
        {
        	myLoc = rc.getLocation();
        	mode = "getgoal";
        	getGoal();
        }
        incrementCountOnSpawn();
        
        while (true) {
            try {            	
            	myLoc = rc.getLocation();
            	robots = rc.senseNearbyRobots(-1,enemy);

                if(robots.length > 0) {
                    killRobot();
                } 
                else {
                	TreeInfo[] enemyTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                	TreeInfo[] neutralTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, Team.NEUTRAL);
                	if(enemyTrees.length == 0 && neutralTrees.length == 0)
                	{
                		//move to goal
                		Direction dir = myLoc.directionTo(goal);
                		Util.tryMove(dir, 30.0f, 4);
                		if(myLoc.distanceTo(goal)<3.0f)
                		{
                			getGoal();
                		}
                	}
                	else
                	{
                		if(enemyTrees.length > 0){
                			if(rc.canChop(enemyTrees[0].location))
                			{
                				rc.chop(enemyTrees[0].location);
                			}
                		}
                		else if(neutralTrees.length > 0){
                			if(rc.canChop(neutralTrees[0].location))
                			{
                				rc.chop(neutralTrees[0].location);
                			}
                		}
                	}
                }
                Clock.yield();

            } catch (Exception e) {
                System.out.println("Lumberjack Exception");
                e.printStackTrace();
            }
        }
    }
	
	public static void getGoal() throws GameActionException
	{
		int[] recent = new int[3];
    	recent[0] = -9999;
    	recent[1] = -9999;
    	recent[2] = -9999;
		int[] indexSave = new int[3];
		int currentRound = rc.getRoundNum();
		for(int i=100;i<120;i++){
        	int round = rc.readBroadcast(i);
        	if(round != -1)
        	{
        		for(int j=0;j<3;j++){
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
    	if(recent[0] != -9999)
    	{
	    	for(int i=0;i<3;i++){
	    		if(recent[i] != -9999)
	    		{
		    		MapLocation loc = new MapLocation(rc.readBroadcast(20+indexSave[i]), rc.readBroadcast(40+indexSave[i]));
		    		if(myLoc.distanceTo(loc) < shortestDistance){
		    			shortestDistance = myLoc.distanceTo(loc);
		    			goal = loc;
		    		}
	    		}
	    	}
	    	mode = "seek";
    	}
	}
	
	
	
	public static void killRobot() throws GameActionException
	{
		float minDistance = 9999.0f;
		int robotIndex = -1;
		for(int i=0;i<robots.length;i++)
		{
			if(myLoc.distanceTo(robots[i].location) < minDistance)
			{
				minDistance = myLoc.distanceTo(robots[i].location);
				robotIndex = i;
			}
		}
		if(minDistance < RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS)
		{
			if(!rc.hasAttacked())
			{
                rc.strike();
			}
		}
		
        boolean flag = Util.tryMove(myLoc.directionTo(robots[robotIndex].location), 20.0f, 3);
        if(!flag)
        {
        	//chop tree if cant move
        	TreeInfo[] enemyTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
        	TreeInfo[] neutralTrees = rc.senseNearbyTrees(GameConstants.LUMBERJACK_STRIKE_RADIUS, Team.NEUTRAL);

    		if(enemyTrees.length > 0){
    			if(rc.canChop(enemyTrees[0].location))
    			{
    				rc.chop(enemyTrees[0].location);
    			}
    		}
    		else if(neutralTrees.length > 0){
    			if(rc.canChop(neutralTrees[0].location))
    			{
    				rc.chop(neutralTrees[0].location);
    			}
    		}
        }
	}
}