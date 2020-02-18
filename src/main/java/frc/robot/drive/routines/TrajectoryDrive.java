package frc.robot.drive.routines;

import static edu.wpi.first.wpilibj.util.ErrorMessages.requireNonNullParam;

import java.util.function.Supplier;

import org.strongback.components.Clock;

import edu.wpi.first.wpilibj.Timer;
import edu.wpi.first.wpilibj.controller.PIDController;
import edu.wpi.first.wpilibj.controller.RamseteController;
import edu.wpi.first.wpilibj.controller.SimpleMotorFeedforward;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.kinematics.ChassisSpeeds;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveKinematics;
import edu.wpi.first.wpilibj.kinematics.DifferentialDriveWheelSpeeds;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import frc.robot.Constants;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.interfaces.LocationInterface;
import frc.robot.interfaces.Log;

/**
 * Walks the drivebase through a supplied list of waypoints.
 */
public class TrajectoryDrive implements DriveRoutine {

	private final Supplier<Pose2d> m_pose;
	private final RamseteController m_follower;
	private final SimpleMotorFeedforward m_feedforward;
	private final DifferentialDriveKinematics m_kinematics;
	private final PIDController m_leftController;
	private final PIDController m_rightController;
	private DifferentialDriveWheelSpeeds m_prevSpeeds;
	private double m_prevTime;
	private double m_startTime = 0;
  
	// Variables extracted to support logging.
	private double m_leftSpeedSetpoint = 0;
	private double m_rightSpeedSetpoint = 0;
	private double m_leftSpeedError = 0;
	private double m_rightSpeedError = 0;
	private double m_leftPIDResult = 0;
	private double m_rightPIDResult = 0;
	private double m_leftFeedforward = 0;
	private double m_rightFeedforward = 0;
	private double m_leftOutput = 0;
	private double m_rightOutput = 0;
	private double m_targetSpeed = 0;
  
	private static Pose2d m_targetPose = new Pose2d();
	private static Pose2d m_actualPose = new Pose2d();
	private static Pose2d m_errorPose = new Pose2d();
  
	private Trajectory m_trajectory;
	private Boolean enabled = false;

	private Clock clock;
	private Log log;

	public TrajectoryDrive(LocationInterface location, Clock clock, Log log) {
		this.log = log;
		this.clock = clock;
		m_pose = location::getPose;
		m_follower = new RamseteController(Constants.DriveConstants.kRamseteB, Constants.DriveConstants.kRamseteZeta);
		m_kinematics = Constants.DriveConstants.kDriveKinematics;

		m_feedforward = new SimpleMotorFeedforward(
				Constants.DriveConstants.ksVolts,
				Constants.DriveConstants.kvVoltSecondsPerMeter,
				Constants.DriveConstants.kaVoltSecondsSquaredPerMeter);
		m_leftController =  new PIDController(Constants.DriveConstants.kPDriveVel, 0, 0);
        m_rightController = new PIDController(Constants.DriveConstants.kPDriveVel, 0, 0);

		// TODO: Also log the error in the x & y position as the RamseteController was doing.
		log.register(true, () -> clock.currentTime() - m_startTime, "TrajectoryDrive/elapsedTime")
				.register(true, () -> m_trajectory == null ? 0 : m_trajectory.getTotalTimeSeconds(), "TrajectoryDrive/totalTime")
				.register(true, () -> m_leftSpeedSetpoint, "TrajectoryDrive/trajectory/speed/leftSepoint")
				.register(true, () -> m_rightSpeedSetpoint, "TrajectoryDrive/trajectory/speed/rightSepoint")
				.register(true, () -> m_leftSpeedError, "TrajectoryDrive/trajectory/speed/leftError")
				.register(true, () -> m_rightSpeedError, "TrajectoryDrive/trajectory/speed/rightError")
				.register(true, () -> m_leftPIDResult, "TrajectoryDrive/trajectory/speed/leftPIDResult")
				.register(true, () -> m_rightPIDResult, "TrajectoryDrive/trajectory/speed/rightPIDResult")
				.register(true, () -> m_leftOutput, "TrajectoryDrive/leftOutput")
				.register(true, () -> m_rightOutput, "TrajectoryDrive/rightOutput")
				.register(true, () -> m_targetSpeed, "TrajectoryDrive/targetSpeed")
				.register(true, () -> m_targetPose.getTranslation().getX(), "TrajectoryDrive/desired/x")
				.register(true, () -> m_targetPose.getTranslation().getY(), "TrajectoryDrive/desired/y")
				.register(true, () -> m_actualPose.getTranslation().getX(), "TrajectoryDrive/actual/x")
				.register(true, () -> m_actualPose.getTranslation().getY(), "TrajectoryDrive/actual/y")
				.register(true, () -> m_errorPose.getTranslation().getX(), "TrajectoryDrive/error/x")
				.register(true, () -> m_errorPose.getTranslation().getY(), "TrajectoryDrive/error/y");
	}
	
