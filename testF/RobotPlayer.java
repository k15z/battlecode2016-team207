package testF;

import battlecode.common.*;

import java.util.Random;

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
    	// initialize
		try {
			while (!robot.isCoreReady())
				Clock.yield();
			if (robot.canBuild(Direction.SOUTH_EAST, RobotType.SCOUT))
				robot.build(Direction.SOUTH_EAST, RobotType.SCOUT);
			
			while (!robot.isCoreReady())
				Clock.yield();
			if (robot.canBuild(Direction.SOUTH_WEST, RobotType.SCOUT))
				robot.build(Direction.SOUTH_WEST, RobotType.SCOUT);
			
			while (!robot.isCoreReady())
				Clock.yield();
			if (robot.canBuild(Direction.NORTH_WEST, RobotType.SCOUT))
				robot.build(Direction.NORTH_WEST, RobotType.SCOUT);
			
			while (!robot.isCoreReady())
				Clock.yield();
			if (robot.canBuild(Direction.NORTH_EAST, RobotType.SCOUT))
				robot.build(Direction.NORTH_EAST, RobotType.SCOUT);
		} catch (Exception e) {
			e.printStackTrace();
		}
    	
    	MapLocation loc = null;
    	boolean final_pos = false;
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			if (final_pos) {
	    				if (random.nextDouble() < 0.5) {
		    				Direction direction = directions[random.nextInt(8)];
	    					if (robot.canBuild(direction, RobotType.GUARD)) {
			    				robot.build(direction, RobotType.GUARD);
	    					}
	    				}
 
	    				Direction dir = robot.getLocation().directionTo(loc);
		    			if (robot.canMove(dir))
		    				robot.move(dir);
		    			else if (robot.canMove(dir.rotateLeft()))
		    				robot.move(dir.rotateLeft());
		    			else if (robot.canMove(dir.rotateRight()))
		    				robot.move(dir.rotateRight());
		    			else {
		    				Direction direction = directions[random.nextInt(8)];
		    				if (robot.canBuild(direction.opposite(), RobotType.TURRET))
		    					robot.build(direction.opposite(), RobotType.TURRET);
		    			}
	    			}
	    			else {
		    			Signal[] data = robot.emptySignalQueue();
		    			if (data.length > 0) {
		    				int x = data[0].getMessage()[0];
		    				int y = data[0].getMessage()[1];
		    				loc = new MapLocation(x,y);
		    				final_pos = true;
		    			}
	    			}
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void guard() {
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
    	int moves = 1;
    	Direction dir = robot.senseNearbyRobots(2)[0].location.directionTo(robot.getLocation());
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			moves++;
	    			if (robot.canMove(dir))
	    				robot.move(dir);
	    			else if (robot.canMove(dir.rotateLeft()))
	    				robot.move(dir.rotateLeft());
	    			else if (robot.canMove(dir.rotateRight()))
	    				robot.move(dir.rotateRight());
	    			else
	    				robot.broadcastMessageSignal(robot.getLocation().x, robot.getLocation().y, moves * moves);
	    			
	    			Signal[] data = robot.emptySignalQueue();
	    			if (data.length > 0)
	    				robot.disintegrate();
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
