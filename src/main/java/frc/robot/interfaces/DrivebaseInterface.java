package frc.robot.interfaces;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

import org.strongback.Executable;

import edu.wpi.first.wpilibj.Filesystem;
import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import edu.wpi.first.wpilibj.trajectory.Trajectory;
import edu.wpi.first.wpilibj.trajectory.TrajectoryConfig;
import edu.wpi.first.wpilibj.trajectory.TrajectoryGenerator;
import edu.wpi.first.wpilibj.trajectory.TrajectoryUtil;
import frc.robot.Config;
import frc.robot.drive.routines.DriveRoutine;

/**
 * The Drivebase subsystem is responsible for dealing with the drivebase.
 * It will call the location subsystem when things on the drivebase change, and it
 * requests information from the DriveControl to tell it how to move.
 * 
 * The Drivebase is passed the motors and other devices it uses and implements the
 * control algorithms needed to co-ordinate actions on these devices.
 */
public abstract interface DrivebaseInterface extends Executable, SubsystemInterface, DashboardUpdater {
	public enum DriveRoutineType {
		CONSTANT_POWER,  // Set a constant power to drive wheels.
		CONSTANT_SPEED,  // Set a constant speed to drive wheels.
		ARCADE_DUTY_CYCLE,  // Normal arcade drive.
		CHEESY,  // Cheesy drive using the drivers joysticks.
		TRAJECTORY,  // Drive through waypoints.
		VISION_ASSIST,  // Driver has speed control and vision has turn control.
		TAPE_ASSIST,  // Driver has speed control and tape subsystem has turn control.
		TURN_TO_ANGLE,  // Turn to specified angle, relative to the angle the robot started.
		POSITION_PID_ARCADE,
		ARCADE_VELOCITY,// Normal arcade drive.;
		VISION_AIM;  //Shooter things

	}

	public DrivebaseInterface activateClimbMode(boolean enabled);
	public DrivebaseInterface applyBrake(boolean enabled);


	/**
	 * The values to give to the motors on each side of the robot.
	 */
	public class DriveMotion {
		public double left;
		public double right;
		
		public DriveMotion(double left, double right) {
			this.left = left;
			this.right = right;
		}

		@Override
		public String toString() {
			return "Left: " + left + ", Right: " + right;
		}
		
		public boolean equals(Object o) {
			if (o == this) {
				return true;
			}
			if (!(o instanceof DriveMotion)) {
				return false;
			}
			DriveMotion m = (DriveMotion) o;
			return m.left == left && m.right == right;
		}

		public int hashCode() {
			return (int) (1000 * left + right);
		}
	}

	

	public class DriveRoutineParameters {
		public DriveRoutineParameters(DriveRoutineType type) {
			this.type = type;
		}  // Disable.

		public static DriveRoutineParameters getConstantPower(double power) {
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.CONSTANT_POWER);
			p.value = power;
			return p;
		}

