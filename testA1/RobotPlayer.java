package testA1;

import battlecode.common.*;

import java.util.Random;

/**
 * testA1
 *  - send scouts out to lure enemy away
 *    - travel towards zombie dens and then towards enemy
 *  - build turrets around archons
 *  - find optimal turret/scout ratio
 *  - move archons around
 *  - build turret clusters
 */
public class RobotPlayer {
	static Random random;
	static RobotController robot;
	static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
        Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    
    public static void run(RobotController rc) {
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
    
    static void archon() {
    	Direction open = directions[random.nextInt(8)];
    	try {
	    	while (robot.isLocationOccupied(robot.getLocation().add(open)))
	    		open = directions[random.nextInt(8)];
    	} catch (Exception e) {}
    	
    	double SCOUT_RATIO = 0.01;
    	double TURRET_RATIO = 0.5;
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			double rand = random.nextDouble();
	    			if (rand < SCOUT_RATIO) {
		    			if (robot.canBuild(open, RobotType.SCOUT))
		    				robot.build(open, RobotType.SCOUT);
	    			} else if (rand < TURRET_RATIO) {
	    				Direction dir = directions[random.nextInt(8)];
	    				for (int i = 0; i < 8; i++)
			    			if (dir != open && robot.canBuild(dir, RobotType.TURRET))
			    				robot.build(dir, RobotType.TURRET);
			    			else
			    				dir = dir.rotateLeft();
	    			} else {
		    			RobotInfo[] allies = robot.senseNearbyRobots(2, robot.getTeam());
		    			if (allies.length >= 7) {
		    				if (robot.canMove(open))
		    					robot.move(open);
		    				if (robot.canMove(open))
		    					robot.move(open);
		    				open = directions[random.nextInt(8)];
		    		    	try {
		    			    	while (robot.isLocationOccupied(robot.getLocation().add(open)))
		    			    		open = directions[random.nextInt(8)];
		    		    	} catch (Exception e) {}
		    			}
		    			for (int i = 0; i < allies.length; i++)
		    				if (allies[i].health < allies[i].maxHealth)
		    					robot.repair(allies[i].location);
		    			
	    				Direction dir = directions[random.nextInt(8)];
	    				for (int i = 0; i < 8; i++)
		    				if (robot.senseRubble(robot.getLocation().add(dir)) > 0.1)
		    					robot.clearRubble(dir);
			    			else
			    				dir = dir.rotateLeft();
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
    	Direction open = directions[random.nextInt(8)];
    	try {
	    	while (!robot.canMove(open))
	    		open = directions[random.nextInt(8)];
    	} catch (Exception e) {}
    	
    	double MOVE_RATIO = 0.5;
		while (true) {
	    	try {
	    		if (robot.isCoreReady() && random.nextDouble() < MOVE_RATIO) {
	    			if (robot.canMove(open))
	    				robot.move(open);
	    			else
	    		    	while (!robot.canMove(open))
	    		    		open = directions[random.nextInt(8)];
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void ttm() {
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
    
    static void turret() {
    	int attackRange = robot.getType().attackRadiusSquared;
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			RobotInfo[] robots = robot.senseNearbyRobots(attackRange);
	    			for (int i = 0; i < robots.length; i++)
	    				if (robots[i].team != robot.getTeam())
	    					robot.attackLocation(robots[i].location);
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
