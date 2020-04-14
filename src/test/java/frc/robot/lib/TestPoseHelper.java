package frc.robot.lib;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.is;

import static frc.robot.lib.PoseHelper.*;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import org.junit.Test;

/**
 * Test the PoseHelper class.
 * 
 * To run just this test, use:
 *   ./gradlew test --tests "frc.robot.lib.TestPoseHelper"
 */
public class TestPoseHelper {
	@Test
	public void testAddVector() {
        Pose2d zero = new Pose2d(0, 0, new Rotation2d(0));
        assertThat(approachPose(zero, 0, 0), is(new Pose2d(0, 0, new Rotation2d(0))));

        double dist = 5.0;
        //Testing 0, 90, 180, 270, 360
        assertThat(approachPose(zero, dist, 0), is(new Pose2d(dist, 0, new Rotation2d(0))));
        assertThat(approachPose(zero, dist, 90), is(new Pose2d(0, dist, new Rotation2d(0))));
        assertThat(approachPose(zero, dist, 180), is(new Pose2d(-dist, 0, new Rotation2d(0))));
        assertThat(approachPose(zero, dist, 270), is(new Pose2d(0, -dist, new Rotation2d(0))));
        assertThat(approachPose(zero, dist, 360), is(new Pose2d(dist, 0, new Rotation2d(0))));

        // Some other angles 
        assertThat(approachPose(zero, dist, -90), is(new Pose2d(0, -dist, new Rotation2d(0))));
        assertThat(approachPose(zero, dist, 450), is(new Pose2d(0, dist, new Rotation2d(0))));
        assertThat(approachPose(zero, dist, 30), is(new Pose2d(dist * Math.sqrt(3) / 2, dist / 2, new Rotation2d(0))));

        // -ve distance 
        assertThat(approachPose(zero, -dist, 0), is(new Pose2d(-dist, 0, new Rotation2d(0))));
	}
}
