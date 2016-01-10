package team207;

import battlecode.common.*;

import java.util.*;

/**
 * The C1A. Builds checker-board of turrets to hide behind and sends scouts 
 * to patrol the "final" frontiers. Sends kamikaze scouts to lead zombies 
 * to the enemy team. If the turrets die, run around and play hide-and-seek.
 */
public class RobotPlayer {
	static int A2A_MESSAGE = 0;
	static int A2S_MESSAGE = 1;
	
	static Random random;
	static RobotController robot;
	static Direction[] evenDir = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
	static Direction[] oddDir = {Direction.SOUTH_WEST, Direction.NORTH_WEST, Direction.NORTH_EAST, Direction.SOUTH_EAST};
    
	/**
	 * Initializes static variables and switches between different modes of 
	 * operation for each type of robot.
	 */
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
    
    /**
     * The first archon sends out a message, and all other archons come running 
     * towards it. They work together to build a checker-board of turrets and 
     * use scouts to patrol the outer edges. If its health falls too much, the 
     * archonflees from it's home.
     */
    static void archon() {
    	{ /* Initialize. */
	    	// Handle initial signals.
	    	Signal signal = null;
	    	Signal[] signals = robot.emptySignalQueue();
	    	for (Signal sig : signals)
	    		if (sig.getTeam() == robot.getTeam() && sig.getMessage()[0] == A2A_MESSAGE)
	    			signal = sig;
	    	
	    	try {
	        	if (signal == null) {
	        		// I'm the first archon!
					robot.broadcastMessageSignal(A2A_MESSAGE, A2A_MESSAGE, 1000);
		        	for (Direction direction : oddDir) {
		    	    	while (!robot.isCoreReady() || !robot.hasBuildRequirements(RobotType.TURRET))
		    	    		Clock.yield();
		    	    	if (robot.canBuild(direction, RobotType.TURRET))
		    	    		robot.build(direction, RobotType.TURRET);
		        	}
	        	} else {
	        		// I'm NOT the first archon :(
	        		int attempts = 0;
	        		int MAX_ATTEMPTS = 50;
	        		MapLocation src = robot.getLocation();
	        		MapLocation dest = signal.getLocation();
	        		
                	// travel to archons
	            	while (src.distanceSquaredTo(dest) > 9 && attempts++ < MAX_ATTEMPTS) {
	                	while (!robot.isCoreReady())
	                		Clock.yield();
	            		try {
	            			Direction dir = src.directionTo(dest);
	            			if (robot.canMove(dir))
	            				robot.move(dir);
	            			else if (robot.canMove(dir.rotateLeft()))
	            				robot.move(dir.rotateLeft());
	            			else if (robot.canMove(dir.rotateRight()))
	            				robot.move(dir.rotateRight());
	            			else {
	            				robot.clearRubble(dir);
	            				continue;
	            			}
	            			src = robot.getLocation();
	            		} catch(Exception e) {
	            			e.printStackTrace();
	            		}
	            	}
	        	}
			} catch (Exception e) {
				e.printStackTrace();
			}
    	}
    	
    	{ /* Loop. */
    		int[] raw_schedule = robot.getZombieSpawnSchedule().getRounds();
    		List<Integer> schedule = new LinkedList<Integer>();
    		for (int rs: raw_schedule)
    			schedule.add(rs);
    		
    		int TURRET_SCOUT = 5;
    		int ESCAPE_HEALTH = 500;
    		int AVE_NUM_ARCHONS = 3;
    		int dir_i = 0;
    		Direction dir = evenDir[dir_i];
    		while (true) {
		    	while (!robot.isCoreReady())
		    		Clock.yield();
    			try {
    				if (robot.getHealth() < ESCAPE_HEALTH)
    					archon_escape();
    				RobotInfo[] friendsIn2 = robot.senseNearbyRobots(24, robot.getTeam());
    				for(RobotInfo toHeal : friendsIn2)
						if(toHeal.health < 99 && toHeal.type != RobotType.ARCHON){
							try {
							robot.repair(toHeal.location);
							} catch(Exception e) {};
							Clock.yield();
						}
    				
    				try {
	    				while (schedule.size() > 0 && schedule.get(0) < robot.getRoundNum())
	    					schedule.remove(0);
	    				if (schedule.size() > 0 && schedule.get(0) - robot.getRoundNum() < 128) {
	    					// activate kamikaze
	    					RobotInfo[] near = robot.senseNearbyRobots(16, robot.getTeam());
	    					for (RobotInfo ri : near) {
	    						if (ri.type == RobotType.SCOUT) {
		    						robot.broadcastMessageSignal(A2S_MESSAGE, ri.ID, 16);
		        					schedule.remove(0);
		        					break;
	    						}
	    					}
	    				}
    				} catch (Exception e) {e.printStackTrace();}
    				
    				if (random.nextDouble() < 1.0/AVE_NUM_ARCHONS)
	    				if (random.nextDouble() > 1.0/TURRET_SCOUT) {
		    	    	    	while (!robot.isCoreReady() || !robot.hasBuildRequirements(RobotType.TURRET)) {
		    	    	    		Clock.yield();
		    	    				if (robot.getHealth() < ESCAPE_HEALTH)
		    	    					archon_escape();
		    	    				friendsIn2 = robot.senseNearbyRobots(24, robot.getTeam());
		    	    				for(RobotInfo toHeal : friendsIn2)
		    							if(toHeal.health < 99 && toHeal.type != RobotType.ARCHON){
		    								try {
		    								robot.repair(toHeal.location);
		    								} catch(Exception e) {};
		    								Clock.yield();
		    							}
		    	    	    	}
		    	    	    	if (robot.canBuild(dir, RobotType.TURRET))
		    	    	    		robot.build(dir, RobotType.TURRET);
		    	    	    	dir_i = (dir_i+1)%4;
		    					dir = evenDir[dir_i];
	    				} else {
	    	    	    	while (!robot.isCoreReady() || !robot.hasBuildRequirements(RobotType.SCOUT)) {
	    	    	    		Clock.yield();
	    	    				if (robot.getHealth() < ESCAPE_HEALTH)
	    	    					archon_escape();
	    	    				friendsIn2 = robot.senseNearbyRobots(24, robot.getTeam());
	    	    				for(RobotInfo toHeal : friendsIn2)
	    							if(toHeal.health < 99 && toHeal.type != RobotType.ARCHON){
	    								try {
	    								robot.repair(toHeal.location);
	    								} catch(Exception e) {};
	    								Clock.yield();
	    							}
	    	    	    	}
	    	    	    	if (robot.canBuild(dir, RobotType.SCOUT))
	    	    	    		robot.build(dir, RobotType.SCOUT);
	    	    	    	dir_i = (dir_i+1)%4;
	    					dir = evenDir[dir_i];
	    				}
    			} catch (Exception e) {
    				e.printStackTrace();
    			}
    		}
    	}
    }
    
