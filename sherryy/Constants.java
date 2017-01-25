package sherryy;

import battlecode.common.GameConstants;

/**
 *  Extended constants. See constants provided by the library:
 *  http://s3.amazonaws.com/battlecode-releases-2017/releases/javadoc/index.html
 */
public class Constants implements GameConstants{
    
    // regular constants
    static final int GARDENER_MAX = 20;
    static final int LUMBERJACK_MAX = 20;
    
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
    
    public enum PackedGardenerDelta {
        ONE(5f, (float)Math.sqrt((3f))),
        TWO(-5f, -(float)Math.sqrt(3f)),
        THREE((float) (2 * Math.sqrt(3f)), -4f),
        FOUR(-(float) (2 * Math.sqrt(3f)), 4f),
        FIVE(-1f, -(float)(3 * Math.sqrt(3f))),
        SIX(1f, (float)(3 * Math.sqrt(3f)));
        
        protected float x;
        protected float y;
        
        PackedGardenerDelta(float x_, float y_) {
            x = x_;
            y = y_;
        }
    }
}