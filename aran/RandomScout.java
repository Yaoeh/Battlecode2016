package aran;
import aran.InfoNet;
import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import aran.Info;
import battlecode.common.*;
public class RandomScout extends RobotPlayer
{
	public static Team enemy;
	public static String mode = "getgoal";
	public static MapLocation goal;
	public static MapLocation myLoc;
	
	public static Direction seekDir;
	public static int randomRoundCount = 0;
	public static int randomRoundCountLimit = 20;
	public static RobotInfo[] robots;

			
	public static void run(RobotController rc) throws GameActionException {
		incrementCountOnSpawn(); 
		seekDir = Util.randomDirection();
		while (true) {
            try {
            	randomRoundCount += 1;
            	if(randomRoundCount > randomRoundCountLimit){
            		randomRoundCount = 0;
            		seekDir = Util.randomDirection();
            	}
            	if(!Util.dodgeBullets(rc, rc.getLocation()))
                {
            		Util.tryMove(seekDir, 22.5f, 6);
                }
            	
                robots = rc.senseNearbyRobots(-1, enemy);
                if(robots.length>0)
                {
                	//go in different direction once robot is found
                	randomRoundCount = 0;
            		seekDir = Util.randomDirection();
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
            		rc.broadcast(channelToUse, currentRound);
            		rc.broadcast(channelToUse+20, (int)bestRobot.location.x);
            		rc.broadcast(channelToUse+40, (int)bestRobot.location.y);
                }
            	
            	decrementCountOnLowHealth(Constants.LOW_HEALTH_DECREMENT_VALUE);
                Clock.yield();
            } catch (Exception e) {
            }
		}
	}
}