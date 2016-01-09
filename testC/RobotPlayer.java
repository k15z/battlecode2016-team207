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
    static int allIsWrong = 0;
    
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
    	
		int count = 1;
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    	    	for (Direction direction : evenDir) {
		    	    
	    	    		int numberEnemies = robot.senseHostileRobots(robot.getLocation(), 35).length;
		    	    	double health = robot.getHealth();
		    	    	//if is being attacked, escape
		    	    	if(health < 500 && numberEnemies > 7){
			    			while(true){
			    				escape();
			    			}
			    		}
		    	    	if (count > 0 && count < 8){
		    	    		while (!robot.isCoreReady())
		    		    		Clock.yield();
	    	    			if (robot.hasBuildRequirements(RobotType.SOLDIER)) {
			    		    	if (robot.canBuild(direction, RobotType.SOLDIER)) {
			    		    		robot.build(direction, RobotType.SOLDIER);
				    	    		count++;
			    		    	}
	    	    			}
		    	    	}
		    	    	else if (count%5 != 0) {
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
	    	
	    	if(allIsWrong == 1){
	    		for (int _ = 0; _ < 15; _++) {
	    	    	try {
	    	    		if (robot.isCoreReady()) {
	    	    			RobotInfo target = robot.senseHostileRobots(robot.getLocation(), robot.getType().attackRadiusSquared)[0];
	    	    			robot.attackLocation(target.location);
	    	    		}
	    	    	} catch (Exception e) {}
	        	}
	        	int sensorRange = robot.getType().sensorRadiusSquared;    	
	        	Direction prev_dir = Direction.NORTH;
	        	
	    		while (true) {
	    	    	try {
	        			// score each direction
	        			double[] score = new double[8];
	        			
	        			// compute hostiles
	        			MapLocation myLocation = robot.getLocation();
	        			RobotInfo[] hostiles = robot.senseHostileRobots(myLocation, sensorRange);
	        			for (RobotInfo hostile : hostiles)
	        				score[d2i(myLocation.directionTo(hostile.location))] += 3.0 / myLocation.distanceSquaredTo(hostile.location);
	        			
	        			// compute walls/rubble
	        			for (int i = 0; i < 8; i++) {
	        				if (!robot.onTheMap(myLocation.add(i2d(i))))
	        					score[i] += 2.0;
	        				else if (robot.senseRubble(myLocation.add(i2d(i))) > 0.5)
	        					score[i] += 2.0;
	        				if (!robot.onTheMap(myLocation.add(i2d(i)).add(i2d(i))))
	        					score[i] += 0.5;
	        				else if (robot.senseRubble(myLocation.add(i2d(i)).add(i2d(i))) > 0.5)
	        					score[i] += 0.5;
	        			}
	        			
	        			// find best direction
	        			int best_i = 0;
	        			double best = 999;
	        			for (int i = 0; i < 8; i++) {
	        				double current = 0.8 * score[i] + 0.1 * score[(i-1+8)%8] + 0.1 * score[(i+1)%8];
	        				if (current < best) {
	        					best_i = i;
	        					best = current;
	        				} else if (current == best && random.nextDouble() < 0.5) {
	        					best_i = i;
	        					best = current;
	        				}
	        			}
	        			
	        			// direction bias
	    				if (score[d2i(prev_dir)] <= best) {
	    					best_i = d2i(prev_dir);
	    					best = score[d2i(prev_dir)];
	    				}
	        			
	    				// move
	        			if (robot.canMove(i2d(best_i)))
	        				robot.move(i2d(best_i));
	        			prev_dir = i2d(best_i);
	        			Clock.yield();
	    	    	} catch (Exception e) {}
	    	    	Clock.yield();
	    		}
	    	}
		}
    }
    
    static void soldier() {
    	// initialize
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
    		//if turret is almost dying, go for help
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

    static int d2i(Direction d) {
    	if (d == Direction.NORTH)
    		return 0;
    	if (d == Direction.NORTH_EAST)
    		return 1;
    	if (d == Direction.EAST)
    		return 2;
    	if (d == Direction.SOUTH_EAST)
    		return 3;
    	if (d == Direction.SOUTH)
    		return 4;
    	if (d == Direction.SOUTH_WEST)
    		return 5;
    	if (d == Direction.WEST)
    		return 6;
    	if (d == Direction.NORTH_WEST)
    		return 7;
    	return -1;
    }
    
    static Direction i2d(int i) {
    	if (i == 0)
    		return Direction.NORTH;
    	if (i == 1)
    		return Direction.NORTH_EAST;
    	if (i == 2)
    		return Direction.EAST;
    	if (i == 3)
    		return Direction.SOUTH_EAST;
    	if (i == 4)
    		return Direction.SOUTH;
    	if (i == 5)
    		return Direction.SOUTH_WEST;
    	if (i == 6)
    		return Direction.WEST;
    	if (i == 7)
    		return Direction.NORTH_WEST;
    	return Direction.NONE;
    }

    static void escape(){
    	
    	//if everything goes wrong
    	allIsWrong = 1;
    	int decoy = 0;
    	int sensorRange = robot.getType().sensorRadiusSquared;
    	Direction prev_dir = i2d(random.nextInt(8));
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			// score each direction
	    			double[] score = new double[8];
	    			
	    			// compute hostiles
	    			int hostiles = 0;
	    			MapLocation myLocation = robot.getLocation();
	    			RobotInfo[] bots = robot.senseNearbyRobots(sensorRange);
	    			RobotInfo[] nearZombies = robot.senseNearbyRobots(9, Team.ZOMBIE);
	    			int fastZombies = 0;
	    			for(RobotInfo isFastZombie : nearZombies)
	    				if(isFastZombie.type == RobotType.FASTZOMBIE)
	    					fastZombies++;
	    			
		    		if(bots.length > 0){	
	    				for (RobotInfo bot : bots) {
		    				double current = 0.0;
		    				if (bot.team == robot.getTeam().opponent()) {
		    					++hostiles;
		    					current = 3.1 / myLocation.distanceSquaredTo(bot.location);
		    				}
		    				if (bot.team == Team.ZOMBIE) {
		    					++hostiles;
		    					current = 3.0 / myLocation.distanceSquaredTo(bot.location);
		    				}
		    				if (bot.team == robot.getTeam())
		    					current = 2.09 / myLocation.distanceSquaredTo(bot.location);
		    				if (bot.type == RobotType.FASTZOMBIE)
		    					++hostiles;
		    				score[d2i(myLocation.directionTo(bot.location))] += current;
		    			}
		    			
		    			if (fastZombies > 0 && decoy == 0 && hostiles < 10) {
			    			// many hostiles
		    				Direction dir = i2d(random.nextInt(8));
		    				if (robot.canBuild(dir, RobotType.SOLDIER)) {
			    				decoy = 10;
		    					robot.build(dir, RobotType.SOLDIER);
		    				}
		    			}else if (decoy > 0)
		    				decoy--;
		    			if (hostiles == 0 && Math.random() < 0.8)
		    				continue;
		    			
		    			// compute walls/rubble
		    			for (int i = 0; i < 8; i++) {
		    				if (!robot.onTheMap(myLocation.add(i2d(i))))
		    					score[i] += 2.0;
		    				else if (robot.senseRubble(myLocation.add(i2d(i))) > 0.5)
		    					score[i] += 2.0;
		    				if (!robot.onTheMap(myLocation.add(i2d(i)).add(i2d(i))))
		    					score[i] += 0.5;
		    				else if (robot.senseRubble(myLocation.add(i2d(i)).add(i2d(i))) > 0.5)
		    					score[i] += 0.5;
		    			}
		    			
		    			// find best direction
		    			int best_i = 0;
		    			double best = 999;
		    			for (int i = 0; i < 8; i++) {
		    				double current = 0.8 * score[i] + 0.1 * score[(i-1+8)%8] + 0.1 * score[(i+1)%8];
		    				if (current < best) {
		    					best_i = i;
		    					best = current;
		    				} else if (current == best && random.nextDouble() < 0.5) {
		    					best_i = i;
		    					best = current;
		    				}
		    			}
		    			
		    			// direction bias
	    				if (score[d2i(prev_dir)] <= best) {
	    					best_i = d2i(prev_dir);
	    					best = score[d2i(prev_dir)];
	    				}
		    			
	    				// move
		    			if (robot.canMove(i2d(best_i)))
		    				robot.move(i2d(best_i));
		    			prev_dir = i2d(best_i);
	    			}
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    	
    }
}
