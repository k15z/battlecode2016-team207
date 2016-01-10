package testO;

import battlecode.common.*;

import java.util.*;

/**
 * O for overpopulation. Too many soldiers!!!
 */
public class RobotPlayer {
	static Random random;
	static RobotController robot;
	static Direction[] directions = {
		Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST,
		Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST
	};
    
    public static void run(RobotController rc) {
        robot = rc;
        random = new Random(robot.getID());
        switch (robot.getType()) {
			case ARCHON:
				archon();
				break;
			case SOLDIER:
				soldier();
				break;
			default:
				throw new RuntimeException("Wtf...");
        }
    }
    
    static void archon() {
		while (true) {
	    	try {
	    		for (Direction dir : directions) {
	    			while (!robot.isCoreReady())
	    				Clock.yield();
    				if (robot.senseHostileRobots(robot.getLocation(), 9).length > 2)
    					archon_escape();
	    			while (!robot.isCoreReady())
	    				Clock.yield();
	    			while (!robot.hasBuildRequirements(RobotType.SOLDIER)) {
	    				if (robot.senseHostileRobots(robot.getLocation(), 9).length > 2)
	    					archon_escape();
	    				Clock.yield();
	    			}
	    			if (robot.canBuild(dir, RobotType.SOLDIER))
	    				robot.build(dir, RobotType.SOLDIER);
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    static void archon_escape(){
    	//if everything goes wrong
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
	    				if (robot.senseRubble(robot.getLocation().add(i2d(best_i))) > 0)
	    					robot.clearRubble(i2d(best_i));
		    			if (robot.canMove(i2d(best_i)))
		    				robot.move(i2d(best_i));
		    			prev_dir = i2d(best_i);
	    			}
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    /**
     * Direction to index. TODO: optimize.
     */
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
    
    /**
     * Index to direction. TODO: optimize.
     */
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
    

    static void soldier() {
    	Direction prev = directions[random.nextInt(8)];
    	
		while (true) {
	    	try {
    			while (!robot.isCoreReady())
    				Clock.yield();
    			
    			RobotInfo[] enemies = robot.senseHostileRobots(robot.getLocation(), robot.getType().attackRadiusSquared);
    			for (RobotInfo enemy : enemies) {
    				if (random.nextDouble() < 0.5)
    					robot.broadcastSignal(robot.getType().sensorRadiusSquared*10);
					robot.attackLocation(enemy.location);
    			}
    			
    			Signal[] signals = robot.emptySignalQueue();
    			if (signals.length > 0) {
    				int moves = 0;
    				MapLocation dest = signals[random.nextInt(signals.length)].getLocation();
    				while (robot.getLocation().distanceSquaredTo(dest) > 4 && moves++ < 50) {
    					Direction dir = robot.getLocation().directionTo(dest);
    					while (robot.senseRubble(robot.getLocation().add(dir)) > 10)
    						robot.clearRubble(dir);
    					if (robot.canMove(dir))
    						robot.move(dir);
    					else if (robot.canMove(dir.rotateLeft()))
    						robot.move(dir.rotateLeft());
    					else if (robot.canMove(dir.rotateRight()))
    						robot.move(dir.rotateRight());
    					else break;
    					if (robot.canMove(dir))
    						robot.move(dir);
    					else if (robot.canMove(dir.rotateLeft()))
    						robot.move(dir.rotateLeft());
    					else if (robot.canMove(dir.rotateRight()))
    						robot.move(dir.rotateRight());
    					else break;
    				}
    			}
    			
    			for (RobotInfo bot : robot.senseNearbyRobots(2, robot.getTeam()))
	    			if (bot.type == RobotType.SOLDIER) {
	        			if (random.nextDouble() < 0.2)
	        				robot.move(directions[random.nextInt(8)]);
	    				break;
	    			}
    			
    			if (random.nextDouble() < 0.5)
    				robot.move(prev);
    			if (random.nextDouble() < 0.3)
    				prev = directions[random.nextInt(8)];
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
}
