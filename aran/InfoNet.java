package aran;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import battlecode.common.GameActionException;
import battlecode.common.MapLocation;
import battlecode.common.RobotController;
import battlecode.common.RobotType;
import aran.Constants.AddInfo;
import aran.Constants.InfoEnum;

public class InfoNet {
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
	
	
	public final static int NUM_ARCHONS_TRACKED= 3;
	public final static int NUM_GARDENERS_TRACKED= 20;
	public final static int NUM_SOLDIERS_TRACKED= 20;
	public final static int NUM_SCOUTS_TRACKED= 5;
	public final static int NUM_TANKS_TRACKED= 3;
	public final static int NUM_LUMBERJACKS_TRACKED= 5;
	public final static int NUM_BLACKlIST_TRACKED= 50;
	
	public static int getNumTypeTracked(RobotType rt){
		int answer= 0;
		switch (rt){
			case ARCHON:
				answer= NUM_ARCHONS_TRACKED;
				break;
			case GARDENER:
				answer= NUM_GARDENERS_TRACKED;
				break;
			case SOLDIER:
				answer= NUM_SOLDIERS_TRACKED;
				break;
			case SCOUT:
				answer= NUM_SCOUTS_TRACKED;
				break;
			case TANK:
				answer= NUM_TANKS_TRACKED;
				break;
			case LUMBERJACK:
				answer= NUM_LUMBERJACKS_TRACKED;
				break;
		}
		return answer;
	}
	
	public static InfoEnum getCountEnumTypeTracked(RobotType rt){
		InfoEnum answer= null;
		switch (rt){
			case ARCHON:
				answer= InfoEnum.ARCHON_COUNT;
				break;
			case GARDENER:
				answer= InfoEnum.GARDENER_COUNT;
				break;
			case SOLDIER:
				answer= InfoEnum.SOLDIER_COUNT;
				break;
			case SCOUT:
				answer= InfoEnum.SCOUT_COUNT;
				break;
			case TANK:
				answer= InfoEnum.TANK_COUNT;
				break;
			case LUMBERJACK:
				answer= InfoEnum.LUMBERJACK_COUNT;
				break;
		}
		return answer;
	}
	
	
	public static ArrayList<InfoEnum> ARCH_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.ID,
						InfoEnum.LOCATION,
						InfoEnum.STATUS,
						InfoEnum.UPDATE_TIME
					)
			);
	
	public static ArrayList<InfoEnum> ITERATED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.ITERATOR
					)
			);
	
	public static ArrayList<InfoEnum> GENERIC_TRACKED_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.ID,
