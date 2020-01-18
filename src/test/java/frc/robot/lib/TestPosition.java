package frc.robot.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

/**
 * Test the Position class.
 * 
 * To run just this test, use:
 *   ./gradlew test --tests "frc.robot.lib.TestPosition"
 */
public class TestPosition {

	// Check that a pose matches expected values.
	public void assertPosition(double x, double y, double h, Position actual) {
		Position expected = new Position(x, y, h, 0, 0);
		System.out.printf("Checking expected pose(%s)\n", expected);
		System.out.printf("   against actual pose(%s)\n", actual);
		assertEquals(x, actual.x, 0.01);
		assertEquals(y, actual.y, 0.01);
		assertEquals(h, actual.heading, 0.01);
		// Ignore the speed for now.
	}
	
    @Test
    public void testGetRelativeToPosition() {
        // Used to get the position of something (like a vision target) to the robot.
        /*
          Define position of the robot and something -90 off to the side of it.
                                       ^ +ve X
                                       |
                     target (10,4,|)   |
                                  v    |
                                       |
                                       |
                                       |
                                       |
                                       |
                      robot (2,4,<-)   |
                                       |
           +ve Y <---------------------0-> -ve Y        
        
        */

        Position robot = new Position(2, 4, 90);
        Position target = new Position(10, 4, 180);
        assertPosition(0, -8, -90, target.getRelativeToPosition(robot));
    }

    @Test
    public void testAdd() {
        // Pretty much the inverse of getRelativeToPosition().
        // Uses the same numbers as testGetRelativeToPosition().

        Position robot = new Position(2, 4, 90);
        Position target = new Position(10, 4, 180);
        Position relativeTarget = target.getRelativeToPosition(robot);
        // Adding relativeTarget to robot should give back target.
        assertPosition(target.x, target.y, target.heading, robot.add(relativeTarget));
    }

    @Test
    public void testCopyFrom() {
        Position robot = new Position(2, 4, 90);
        Position value = new Position(0, 0, 0);
        value.copyFrom(robot);
        assertPosition(2, 4, 90, value);
    }

    @Test
    public void testAddVector() {
        /*
          Define position of the robot and something -90 off to the side of it.
                                       ^ +ve X
                                       |
                                       |
                                       |
                                       |
                                       |
                      robot (2,4,<-)   |
                                       |
           +ve Y <---------------------0-> -ve Y        
        
        */
        Position robot = new Position(2, 4, 90);
        assertPosition(2, 5, 90, robot.addVector(1, 0));
        assertPosition(1, 4, 90, robot.addVector(1, 90));
        assertPosition(3, 4, 90, robot.addVector(1, -90));
        assertPosition(2, 3, 90, robot.addVector(1, 180));
    }

    @Test
    public void testBearingTo() {
        /*
          Define position of the robot and something -90 off to the side of it.
                                       ^ +ve X
                                       |
                     target (10,4,|)   |
                                  v    |
                                       |
                                       |
                                       |
                                       |
                                       |
                      robot (2,4,<-)   |
                                       |
           +ve Y <---------------------0-> -ve Y        
        
        */
        Position robot = new Position(2, 4, 90);
        Position target = new Position(10, 4, 180);  // To the right of the robot.
		assertEquals(-90, robot.bearingTo(target), 0.01);
        Position target2 = new Position(2, 8, 180);  // Straight ahead of the robot.
		assertEquals(0, robot.bearingTo(target2), 0.01);
        Position target3 = new Position(0, 4, 180);  // Left of the robot.
		assertEquals(90, robot.bearingTo(target3), 0.01);
    }

    @Test
    public void testDistanceTo() {
        Position robot = new Position(2, 4, -90);
        Position target = new Position(5, 8, 180);  // x+=3,y+=4 for total distance of 5. 
		assertEquals(5, robot.distanceTo(target), 0.01);
    }
}
