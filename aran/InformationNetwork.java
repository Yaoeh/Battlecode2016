package aran;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import battlecode.common.GameActionException;
import battlecode.common.GameConstants;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;

import aran.Constants.InfoEnum;

public interface InformationNetwork {
	/*
	//This is basically the communications class
	//How it works:
	 * UNIT_COUNT||ARCHON INFO || GARDENER INFO || SOLDIER INFO || SCOUT INFO || TANK INFO || LUMBERJACK INFO || GLOBAL INFO
	 * 
	//[0~6], Unit Count: Archon, Gardener, Soldier, Scout, Tank, Lumberjack, respectively
	
	////////////////////
	INTO UPDATED PER UNIT (multiplied by however many tracked): 
	////////////////////
	ARCHON
		Location
		In Danger
		Stuck
		Update time
	GARDENER
		Location
		Update time
	SOLDIER
		Location
		Update time
	SCOUT
		Location
		Update time	
	TANK
		Location
		Update time	
	LUMBERJACK
		Location
		Update time	
	
	////////////////////
	GLOBAL INFORMATION
	////////////////////
	BLACKLIST [20] //list of enemy locations most wanted dead
		Location
		Priority Value
		Update time
		
	call isInfoDefValid() in the test class to check if the set definition is legal
		
	*/
	public abstract void updateOwnInfo(RobotController rc) throws GameActionException;
	
	public final static int ARCHON_COUNT_INDEX= 0;
	public final static int GARDENER_COUNT_INDEX= 1;
	public final static int SOLDIER_COUNT_INDEX= 2;
	public final static int SCOUT_COUNT_INDEX= 3;
	public final static int TANK_COUNT_INDEX= 4;
	public final static int LUMBERJACK_COUNT_INDEX= 5;
	public final static int STARTING_OFFSET= 6;
	
	public final static int NUM_ARCHONS_TRACKED= 3;
	public final static int NUM_GARDENERS_TRACKED= 10;
	public final static int NUM_SOLDIERS_TRACKED= 20;
	public final static int NUM_SCOUTS_TRACKED= 5;
	public final static int NUM_TANKS_TRACKED= 3;
	public final static int NUM_LUMBERJACKS_TRACKED= 5;
	public final static int NUM_BLACKlIST_TRACKED= 10;
	
