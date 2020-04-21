package frc.robot.lib;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;

import static frc.robot.Constants.*;

public class PoseHelper {
    /**
	 * Stops at a distance as we approach a pose
	 * 
	 * @param pose the pose to approach
	 * @param dist distance away from pose
	 * @param bearing the bearing we approach pose from
	 */
	public static Pose2d approachPose(Pose2d pose, double dist, double bearing) {
		double x = dist * MathUtil.cos(bearing);
		double y = dist * MathUtil.sin(bearing);
		
		return new Pose2d(pose.getTranslation().getX() + x, pose.getTranslation().getY() + y, new Rotation2d(MathUtil.degreesToRadians(bearing)));
	}

	/**
	 * Stops half robot length away from balls to intake
	 * 
	 * @param pose the pose of the target ball
	 * @param bearing the bearing we approach pose from
	 */
	public static Pose2d intakeAt(Pose2d pose, double bearing) {		
		return approachPose(pose, HALF_ROBOT_LENGTH, bearing);
	}
}