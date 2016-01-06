package test0;

import battlecode.common.*;

import java.util.Random;

/**
 * This RobotPlayer class is designed to help rapidly prototype and test ideas 
 * and strategies. It is written for readability and should be refactored before 
 * being submitted in order to minimize bytecode costs.
 * 
 * @author kevz
 * @version 0.1
 */
public class RobotPlayer {
	static Random random;
	static RobotController robot;
    
	/**
	 * Initialize "local" variables and switch between the different types of 
	 * robots. Each robot type is responsible for keeping their own thread open 
	 * and handling exceptions.
	 * 
	 * @param rc - is a RobotController for this player to use
	 */
    public static void run(RobotController rc) {
        robot = rc;
        random = new Random(robot.getID());
        switch (robot.getType()) {
			case ARCHON:
				while (true) { archon(); }
			case GUARD:
				while (true) { guard(); }
			case SOLDIER:
				while (true) { soldier(); }
	    	case SCOUT:
				while (true) { scout(); }
			case TTM:
				while (true) { ttm(); }
			case TURRET:
				while (true) { turret(); }
			case VIPER:
				while (true) { viper(); }
			default:
				throw new RuntimeException("Wtf...");
        }
    }
    
    static Direction[] directions = {Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST,
            Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST};
    static void archon() {
    	try {
    		if (robot.isCoreReady()) {
    			/*
    			RobotInfo[] zombies = robot.senseNearbyRobots(robot.getType().sensorRadiusSquared, Team.ZOMBIE);
    			if (zombies.length >= 1) {
    				Direction dir = zombies[0].location.directionTo(robot.getLocation());
    				for (int i = 0; i < 8; i++)
	    				if (robot.canMove(dir))
	    					robot.move(dir);
	    				else
	    					dir = dir.rotateLeft();
    			}
    			
    			RobotInfo[] enemies = robot.senseNearbyRobots(robot.getType().sensorRadiusSquared, robot.getTeam().opponent());
    			if (enemies.length >= 1) {
    				Direction dir = enemies[0].location.directionTo(robot.getLocation());
    				for (int i = 0; i < 8; i++)
	    				if (robot.canMove(dir))
	    					robot.move(dir);
	    				else
	    					dir = dir.rotateLeft();
    			}
    			*/
    			
    			if (random.nextDouble() < 0.01) {
	    			if (robot.hasBuildRequirements(RobotType.SCOUT)) {
	    				Direction d = directions[random.nextInt(8)];
	    				for (int i = 0; i < 8; i++)
		    				if (robot.canBuild(d, RobotType.SCOUT)) {
		    					robot.build(d, RobotType.SCOUT);
		    					break;
		    				}
		    				else
		    					d = d.rotateLeft();
	    			}
    			} else {
	    			if (robot.hasBuildRequirements(RobotType.TURRET)) {
	    				Direction d = directions[random.nextInt(8)];
	    				for (int i = 0; i < 8; i++)
		    				if (robot.canBuild(d, RobotType.TURRET)) {
		    					robot.build(d, RobotType.TURRET);
		    					break;
		    				}
		    				else
		    					d = d.rotateLeft();
	    			}
    			}
    			
    			/*
    			if (robot.canMove(Direction.SOUTH_EAST))
    				robot.move(Direction.SOUTH_EAST);
    			if (robot.canMove(Direction.EAST))
    				robot.move(Direction.EAST);
    			if (robot.canMove(Direction.SOUTH))
    				robot.move(Direction.SOUTH);
    			*/
    			
    			/*
    			RobotInfo[] friendlies = robot.senseNearbyRobots(2, robot.getTeam());
    			if (friendlies.length >= 1)
    				robot.move(friendlies[0].location.directionTo(robot.getLocation()));
    			
    			Direction dir = Direction.NORTH_WEST;
    			for (int i = 0; i < 8; i++)
    				if (!robot.canBuild(dir, RobotType.TURRET) && robot.getTeamParts() > 75*3)
    					dir = dir.rotateLeft();
    				else {
    	   				robot.build(dir, RobotType.TURRET);
    	   				break;
    				}
    				*/
   				
    			/*
   				// if shell is full, send message
    			RobotInfo[] friendlies = robot.senseNearbyRobots(2, robot.getTeam());
    			if (friendlies.length >= 8) {
    				robot.broadcastSignal(16);
    			}
    			*/
    		}
    	} catch (Exception e) {
    		
    	}
    	Clock.yield();
    }
    
