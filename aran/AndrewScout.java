package aran;
import battlecode.common.*;
public class AndrewScout extends RobotPlayer
{
	public static void run(RobotController rc) throws GameActionException{
		MapLocation[] enemyArchonLocs = rc.getInitialArchonLocations(rc.getTeam().opponent());
		MapLocation closestEnemyArchon= getClosestLoc(enemyArchonLocs, rc.getLocation());
		String mode = "seekinit";
		if(rc.getRoundNum()>200)
		{
			mode = "seek2";
		}
		Team enemy = rc.getTeam().opponent();
		 MapLocation goal = rc.getLocation();
		int scoutID = rc.readBroadcast(0) + 1;
		rc.broadcast(0, scoutID);
		int broadcastCounter = 21;
		int scoutLength = 0;
		boolean seekerScout = false;
		Direction randomDir = goal.directionTo(closestEnemyArchon);
		int bulletwait = 0;
		if(Math.random() < 0.5)
		{
			seekerScout = true;
		}
		incrementCountOnSpawn();
		 while (true) {
	            try {
	            	//infoUpdate();
	            	MapLocation myLoc = rc.getLocation();
	            	if(mode == "seekinit")//initially go to archon spawn
	            	{
		            	Direction dir;
		            	
		            	dir = myLoc.directionTo(closestEnemyArchon);
		            	dir = Util.getDodgeBulletDirection(rc, myLoc, dir);
	
		            	// See if there are any nearby enemy robots
		                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
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
	            	else if(mode == "seek")//find robots to kill by traveling in A random direction until you need to rotate
	            	{
	            		RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
		                
		                // If there are some...
		                if (robots.length > 0) {
		                	broadcastCounter = 21;
		                	mode = "destroy";
		                }
	            		while(!rc.canMove(randomDir))
	            		{
	            			randomDir = Util.randomDirection();
	            		}
	            		rc.move(randomDir);
	            		Clock.yield();
	            	}
	            	else if(mode == "destroy"){
	            		// See if there are any nearby enemy robots
		                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
		                broadcastCounter ++;
		                bulletwait--;
		                //broadcast enemy location every 20 turns
		                if (robots.length > 0) {
		                	MapLocation robLocation = robots[0].getLocation();
		                	if(broadcastCounter>20)
		                	{
			                	int hash = fastHash(rc.getRoundNum(), (int)robLocation.x, (int)robLocation.y);
		                		rc.broadcast(scoutID, hash);
		                		broadcastCounter = 0;
		                	}
		                    if (rc.canFireSingleShot() && bulletwait < 0) {
		                        rc.fireSingleShot(rc.getLocation().directionTo(robLocation));
		                        bulletwait = 5;
		                        Clock.yield();
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
		                		randomDir = Util.randomDirection();
		                	}
		                	else
		                	{
		                		mode = "getgoal";
		                	}
		                	scoutLength = rc.readBroadcast(0);
		                }
		                
		                if(!Util.dodgeBullets(rc, rc.getLocation()))
			            {  
		                	if(bulletwait < 0)
			                {
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
			                }
			            }
		                

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
	                    
	                    decrementCountOnLowHealth(5);
	            		Clock.yield();
	            	}
	
	            } catch (Exception e) {
	                System.out.println("Soldier Exception");
	                e.printStackTrace();
	            }
	        }
	}
}