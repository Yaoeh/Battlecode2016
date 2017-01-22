package aran;

import java.util.Arrays;
import java.util.HashSet;

import battlecode.common.GameConstants;
import battlecode.common.RobotType;

/**
 *  Extended constants. See constants provided by the library:
 *  http://s3.amazonaws.com/battlecode-releases-2017/releases/javadoc/index.html
 */
public class Constants implements GameConstants{
    
    // regular constants
    static final int GARDENER_MAX = 30;
    static final int LUMBERJACK_MAX = 20;
    static final int SOLDIER_MAX = 20;
    static final int TANK_MAX = 20;
    
    
	static final int stepsUntilJiggle= 5; //jiggle so moving in a direction they don't get stuck
	static final float percentageUntilDangerOverride= 0.5f;
	static final float treeGardenMultiplier= 2;
	
	static final float archonGoalRefreshRate= 10;
	static final float targetRefreshRate= 3;
	

	static final HashSet<RobotType> ignoreNone= new HashSet<RobotType>();
	static final HashSet<RobotType> ignoreScout= new HashSet<RobotType>(Arrays.asList(RobotType.SCOUT));
	static final HashSet<RobotType> ignoreArchon= new HashSet<RobotType>(Arrays.asList(RobotType.ARCHON));
	static final HashSet<RobotType> ignoreArchonGardener= new HashSet<RobotType>(Arrays.asList(RobotType.ARCHON, RobotType.GARDENER));
	static final HashSet<RobotType> ignoreAllExceptArchon= new HashSet<RobotType>(Arrays.asList(RobotType.GARDENER, RobotType.LUMBERJACK, RobotType.SCOUT, RobotType.SOLDIER, RobotType.TANK));
    
	public enum InfoEnum {
	    LOCATION, STATUS, UPDATE_TIME, PRIORITY
	}
	
    /**
     * Team shared array.
     */
    public static class Channel {
        // counters
        static final int ARCHON_COUNTER = 0;
        static final int GARDENER_COUNTER = 1;
        static final int SOILDIER_COUNTER = 2;
        static final int TANK_COUNTER = 3;
        static final int SCOUT_COUNTER = 4;
        static final int LUMBERJACK_COUNTER = 5;
    }
    
    /**
     * six angles that cover 360 degrees
     * Use Clock.values() to iterate over all 6 angles
     */
    public enum SixAngle {
        ONE(0),
        TWO((float)Math.PI / 3),
        THREE((float)Math.PI * 2 / 3),
        FOUR((float)Math.PI),
        FIVE((float)Math.PI * 4 / 3),
        SIX((float)Math.PI * 5 / 3);
        
        private final float radians;
        
        private SixAngle(float radians){
            this.radians = radians;
        }
        
        public float getRadians() {
            return radians;
        }
    }
    
	public enum SenseRefresh{
		//0: bullets, 1: friends, 2: enemies, 3: trees
		BULLET(0),
		FRIEND(1),
		ENEMY(2),
		TREE(3);
		
		private final int index;
		
		private SenseRefresh(int index) {
			this.index= index;
	    }
		
		public int getIndex(){
			return index;
		}

	}
}