		public static DriveRoutineParameters getConstantSpeed(double speed) {
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.CONSTANT_SPEED);
			p.value = speed;
			return p;
		}

		public static DriveRoutineParameters getArcade() {
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.ARCADE_DUTY_CYCLE);
			return p;
		}

		public static DriveRoutineParameters getDriveWaypoints(Pose2d start, List<Translation2d> interiorWaypoints,
				Pose2d end, boolean forward, boolean relative) {
			
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.TRAJECTORY);
			
			p.trajectory = generateTrajectory(start, interiorWaypoints, end, forward, relative);
			p.relative = relative;
			return p;
		}

		public static DriveRoutineParameters turnToAngle(double angle) {
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.TURN_TO_ANGLE);
			p.value = angle;
			return p;
		}

		public static DriveRoutineParameters positionPIDArcade() {
			return new DriveRoutineParameters(DriveRoutineType.POSITION_PID_ARCADE);
		}

		/**
		 * Returns a trajectory by first checking for any cached trajectories in the deploy directory. 
		 * If it doesn't already exist, generate a trajectory then export it. 
		 * 
		 * This should only be used for unit tests.
		 */
		public static Trajectory generateTrajectory(Pose2d start, List<Translation2d> interiorWaypoints,
				Pose2d end, boolean forward, boolean relative, Path path) {
			
			int hash = Arrays.deepHashCode(new Object[] {start, interiorWaypoints, end, forward});
			String trajectoryJSON = String.valueOf(hash) + ".wpilib.json";
			Path trajectoryPath = path.resolve(trajectoryJSON);
			
			try {
				return TrajectoryUtil.fromPathweaverJson(trajectoryPath);
			} catch (FileNotFoundException e) {
				System.out.println("Trajectory file not found: Starting to generate and caching spline.");		
			} catch (IOException e1) {
				System.out.println(e1);
			}

			// Build the trajectory on start so that it's ready when needed.
			// Create config for trajectory
			TrajectoryConfig config = new TrajectoryConfig(Config.drivebase.trajectory.maxSpeedMetersPerSecond,
					Config.drivebase.trajectory.maxAccelerationMetersPerSecondSquared)
							// Add kinematics to ensure max speed is actually obeyed
							.setKinematics(Config.drivebase.trajectory.driveKinematics)
							// Apply the voltage constraint
							.addConstraint(Config.drivebase.trajectory.autoVoltageConstraint)
							.setReversed(!forward);

			// An example trajectory to follow. All units in meters.
			long t = System.currentTimeMillis();
			Trajectory trajectory = TrajectoryGenerator.generateTrajectory(start, interiorWaypoints, end, config);

			try {
				TrajectoryUtil.toPathweaverJson(trajectory, trajectoryPath);
				System.out.printf("Trajectory Generator: took %d milliseconds to generate and write this spline to file\n", System.currentTimeMillis() - t);
			} catch (IOException e) {
				System.out.println(e);
			}

			return trajectory;
		}

		/**
		 * Returns a trajectory by first checking for any cached trajectories in the deploy directory. 
		 * If it doesn't already exist, generate a trajectory then export it. 
		 */
		public static Trajectory generateTrajectory(Pose2d start, List<Translation2d> interiorWaypoints,
				Pose2d end, boolean forward, boolean relative)  {
			Path path = Filesystem.getDeployDirectory().toPath().resolve("paths");
			return generateTrajectory(start, interiorWaypoints, end, forward, relative, path);
		}

		public DriveRoutineType type = DriveRoutineType.ARCADE_DUTY_CYCLE;

		// Waypoint parameters.
		public Trajectory trajectory;
		public boolean relative = true;

		// Constant drive parameters
		public double value = 0;

		@Override
		public boolean equals(Object obj) {
			if (obj == null)
				return false;
			if (!(obj instanceof DriveRoutineParameters))
				return false;
			if (obj == this)
				return true;
			DriveRoutineParameters other = (DriveRoutineParameters)obj;
			// TODO: Check for an equals operator? Should we make this faster since it'll be called often?
			return type == other.type && value == other.value && trajectory == other.trajectory && relative == other.relative;
		}

		@Override
		public String toString() {
			if (type == DriveRoutineType.CONSTANT_POWER) {
				return String.format("constant power %.1f", value);
			}
			if (type == DriveRoutineType.CONSTANT_SPEED) {
				return String.format("constant speed %.1f", value);
			}
			if (type == DriveRoutineType.TURN_TO_ANGLE) {
				return String.format("turn to angle %.1f", value);
			}
			return String.format("routine=%s", type.toString().toLowerCase());
		}
	}

	/**
	 * Tell the drivebase what action/drive mode to operate in.
	 * @param parameters
	 */
	public void setDriveRoutine(DriveRoutineParameters parameters);

	public default void setArcadeDrive() {
		setDriveRoutine(DriveRoutineParameters.getArcade());
	}

	/**
	 * Get the action that was requested of the drivebase.
	 * @return
	 */
	public DriveRoutineParameters getDriveRoutineParameters();

	/**
	 * Returns false if the drivebase has more to do.
	 * Only Trajectory drive can return false in case it has
	 * more driving to do. 
	 */
	public boolean hasFinished();

	public boolean isClimbModeEnabled();
	public boolean isDriveModeEnabled();

	public boolean isBrakeApplied();
	public boolean isBrakeReleased();

	/**
	 * Register with the drivebase a way to drive the requested mode by using the supplied routine.
	 */
	public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine);
}