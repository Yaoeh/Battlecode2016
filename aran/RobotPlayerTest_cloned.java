package aran;

import static org.junit.Assert.*;
import org.junit.Test;

import aran.Constants.AddInfo;
import battlecode.common.RobotType;

public class RobotPlayerTest_cloned {

	@Test
	public void testSanity() {
		assertEquals(true, InfoNet.unitInfoMap.size() > 3);
		assertEquals(true, InfoNet.encodingTest());
		assertEquals(true, InfoNet.isInfoDefValid());
		assertEquals(true, Constants.UNIT_COUNT_UPDATE_OFFSET < Constants.DEAD_TOLERANCE_ROUNDNUM);

		printedJumpTest("ARCHON", "GARDENER",InfoNet.unitInfoMap.get(RobotType.ARCHON).getNextInfoStartIndex(),InfoNet.unitInfoMap.get(RobotType.GARDENER).getStartIndex());
		printedJumpTest("GARDENER", "SOLDIER",InfoNet.unitInfoMap.get(RobotType.GARDENER).getNextInfoStartIndex(),InfoNet.unitInfoMap.get(RobotType.SOLDIER).getStartIndex());
		printedJumpTest("SOLDIER", "SCOUT",InfoNet.unitInfoMap.get(RobotType.SOLDIER).getNextInfoStartIndex(),InfoNet.unitInfoMap.get(RobotType.SCOUT).getStartIndex());
		printedJumpTest("SCOUT", "TANK",InfoNet.unitInfoMap.get(RobotType.SCOUT).getNextInfoStartIndex(),InfoNet.unitInfoMap.get(RobotType.TANK).getStartIndex());
		printedJumpTest("TANK", "LUMBERJACK",InfoNet.unitInfoMap.get(RobotType.TANK).getNextInfoStartIndex(),InfoNet.unitInfoMap.get(RobotType.LUMBERJACK).getStartIndex());
		printedJumpTest("LUMBERJACK", "BLACKLIST",InfoNet.unitInfoMap.get(RobotType.LUMBERJACK).getNextInfoStartIndex(),InfoNet.addInfoMap.get(AddInfo.BLACKLIST).getStartIndex());
		printedJumpTest("BLACKLIST", "UNITCOUNT",InfoNet.addInfoMap.get(AddInfo.BLACKLIST).getNextInfoStartIndex(),InfoNet.addInfoMap.get(AddInfo.UNITCOUNT).getStartIndex());
	}
	
	public void printedJumpTest(String nameA, String nameB, int valA, int valB){
		System.out.println("Testing if "+nameA+" jump: " + valA + " equals "+nameB+" start: "+ valB);
		assertEquals(valA, valB);
	}

}