	public final ArrayList<InfoEnum> ARCH_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.LOCATION,
						InfoEnum.STATUS,
						InfoEnum.UPDATE_TIME
					)
			);
	
	public final ArrayList<InfoEnum> GENERIC_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.LOCATION,
						InfoEnum.UPDATE_TIME
					)
			);
	
	public final ArrayList<InfoEnum> BLACKLIST_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
    					InfoEnum.LOCATION,
    					InfoEnum.PRIORITY,
    					InfoEnum.UPDATE_TIME
					)
			);
	
    static final HashMap<RobotType, Info> unitInfoMap= 
    		new HashMap<RobotType, Info>() {{
    			//Info(reserved channel, then track count)
    			
    			put(RobotType.ARCHON, 
    					new Info(
	    					STARTING_OFFSET, 
	    					ARCH_TRACKED_INFO
    					,NUM_ARCHONS_TRACKED)
				); 
    			
    			put(RobotType.GARDENER, 
    					new Info(
							unitInfoMap.get(RobotType.ARCHON).getNextInfoStartIndex(),
							GENERIC_TRACKED_INFO
    					,NUM_GARDENERS_TRACKED)
				); 
    			
    			put(RobotType.SOLDIER, 
    					new Info(
							unitInfoMap.get(RobotType.GARDENER).getNextInfoStartIndex(),
							GENERIC_TRACKED_INFO
    					,NUM_SOLDIERS_TRACKED)
				); 
    			
    			put(RobotType.SCOUT, 
    					new Info(
							unitInfoMap.get(RobotType.SOLDIER).getNextInfoStartIndex(),
							GENERIC_TRACKED_INFO
    					,NUM_SCOUTS_TRACKED)
				); 
    			
    			put(RobotType.TANK, 
    					new Info(
							unitInfoMap.get(RobotType.SCOUT).getNextInfoStartIndex(),
							GENERIC_TRACKED_INFO
    					,NUM_TANKS_TRACKED)
				); 
    			
    			put(RobotType.LUMBERJACK, 
    					new Info(
							unitInfoMap.get(RobotType.TANK).getNextInfoStartIndex(),
							GENERIC_TRACKED_INFO
    					,NUM_LUMBERJACKS_TRACKED)
				); 
			}};
	
    static final HashMap<String, Info> additionalInfoMap= 
    		new HashMap<String, Info>() {{
    			//Info(reserved channel, then track count)
    			put("Blacklist", 
    					new Info(
							unitInfoMap.get(RobotType.LUMBERJACK).getNextInfoStartIndex(),
							BLACKLIST_TRACKED_INFO
    					,NUM_BLACKlIST_TRACKED)
				);
			}};
	
	public static int getFirstBehindRoundUpdateRobotIndex(RobotController rc) throws GameActionException{ 
		// Returns the first not yet update robot of the round, i.e. stops at
		// the first robot where the update round number is behind the current round number
		// returns Integer.MinValue on fail
		
		
		int answer= Integer.MIN_VALUE;
		if (unitInfoMap.containsKey(rc.getType())){
			//Search for an update time of same type below the current time,
			//If there is no update time below, do nothing
			//else, update the time
			
			Info trackedInfo= unitInfoMap.get(rc.getType());
			for (int i = 0; i < trackedInfo.getTrackCount(); i++){
				int iteratedStartIndex= trackedInfo.getStartIndex()+ trackedInfo.getChannelSize()*i;
				int updateChannelOffset= trackedInfo.getIndex(InfoEnum.UPDATE_TIME); //returns -1 on non-existant enums
				if (updateChannelOffset!= -1){ 
					int lastUpdateTime= rc.readBroadcast(iteratedStartIndex+ updateChannelOffset);
					if (rc.getRoundNum() > lastUpdateTime){
						answer= iteratedStartIndex;
						break;
					}
				}
				
				
			}
		}
		return answer;
	}
	
	public static int getFirstBehindRoundUpdateAddInfoIndex(RobotController rc, String keyname) throws GameActionException{ 
		// Returns the first not yet update robot of the round, i.e. stops at
		// the first robot where the update round number is behind the current round number
		// returns Integer.MinValue on fail
		
		
		int answer= Integer.MIN_VALUE;
		if (additionalInfoMap.containsKey(keyname)){
			//Search for an update time of same type below the current time,
			//If there is no update time below, do nothing
			//else, update the time
			
			Info trackedInfo= additionalInfoMap.get(keyname);
			for (int i = 0; i < trackedInfo.getTrackCount(); i++){
				int iteratedStartIndex= trackedInfo.getStartIndex()+ trackedInfo.getChannelSize()*i;
				int updateChannelOffset= trackedInfo.getIndex(InfoEnum.UPDATE_TIME); //returns -1 on non-existant enums
				if (updateChannelOffset!= -1){ 
					int lastUpdateTime= rc.readBroadcast(iteratedStartIndex+ updateChannelOffset);
					if (rc.getRoundNum() > lastUpdateTime){
						answer= iteratedStartIndex;
						break;
					}
				}
				
				
			}
		}
		return answer;
	}
	
	
	public static int condenseMapLocation(MapLocation loc){
		int answer= Integer.MIN_VALUE;
		answer+= loc.x* Constants.MAP_MAX_WIDTH;
		answer+= loc.y;
		return answer;
	}
	
	public static MapLocation extractMapLocation(int code){
		return new MapLocation(code/Constants.MAP_MAX_WIDTH, code%Constants.MAP_MAX_WIDTH);
	}

	//======================================================
	///TESTING 
	//======================================================
	public static int getTotalNeededChannels(){
		int answer= 0;
		
		answer+= 6; //unit count track locations
		
		for (Info value : unitInfoMap.values()) {
			answer+= value.getNumChannelsNeeded();
		}
		
		for (Info value : additionalInfoMap.values()) {
			answer+= value.getNumChannelsNeeded();
		}

		return answer;
	}
	
	public static boolean isInfoDefValid(){ //to run in test
		boolean answer= false;
		
		System.out.println("Max channels: " +Constants.BROADCAST_MAX_CHANNELS + "/ Needed channels: "+ getTotalNeededChannels());
		if (Constants.BROADCAST_MAX_CHANNELS < getTotalNeededChannels()){
			answer= true;
		}
		return answer;
	}
	
	public static boolean encodingTest(){
		boolean answer= false;
		MapLocation testMapLoc= new MapLocation(999,306);
		int encoded= condenseMapLocation(testMapLoc);
		MapLocation decoded= extractMapLocation(encoded);
		answer= testMapLoc.equals(decoded);
		return answer;
	}
}
