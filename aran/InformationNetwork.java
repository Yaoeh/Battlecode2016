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

public class InformationNetwork {
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
	
	public static ArrayList<InfoEnum> ARCH_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.LOCATION,
						InfoEnum.STATUS,
						InfoEnum.UPDATE_TIME
					)
			);
	
	public static ArrayList<InfoEnum> GENERIC_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.LOCATION,
						InfoEnum.UPDATE_TIME
					)
			);
	
	public static ArrayList<InfoEnum> BLACKLIST_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
    					InfoEnum.LOCATION,
    					InfoEnum.PRIORITY,
    					InfoEnum.UPDATE_TIME
					)
			);
	
    public static HashMap<RobotType, Info> unitInfoMap= 
    		new HashMap<RobotType, Info>() {{
    			//Info(reserved channel, then track count)
    			
    			put(RobotType.ARCHON, 
    					new Info(
	    					ARCH_TRACKED_INFO
    					,NUM_ARCHONS_TRACKED)
				); 
    			
    			put(RobotType.GARDENER, 
    					new Info(
							GENERIC_TRACKED_INFO
    					,NUM_GARDENERS_TRACKED)
				); 
    			
    			put(RobotType.SOLDIER, 
    					new Info(
							GENERIC_TRACKED_INFO
    					,NUM_SOLDIERS_TRACKED)
				); 
    			
    			put(RobotType.SCOUT, 
    					new Info(
							GENERIC_TRACKED_INFO
    					,NUM_SCOUTS_TRACKED)
				); 
    			
    			put(RobotType.TANK, 
    					new Info(
							GENERIC_TRACKED_INFO
    					,NUM_TANKS_TRACKED)
				); 
    			
    			put(RobotType.LUMBERJACK, 
    					new Info(
							GENERIC_TRACKED_INFO
    					,NUM_LUMBERJACKS_TRACKED)
				); 
			}};
	
	public static int ARCHON_START_INDEX= unitInfoMap.get(RobotType.ARCHON).startIndex = STARTING_OFFSET;
	public static int GARDENER_START_INDEX= unitInfoMap.get(RobotType.GARDENER).startIndex = unitInfoMap.get(RobotType.ARCHON).getNextInfoStartIndex();
	public static int SOLDIER_START_INDEX= unitInfoMap.get(RobotType.SOLDIER).startIndex = unitInfoMap.get(RobotType.GARDENER).getNextInfoStartIndex();
	public static int SCOUT_START_INDEX= unitInfoMap.get(RobotType.SCOUT).startIndex = unitInfoMap.get(RobotType.SOLDIER).getNextInfoStartIndex();
	public static int TANK_START_INDEX= unitInfoMap.get(RobotType.TANK).startIndex = unitInfoMap.get(RobotType.SCOUT).getNextInfoStartIndex();
	public static int LUMBERJACK_START_INDEX= unitInfoMap.get(RobotType.LUMBERJACK).startIndex = unitInfoMap.get(RobotType.TANK).getNextInfoStartIndex();
	
    public static HashMap<String, Info> additionalInfoMap= 
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
		return (int) (loc.x* (1000)+ loc.y);
	}
	
	public static MapLocation extractMapLocation(int code){
		return new MapLocation(code/(1000), code%(1000));
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
		
		System.out.println("[Needed channels/Max channels:  " +getTotalNeededChannels()+"/"+Constants.BROADCAST_MAX_CHANNELS);
		if (Constants.BROADCAST_MAX_CHANNELS > getTotalNeededChannels()){
			answer= true;
		}
		return answer;
	}
	
	public static boolean encodingTest(){
		boolean answer= false;
		MapLocation testMapLoc= new MapLocation(999,502);
		int encoded= condenseMapLocation(testMapLoc);
		MapLocation decoded= extractMapLocation(encoded);
		answer= testMapLoc.equals(decoded);
		System.out.println("[Encoded loc / Decoded loc: ]"+ testMapLoc.toString() + "/"+decoded.toString());
		return answer;
	}
}
