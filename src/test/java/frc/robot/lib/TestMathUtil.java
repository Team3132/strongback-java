package frc.robot.lib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.closeTo;
import static org.hamcrest.Matchers.is;

import static frc.robot.lib.MathUtil.getAngleDiff;

import org.junit.Test;

public class TestMathUtil {
	@Test
	public void testGetAngleDiff() {
		assertThat(getAngleDiff(0, 0), is(closeTo(0.0, 0.1)));
		assertThat(getAngleDiff(5, 0), is(closeTo(5.0, 0.1)));
		assertThat(getAngleDiff(0, 5), is(closeTo(-5.0, 0.1)));
		assertThat(getAngleDiff(-5, 0), is(closeTo(-5.0, 0.1)));
		assertThat(getAngleDiff(0, -5), is(closeTo(5.0, 0.1)));
		// Weird angles that aren't in the range of -180...180
		assertThat(getAngleDiff(361, 361), is(closeTo(0.0, 0.1)));
		assertThat(getAngleDiff(1, 361), is(closeTo(0.0, 0.1)));
		assertThat(getAngleDiff(-1, -361), is(closeTo(0.0, 0.1)));
		assertThat(getAngleDiff(361, -361), is(closeTo(2.0, 0.1)));
		// Check the shortest distane calculation
		assertThat(getAngleDiff(-175, 175), is(closeTo(10.0, 0.1)));
		assertThat(getAngleDiff(175, -175), is(closeTo(-10.0, 0.1)));
		// Directly opposite angles.
		assertThat(getAngleDiff(270, 90), is(closeTo(180.0, 0.1)));
		assertThat(getAngleDiff(-90, 90), is(closeTo(-180.0, 0.1)));
		assertThat(getAngleDiff(-270, -90), is(closeTo(-180.0, 0.1)));
	}
}
