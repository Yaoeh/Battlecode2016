package aran_v2;
import battlecode.common.*;
public class Lumberjack extends RobotPlayer
{
	public static void run(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        String mode = "seek";
        MapLocation goalTreeLoc = rc.getLocation();
        MapLocation movegoal = rc.getLocation();
        Direction randomDir = Util.randomDirection();
        int seekrounds = 40;
        Clock.yield();
        while (true) {
            try {
            	MapLocation myLoc = rc.getLocation();
                RobotInfo[] robots = rc.senseNearbyRobots(RobotType.LUMBERJACK.bodyRadius+GameConstants.LUMBERJACK_STRIKE_RADIUS, enemy);
                if(robots.length > 0 && !rc.hasAttacked()) {
                    rc.strike();
                } else {
                    // No close robots, so search for robots within sight radius
                    robots = rc.senseNearbyRobots(-1,enemy);

                    // If there is a robot, move towards it
                    if(robots.length > 0) {
                        MapLocation myLocation = rc.getLocation();
                        MapLocation enemyLocation = robots[0].getLocation();
                        Direction toEnemy = myLocation.directionTo(enemyLocation);

                        Util.tryMove(toEnemy);
                        Clock.yield();
                    } 
                    else {
                    	if(mode == "seek")
                    	{
                    		
	                        // look for trees
	                    	TreeInfo[] trees = rc.senseNearbyTrees(-1, Team.NEUTRAL);
	                    	if(trees.length > 0)
	                    	{
	                    		//choose closest tree
	                    		float minDistance = 99999.0f;
	                    		for(int i=0;i<trees.length;i++){
	                    			if(myLoc.distanceTo(trees[i].location) < minDistance)
	                    			{
	                    				minDistance = myLoc.distanceTo(trees[i].location);
	                    				goalTreeLoc = trees[i].location;
	                    			}
	                    		}
	                    		mode = "chopper";
	                    		Clock.yield();
	                    	}
	                    	else
	                    	{
	                    		//move toward signals
	                    		int scoutLength = rc.readBroadcast(0);
	                        	int[] recent = new int[3];
	                        	recent[0] = -9999;
	                        	recent[1] = -9999;
	                        	recent[2] = -9999;
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
	                        	float shortestDistance = 99999.0f;
	                        	int index = -1;
	                        	//goal = new MapLocation((float)locations[0][0],(float)locations[0][1]);
	                        	for(int i=1;i<3;i++){
	                        		if(myLoc.distanceTo(new MapLocation(locations[i][0], locations[i][1])) < shortestDistance && rc.getRoundNum() - recent[i] < 60){
	                        			shortestDistance = myLoc.distanceTo(new MapLocation(locations[i][0], locations[i][1]));
	                        			index = i;
	                        			//goal = new MapLocation((float)locations[i][0],(float)locations[i][1]);
	                        		}
	                        	}
	                        	if(index != -1)
	                        	{
	                        		movegoal = new MapLocation((float)locations[index][0],(float)locations[index][1]);
	                        		mode = "moveseek";
	                        		seekrounds = 40;
	                        	}
	                        	else{
	                        		while(!rc.canMove(randomDir))
	        	            		{
	        	            			randomDir = Util.randomDirection();
	        	            		}
	        	            		rc.move(randomDir);
	                        	}
	                    	}
	                    	Clock.yield();
                    	}
                    	else if(mode == "chopper"){
                    		if(rc.canChop(goalTreeLoc)) //chop tree
                    		{
                    			rc.chop(goalTreeLoc);
                    			if(rc.senseTreeAtLocation(goalTreeLoc) == null)
                    			{
                    				mode = "seek";
                    			}
                    		}
                    		else{
                    			//move toward tree
                    			Direction dir = myLoc.directionTo(goalTreeLoc);
                    			int count = 0;
                    			float rotateamount = 15.0f;
                                if(rc.getRoundNum()%100<50){
                                	rotateamount = -15.0f;
                                }
    			            	while(!rc.canMove(dir) && count<24){
    			            		dir = dir.rotateLeftDegrees(rotateamount);
    			            		count+=1;
    			            	}
    			            	Util.tryMove(dir);
    			            	//rc.move(dir);
    		                    Clock.yield();
                    		}
                    	}
                    	else if(mode == "moveseek")
                    	{
                    		seekrounds --;
                    		//rc.setIndicatorDot(myLoc, 100, 100, 100);
                    		if(myLoc.distanceTo(movegoal) < 5.0f || seekrounds < 0){
                    			mode = "seek";
                    		}
                    		Direction dir = myLoc.directionTo(movegoal);
                			int count = 0;
                			float rotateamount = 15.0f;
                            if(rc.getRoundNum()%100<50){
                            	rotateamount = -15.0f;
                            }
			            	while(!rc.canMove(dir) && count<24){
			            		dir = dir.rotateLeftDegrees(rotateamount);
			            		count+=1;
			            	}
		                    rc.move(dir);
		                    Clock.yield();
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
}