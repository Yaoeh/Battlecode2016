package aran;
import aran.InfoNet;
import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import aran.Info;
import battlecode.common.*;
public class Soldier extends RobotPlayer
{
	public static void run(RobotController rc) throws GameActionException {
        Team enemy = rc.getTeam().opponent();
        int scoutLength = rc.readBroadcast(0);
        String mode = "getgoal";
        MapLocation goal = rc.getLocation();
        int bulletwait = 0;
        int seekRounds = 25;
        
        //
        while (true) {
            try {
            	infoUpdate();
            	
                MapLocation myLoc = rc.getLocation();

                // See if there are any nearby enemy robots
                RobotInfo[] robots = rc.senseNearbyRobots(-1, enemy);
                if(mode == "getgoal")
                {
                	scoutLength = rc.readBroadcast(0);
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
                		goal = new MapLocation((float)locations[index][0],(float)locations[index][1]);
                		mode = "destroy";
                	}
                	else{
                		mode = "seek";
                		seekRounds = 25;
                	}
                		
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
                            bulletwait = 2;
                        }
                        mode = "destroy";
                    }
                    else{
                    	seekRounds --;
                    	if(seekRounds < 0){
                    		mode = "getgoal";
                    	}
                    }
                  //dodge any bullets
	            	
                	// Move randomly
                    Util.tryMove(Util.randomDirection());

                    // Clock.yield() makes the robot wait until the next turn, then it will perform this loop again
                    Clock.yield();
                }
                else if(mode=="destroy")
                {
                	bulletwait--;
                	// If there are some...
                    if (robots.length > 0) {
                        // And we have enough bullets, and haven't attacked yet this turn...
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
                    	goal = robots[bestrobot].location;
                    	if(robots.length > 3 && rc.canFirePentadShot())
                    	{
                    		rc.firePentadShot(rc.getLocation().directionTo(robots[bestrobot].location));
                    		bulletwait = 2;
                    		Clock.yield();
                    	}
                    	else if(robots.length > 2 && rc.canFireTriadShot())
                    	{
                    		rc.fireTriadShot(rc.getLocation().directionTo(robots[bestrobot].location));
                    		bulletwait = 2;
                    		Clock.yield();
                    	}
                    	else if (rc.canFireSingleShot()) {
                            // ...Then fire a bullet in the direction of the enemy.
                            rc.fireSingleShot(rc.getLocation().directionTo(robots[bestrobot].location));
                            bulletwait = 2;
                            Clock.yield();
                        }
                    }
                    else{
	                    if(myLoc.distanceTo(goal) < 5.0f){
	                    	mode = "getgoal";
	                    	Clock.yield();
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
	                Clock.yield();
                }
                
                
                
                

            } catch (Exception e) {
                System.out.println("Soldier Exception");
                e.printStackTrace();
            }
        }
    }
	
	public static void updateOwnInfo() throws GameActionException {
		Info trackedInfo= InfoNet.unitInfoMap.get(rc.getType());
		int indexOffset= InfoNet.getFirstBehindRoundUpdateRobotIndex(rc); //starting index of an not updated robot type
				
		if (indexOffset!= Integer.MIN_VALUE){
			//scoutNum= (indexOffset - trackedInfo.getStartIndex()) / trackedInfo.reservedChannels.size(); //number in the info net slot
			if (indexOffset== InfoNet.unitInfoMap.get(rc.getType()).getStartIndex()){
				firstOfType= true;
			}
			
			for (int i = 0; i < trackedInfo.reservedChannels.size(); i++){ //Iterate through all needed channels
				InfoEnum state= trackedInfo.getInfoEnum(i);
				
				switch (state) {
					case UPDATE_TIME:
						broadcastPrint(rc,indexOffset+ i, rc.getRoundNum(), "time");
						break;
					case ID:
						broadcastPrint(rc, indexOffset+i, rc.getID());
					default:
						break;
				}
			}
		}else{
			//System.out.println("Index offset returning a failed number: " + indexOffset);
		}
	}
	
    public static void updateUnitCounts() throws GameActionException{
    	//In case the archon dies, this runs. Only works for the first of the unit
    	//!!! Gardener can check the validity of the unit count by checking first if the archon is dead, and then if the first of the unit type is dead

    	if (firstOfType){
	    	Info archonAliveInfo= InfoNet.unitInfoMap.get(RobotType.ARCHON);
	    	boolean archonDead= false;
	    	int firstArchonUpdateIndex= archonAliveInfo.getStartIndex()+ archonAliveInfo.getIndex(InfoEnum.UPDATE_TIME);
	    	int archonUpdateTime= rc.readBroadcast(firstArchonUpdateIndex);
	    	if (archonUpdateTime- rc.getRoundNum() > Constants.DEAD_TOLERANCE_ROUNDNUM){ //if archon is past due of update, it is presumed dead
	    		archonDead= true;
	    	}
	    	
	    	if (archonDead){
	    		//Only initiate the update if you are the first guy. THe dead tolerance has to be greater than the update time. 
	    		//This is set in the tests

				Info trackedInfo= InfoNet.addInfoMap.get(AddInfo.UNITCOUNT);
				int unitCount= InfoNet.countUnits(rc, rc.getType());
				int broadcastIndex= trackedInfo.getStartIndex()+ trackedInfo.getIndex(InfoEnum.SOLDIER_COUNT);
				broadcastPrint(rc, broadcastIndex, unitCount, "Soldier count");
	    	}
    	}
    }
}