//						InfoEnum.LOCATION,
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
	
	public static ArrayList<InfoEnum> UNIT_COUNT_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
    					InfoEnum.ARCHON_COUNT,
    					InfoEnum.GARDENER_COUNT,
    					InfoEnum.SOLDIER_COUNT,
    					InfoEnum.SCOUT_COUNT,
       					InfoEnum.TANK_COUNT,
       					InfoEnum.LUMBERJACK_COUNT
       					//InfoEnum.UPDATE_TIME 
       					//You can check this by first checking if the archon is dead, and then by the first of the corresponding unit type is dead
					)
			);
	
	public static ArrayList<InfoEnum> MAP_EDGE_INFO = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.MIN_Y,
						InfoEnum.MAX_X,
						InfoEnum.MAX_Y,
						InfoEnum.MIN_X
					)
			);
	
	public static ArrayList<InfoEnum> ANDYKEY_ITERATOR = 	    					
			new ArrayList<InfoEnum>(
				Arrays.asList(
						InfoEnum.STATUS
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
	
	//Since you can't declare the start index of the item before the map is instantiated, use this stupid thing here to initialize it
			
	public static int ARCHON_START_INDEX= unitInfoMap.get(RobotType.ARCHON).setStartIndex(1000); //starting at 1000 since andrew uses the first 1000
	public static int GARDENER_START_INDEX= unitInfoMap.get(RobotType.GARDENER).setStartIndex(unitInfoMap.get(RobotType.ARCHON).getNextInfoStartIndex());
	public static int SOLDIER_START_INDEX= unitInfoMap.get(RobotType.SOLDIER).setStartIndex(unitInfoMap.get(RobotType.GARDENER).getNextInfoStartIndex());
	public static int SCOUT_START_INDEX= unitInfoMap.get(RobotType.SCOUT).setStartIndex(unitInfoMap.get(RobotType.SOLDIER).getNextInfoStartIndex());
	public static int TANK_START_INDEX= unitInfoMap.get(RobotType.TANK).setStartIndex(unitInfoMap.get(RobotType.SCOUT).getNextInfoStartIndex());
	public static int LUMBERJACK_START_INDEX= unitInfoMap.get(RobotType.LUMBERJACK).setStartIndex(unitInfoMap.get(RobotType.TANK).getNextInfoStartIndex());
	
	
    public static HashMap<AddInfo, Info> addInfoMap= 
    		new HashMap<AddInfo, Info>() {{
    			//Info(reserved channel, then track count)
    			put(AddInfo.BLACKLIST, 
    					new Info(
	    					unitInfoMap.get(RobotType.LUMBERJACK).getNextInfoStartIndex(),
							BLACKLIST_TRACKED_INFO
	    					,NUM_BLACKlIST_TRACKED)
				);
    			put(AddInfo.UNITCOUNT, 
    					new Info(
							UNIT_COUNT_INFO
	    					,1) //should only count units once
				);
    			put(AddInfo.MAP_EDGE, 
    					new Info(
							MAP_EDGE_INFO
	    					,1) //should only count units once
				);
			}};
	public static int UNIT_COUNT_START_INDEX= addInfoMap.get(AddInfo.UNITCOUNT).setStartIndex(addInfoMap.get(AddInfo.BLACKLIST).getNextInfoStartIndex());
	public static int MAP_EDGE_START_INDEX= addInfoMap.get(AddInfo.MAP_EDGE).setStartIndex(addInfoMap.get(AddInfo.UNITCOUNT).getNextInfoStartIndex());

	
	
	public static int countUnits(RobotController rc, RobotType rt) throws GameActionException{
		int unitCount= 0;
		Info trackedInfo= unitInfoMap.get(rt);
		
		for (int i = 0; i < trackedInfo.getTrackCount(); i++){
			if (trackedInfo.getIndex(InfoEnum.UPDATE_TIME)!= -1){
				int channel= trackedInfo.getStartIndex()+ (trackedInfo.reservedChannels.size()*i) + trackedInfo.getIndex(InfoEnum.UPDATE_TIME);
				int lastUpdateRoundNum= rc.readBroadcast(channel);
				//System.out.println("\t" + rt.name() + " Channel: " + channel + " Last update round: " +  lastUpdateRoundNum + " Difference: "+  (rc.getRoundNum() - lastUpdateRoundNum));
				if ( (rc.getRoundNum() - lastUpdateRoundNum) < Constants.DEAD_TOLERANCE_ROUNDNUM){
					unitCount++;
				}else{
					break; //break on the first one not updated
				}
			}
		}
		System.out.println("\t" + rt.name() + " count: "+ unitCount);

		return unitCount;
	}
	
	public static int getClosestAddInfoTargetIndex(RobotController rc, AddInfo ai) throws GameActionException{ //returns Integer.MIN_VALUE on fail
		int channelIndex= Integer.MIN_VALUE;
		MapLocation closestLoc= null;
		Info trackedInfo= addInfoMap.get(ai);
		
		for (int i = 0; i < trackedInfo.getTrackCount(); i++){
			int startingIndex = trackedInfo.getStartIndex()+ (trackedInfo.reservedChannels.size()*i);
			int mapIndexOffset= trackedInfo.getIndex(InfoEnum.LOCATION);
			if (mapIndexOffset!= -1){
				int mapInt= rc.readBroadcast(startingIndex + trackedInfo.getIndex(InfoEnum.LOCATION));
				MapLocation contender= null;
				if (mapInt!= 0){
					contender= InfoNet.extractMapLocation(mapInt);
				}
				if (closestLoc== null && contender!= null){
					closestLoc= contender;
					channelIndex= startingIndex;
				}else if (contender!= null && closestLoc!= null && rc.getLocation().distanceTo(closestLoc) > rc.getLocation().distanceTo(contender)){
					closestLoc= contender;
					channelIndex= startingIndex;
				}
			}else{
				break;
			}
		}
		
		return channelIndex;
	}
	
	public static int getClosestRobotTypeIndex(RobotController rc, RobotType rt) throws GameActionException{ 
		//returns Integer.MIN_VALUE on fail
		
		int channelIndex= Integer.MIN_VALUE;
		MapLocation closestRobotOfTypeLoc= null;
		Info trackedInfo= unitInfoMap.get(rt);
		
		for (int i = 0; i < trackedInfo.getTrackCount(); i++){
			int startingIndex = trackedInfo.getStartIndex()+ (trackedInfo.reservedChannels.size()*i);
			int mapIndexOffset= trackedInfo.getIndex(InfoEnum.LOCATION);
			if (mapIndexOffset!= -1){
				int mapInt= rc.readBroadcast(startingIndex + trackedInfo.getIndex(InfoEnum.LOCATION));
				MapLocation contender= null;
				if (mapInt!= 0){
					contender= InfoNet.extractMapLocation(mapInt);
				}
				if (closestRobotOfTypeLoc== null && contender!= null){
					closestRobotOfTypeLoc= contender;
					channelIndex= startingIndex;
				}else if (contender!= null && closestRobotOfTypeLoc!= null && rc.getLocation().distanceTo(closestRobotOfTypeLoc) > rc.getLocation().distanceTo(contender)){
					closestRobotOfTypeLoc= contender;
					channelIndex= startingIndex;
				}
			}else{
				break;
			}
		}
		
		return channelIndex;
	}
	
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
				int infoIndex= trackedInfo.getStartIndex() + (trackedInfo.reservedChannels.size()*i);	
				if (trackedInfo.getIndex(InfoEnum.ID)!= -1){ //If the ID is tracked, just replace yourself
					int id= rc.readBroadcast(infoIndex + trackedInfo.getIndex(InfoEnum.ID));
					if (id!= 0 && id== rc.getID()){
						answer= infoIndex;
						break;
					}
					//Last update time is = rc.readBroadcast(infoIndex+ updateChannelOffset);
				}				
			}
			
			if (answer == Integer.MIN_VALUE){ //if the ID method fails (i.e. you can't find yourself) //Search until you find a timely replacement
				for (int i = 0; i < trackedInfo.getTrackCount(); i++){
					int infoIndex= trackedInfo.getStartIndex() + (trackedInfo.reservedChannels.size()*i);
					if (trackedInfo.getIndex(InfoEnum.UPDATE_TIME)!= -1){ //If time is tracked, replace the 'dead' guy
						int lastUpdated= rc.readBroadcast(infoIndex+ trackedInfo.getIndex(InfoEnum.UPDATE_TIME));
						if (rc.getRoundNum() - lastUpdated > Constants.DEAD_TOLERANCE_ROUNDNUM){
							answer= infoIndex;
							break;
						}
					}
				}
			}
		}else{
			System.out.println("**Unit info map does not contain key : " + rc.getType().name());
		}
		return answer;
	}
	
	public static int getFirstBehindRoundUpdateOtherRobotIndex(RobotController rc, RobotType rt) throws GameActionException{ 
		// Returns the first not yet update robot of the round, i.e. stops at
		// the first robot where the update round number is behind the current round number
		// returns Integer.MinValue on fail
		
		
		int answer= Integer.MIN_VALUE;
		if (unitInfoMap.containsKey(rt)){
			//Search for an update time of same type below the current time,
			//If there is no update time below, do nothing
			//else, update the time
			
			Info trackedInfo= unitInfoMap.get(rt);
			
			if (answer == Integer.MIN_VALUE){ //if the ID method fails (i.e. you can't find yourself) //Search until you find a timely replacement
				for (int i = 0; i < trackedInfo.getTrackCount(); i++){
					int infoIndex= trackedInfo.getStartIndex() + (trackedInfo.reservedChannels.size()*i);
					if (trackedInfo.getIndex(InfoEnum.UPDATE_TIME)!= -1){ //If time is tracked, replace the 'dead' guy
						int lastUpdated= rc.readBroadcast(infoIndex+ trackedInfo.getIndex(InfoEnum.UPDATE_TIME));
						if (rc.getRoundNum() - lastUpdated > Constants.DEAD_TOLERANCE_ROUNDNUM){
							answer= infoIndex;
							break;
						}
					}
				}
			}
		}else{
			System.out.println("**Unit info map does not contain key : " + rc.getType().name());
		}
		return answer;
	}
	
	public static boolean channelWithinBoradcastRange(int checkedChannel){
		return checkedChannel >= 0 && checkedChannel <Constants.BROADCAST_MAX_CHANNELS;
	}
	
