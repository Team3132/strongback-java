package frc.robot.lib;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.strongback.mock.Mock;
import org.strongback.mock.MockClock;
import frc.robot.Constants;

/**
 * Test the LocationHistory class - a work in progress.
 * 
 * To run just this test, use:
 *   ./gradlew test --tests "frc.robot.lib.TestLocationHistory"
 */
public class TestLocationHistory {

	// Check that a pose matches expected values.
	public void assertPosition(double x, double y, double h, double timeSec, Position actual) {
		//System.out.printf("Checking expected position(%s)\n", expected);
		//System.out.printf("   against actual position(%s)\n", actual);
		assertEquals(x, actual.x, 0.01);
		assertEquals(y, actual.y, 0.01);
		assertEquals(h, actual.heading, 0.01);
		// Ignore the speed for now.
	}
	
    @Test
    public void testHistory() {
        MockClock clock = Mock.clock();
        LocationHistory history = new LocationHistory(clock);

        for (int i=0; i<Constants.LOCATION_HISTORY_MEMORY_SECONDS * Constants.LOCATION_HISTORY_CYCLE_SPEED; i++) {
            Position p = new Position(20*i, i, 0, 0, clock.currentTime());
            history.addLocation(p);
            assertPosition(20*i, i, 0, clock.currentTime(), history.getLocation(clock.currentTime()));
            clock.incrementByMilliseconds(20);
        }
    }
}
