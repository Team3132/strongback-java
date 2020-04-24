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
    Pose2d zero = new Pose2d(0, 0, new Rotation2d(0));
    Pose2d pose = new Pose2d(-10, 10, new Rotation2d(MathUtil.degreesToRadians(150))); 

    @Test
    public void testZero() {

        assertThat(approachPose(zero, 0, 0), is(new Pose2d(0, 0, new Rotation2d(MathUtil.degreesToRadians(0)))));
    }

    @Test 
    public void testPositiveDistances() {

        assertThat(approachPose(zero, 2, 0), is(new Pose2d(2, 0, new Rotation2d(MathUtil.degreesToRadians(0)))));
        assertThat(approachPose(pose, 19.5, 0), is(new Pose2d(9.5, 10, new Rotation2d(MathUtil.degreesToRadians(0)))));
    }

    @Test 
    public void testNegativeDistances() {

        assertThat(approachPose(zero, -2, 0), is(new Pose2d(-2, 0, new Rotation2d(MathUtil.degreesToRadians(0)))));
        assertThat(approachPose(pose, -19.5, 0), is(new Pose2d(-29.5, 10, new Rotation2d(MathUtil.degreesToRadians(0)))));
    }

    @Test 
    public void testAngles() {
        double dist = 5.0;
        
        //Testing 0, 90, 180, 270, 360
        assertThat(approachPose(pose, dist, 0), is(new Pose2d(-10 + dist, 10, new Rotation2d(MathUtil.degreesToRadians(0)))));
        assertThat(approachPose(pose, dist, 90), is(new Pose2d(-10, 10 + dist, new Rotation2d(MathUtil.degreesToRadians(90)))));
        assertThat(approachPose(pose, dist, 180), is(new Pose2d(-10 - dist, 10, new Rotation2d(MathUtil.degreesToRadians(180)))));
        assertThat(approachPose(pose, dist, 270), is(new Pose2d(-10, 10 - dist, new Rotation2d(MathUtil.degreesToRadians(270)))));
        assertThat(approachPose(pose, dist, 360), is(new Pose2d(-10 + dist, 10, new Rotation2d(MathUtil.degreesToRadians(360)))));

    }

    @Test 
    public void testWeirderAngles() {
        double dist = 5.0;
        
        assertThat(approachPose(zero, dist, -90), is(new Pose2d(0, -dist, new Rotation2d(MathUtil.degreesToRadians(270)))));
        assertThat(approachPose(zero, dist, 450), is(new Pose2d(0, dist, new Rotation2d(MathUtil.degreesToRadians(90)))));
        assertThat(approachPose(zero, dist, 30), is(new Pose2d(dist * Math.sqrt(3) / 2, dist / 2, new Rotation2d(MathUtil.degreesToRadians(30)))));
        assertThat(approachPose(pose, 0, 275), is(new Pose2d(-10, 10, new Rotation2d(MathUtil.degreesToRadians(275)))));
        assertThat(approachPose(pose, -41.5, 300), is(new Pose2d(- 10 + -41.5 / 2, 10 - -41.5 * Math.sqrt(3) / 2, new Rotation2d(MathUtil.degreesToRadians(300)))));
    }

    @Test 
    public void testPoses() {
        
        Pose2d firstQuadrant = new Pose2d(7, 13, new Rotation2d(MathUtil.degreesToRadians(125)));
        assertThat(approachPose(firstQuadrant, -3, 45), is(new Pose2d(7 - 3.0 / Math.sqrt(2), 13 - 3.0 / Math.sqrt(2), new Rotation2d(MathUtil.degreesToRadians(45)))));

        Pose2d secondQuadrant = new Pose2d(-11, 8, new Rotation2d(MathUtil.degreesToRadians(339)));
        assertThat(approachPose(secondQuadrant, -3, 0), is(new Pose2d(-11 - 3, 8, new Rotation2d(MathUtil.degreesToRadians(0)))));

        Pose2d thirdQuadrant = new Pose2d(-13, -198.4, new Rotation2d(MathUtil.degreesToRadians(0)));
        assertThat(approachPose(thirdQuadrant, -3, 30), is(new Pose2d(-13 - 3.0 * Math.sqrt(3) / 2, -198.4 - 3.0 / 2, new Rotation2d(MathUtil.degreesToRadians(30)))));

        Pose2d fourthQuadrant = new Pose2d(17, -2, new Rotation2d(MathUtil.degreesToRadians(150)));
        assertThat(approachPose(fourthQuadrant, -3, -30), is(new Pose2d(17 - 3.0 * Math.sqrt(3) / 2, -2 + 3.0 / 2, new Rotation2d(MathUtil.degreesToRadians(330)))));      

    }

}