//	public static int getFirstBehindRoundUpdateAddInfoIndex(RobotController rc, String keyname) throws GameActionException{ 
//		// Returns the first not yet update robot of the round, i.e. stops at
//		// the first robot where the update round number is behind the current round number
//		// returns Integer.MinValue on fail
//		
//		
//		int answer= Integer.MIN_VALUE;
//		if (addInfoMap.containsKey(keyname)){
//			//Search for an update time of same type below the current time,
//			//If there is no update time below, do nothing
//			//else, update the time
//			
//			Info trackedInfo= addInfoMap.get(keyname);
//			for (int i = 0; i < trackedInfo.getTrackCount(); i++){
//				int iteratedStartIndex= trackedInfo.getStartIndex()+ trackedInfo.reservedChannels.size()*i;
//				int updateChannelOffset= trackedInfo.getIndex(InfoEnum.UPDATE_TIME); //returns -1 on non-existant enums
//				if (updateChannelOffset!= -1){ 
//					int lastUpdateTime= rc.readBroadcast(iteratedStartIndex+ updateChannelOffset);
//					if (rc.getRoundNum() > lastUpdateTime){
//						answer= iteratedStartIndex;
//						break;
//					}
//				}
//				
//				
//			}
//		}
//		return answer;
//	}
	
	
	public static int condenseMapLocation(MapLocation loc){
		return (int) (loc.x* (1000)+ loc.y) + Constants.MAP_MAX_WIDTH;
	}
	
	public static MapLocation extractMapLocation(int code){
		code-= Constants.MAP_MAX_WIDTH;
		return new MapLocation(code/(1000), code%(1000));
	}

	//======================================================
	///TESTING RUN THESE FUNCTION IN THE TEST PLAYER
	//======================================================
	public static int getTotalNeededChannels(){
		int answer= 0;
		
		answer+= 6; //unit count track locations
		
		for (Info value : unitInfoMap.values()) {
			answer+= value.getNumChannelsNeeded();
		}
		
		for (Info value : addInfoMap.values()) {
			answer+= value.getNumChannelsNeeded();
		}

		return answer;
	}

	
	public static boolean isInfoDefValid(){ //to run in test
		boolean answer= false;
		
		System.out.println("[Needed channels/Max channels: ]" +getTotalNeededChannels()+"/"+Constants.BROADCAST_MAX_CHANNELS);
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
