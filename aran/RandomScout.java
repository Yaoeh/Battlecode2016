package aran;
import aran.InfoNet;
import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import aran.Info;
import battlecode.common.*;
public class RandomScout extends RobotPlayer
{
	public static Team enemy;
	
	
	public static MapLocation myLoc;
	
	public static Direction seekDir;
	public static int randomRoundCount = 0;
	public static int randomRoundCountLimit = 50;
	public static RobotInfo[] robots;

			
	public static void run(RobotController rc) throws GameActionException {
		incrementCountOnSpawn(); 
		seekDir = Util.randomDirection();
		while (true) {
            try {
            	
            	
                robots = rc.senseNearbyRobots(-1, enemy);
                if(robots.length>0)
                {
                	//go in different direction once robot is found
                	randomRoundCount = 35;
            		seekDir = robots[0].location.directionTo(rc.getLocation());
            		RobotInfo bestRobot = robots[0];
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
            		/*rc.broadcast(channelToUse, currentRound);
            		rc.broadcast(channelToUse+20, (int)bestRobot.location.x);
            		rc.broadcast(channelToUse+40, (int)bestRobot.location.y);*/
                }
                
            	if(!Util.oldDodgeBullets(rc, rc.getLocation()))
                {
            		//Util.tryMove(seekDir, 22.5f, 4);
            		float rotateamount = 15.0f;
                    if(rc.getRoundNum()%180<90){
                    	rotateamount = -15.0f;
                    }
                    Direction dir = seekDir;
                    int count = 0;
                	while(!rc.canMove(dir) && count<24){
                		dir = dir.rotateLeftDegrees(rotateamount);
                		count+=1;
                	}
                	if(count <24)
                	{
                		rc.move(dir);
                		randomRoundCount += 1;
                    	if(randomRoundCount > randomRoundCountLimit){
                    		randomRoundCount = 0;
                    		seekDir = Util.randomDirection();
                    	}
                	}
                }
            	decrementCountOnLowHealth(Constants.LOW_HEALTH_DECREMENT_VALUE);
                Clock.yield();
            } catch (Exception e) {
            }
		}
	}
}