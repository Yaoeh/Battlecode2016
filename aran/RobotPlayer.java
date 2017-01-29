package aran;
import java.util.HashSet;

import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;
import battlecode.common.*;

public strictfp class RobotPlayer{
    static RobotController rc;
    static Sensor sensor= new Sensor();
    static boolean firstOfType= false;
    /**
     * run() is the method that is called when a robot is instantiated in the Battlecode world.
     * If this method returns, the robot dies!
    **/
    @SuppressWarnings("unused")
    public static void run(RobotController rc) throws GameActionException {

        // This is the RobotController object. You use it to perform actions from this robot,
        // and to get information on its current status.
        RobotPlayer.rc = rc;
    	
        // Here, we've separated the controls into a different method for each RobotType.
        // You can add the missing ones or rewrite this into your own control structure.
        switch (rc.getType()) {
	        case ARCHON:
	            Archon.run(rc);
	            break;
	        case GARDENER:
	            Gardener.run(rc);
	            break;
	        case SOLDIER:
	            Soldier.run(rc);
	            break;
	        case SCOUT:
	        	ScoutingScout.run(rc);
	            break;
	        case TANK:
	            Tank.run(rc);
	        case LUMBERJACK:
	            Lumberjack.run(rc);
	            break;
	    }
	}
    
    public static void removeGoalOnFound() throws GameActionException{
    	if (sensor.goalLoc!= null){ //remove goals once you reach them
    		if (rc.canSenseLocation(sensor.goalLoc) && rc.getLocation().distanceTo(sensor.goalLoc) <= rc.getType().bodyRadius){
    			sensor.goalLoc= null;
    		}
    	}
    }

    public static void notMoveGeneric(int[] profile, float[] rads) throws GameActionException{ //incomplete, base on profiles
    	sensor.senseTrees(rc);
		sensor.senseFriends(rc);
		sensor.senseEnemies(rc);
    	if(rc.getType().canAttack() && sensor.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) Value.getHighestPriorityBody(rc, sensor.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		sensor.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
		sensor.senseBullets(rc);
		removeGoalOnFound();
    	if (rc.canShake() && sensor.nearbyNeutralTrees!=null && sensor.nearbyNeutralTrees.length > 0){
    		sensor.tryShakeTree(rc);
        }    	
    }
    
   
    public static void notMoveGeneric() throws GameActionException{
    	sensor.senseTrees(rc);
		sensor.senseFriends(rc);
		sensor.senseEnemies(rc, rc.getType().sensorRadius);
    	if(rc.getType().canAttack() && sensor.nearbyEnemies.length > 0 && !rc.hasAttacked()) {        	
        	RobotInfo highPRobotInfo= (RobotInfo) Value.getHighestPriorityBody(rc, sensor.nearbyEnemies,rc.getLocation(), Integer.MAX_VALUE);
    		sensor.tryfireSingleShot(rc, highPRobotInfo.getLocation());
    	}
		sensor.senseBullets(rc);
		sensor.senseEnemies(rc, rc.getType().sensorRadius-2); //this is how you move, not how you shoot
		removeGoalOnFound();
    	if (rc.canShake() && sensor.nearbyNeutralTrees!=null && sensor.nearbyNeutralTrees.length > 0){
    		sensor.tryShakeTree(rc);
        }    	
    }
    
    public static void broadcastPrint(RobotController rc, int channel, int message) throws GameActionException{
    	System.out.println("\tChannel: " + channel + ": "+ message);
    	rc.broadcast(channel, message);
    }
    public static void broadcastPrint(RobotController rc, int channel, int message, String descript) throws GameActionException{
    	System.out.println("\t"+descript+ " Channel: " + channel + ": "+ message);
    	rc.broadcast(channel, message);
    }
    
    static int fastHash(int rounds, int x1, int y1){
    	return rounds*600*600 + x1*600 + y1;
    }
    static int[] fastUnHash(int m){
    	int[] ans = new int[3];
    	
    	ans[0] = m/600/600;
    	m -= ans[0]*600*600;
    	ans[1] = m/600;
    	ans[2] = m%600;
    	
    	
    	return ans;
    }
    
    public static MapLocation getClosestLoc(MapLocation[] locs, MapLocation ref){
		MapLocation cloestLoc= locs[0];

		float shortestLength= ref.distanceTo(cloestLoc);
		for (int i = 1; i< locs.length; i++){
			float candidateDis= ref.distanceTo(locs[i]);
			if (candidateDis < shortestLength){
				shortestLength= candidateDis;
				cloestLoc= locs[i];
			}
		}

		return cloestLoc;
	}
    
    public static void infoUpdate() throws GameActionException{
		if (rc.getRoundNum()%Constants.UNIT_COUNT_UPDATE_OFFSET== 0){
			updateOwnInfo(); 
			updateUnitCounts();
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