    static Direction mydir = null;
    static void guard() {
    	try {
    		if (robot.isCoreReady()) {
    			Signal[] receive = robot.emptySignalQueue();
    			if (receive.length > 0) {
    				if (mydir == null) {
	        			RobotInfo[] friendlies = robot.senseNearbyRobots(2, robot.getTeam());
	        			if (friendlies.length >= 2) {
		        			for (int k = 0; k < friendlies.length; k++)
		        				if (friendlies[k].type == RobotType.ARCHON) {
		        						mydir = friendlies[k].location.directionTo(robot.getLocation());
		        				}
		        			if (robot.senseRubble(robot.getLocation().add(mydir)) > 0.0)
		        				robot.clearRubble(mydir);
	        			}
    				}
    				if (robot.canMove(mydir))
    					robot.move(mydir);
    			}
    			
    			
    			// attack
    			RobotInfo[] zombies = robot.senseNearbyRobots(2, Team.ZOMBIE);
    			if (zombies.length > 0) {
    				robot.attackLocation(zombies[0].location);
    			} else {
        			/*
        			RobotInfo[] friendlies = robot.senseNearbyRobots(2, robot.getTeam());
        			if (friendlies.length >= 2) {
	        			for (int k = 0; k < friendlies.length; k++)
	        				if (friendlies[k].type == RobotType.ARCHON) {
	        					if (mydir == null)
	        						mydir = friendlies[k].location.directionTo(robot.getLocation());
	        				}
	        			if (robot.senseRubble(robot.getLocation().add(mydir)) > 0.0)
	        				robot.clearRubble(mydir);
	        			robot.move(mydir);
        			}
        			
        			*/
    			}
    		}
    	} catch (Exception e) {
    		
    	}
    	Clock.yield();
    }
    
    static void soldier() {
    	Clock.yield();
    }
    
    static Direction d = null;
    static void scout() {
    	try {
    		if (robot.isCoreReady()) {
    			RobotInfo[] friends = robot.senseNearbyRobots(25, robot.getTeam());
    			for (int i = 0; i < friends.length; i++)
    				if (friends[i].type.equals(RobotType.ARCHON)) {
    					Direction dir = friends[i].location.directionTo(robot.getLocation());
    					if (robot.canMove(dir)) {
    						robot.move(dir);
    						break;
    					}
    					if (robot.canMove(dir.rotateLeft())) {
    						robot.move(dir.rotateLeft());
    						break;
    					}
    					if (robot.canMove(dir.rotateRight())) {
    						robot.move(dir.rotateRight());
    						break;
    					}
    				}
    			
    			
	    		if (d == null)
	    			d = directions[random.nextInt(8)];
				for (int i = 0; i < 8; i++)
					if (robot.canMove(d)) {
						robot.move(d);
						break;
					}
					else
						d = directions[random.nextInt(8)];
		    	Clock.yield();
    		}
		} catch (Exception e) {
			e.printStackTrace();
		}
    }
    
    static void turret() {
    	try {
    		if (robot.isCoreReady()) {
				RobotInfo[] zombies = robot.senseNearbyRobots(robot.getType().attackRadiusSquared, Team.ZOMBIE);
				if (zombies.length > 0) {
					robot.attackLocation(zombies[0].location);
				}
				
				RobotInfo[] enemies = robot.senseNearbyRobots(robot.getType().attackRadiusSquared, robot.getTeam().opponent());
				if (enemies.length > 0) {
					robot.attackLocation(enemies[0].location);
				}
    		}
		} catch (Exception e) {
//			e.printStackTrace();
		}
    	Clock.yield();
    }
    
    static void ttm() {
    	try {
    		robot.unpack();
    	} catch(Exception e) {
    		
    	}
    	Clock.yield();
    }
    
    static void viper() {
    	try {
			robot.attackLocation(robot.getLocation().add(Direction.NORTH));
		} catch (GameActionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	Clock.yield();
    }
}
