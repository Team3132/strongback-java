package frc.robot.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;

import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineType;
import org.strongback.components.Clock;

import frc.robot.interfaces.ClimberInterface.ClimberAction;
import frc.robot.interfaces.ColourWheelInterface.Colour;
import frc.robot.interfaces.ColourWheelInterface.ColourAction;
import frc.robot.interfaces.JevoisInterface.CameraMode;
import frc.robot.lib.TimeAction;
import frc.robot.subsystems.Subsystems;

import jaci.pathfinder.Waypoint;

/**
 * Top level class to hold / specify some sort of current or target state.
 * 
 * This allows callers of the Controller to specify the target state.
 * These are held by 
 */

public class State {
	// Double and Boolean are used instead of double and boolean
	// so that null can be used to indicate that the state shouldn't
	// be changed and the current state be preserved.

	// Time
	public TimeAction timeAction = null; // How we should/shouldn't delay at the end of this state

	// Intake
	public Boolean intakeExtended = null; // Intake is either extended or retracted.
	public Double intakeMotorOutput = null;  // How much current to give the intake motors.
	
	// Passthrough
	public Double passthroughMotorOutput = null;

	// Vision
	public CameraMode cameraMode = null;

	// Climber
	public ClimberAction climber = null;  // What the climber should do.

	// Driving.
	public DriveRoutineParameters drive = null;

	public Waypoint resetPosition = null;  // Reset where the location subsystem thinks the robot is

	//Colour Wheel
	public ColourAction colourWheel = null;

	/**
	 * Create a blank state
	 */
	public State() {}

	/**
	 * Create a state based on the current positions, targets, time etc.
	 * as tracked or measured by the subsystems
	 */
	public State(Subsystems subsystems, Clock clock) {
		setDelayUntilTime(clock.currentTime());
		intakeMotorOutput = subsystems.intake.getMotorOutput();
		intakeExtended = subsystems.intake.isExtended();
		passthroughMotorOutput = subsystems.passthrough.getTargetMotorOutput();
		climber = subsystems.climber.getDesiredAction();
		drive = subsystems.drivebase.getDriveRoutine();
		colourWheel = subsystems.colourWheel.getDesiredAction();
	}

	// Time
	/**
	 * Set absolute time that the robot has to wait until.
	 * Use this or setDelayDelta(), not both.
	 * @param time measured in seconds, eg time_t.
	 */
	public State setDelayUntilTime(double time) {
		timeAction = new TimeAction(TimeAction.Type.DELAY_UNTIL, time);
		return this;
	}

	/**
	 * Wait for delta seconds.
	 * Use this or setDelayUntilTime(), not both.
	 * @param seconds to apply to the current time.
	 */
	public State setDelayDelta(double seconds) {
		timeAction = new TimeAction(TimeAction.Type.DELAY_DELTA, seconds);
		return this;
	}

	// Intake
	public State deployIntake() {
		intakeExtended = Boolean.valueOf(true);
		return this;
	}

	public State stowIntake() {
		intakeExtended = Boolean.valueOf(true);
		return this;
	}

	public State setIntakeExtended(boolean extended) {
		intakeExtended = Boolean.valueOf(extended);
		return this;
	}

	public State setIntakeMotorOutput(double output) {
		intakeMotorOutput = Double.valueOf(output);
		return this;
	}

	// Passthrough
	public State setPassthroughMotorOutput(double output) {
		passthroughMotorOutput = Double.valueOf(output);
		return this;
	}

	// Vision
	public State doCameraWebcam() {
		this.cameraMode = CameraMode.WEBCAM;
		return this;
	}

	public State doCameraVision() {
		this.cameraMode = CameraMode.VISION;
		return this;
	}


	// Climber
	public State setFrontHeight(double height) {
		climber = new ClimberAction(ClimberAction.Type.SET_FRONT_HEIGHT, height);
		return this;
	}

	public State stopBothHeight() {
		climber = new ClimberAction(ClimberAction.Type.STOP_BOTH_HEIGHT, 0);
		return this;
	}

	public State setRearHeight(double height) {
		climber = new ClimberAction(ClimberAction.Type.SET_REAR_HEIGHT, height);
		return this;
	}

	public State setBothHeight(double height) {
		climber = new ClimberAction(ClimberAction.Type.SET_BOTH_HEIGHT, height);
		return this;
	}

	public State setClimberDriveSpeed(double speed) {
		climber = new ClimberAction(ClimberAction.Type.SET_DRIVE_SPEED, speed);
		return this;
	}

	// Color Wheel
	public State colourWheelRotational() {
		colourWheel = new ColourAction(ColourAction.Type.ROTATION, Colour.UNKNOWN);
		return this;
	}

	public State colourWheelPositional(Colour colour) {
		colourWheel = new ColourAction(ColourAction.Type.POSITION, colour);
		return this;
	}