	/**
	 * This drive routine was requested by this action. Contains the waypoints to
	 * drive through.
	 * 
	 * @param parameters
	 */
	synchronized public void reset(DriveRoutineParameters parameters) {
		m_targetSpeed = 0;
		m_prevTime = 0;
		m_trajectory = requireNonNullParam(parameters.trajectory, "trajectory", "TrajectoryDrive");
		var initialState = m_trajectory.sample(0);
		m_prevSpeeds = m_kinematics.toWheelSpeeds(
			new ChassisSpeeds(initialState.velocityMetersPerSecond,
				0,
				initialState.curvatureRadPerMeter
					* initialState.velocityMetersPerSecond));
		m_leftController.reset();
		m_rightController.reset();
	}

	@Override
	public void enable() {
		// reset() does most of the work
		enabled = true;
		m_startTime = clock.currentTime();
	}

	@Override
	synchronized public void disable() {
		enabled = false;
		m_leftSpeedSetpoint = 0;
		m_rightSpeedSetpoint = 0;
	}

	/**
	 * Query the followers to see what power level to give the motors
	 * based on the trajectory and how closely the robot is following it.
	 */
	@Override
	synchronized public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
		if (!enabled) {
			return new DriveMotion(0, 0);
		}

		double curTime = clock.currentTime() - m_startTime;
		double dt = curTime - m_prevTime;

		m_targetPose = m_trajectory.sample(curTime).poseMeters;
		m_targetSpeed = m_trajectory.sample(curTime).velocityMetersPerSecond;
		m_actualPose = m_pose.get();
		m_errorPose = m_targetPose.relativeTo(m_actualPose);

		var targetWheelSpeeds = m_kinematics
				.toWheelSpeeds(m_follower.calculate(m_pose.get(), m_trajectory.sample(curTime)));

		m_leftSpeedSetpoint = targetWheelSpeeds.leftMetersPerSecond;
		m_rightSpeedSetpoint = targetWheelSpeeds.rightMetersPerSecond;

		m_leftFeedforward = m_feedforward.calculate(m_leftSpeedSetpoint,
				(m_leftSpeedSetpoint - m_prevSpeeds.leftMetersPerSecond) / dt);

		m_rightFeedforward = m_feedforward.calculate(m_rightSpeedSetpoint,
				(m_rightSpeedSetpoint - m_prevSpeeds.rightMetersPerSecond) / dt);

		m_leftPIDResult = m_leftController.calculate(leftSpeed, m_leftSpeedSetpoint);
		m_leftOutput = m_leftFeedforward + m_leftPIDResult;
		m_leftSpeedError = m_leftSpeedSetpoint - leftSpeed;

		m_rightPIDResult = m_rightController.calculate(rightSpeed, m_rightSpeedSetpoint);
		m_rightOutput = m_rightFeedforward + m_rightPIDResult;
		m_rightSpeedError = m_rightSpeedSetpoint - rightSpeed;

		m_prevTime = curTime;
		m_prevSpeeds = targetWheelSpeeds;

		return new DriveMotion(m_leftOutput, m_rightOutput);
	}
	
	@Override
	public boolean hasFinished() {
		return clock.currentTime() - m_startTime > m_trajectory.getTotalTimeSeconds();
	}
	
	@Override
	public String getName() {
		return "Trajectory";
	}
}
