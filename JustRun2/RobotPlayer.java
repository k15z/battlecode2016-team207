package JustRun2;

import battlecode.common.*;

import java.util.Random;

/**
 * Run like crazy and try not to die.
 */
public class RobotPlayer {
	static Random random;
	static RobotController robot;
    
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
    
    static void scout() {
    	// initialize
    	for (int _ = 0; _ < 6; _++)
    		Clock.yield();
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
    	// initialize
    	
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			RobotInfo target = robot.senseHostileRobots(robot.getLocation(), robot.getType().attackRadiusSquared)[0];
	    			robot.attackLocation(target.location);
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
