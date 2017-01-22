package aran;

import java.util.ArrayList;

import aran.Constants.InfoEnum;
import battlecode.common.MapLocation;

public class Info {
	private int startIndex= Integer.MIN_VALUE;
	public final ArrayList<InfoEnum> reservedChannels; //number of required channels
	private int trackCount; //number of info tracked
	private int nextInfoStartIndex; //the place where the other guy starts

	public Info(int startIndex, ArrayList<InfoEnum> reservedChannels, int trackCount){
		this.startIndex= startIndex;
		this.reservedChannels= reservedChannels;
		this.trackCount= trackCount;
		//this.nextInfoStartIndex= startIndex+ getNumChannelsNeeded();
	}
	
	public Info(ArrayList<InfoEnum> reservedChannels, int trackCount){
		this.reservedChannels= reservedChannels;
		this.trackCount= trackCount;
		//this.nextInfoStartIndex= startIndex+ getNumChannelsNeeded();
	}
	
	
	public int getNumChannelsNeeded(){
		if (reservedChannels!= null){
			//System.out.println("Channels needed: " + reservedChannels.size() + " x " + trackCount + " = " + reservedChannels.size()*trackCount);
			return reservedChannels.size()*trackCount;
		}else{
			return Integer.MIN_VALUE;
		}
	}
		
	public int getStartIndex(){
		//System.out.println("Start index: " + startIndex);
		return startIndex;
	}
	
	public int setStartIndex(int startIndex){
		this.startIndex= startIndex;
		return this.startIndex;
	}
	
	public int getTrackCount(){
		return trackCount;
	}
	public int getNextInfoStartIndex(){
		if (startIndex!= Integer.MIN_VALUE){
			nextInfoStartIndex= startIndex + getNumChannelsNeeded();
			//System.out.println("Next start index: " + nextInfoStartIndex);
			return nextInfoStartIndex;
		}else{
			return -1;
		}
	}
	
	public int getIndex(InfoEnum ie){ //safer way to go?
		int answer= -1;
		if (reservedChannels.contains(ie)){
			answer= reservedChannels.indexOf(ie);
		}
		return answer;
	}
	
	public InfoEnum getInfoEnum(int index){
		InfoEnum answer= null;
		if (index< reservedChannels.size() && index> -1){
			return reservedChannels.get(index);
		}
		return answer;
	}
}

//public class Bar<E extends Enum<E>> {
//
//    private final E item;
//
//    public E getItem(){
//        return item;
//    }
//
//    public Bar(final E item){
//        this.item = item;
//    }
//}