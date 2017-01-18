package AndrewBot;
import battlecode.common.*;
public class Archon extends RobotPlayer
{
	public static void run(RobotController rc) throws GameActionException {
	    double chance = 1.0;
	    double defaultchance = 0.10;
	    rc.broadcast(999, 1);
	    int hold = 2;
	    while (true) {
	        try {
	            Direction dir = randomDirection();
	
	            if (rc.canHireGardener(dir) && Math.random() < chance) {
	            	int builtSoldier = rc.readBroadcast(999);
	            	if(builtSoldier == 1)
	            	{
	            		rc.hireGardener(dir);
		            	rc.broadcast(999, 0);
	            	}
	            }
	            if(!Utility.dodgeBullets(rc, rc.getLocation()))
	            {
	            	hold--;
	            	if(hold < 0)
	            	{
	            		tryMove(randomDirection());
	            		hold = 2;
	            	}
	            }
	            Clock.yield();
	
	        } catch (Exception e) {
	            System.out.println("Archon Exception");
	            e.printStackTrace();
	        }
	    }
	}
}