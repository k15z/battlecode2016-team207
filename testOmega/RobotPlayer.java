package testOmega;

import java.util.*;
import battlecode.common.*;

/**
 * This is it. This is the end. This is testOmega.
 * ----------------------------------------------------------------------------
 * The `testOmega` implementation of RobotPlayer incorporates the best aspects 
 * of `testC1A` (impenetrable turret-based defense) and `testM16` (aggressive 
 * soldier-based offense) to defeat any and all enemies.
 * ----------------------------------------------------------------------------
 * @author Kevin Zhang <kevz@mit.edu>, Felipe Hofmann <????@mit.edu>
 */
public class RobotPlayer {
	final static int SUMMON_ARCHONS = 0;
	final static int SECRET_MISSIONS = 1;
	final static int ATTACK_LOCATION = 2;
	final static Direction[] evenDirection = {
		Direction.NORTH, Direction.EAST, 
		Direction.SOUTH, Direction.WEST
	};
	final static Direction[] oddDirection = {
		Direction.SOUTH_WEST, Direction.NORTH_WEST, 
		Direction.NORTH_EAST, Direction.SOUTH_EAST
	};
	final static Direction[] allDirection = {
		Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, 
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
	};
	
	static Random random;
	static RobotController robot;
    public static void run(RobotController controller) throws GameActionException {
        robot = controller;
        random = new Random(robot.getID());
        switch (robot.getType()) {
			case ARCHON:
				archon();
				break;
			case SOLDIER:
				soldier();
				break;
	    	case SCOUT:
				scout();
				break;
			case TURRET:
				turret();
				break;
			default:
				throw new RuntimeException("Unsupported RobotType.");
        }
    }
    
    // TODO: soldiers + mission control
    static void archon() throws GameActionException {
    	boolean isFirstArchon = true;
    	MapLocation homeBase = robot.getLocation();
		Signal[] signals = robot.emptySignalQueue();
		for (Signal signal : signals)
			if (signal.getTeam() == robot.getTeam())
				if (signal.getMessage()[0] == SUMMON_ARCHONS) {
					int x = signal.getMessage()[1] >> 16;
					int y = signal.getMessage()[1] & 0xFFFF;
					isFirstArchon = false;
					homeBase = new MapLocation(x, y);
				}
		
		if (isFirstArchon) {
			int value = (homeBase.x << 16) | homeBase.y;
			robot.broadcastMessageSignal(SUMMON_ARCHONS, value, 1000);
			for (Direction dir : oddDirection) {
				while (!robot.isCoreReady())
					Clock.yield();
				while (!robot.hasBuildRequirements(RobotType.TURRET))
					Clock.yield();
				if (robot.canBuild(dir, RobotType.TURRET)) {
					robot.build(dir, RobotType.TURRET);
					Clock.yield();
				}
			}
		} else {
			int attempts = 0;
			MapLocation current = robot.getLocation();
			while (9 < current.distanceSquaredTo(homeBase) && attempts < 50) {
				while (!robot.isCoreReady())
					Clock.yield();
				tryMove(current.directionTo(homeBase));
				current = robot.getLocation();
				Clock.yield();
				attempts++;
			}
			while (((current.x - homeBase.x) + (current.y - homeBase.y)) % 2 != 0) {
				while (!robot.isCoreReady())
					Clock.yield();
				tryMove(evenDirection[random.nextInt(4)]);
				current = robot.getLocation();
				Clock.yield();
			}
		}
		
		while (true) {
			if (robot.isCoreReady()) {
				if (random.nextDouble() < 0.33 && robot.hasBuildRequirements(RobotType.TURRET)) {
					double rnd = random.nextDouble();
					Direction dir = evenDirection[random.nextInt(4)];
					if (rnd < 0.6) {
						if (robot.canBuild(dir, RobotType.TURRET)) {
							robot.build(dir, RobotType.TURRET);
						}
					} else {
						if (robot.canBuild(dir, RobotType.SCOUT)) {
							robot.build(dir, RobotType.SCOUT);
						}
					}
					Clock.yield();
				}
			}
			Clock.yield();
		}
    }
    
    // TODO: rubble + offensives
    static void soldier() throws GameActionException {
    	moveToEdgeOfBase(true);
		while (true) {
			Clock.yield();
		}
    }
    