    /**
     * Run like the hounds of hell are after you.
     */
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
    
    /**
     * Stay on the fringes of the checker-board and broadcast signals letting 
     * turrets know where the zombies are. Politely step out of the way for 
     * any TTMs.
     */
    static void scout() {
    	while (!robot.isCoreReady())
    		Clock.yield();
    	
    	while (true) {
    		Clock.yield();
    		while (!robot.isCoreReady())
    			Clock.yield();
    		
    		// exit checker-board
    		while(!robot.canMove(Direction.NORTH) && !robot.canMove(Direction.EAST) && 
					!robot.canMove(Direction.SOUTH) && !robot.canMove(Direction.WEST)) {
	    		try {
	    			Direction dir = oddDir[random.nextInt(4)];
		    		while (!robot.canMove(dir))
		    			dir = oddDir[random.nextInt(4)];
		    		robot.move(dir);
	    		} catch(Exception e) {e.printStackTrace();};
	    		while (!robot.isCoreReady())
	    			Clock.yield();
    		}
	    	
    		// sense enemies
    		RobotInfo[] enemies = robot.senseHostileRobots(robot.getLocation(), robot.getType().sensorRadiusSquared);
    		for (RobotInfo enemy : enemies)
	    		try {
	    			robot.broadcastMessageSignal(enemy.location.x, enemy.location.y, 16);
	    		} catch(Exception e) {e.printStackTrace();};
    		
    		// move away for allies
	    	RobotInfo[] allies = robot.senseNearbyRobots(2, robot.getTeam()); 
    		try {
    			for(RobotInfo ally: allies){
    				if(ally.type == RobotType.TTM){
    					Direction dir = oddDir[random.nextInt(4)];
    					if(robot.canMove(dir)) {
    						robot.move(dir);
    						int count = 0;
    						while (!robot.canMove(dir.opposite()) || count++ < 2)
    							Clock.yield();
    						robot.move(dir.opposite());
    					}
    				}
    			}
    		} catch(Exception e) {};
    		
    		// sabotage?
    		Signal[] signals = robot.emptySignalQueue();
    		for (Signal sig : signals)
    			if (sig.getMessage()[0] == A2S_MESSAGE && sig.getMessage()[1] == robot.getID())
    			{
					System.out.println("Sabatoge time!");
    				scout_secret();
    			}
    	}
    }

    /**
     * Move diagonally (odd directions) until you reach and edge; then displace 
     * in a even direction to fit into the checker-board pattern.
     */
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
    

    /**
     * Pack and attack. It rhymes!
     */
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
	    
    	Team myTeam = robot.getTeam();
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
	                    MapLocation l = s.getLocation();
	    				if (
	    						( // my team
	    							s.getTeam() == myTeam && 
	    							s.getMessage()[0] != A2A_MESSAGE && 
	    							robot.canAttackLocation(new MapLocation(s.getMessage()[0], s.getMessage()[1]))
	    						) ||
	    						( // intercepted enemy
	    							s.getTeam() != myTeam && 
	    							robot.canAttackLocation(new MapLocation(l.x, l.y))
	    						)
	    					)
	    					robot.attackLocation(new MapLocation(s.getMessage()[0], s.getMessage()[1]));
	    			}
	    		}
	    	} catch (Exception e) {}
	    	Clock.yield();
		}
    }
    
    /**
     * Kamikaze.
     */
    static void scout_secret() {
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
		    					current = 9.0 / myLocation.distanceSquaredTo(bot.location);
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
		    				//else if (robot.senseRubble(myLocation.add(i2d(i))) > 0.5)
		    				//	score[i] += 2.0;
		    				if (!robot.onTheMap(myLocation.add(i2d(i)).add(i2d(i))))
		    					score[i] += 0.5;
		    				//else if (robot.senseRubble(myLocation.add(i2d(i)).add(i2d(i))) > 0.5)
		    				//	score[i] += 0.5;
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
		    		if (random.nextDouble() < 0.5)
		    			Clock.yield();
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
    
    /*** NOT USED. ***/
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
    
    /*** NOT USED. ***/
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
