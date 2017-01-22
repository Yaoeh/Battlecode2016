package aran;

import battlecode.common.GameActionException;
import battlecode.common.RobotController;

public interface InfoContributor {
	public abstract void updateOwnInfo(RobotController rc) throws GameActionException;
}
