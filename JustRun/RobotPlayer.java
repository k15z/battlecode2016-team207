package JustRun;

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
    
		while (true) {
	    	try {
	    		if (robot.isCoreReady()) {
	    			escape();
	    		}
	    	} catch (Exception e) {}
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

	static void escape(){
		try {
			while (!robot.isCoreReady())
	    		Clock.yield();
			float min = 1000;
			int sightRange = 35;
			RobotInfo nearestEnemy = null;
			RobotInfo[] enemies = robot.senseHostileRobots(robot.getLocation(),sightRange);
			for(RobotInfo enemy : enemies){
				float distance = robot.getLocation().distanceSquaredTo(enemy.location);
				if(distance < min){
					min = distance;
					nearestEnemy = enemy;
				}
			}
			
			while (!robot.isCoreReady())
	    		Clock.yield();
			MapLocation robotLocation = robot.getLocation();
			Direction dir = nearestEnemy.location.directionTo(robotLocation);
			if(robot.canMove(dir))
				robot.move(dir);
			else if(robot.canMove(dir.rotateLeft()))
				robot.move(dir.rotateLeft());
			else if(robot.canMove(dir.rotateRight()))
				robot.move(dir.rotateRight());
			else if(robot.canMove(dir.rotateLeft().rotateLeft()))
				robot.move(dir.rotateLeft().rotateLeft());
			else if(robot.canMove(dir.rotateRight().rotateRight()))
				robot.move(dir.rotateRight().rotateRight());
			else if(robot.canMove(dir.rotateRight().rotateRight().rotateRight()))
				robot.move(dir.rotateRight().rotateRight().rotateRight());
			else if(robot.canMove(dir.rotateLeft().rotateLeft().rotateLeft()))
				robot.move(dir.rotateLeft().rotateLeft().rotateLeft());
			else if( robot.onTheMap(robotLocation.add(dir)) && robot.senseRubble(robotLocation.add(dir)) > 0)
				robot.clearRubble(dir);
			else if( robot.onTheMap(robotLocation.add(dir.rotateLeft())) && robot.senseRubble(robotLocation.add(dir.rotateLeft())) > 0)
				robot.clearRubble(dir.rotateLeft());
			else if ( robot.onTheMap(robotLocation.add(dir.rotateRight())) && robot.senseRubble(robotLocation.add(dir.rotateRight())) > 0)
				robot.clearRubble(dir.rotateRight());
			else{}
			
		} catch (Exception e) {e.printStackTrace();}
	}
}
