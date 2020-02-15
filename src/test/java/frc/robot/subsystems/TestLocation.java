package frc.robot.subsystems;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import org.strongback.mock.MockDoubleSupplier;
import org.strongback.mock.MockGyroscope;

import frc.robot.lib.MathUtil;
import frc.robot.lib.Position;
import frc.robot.mock.MockLog;
import frc.robot.mock.MockNetworkTableHelper;
import frc.robot.subsystems.Location;

/**
 * Test the Location class.
 * 
 * To run just this test, use:
 *   ./gradlew test --tests "frc.robot.subsystems.TestLocation"
 */
public class TestLocation {

	// Check that a pose matches expected values.
	public void assertPosition(double x, double y, double h, Position actual) {
		Position expected = new Position(x, y, h, 0, 0);
		System.out.printf("Checking expected location(%s)\n", expected);
		System.out.printf("   against actual location(%s)\n", actual);
		assertEquals(x, actual.x, 0.01);
		assertEquals(y, actual.y, 0.01);
		assertEquals(h, actual.heading, 0.01);
		// Ignore the speed for now.
	}
	
    @Test
    public void testLocation() {
    	MockDoubleSupplier leftDistance = Mock.doubleSupplier();
    	MockDoubleSupplier rightDistance = Mock.doubleSupplier();
    	MockGyroscope gyro = Mock.gyroscope();
    	MockClock clock = Mock.clock();
        Location location = new Location(leftDistance, rightDistance, gyro, clock, new MockNetworkTableHelper("Location") ,null, new MockLog());
        
        // Initial location should always be 0,0,0
        gyro.setAngle(0); // Facing towards the opposing alliances wall.
        clock.incrementByMilliseconds(20);
        assertPosition(0, 0, 0, location.getCurrentLocation());
        location.execute(0);
        
        // Roll forward both encoders 10 inches so that the robot drives
        // straight forward towards the other end of the field.
        leftDistance.setValue(leftDistance.getValue() + 10);
        rightDistance.setValue(rightDistance.getValue() + 10);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        // Towards other end of the field is x=10, y=0
        assertPosition(10, 0, 0, location.getCurrentLocation());
        
        // Roll backwards the same distance and see if we get to the same position.
        leftDistance.setValue(leftDistance.getValue() - 10);
        rightDistance.setValue(rightDistance.getValue() - 10);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(0, 0, 0, location.getCurrentLocation());
        
        // Pretend to turn 180 degrees on the spot, the angle should change and nothing else.
        leftDistance.setValue(leftDistance.getValue() + 20);
        rightDistance.setValue(rightDistance.getValue() - 20);
        gyro.setAngle(180);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(0, 0, 180, location.getCurrentLocation());
        
        // Turn again and the angle should be back to 0.
        leftDistance.setValue(leftDistance.getValue() + 20);
        rightDistance.setValue(rightDistance.getValue() - 20);
        gyro.setAngle(0);
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(0, 0, 0, location.getCurrentLocation());
        
        // Roll forward and a bit clockwise.
        leftDistance.setValue(leftDistance.getValue() + 20);
        rightDistance.setValue(rightDistance.getValue() + 10);
        gyro.setAngle(-45);  // Clockwise is a -ve angle.
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(15 * MathUtil.cos(-45./2), 15 * MathUtil.sin(-45./2), -45, location.getCurrentLocation());

        // Reset heading and the heading will be zero again, but the gryo will be 45 degrees further ahead.
        location.resetHeading();
        assertPosition(15 * MathUtil.cos(-45./2), 15 * MathUtil.sin(-45./2), 0, location.getCurrentLocation());
        clock.incrementByMilliseconds(20);
        location.execute(0);
        assertPosition(15 * MathUtil.cos(-45./2), 15 * MathUtil.sin(-45./2), 0, location.getCurrentLocation());
    }
}
