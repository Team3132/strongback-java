package frc.robot.drive.util;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

import org.junit.Test;
import org.strongback.mock.MockClock;

import frc.robot.mock.MockLog;

public class TestPositionCalc {
	
	/**
	 * Check the speed and position calculation
	 */
	@Test
	public void testBasic() {
		//assertThat(calculateSpeed(p), is(equalTo(0.0)));
		double position = 10;  // Initial position is at 10".
		double speed = 1;
		double maxJerk = 1;
		MockClock clock = new MockClock();
		MockLog log = new MockLog();

		PositionCalc calc = new PositionCalc(position, speed, maxJerk, clock, log);
		long dtMSec = 1000;  // A whole second.
		clock.incrementByMilliseconds(dtMSec);
		double newPos = calc.update();
		assertThat(newPos - position, is(closeTo(speed, 0.1)));
		position = newPos;

		// Update the target speed;
		calc.setTargetSpeed(speed + 2);
		clock.incrementByMilliseconds(dtMSec);
		newPos = calc.update();
		double avgSpeed = speed + maxJerk / 2;
		speed += maxJerk;
		// Final speed should have increased by 1 (due to the jerk),
		// but the average should be be speed + 0.5
		assertThat(newPos - position, is(closeTo(avgSpeed, 0.1)));
		assertThat(calc.getSpeed(), is(closeTo(speed, 0.1)));
		position = newPos;

		// Run it again to get to the target speed.
		clock.incrementByMilliseconds(dtMSec);
		newPos = calc.update();
		avgSpeed = speed + maxJerk / 2;
		speed += maxJerk;
		// Final speed should have increased again by 1 (due to the jerk),
		// but the average should be be speed + 0.5
		assertThat(newPos - position, is(closeTo(avgSpeed, 0.1)));
		assertThat(calc.getSpeed(), is(closeTo(speed, 0.1)));
		position = newPos;

		// Now it should keep going at the target speed.
		clock.incrementByMilliseconds(dtMSec);
		newPos = calc.update();
		// Final speed should have increased again by 1 (due to the jerk),
		// but the average should be be speed + 0.5
		assertThat(newPos - position, is(closeTo(speed, 0.1)));
		assertThat(calc.getSpeed(), is(closeTo(speed, 0.1)));
		position = newPos;

		// Now drop the target speed by one.
		calc.setTargetSpeed(speed - 1);
		clock.incrementByMilliseconds(dtMSec);
		newPos = calc.update();
		avgSpeed = speed - maxJerk / 2;
		speed -= maxJerk;
		// Final speed should have decreased by 1 (due to the jerk),
		// but the average should be be speed - 0.5
		assertThat(newPos - position, is(closeTo(avgSpeed, 0.1)));
		assertThat(calc.getSpeed(), is(closeTo(speed, 0.1)));
		position = newPos;

		// With the speed = current speed, it should maintain this speed.
		clock.incrementByMilliseconds(dtMSec);
		newPos = calc.update();
		assertThat(newPos - position, is(closeTo(speed, 0.1)));
		assertThat(calc.getSpeed(), is(closeTo(speed, 0.1)));
		position = newPos;
	}	
}
