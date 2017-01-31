package aran;
import aran.InfoNet;
import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import aran.Info;
import battlecode.common.*;
public class Soldier extends RobotPlayer
{
	public static Team enemy;
	public static int scoutLength = 20;
	public static String mode = "getgoal";
	public static MapLocation goal;
	public static MapLocation myLoc;
	public static int seekRoundCount = 21;
	public static int seekRoundCountLimit = 20;
	public static Direction seekDir;
	public static String soldierType = "fighter";
	public static int randomRoundCount = 0;
	public static int randomRoundCountLimit = 15;
	public static int bulletwait = 0;
	public static RobotInfo[] robots;
	public static int broadcastRoundCount = 0;
	public static int broadcastRoundCountLimit = 15;
			
	public static void run(RobotController rc) throws GameActionException {
        enemy = rc.getTeam().opponent();
        goal = rc.getLocation();
        myLoc = rc.getLocation();
        
        
        MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
    	MapLocation closestEnemyArchon= getClosestLoc(enemyArchonLocs, rc.getLocation());
    	
        int soldierN = rc.readBroadcast(2);
        rc.broadcast(2, soldierN+1);
        
        seekDir = Util.randomDirection();
        if(soldierN % 7 == 0)
        {
        	soldierType = "seeker";
        	mode = "seek";
        	//scouting soldier
        	if(soldierN % 20 == 0){
        		//first seek location is enemy archon location.
        		seekDir = rc.getLocation().directionTo(closestEnemyArchon);
        		mode = "destroy";
        	}
        	
        }
        
    	incrementCountOnSpawn(); 
        while (true) {
            try {
            	//infoUpdate();

                myLoc = rc.getLocation();
                robots = rc.senseNearbyRobots(-1, enemy);
                if(mode == "getgoal")
                {
                	getGoal();
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
		bulletwait--;
        if (robots.length > 0) {
            // And we have enough bullets, and haven't attacked yet this turn...
        	RobotInfo bestRobot = getBestRobot();
        	
        	goal = bestRobot.location;
        	if(robots.length > 2 && rc.canFirePentadShot())
        	{
        		rc.firePentadShot(rc.getLocation().directionTo(bestRobot.location));
        	}
        	else if(robots.length > 1 && rc.canFireTriadShot())
        	{
        		rc.fireTriadShot(rc.getLocation().directionTo(bestRobot.location));
        	}
        	else if (rc.canFireSingleShot()) {
                rc.fireSingleShot(rc.getLocation().directionTo(bestRobot.location));
            }
        	bulletwait = 2;
        	
        	//broadcast enemy location
        	broadcastRoundCount -= 1;
        	if(broadcastRoundCount < 0)
        	{
        		broadcastRoundCount = broadcastRoundCountLimit;
        		int lowestRoundCount = rc.getRoundLimit() + 10;
        		for(int i=100;i<120;i++){
        			
        		}
        		
        	}
        }
        else{
            if(myLoc.distanceTo(goal) < 5.0f){
            	mode = "getgoal";
            	return;
            }
        }
        float rotateamount = 15.0f;
        if(rc.getRoundNum()%100<50){
        	rotateamount = -15.0f;
        }
        if(!Util.dodgeBullets(rc, rc.getLocation()))
        {
        	if(bulletwait < 0)
            {
            	Direction dir = myLoc.directionTo(goal);
            	int count = 0;
            	while(!rc.canMove(dir) && count<24){
            		dir = dir.rotateLeftDegrees(rotateamount);
            		count+=1;
            	}
                rc.move(dir);
            }
        }
	}
	
	public static void doSeek() throws GameActionException
	{
        if (robots.length > 0) {
            mode = "destroy";
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
            	if(!Util.tryMove(seekDir, 30, 4))
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
        	Util.tryMove(Util.randomDirection());
        }
	}
	public static void getGoal() throws GameActionException{
		scoutLength = rc.readBroadcast(0);
    	int[] recent = new int[3];
    	recent[0] = -9999;
    	recent[1] = -9999;
    	recent[2] = -9999;
		int[] indexSave = new int[3];
		int currentRound = rc.getRoundNum();
		for(int i=100;i<100+scoutLength;i++){
        	int round = rc.readBroadcast(i);
        	if(round != -1)
        	{
        		for(int j=0;j<3;j++){
        			if(round>recent[j] && round - currentRound > Constants.MessageValidTime)
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
		    		MapLocation loc = new MapLocation(rc.readBroadcast(120+recent[i]), rc.readBroadcast(140+recent[i]));
		    		if(myLoc.distanceTo(loc) < shortestDistance){
		    			shortestDistance = myLoc.distanceTo(loc);
		    			goal = loc;
		    		}
	    		}
	    	}
	    	mode = "destroy";
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
}