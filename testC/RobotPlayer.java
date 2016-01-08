package testC;

import battlecode.common.*;

import java.util.*;

/**
 * C for CIA... it plays all kinds of dirty tricks :)
 *   - hide behind turrets
 *   - send scouts find zombie dens and enemy locations
 *   - send soldiers, on schedule, to lure zombies to enemy locations
 *   - hijack communications
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
    	// me! me! me!
    	while (!robot.isCoreReady())
    		Clock.yield();
    	
    	Signal ourSignal = null;
    	Signal[] signals = robot.emptySignalQueue();
    	for (Signal signal : signals)
    		if (signal.getTeam() == robot.getTeam() && signal.getMessage()[0] == 2019)
    			ourSignal = signal;
    	
    	// initialize
    	if (ourSignal == null) {
        	robot.broadcastMessageSignal(2019, (robot.getLocation().x << 16) | robot.getLocation().y, 1000);
        	for (Direction direction : oddDir) {
    	    	while (!robot.isCoreReady() || !robot.hasBuildRequirements(RobotType.TURRET))
    	    		Clock.yield();
    	    	if (robot.canBuild(direction, RobotType.TURRET))
    	    		robot.build(direction, RobotType.TURRET);
        	}
    	}
    	else {
    		int x = ourSignal.getMessage()[1] >> 16;
    		int y = ourSignal.getMessage()[1] & 0xFFFF;
        	
        	while (robot.getLocation().distanceSquaredTo(new MapLocation(x,y)) > 9) {
        		try {
        			Direction dir = robot.getLocation().directionTo(new MapLocation(x,y));
        			if (robot.canMove(dir))
        				robot.move(dir);
        			else if (robot.canMove(dir.rotateLeft()))
        				robot.move(dir.rotateLeft());
        			else if (robot.canMove(dir.rotateRight()))
        				robot.move(dir.rotateRight());
        			else{
        				robot.clearRubble(dir);
        			}
        		}catch(Exception e) {e.printStackTrace();}
            	while (!robot.isCoreReady())
            		Clock.yield();
        	}
    	}
    	
		int count = 0;
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    	    	for (Direction direction : evenDir) {
		    	    	//if is being attacked, escape (DOESNT WORK!!!) 
		    	    	/*while (!robot.isCoreReady())
		    	    		Clock.yield();
		    	    	double health = robot.getHealth();
		    	    	if(health < 500){
			    			//while(robot.senseNearbyRobots(9, Team.ZOMBIE).length > 2)
			    			while(true){
			    				escape();
			    			}
			    		}
			    		if(health < 200){
			    			while(robot.senseNearbyRobots(5, Team.ZOMBIE).length > 0)
			    				escape();
			    			if(robot.canBuild(Direction.NORTH, RobotType.SCOUT))
			    				robot.build(Direction.NORTH, RobotType.SCOUT);
			    		}*/
			    		
	    	    		if (count%5 != 0) {
		    		    	while (!robot.isCoreReady())
		    		    		Clock.yield();
	    	    			if (robot.hasBuildRequirements(RobotType.TURRET)) {
			    		    	if (robot.canBuild(direction, RobotType.TURRET)) {
			    		    		robot.build(direction, RobotType.TURRET);
				    	    		count++;
			    		    	}
	    	    			}
	    	    		} else if (robot.hasBuildRequirements(RobotType.SCOUT)) {
		    		    	while (!robot.isCoreReady())
		    		    		Clock.yield();
	    	    			if (robot.hasBuildRequirements(RobotType.SCOUT)) {
			    		    	if (robot.canBuild(direction, RobotType.SCOUT)) {
			    		    		robot.build(direction, RobotType.SCOUT);
				    	    		count++;
			    		    	}
	    	    			}
	    	    		}
	    	    	}
	    		}
	    	} catch (Exception e) {e.printStackTrace();}
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
    	while (!robot.isCoreReady())
    		Clock.yield();
    	
    	while (true) {
    		Direction dir = null;
    		while(!robot.canMove(Direction.NORTH) && !robot.canMove(Direction.EAST) && 
					!robot.canMove(Direction.SOUTH) && !robot.canMove(Direction.WEST)) {
	    		try {
	    			dir = oddDir[random.nextInt(4)];
		    		while (!robot.isCoreReady() && !robot.canMove(dir))
		    			dir = oddDir[random.nextInt(4)];
		    		robot.move(dir);
	    			if (Clock.getBytecodesLeft() < 8500)
	    				Clock.yield();
	    		} catch(Exception e) {};
    		}
    		
    		RobotInfo[] enemies = robot.senseHostileRobots(robot.getLocation(), robot.getType().sensorRadiusSquared);
    		for (RobotInfo enemy : enemies)
	    		try {
	    			robot.broadcastMessageSignal(enemy.location.x, enemy.location.y, 16);
	    			if (Clock.getBytecodesLeft() < 8500)
	    				Clock.yield();
	    		} catch(Exception e) {};
	    	
	    	//moves scouts for ttms to leave
	    	RobotInfo[] nearBuddies = robot.senseNearbyRobots(2, robot.getTeam()); 
    		try {
    			for(RobotInfo isttm: nearBuddies){
    				if(isttm.type == RobotType.TTM){
    					if(robot.canMove(Direction.NORTH_EAST))
    						robot.move(Direction.NORTH_EAST);
    					else if(robot.canMove(Direction.NORTH_WEST))
    						robot.move(Direction.NORTH_WEST);
    					else if(robot.canMove(Direction.SOUTH_EAST))
    						robot.move(Direction.SOUTH_EAST);
    					else if(robot.canMove(Direction.SOUTH_WEST))
    						robot.move(Direction.SOUTH_WEST);
    				}
	    			if (Clock.getBytecodesLeft() < 8500)
	    				Clock.yield();
    			}
    		} catch(Exception e) {};
	    	
    		
			if (Clock.getBytecodesLeft() < 8500)
				Clock.yield();
    	
    		//avoids scouts going too far
    		RobotInfo[] farBuddies = robot.senseNearbyRobots(9, robot.getTeam());
    		try {
    			float min = 10;
    			RobotInfo nearestBuddy = null;
    			for(RobotInfo farBuddy: farBuddies){
    				if(farBuddy.type == RobotType.TURRET){
    					float distance = robot.getLocation().distanceSquaredTo(farBuddy.location);
	    				if(distance < min){
	    					min = distance;
	    					nearestBuddy = farBuddy;
	    				}
    				}
    			}
				if(min > 1){
					Direction direction = robot.getLocation().directionTo(nearestBuddy.location);
					if(robot.canMove(direction))
						robot.move(direction);
					else if(robot.canMove(direction.rotateLeft()))
						robot.move(direction.rotateLeft());
					else if(robot.canMove(direction.rotateRight()))
						robot.move(direction.rotateRight());
					else if(robot.canMove(direction.rotateRight().rotateRight()))
						robot.move(direction.rotateRight().rotateRight());
					else if(robot.canMove(direction.rotateLeft().rotateLeft()))
						robot.move(direction.rotateLeft().rotateLeft());
				}	
    		} catch(Exception e) {};
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
		    	} catch (Exception e) { }
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
	    	if (!robot.canMove(Direction.NORTH) && !robot.canMove(Direction.EAST) && 
				!robot.canMove(Direction.SOUTH) && !robot.canMove(Direction.WEST)) {
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
                        if(robots[i].type == RobotType.BIGZOMBIE)
                            robot.attackLocation(robots[i].location);
                    for (int i = 0; i < robots.length; i++)
                        if (robots[i].team != robot.getTeam())
                            robot.attackLocation(robots[i].location);
					
	    			Signal[] signals = robot.emptySignalQueue();
	    			for (Signal s : signals) {
	    				if (s.getMessage()[0] != 2019 && robot.canAttackLocation(new MapLocation(s.getMessage()[0], s.getMessage()[1])))
	    					robot.attackLocation(new MapLocation(s.getMessage()[0], s.getMessage()[1]));
	    			}
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

    static void escape(){
    	System.out.println("Escape?");
    	try {
			float min = 1000;
    		int sightRange = 35;
    		RobotInfo nearestEnemy = null;
    		RobotInfo[] enemies = robot.senseNearbyRobots(sightRange, Team.ZOMBIE);
    		for(RobotInfo enemy : enemies){
    			float distance = robot.getLocation().distanceSquaredTo(enemy.location);
				if(distance < min){
					min = distance;
					nearestEnemy = enemy;
				}
		    	while (!robot.isCoreReady())
		    		Clock.yield();
				Direction dir = nearestEnemy.location.directionTo(robot.getLocation());
				if(robot.canMove(dir))
					robot.move(dir);
				else if(robot.canMove(dir.rotateLeft()))
					robot.move(dir.rotateLeft());
				else if(robot.canMove(dir.rotateRight()))
					robot.move(dir.rotateRight());
				else if(robot.canMove(dir.rotateLeft().rotateLeft()))
					robot.move(dir.rotateLeft().rotateLeft());
				else if(robot.canMove(dir.rotateRight().rotateRight()))
					robot.move(dir);
				else if(robot.canMove(dir.rotateRight().rotateRight().rotateRight()))
					robot.move(dir);
				else if(robot.canMove(dir.rotateLeft().rotateLeft().rotateLeft()))
					robot.move(dir);
				else{
					if(robot.canBuild(dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft(), RobotType.GUARD))
						robot.build(dir.rotateLeft().rotateLeft().rotateLeft().rotateLeft(), RobotType.GUARD);
				}
    		}
    		
    	} catch (Exception e) {e.printStackTrace();}
    }
}