	public State stopColourWheel() {
		colourWheel = new ColourAction(ColourAction.Type.NONE, Colour.UNKNOWN);
		return this;
	}

	public State colourWheelLeft() {
		colourWheel = new ColourAction(ColourAction.Type.ADJUST_WHEEL_ANTICLOCKWISE, Colour.UNKNOWN);
		return this;
	}

	public State colourWheelRight() {
		colourWheel = new ColourAction(ColourAction.Type.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN);
		return this;
	}

	// Drive base
	/**
	 * Set the power levels on the drive base.
	 * Used to drive the robot forward or backwards in a
	 * "straight" line for the climb.
	 */
	public State setDrivebasePower(double power) {
		drive = DriveRoutineParameters.getConstantPower(power);
		return this;
	}

	/**
	 * Set the speed on the drive base.
	 * Used to drive the robot forward or backwards in a
	 * "straight" line for the L3 climb.
	 */
	public State setDrivebaseSpeed(double speed) {
		drive = DriveRoutineParameters.getConstantSpeed(speed);
		return this;
	}

	/**
	 * Put the drive base in arcade drive mode using velocity control for the driver.
	 */
	public State doArcadeVelocityDrive() {
		drive = new DriveRoutineParameters(DriveRoutineType.ARCADE_VELOCITY);
		return this;
	}

	/**
	 * Put the drive base in vision drive mode using camera to control the steering.
	 */
	public State doVisionAssistDrive() {
		drive = new DriveRoutineParameters(DriveRoutineType.VISION_ASSIST);
		return this;
	}

	/**
	 * Put the drive base in arcade drive mode for the driver.
	 */
	public State doArcadeDrive() {
		drive = new DriveRoutineParameters(DriveRoutineType.ARCADE);
		return this;
	}

	/**
	 * Put the drive base in cheesy drive mode for the driver.
	 */
	public State doCheesyDrive() {
		drive = new DriveRoutineParameters(DriveRoutineType.CHEESY);
		return this;
	}

	public State doPositionPIDArcade() {
		drive = new DriveRoutineParameters(DriveRoutineType.POSITION_PID_ARCADE);
		return this;
	}

	public State doTurnToHeading(double heading) {
		drive = new DriveRoutineParameters(DriveRoutineType.TURN_TO_ANGLE);
		drive.value = heading;
		return this;
	}

	/**
	 * Add waypoints for the drive base to drive through.
	 * Note: The robot will come to a complete halt after each list
	 * of Waypoints, so each State will cause the robot to drive and then
	 * halt ready for the next state. This should be improved.
	 * Wayoints are relative to the robots position.
	 * @param waypoints list of Waypoints to drive through.
	 * @param forward drive forward through waypoints.
	 */
	public State driveRelativeWaypoints(Waypoint[] waypoints, boolean forward) {
		drive = DriveRoutineParameters.getDriveWaypoints(waypoints, forward, true);
		return this;
	}
	

	/**
	 * Creates a copy of desiredState whose null variables are replaced by values in currentState
	 * 
	 * @param desiredState the state we will copy and fill in
	 * @param currentState a state from which we should replace null values with
	 * @return a copy of desiredState whose null variables are replaced with the values in currentState
	 */
	public static State calculateUpdatedState(State desiredState, State currentState) {
		State updatedState = new State();
		Field[] fields = desiredState.getClass().getFields();
		for (Field field : fields) {
			// We arguable should fail here if these are the
			// if (!field.canAccess(this)) continue;
			// if (!field.canAccess(current)) continue;
			try {
				if (field.get(desiredState) == null) {
					field.set(updatedState, field.get(currentState));
				} else {
					field.set(updatedState, field.get(desiredState));
				}
			} catch (IllegalArgumentException | IllegalAccessException e) {
				// these exceptions should not be possible
				// getFields() returns only publicly accessible fields
				// this and current are instances of the same class 
				e.printStackTrace();
			}
		}
		return updatedState;
	}
	
	/**
	 * Append the description and value for this parameter if value is non null.
	 * @param name of the parameter.
	 * @param value of the parameter. May be null.
	 * @param result - StringBuilder to add to.
	 */
	private static <T> void maybeAdd(String name, T value, ArrayList<String> result) {
		if (value == null) return;  // Ignore this value.
		result.add(name + ":" + value);
	}
		
	@Override
	public String toString() {
		ArrayList<String> result = new ArrayList<String>();
		maybeAdd("intakeExtended", intakeExtended, result);
		maybeAdd("intakeMotorOutput", intakeMotorOutput, result);
		maybeAdd("passthroughMotorOutput", passthroughMotorOutput, result);
		maybeAdd("drive", drive, result);
		maybeAdd("climber", climber, result);
		maybeAdd("timeAction", timeAction, result);
		maybeAdd("cameraMode", cameraMode, result);
		maybeAdd("colourwheelMode", colourWheel, result);
	
		return "[" + String.join(",", result) + "]";
	}
}