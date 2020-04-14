package frc.robot.lib;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Transform2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;

import static frc.robot.Constants.*;

public class PoseHelper {
    /**
	 * Adds a vector to a pose
	 * 
	 * @param pose the pose to add a vector to
	 * @param dist distance away from pose
	 * @param bearing the bearing we approach pose from
	 */
	public static Pose2d approachPose(Pose2d pose, double dist, double bearing) {
		double x = dist * MathUtil.cos(bearing);
		double y = dist * MathUtil.sin(bearing);
		
		Translation2d translation = new Translation2d(x, y);
        Transform2d transform = new Transform2d(translation, new Rotation2d(0));
		
		return pose.plus(transform);
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