package testM;

import battlecode.common.*;

import java.util.*;

/**
 * M for map. Let's map out the entire world!
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
			case SCOUT:
				scout();
				break;
			case VIPER:
				viper();
				break;
			default:
				throw new RuntimeException("Wtf...");
        }
    }
    
    static void archon() {
    	try {
			robot.build(Direction.NORTH, RobotType.SCOUT);
			while (!robot.isCoreReady())
				Clock.yield();
		} catch (GameActionException e) {
			e.printStackTrace();
		}
    	
    	int shift_x = robot.getLocation().x;
    	int shift_y = robot.getLocation().y;
    	int[][] map = new int[200][200];
    	while (true) {
    		Signal[] signals = robot.emptySignalQueue();
    		for (Signal signal : signals) {
    			if (signal.getTeam() != robot.getTeam())
    				continue;
    			int[] coordinate = signal.getMessage();
    			int x = shift_x - coordinate[0] + 100;
    			int y = shift_y - coordinate[1] + 100;
    			map[x][y] = 1;
    		}
			while (!robot.isCoreReady())
				Clock.yield();
			
			if (random.nextDouble() < 0.01) {
				for (int y = 0; y < 200; y++) {
					for (int x = 0; x < 200; x++)
						System.out.print(map[x][y]);
					System.out.println();
				}
			}
    	}
    }
    
    static void scout() {
    	
    	Team enemy = robot.getTeam().opponent();
    	MapLocation origin = robot.getLocation();
		Direction dir = directions[random.nextInt(8)];
    	while (true) {
    		try {
				while (!robot.isCoreReady())
					Clock.yield();
				if (robot.canMove(dir.rotateRight()))
					robot.move(dir.rotateRight());
				else if (robot.canMove(dir))
					robot.move(dir);
				else
					dir = dir.rotateLeft();
				
				RobotInfo[] hostiles = robot.senseNearbyRobots(53, enemy);
				for (RobotInfo hostile : hostiles)
					robot.broadcastMessageSignal(
							hostile.location.x, hostile.location.y, 
							robot.getLocation().distanceSquaredTo(origin) * 2
					);
    		} catch (GameActionException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    static void viper() {
    }
}