    // TODO: secret missions
    static void scout() throws GameActionException {
    	moveToEdgeOfBase(false);
    	while (true) {
			if (robot.isCoreReady()) {
				int broadcasts = 0;
				RobotInfo[] neighbors = robot.senseNearbyRobots(2);
				for (RobotInfo neighbor : neighbors) {
					if (neighbor.type == RobotType.TTM) {
						Direction step = oddDirection[random.nextInt(4)];
						if (robot.canMove(step))
							robot.move(step);
						int count = 0;
						while (!robot.canMove(step.opposite()) || count++ < 2)
							Clock.yield();
						if (robot.canMove(step.opposite()))
							robot.move(step.opposite());
						Clock.yield();
					}
				}
				neighbors = robot.senseNearbyRobots(robot.getType().sensorRadiusSquared);
				for (RobotInfo neighbor : neighbors) {
					if (broadcasts < 20 && neighbor.team != robot.getTeam() && neighbor.location.distanceSquaredTo(robot.getLocation()) > 2) {
						int value = (neighbor.location.x << 16) | neighbor.location.y;
						robot.broadcastMessageSignal(ATTACK_LOCATION, value, 25);
						broadcasts++;
					}
				}
			}
    		Clock.yield();
    	}
    }
    
    static void turret() throws GameActionException {
    	boolean wellPlaced = false;
    	for (Direction dir : oddDirection) {
    		RobotInfo bot = robot.senseRobotAtLocation(robot.getLocation().add(dir));
	    	if (bot != null && bot.type == RobotType.ARCHON) {
	    		wellPlaced = true;
	    	}
    	}
    	if (!wellPlaced) {
	    	robot.pack();
	    	ttm();
    	}
    	
		while (true) {
			if (robot.isCoreReady()) {
				RobotInfo[] hostiles = robot.senseHostileRobots(robot.getLocation(), robot.getType().attackRadiusSquared);
				for (RobotInfo hostile : hostiles) {
					if (hostile.type == RobotType.BIGZOMBIE || hostile.type == RobotType.ARCHON)
						if (robot.isWeaponReady() && robot.canAttackLocation(hostile.location)) {
							robot.attackLocation(hostile.location);
							Clock.yield();
						}
				}
				for (RobotInfo hostile : hostiles) {
					if (robot.isWeaponReady() && robot.canAttackLocation(hostile.location)) {
						robot.attackLocation(hostile.location);
						Clock.yield();
					}
				}
				
				Signal[] signals = robot.emptySignalQueue();
				for (Signal signal : signals)
					if (signal.getTeam() == robot.getTeam() && signal.getMessage()[0] == ATTACK_LOCATION) {
						int x = signal.getMessage()[1] >> 16;
						int y = signal.getMessage()[1] & 0xFFFF;
						MapLocation hostile = new MapLocation(x,y);
						if (robot.isWeaponReady() && robot.canAttackLocation(hostile)) {
							robot.attackLocation(hostile);
							Clock.yield();
						}
					}
			}
			Clock.yield();
		}
    }
    
    static void ttm() throws GameActionException {
    	while (!robot.isCoreReady())
    		Clock.yield();
    	moveToEdgeOfBase(false);
		
    	for (Direction dir : evenDirection) {
			if (robot.canMove(dir)) {
				robot.move(dir);
				break;
			}
    	}
    	
    	while (!robot.isCoreReady())
    		Clock.yield();
		robot.unpack();
    }
    
    static void tryMove(Direction forward) throws GameActionException {
    	if (robot.canMove(forward))
    		robot.move(forward);
    	else if (robot.canMove(forward.rotateLeft()))
    		robot.move(forward.rotateLeft());
    	else if (robot.canMove(forward.rotateRight()))
    		robot.move(forward.rotateRight());
    	else if (robot.canMove(forward.rotateLeft().rotateLeft()))
    		robot.move(forward.rotateLeft().rotateLeft());
    	else if (robot.canMove(forward.rotateRight().rotateRight()))
    		robot.move(forward.rotateRight().rotateRight());
    	else if (robot.senseRubble(robot.getLocation().add(forward)) > 0)
    		robot.clearRubble(forward);
    }
    
    static void moveToEdgeOfBase(boolean clear) throws GameActionException {
		while (!robot.canMove(Direction.NORTH) && !robot.canMove(Direction.EAST) && !robot.canMove(Direction.SOUTH) && !robot.canMove(Direction.WEST)) {
    		Direction dir = oddDirection[random.nextInt(4)];
    		if (robot.canMove(dir))
    			robot.move(dir);
    		else if (clear && robot.senseRubble(robot.getLocation().add(dir)) > 0) {
    			while (robot.senseRubble(robot.getLocation().add(dir)) > 0) {
    				robot.clearRubble(dir);
    				Clock.yield();
    			}
    		}
        	while (!robot.isCoreReady())
        		Clock.yield();
		}
    }
}
