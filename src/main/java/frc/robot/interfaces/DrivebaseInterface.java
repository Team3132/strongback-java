package frc.robot.interfaces;

import org.strongback.Executable;
import org.strongback.components.Motor.ControlMode;

import frc.robot.drive.routines.DriveRoutine;

import jaci.pathfinder.Waypoint;

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
		ARCADE,  // Normal arcade drive.
		CHEESY,  // Cheesy drive using the drivers joysticks.
		WAYPOINTS,  // Drive through waypoints.
		VISION_ASSIST,  // Driver has speed control and vision has turn control.
		TAPE_ASSIST,  // Driver has speed control and tape subsystem has turn control.
		TURN_TO_ANGLE,  // Turn to specified angle, relative to the angle the robot started.
		POSITION_PID_ARCADE,
		ARCADE_VELOCITY;  // Normal arcade drive.;

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
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.ARCADE);
			return p;
		}
		public static DriveRoutineParameters getDriveWaypoints(Waypoint[] waypoints, boolean forward, boolean relative) {
			DriveRoutineParameters p = new DriveRoutineParameters(DriveRoutineType.WAYPOINTS);
			p.waypoints = waypoints;
			p.forward = forward;
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

		public DriveRoutineType type = DriveRoutineType.ARCADE;

		// Waypoint parameters.
		public Waypoint[] waypoints;
		public boolean forward = true;
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
			return type == other.type && value == other.value && waypoints == other.waypoints && forward == other.forward
					&& relative == other.relative;
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
	public DriveRoutineParameters getDriveRoutine();

	/**
	 * Returns false if the drivebase has more to do.
	 * Only Trajectory drive can return false in case it has
	 * more driving to do. 
	 */
	public boolean hasFinished();

	/**
	 * Register with the drivebase a way to drive the requested mode by using the supplied routine.
	 */
	public void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine, ControlMode controlMode);
	public default void registerDriveRoutine(DriveRoutineType mode, DriveRoutine routine) {
		registerDriveRoutine(mode, routine, ControlMode.PercentOutput);
	};
}