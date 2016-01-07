package testC;

import battlecode.common.*;

import java.util.Random;

/**
 * C for CIA... it plays all kinds of dirty tricks :)
 *   - hide behind turrets
 *   - send scouts find zombie dens and enemy locations
 *   - send soldiers, on schedule, to lure zombies to enemy locations
 */
public class RobotPlayer {
	static Random random;
	static RobotController robot;
	static Direction[] evenDir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	static Direction[] oddDir = {Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST};
    
    public static void run(RobotController rc) throws Exception {
        robot = rc;
        random = new Random(robot.getID());
        switch (robot.getType()) {
			case ARCHON:
				archon();
				break;
			case GUARD:
				guard();
				break;
			case SOLDIER:
				soldier();
				break;
	    	case SCOUT:
				scout();
				break;
			case TTM:
				ttm();
				break;
			case TURRET:
				turret();
				break;
			case VIPER:
				viper();
				break;
			default:
				throw new RuntimeException("Wtf...");
        }
    }
    
    static void archon() throws GameActionException {
    	// initialize
    	while (!robot.isCoreReady())
    		Clock.yield();
    	for (Direction direction : oddDir) {
	    	while (!robot.isCoreReady() || !robot.hasBuildRequirements(RobotType.TURRET))
	    		Clock.yield();
	    	if (robot.canBuild(direction, RobotType.TURRET))
	    		robot.build(direction, RobotType.TURRET);
    	}
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    	    	for (Direction direction : evenDir) {
	    		    	while (!robot.isCoreReady() || !robot.hasBuildRequirements(RobotType.TURRET))
	    		    		Clock.yield();
	    		    	if (robot.canBuild(direction, RobotType.TURRET))
	    		    		robot.build(direction, RobotType.TURRET);
	    	    	}
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void guard() {
    	// initialize
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			// loop
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void soldier() {
    	// initialize
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			// loop
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void scout() {
    	// initialize
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			// loop
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void ttm() {
    	// initialize
    	while (!robot.isCoreReady())
    		Clock.yield();
    	
    	if (!robot.canMove(Direction.NORTH) || !robot.canMove(Direction.EAST) || 
				!robot.canMove(Direction.SOUTH) || !robot.canMove(Direction.WEST)) {
	    	// navigate
			while (!robot.canMove(Direction.NORTH) && !robot.canMove(Direction.EAST) && 
					!robot.canMove(Direction.SOUTH) && !robot.canMove(Direction.WEST)) {
		    	try {
		    		Direction dir = oddDir[random.nextInt(4)];
		    		if (robot.canMove(dir))
		    			robot.move(dir);
		    	} catch (Exception e) {}
	        	while (!robot.isCoreReady())
	        		Clock.yield();
			}
			
	    	// displace
	    	try {
		    	for (Direction dir : evenDir) {
					if (robot.canMove(dir)) {
						robot.move(dir);
						break;
					}
		    	}
	    	} catch (Exception e) {}
    	}
    	
		// end
    	while (!robot.isCoreReady())
    		Clock.yield();
		try {
			robot.unpack();
    	} catch (Exception e) {}
		return;
    }
    
    static void turret() {
    	// initialize
    	try {
	    	while (!robot.isCoreReady())
	    		Clock.yield();
	    	if (!robot.canMove(Direction.NORTH) || !robot.canMove(Direction.EAST) || 
				!robot.canMove(Direction.SOUTH) || !robot.canMove(Direction.WEST)) {
	    		robot.pack();
	    		ttm();
	    	}
    	}catch(Exception e) {}
	    	
    	int attackRange = robot.getType().attackRadiusSquared;
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
					RobotInfo[] robots = robot.senseNearbyRobots(attackRange);
					for (int i = 0; i < robots.length; i++)
						if (robots[i].team != robot.getTeam())
							robot.attackLocation(robots[i].location);
					Clock.yield();
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void viper() {
    	// initialize
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			// loop
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
}
