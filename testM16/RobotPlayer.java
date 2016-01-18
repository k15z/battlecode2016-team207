package testM16;

import battlecode.common.*;

import java.util.*;

/**
 * M16. Like the C1A, but British and with SAS teams.
 */
public class RobotPlayer {
	static Random random;
	static RobotController robot;
	static Direction[] direction = {
		Direction.NORTH, Direction.NORTH_EAST, Direction.EAST, Direction.SOUTH_EAST, 
		Direction.SOUTH, Direction.SOUTH_WEST, Direction.WEST, Direction.NORTH_WEST
	};
    
    public static void run(RobotController rc) {
        robot = rc;
        random = new Random(robot.getID());
        switch (robot.getType()) {
	        case ARCHON :
	        	archon();
	        	break;
	        case SOLDIER :
	        	soldier();
	        	break;
			default:
				throw new RuntimeException("Wtf...");
        }
    }
    
    static void archon() {
    	boolean greedy = true; // collecting parts doesn't work
    	double health = robot.getHealth();
    	int senseRadius = robot.getType().sensorRadiusSquared;
    	while (true) {
    		try {
        		Direction dir = direction[random.nextInt(8)];
        		for (int i = 0; i < 8; i++) {
            		dir = dir.rotateLeft();
            		while (!robot.isCoreReady())
            			Clock.yield();
            		
            		while (!robot.hasBuildRequirements(RobotType.SOLDIER)) {
	    				if (health - robot.getHealth() > 1) {
	    					health = robot.getHealth();
	    					archon_escape();
	    				} else if (greedy) {
	        				Direction dire = direction[random.nextInt(8)];
	        				MapLocation[] parts = robot.sensePartLocations(senseRadius);
	        				if (parts.length > 0) {
	        					dire = robot.getLocation().directionTo(parts[0]);
		        			}
                    		if (robot.isCoreReady()) {
                    			if (robot.canMove(dire))
                    				robot.move(dire);
                    			else if (robot.onTheMap(robot.getLocation().add(dire)))
                    				robot.clearRubble(dire);
                    		}
	    				}
	    				Clock.yield();
            		}
            		
            		while (!robot.isCoreReady())
            			Clock.yield();
            		if (robot.canBuild(dir, RobotType.SOLDIER))
            			robot.build(dir, RobotType.SOLDIER);
        		}
    		} catch (Exception e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    static void archon_escape(){
    	// everyone get the hell out of my way...
    	int decoy = 0;
    	int sensorRange = robot.getType().sensorRadiusSquared;
    	Direction prev_dir = i2d(random.nextInt(8));
    	
		while (true) {
	    	try {
        		if (robot.senseNearbyRobots(100, robot.getTeam()).length > 0)
        			robot.broadcastMessageSignal(911, 911, 100);
	        	
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
		    					current = 2.9 / myLocation.distanceSquaredTo(bot.location);
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
	    				while (!robot.isCoreReady())
	    					Clock.yield();
		    			if (robot.canMove(i2d(best_i)))
		    				robot.move(i2d(best_i));
		    			prev_dir = i2d(best_i);
	    			}
		    		if (hostiles == 0)
	    				return;
		    		robot.broadcastSignal(50);
	    		}
	    	} catch (Exception e) {e.printStackTrace();}
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
    
    static void soldier() {
    	int attackRange = robot.getType().attackRadiusSquared;
    	int sensorRange = robot.getType().sensorRadiusSquared;
    	while (true) {
    		try {
	    		if (robot.isCoreReady()) {
	    			MapLocation current = robot.getLocation();
	        		RobotInfo[] enemies = robot.senseHostileRobots(current, sensorRange);
	        		for (RobotInfo enemy : enemies)
	        			if (robot.isCoreReady() && enemy.location.distanceSquaredTo(current) + 5 < attackRange) {
	        				Direction escape = enemy.location.directionTo(current);
	        				if (robot.canMove(escape))
	        					robot.move(escape);
	        				else if (robot.canMove(escape.rotateLeft()))
	        					robot.move(escape.rotateLeft());
	        				else if (robot.canMove(escape.rotateRight()))
	        					robot.move(escape.rotateRight());
	        			}
	        		if (enemies.length > 0) {
        				Direction direction = current.directionTo(enemies[0].location);
	        			if (robot.canAttackLocation(enemies[0].location)) {
	        				if (robot.isWeaponReady())
	        					robot.attackLocation(enemies[0].location);
	        				robot.broadcastSignal(25);
	        			}
	        			else if (robot.isCoreReady()){
	        				if (robot.canMove(direction))
	        					robot.move(direction);
	        				else if (robot.canMove(direction.rotateLeft()))
	        					robot.move(direction.rotateLeft());
	        				else if (robot.canMove(direction.rotateRight()))
	        					robot.move(direction.rotateRight());
	        				else if (robot.canMove(direction.rotateLeft().rotateLeft()))
	        					robot.move(direction.rotateLeft().rotateLeft());
	        				else if (robot.canMove(direction.rotateRight().rotateRight()))
	        					robot.move(direction.rotateRight().rotateRight());
	        			}
	        		} else {
        				Direction dir = direction[random.nextInt(8)];
	        			Signal[] targets = robot.emptySignalQueue();
	        			if (targets.length > 0 && targets[0].getLocation().distanceSquaredTo(robot.getLocation()) > 16) {
	        				dir = robot.getLocation().directionTo(targets[0].getLocation());
	        			}
	        			for (Signal target : targets)
	        				if (target.getMessage() != null && target.getMessage()[0] == 911 && target.getMessage()[1] == 911)
	        					dir = target.getLocation().directionTo(robot.getLocation());
        				if (robot.canMove(dir))
        					robot.move(dir);
        				else if (robot.canMove(dir.rotateLeft()))
        					robot.move(dir.rotateLeft());
        				else if (robot.canMove(dir.rotateRight()))
        					robot.move(dir.rotateRight());
        				else if (robot.canMove(dir.rotateLeft().rotateLeft()))
        					robot.move(dir.rotateLeft().rotateLeft());
        				else if (robot.canMove(dir.rotateRight().rotateRight()))
        					robot.move(dir.rotateRight().rotateRight());
        			}
	        		Clock.yield();
	    		}
    		} catch (Exception e) {e.printStackTrace();}
    	}
    }
}
