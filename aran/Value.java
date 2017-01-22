package aran;

import battlecode.common.BodyInfo;
import battlecode.common.BulletInfo;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotInfo;
import battlecode.common.TreeInfo;

public class Value {
	
	public static MapLocation getClosestLoc(MapLocation[] locs, MapLocation ref, int maxConsidered){
		MapLocation cloestLoc= null;
		if (locs!= null){
			if (locs.length> 0){
				cloestLoc= locs[0];
				float shortestLength= ref.distanceTo(cloestLoc);
				for (int i = 1; i< clamp(locs.length,0,maxConsidered); i++){
					float candidateDis= ref.distanceTo(locs[i]);
					if (candidateDis < shortestLength){
						shortestLength= candidateDis;
						cloestLoc= locs[i];
					}
				}
			}
		}
		return cloestLoc;
	}
	
	public static BodyInfo getClosestBody(BodyInfo[] bodies, MapLocation ref, int maxConsidered){
		BodyInfo closestBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				closestBody= bodies[0];
				float shortestLength= ref.distanceTo(closestBody.getLocation());
				for (int i = 1; i< clamp(bodies.length,0,maxConsidered); i++){
					float candidateDis= ref.distanceTo(bodies[i].getLocation());
					if (candidateDis < shortestLength){
						shortestLength= candidateDis;
						closestBody= bodies[i];
					}
				}
			}
		}
		return closestBody;
	}
	
	
	
	public static BodyInfo getHighestPriorityBody(RobotController rc, RobotInfo[] bodies, MapLocation ref, int maxConsidered){
		RobotInfo priorityBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				priorityBody= bodies[0];
				float largestPriority= getDamagePriority(priorityBody);
				for (int i = 1; i< clamp(bodies.length,0 , maxConsidered); i++){
					float candidatePriority = getDamagePriority(bodies[i]) *getHitChance(rc, bodies[i]);
					if (candidatePriority > largestPriority){
						largestPriority= candidatePriority;
						priorityBody= bodies[i];
					}
				}
			}
		}
		return priorityBody;
	}
	
	public static TreeInfo getTastiestBody(RobotController rc, TreeInfo[] bodies, MapLocation ref, int maxConsidered) throws GameActionException{
		TreeInfo priorityBody= null;
		if (bodies!= null){
			if (bodies.length> 0){
				priorityBody= bodies[0];
				int largestPriority= (int) getTastiness(priorityBody,rc);
				for (int i = 1; i< clamp(bodies.length,0 , maxConsidered); i++){
					int candidatePriority = (int) getTastiness(bodies[i],rc);
					rc.setIndicatorDot(bodies[i].location, (int) clamp(candidatePriority,0,255) , 0, 0);
					if (candidatePriority > largestPriority){
						largestPriority= candidatePriority;
						priorityBody= bodies[i];
					}
				}
			}
		}
		return priorityBody;
	}
		
	public static float getDamagePriority(RobotInfo ri){ //higher number, greater the priority
		//Greater the cost more valuable the damage
		//Greater the health, lower the priority
		//Greater the attack power, greater the priority
		
		return (float) (ri.getType().bulletCost- ri.getHealth()+ ri.getType().attackPower);
	}
	
	public static float getHitChance(RobotController rc, RobotInfo ri){
		//further away, the lower the chance
		//slower the stride radius, larger the chance
		//closer the target larger the chance
		return rc.getType().bulletSpeed/(rc.getLocation().distanceTo(ri.location)+ri.getType().strideRadius);
	}
	
	public static double getCharisma(RobotInfo ri){
		return ri.health;
	}
	
	public static double getTastiness(TreeInfo ti, RobotController rc){
		if (rc.canInteractWithTree(ti.location) && ti.containedBullets> 0){
			return ti.containedBullets + ti.health/50;
		}else{
			return 0;
		}
	}
	
	public static double getDanger(BulletInfo bi, RobotController rc){
		return bi.damage/rc.getHealth(); 
	}
	
    
	
	public static float clamp(float val, float min, float max) {
	    return Math.max(min, Math.min(max, val));
	}
}
