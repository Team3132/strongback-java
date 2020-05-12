package frc.robot.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.strongback.components.Clock;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.interfaces.ColourWheelInterface.ColourAction;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineType;
import frc.robot.interfaces.JevoisInterface.CameraMode;
import frc.robot.lib.LEDColour;
import frc.robot.lib.TimeAction;
import frc.robot.lib.WheelColour;
import frc.robot.subsystems.Subsystems;

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


	public String LogString = null;

	// Location
	public Pose2d currentPose = null;

	// Intake
	public Boolean intakeExtended = null; // Intake is either extended or retracted.
	public Double intakeRPS = null; 
	
	// Shooter
	public Double shooterRPS = null;  // Set the shooter target speed.
	public Boolean shooterUpToSpeed = null;
	public Boolean shooterHoodExtended = null;

	// Loader
	public Double loaderPassthroughDutyCycle = null;
	public Boolean loaderPaddleBlocking = null;
	public Double loaderSpinnerRPS = null;
	public Integer expectedNumberOfBalls = null;

	// Vision
	public CameraMode cameraMode = null;

	// Driving / Climbing
	public DriveRoutineParameters drive = null;
	public Boolean climbMode = null; //false means drive mode, true means climb mode
	public Boolean climberBrakeApplied = null;

	// Buddy Climb
	public Boolean buddyClimbDeployed = null;

	//Colour Wheel
	public ColourAction colourAction = null;
	public Boolean extendColourWheel = null;

	//LED strip
	public LEDColour ledColour = null;
	
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
		intakeExtended = subsystems.intake.isExtended();
		intakeRPS = subsystems.intake.getTargetRPS();
		buddyClimbDeployed = subsystems.buddyClimb.isExtended();
		climbMode = subsystems.drivebase.isClimbModeEnabled();
		climberBrakeApplied = subsystems.drivebase.isBrakeApplied();
		loaderSpinnerRPS = subsystems.loader.getTargetSpinnerRPS();
		loaderPassthroughDutyCycle = subsystems.loader.getTargetPassthroughDutyCycle();
		loaderPaddleBlocking = subsystems.loader.isPaddleBlocking();
		shooterRPS = subsystems.shooter.getTargetRPS();
		shooterUpToSpeed = null;  // Leave as null so it can be ignored downstream.
		shooterHoodExtended = subsystems.shooter.isHoodExtended();
		drive = subsystems.drivebase.getDriveRoutineParameters();
		colourAction = subsystems.colourWheel.getDesiredAction();
		extendColourWheel = subsystems.colourWheel.isArmExtended();
		expectedNumberOfBalls = null;  // Leave as null so it can be ignored downstream.
		ledColour = null;
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

	/**  
	*  Set Status Message
	*  @param Status to get as a string.
	*/
	public State setLogMessage(String message) {
		LogString = message;
		return this;
	}

	// Location
	public State setCurrentPostion(Pose2d pose) {
		currentPose = pose;
		return this;
	}

	// Intake
	public State deployIntake() {
		intakeExtended = Boolean.valueOf(true);
		return this;
	}

	public State stowIntake() {
		intakeExtended = Boolean.valueOf(false);
		return this;
	}

	public State setIntakeExtended(boolean extended) {
		intakeExtended = Boolean.valueOf(extended);
		return this;
	}

	public State setIntakeRPS(double rps) {
		intakeRPS = Double.valueOf(rps);
		return this;
	}

	public State setShooterRPS(double rps) {
		shooterRPS = Double.valueOf(rps);
		return this;
	}

	public State waitForShooter() {
		shooterUpToSpeed = true;
		return this;
	}

	public State extendShooterHood() {
		shooterHoodExtended = true;
		return this;
	}

	public State retractShooterHood() {
		shooterHoodExtended = false;
		return this;
	}

	// Loader
	public State setSpinnerRPS(double rps) {
		loaderSpinnerRPS = Double.valueOf(rps);
		return this;
	}
	public State setPassthroughDutyCycle(double output) {
		loaderPassthroughDutyCycle = Double.valueOf(output);
		return this;
	}
	public State setPaddleBlocking(boolean blocking) {
		loaderPaddleBlocking = Boolean.valueOf(blocking);
		return this;
	}

	public State unblockShooter() {
		loaderPaddleBlocking = false;
		return this;
	}
	public State blockShooter() {
		loaderPaddleBlocking = true;
		return this;
	}

	public State waitForBalls(int numBalls) {
		expectedNumberOfBalls = Integer.valueOf(numBalls);
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

	
	// Toggle between drive and climb modes
	public State enableClimbMode() {
		climbMode = true;
		return this;
	}

	public State enableDriveMode() {
		climbMode = false;
		return this;
	}

	public State applyClimberBrake() {
		climberBrakeApplied = true;
		return this;
	}

	public State releaseClimberBrake() {
		climberBrakeApplied = false;
		return this;
	}

	public State deployBuddyClimb() {
		buddyClimbDeployed = true;
		return this;
	}

	public State stowBuddyClimb() {
		buddyClimbDeployed = false;
		return this;
	}
	
	// Color Wheel
	public State colourWheelRotational() {
		colourAction = new ColourAction(ColourAction.ColourWheelType.ROTATION, WheelColour.UNKNOWN);
		return this;
	}

	public State startColourWheelPositional(WheelColour colour) {
		colourAction = new ColourAction(ColourAction.ColourWheelType.POSITION, colour);
		return this;
	}

	public State stopColourWheel() {
		colourAction = new ColourAction(ColourAction.ColourWheelType.NONE, WheelColour.UNKNOWN);
		return this;
	}

	public State colourWheelAnticlockwise() {
		colourAction = new ColourAction(ColourAction.ColourWheelType.ADJUST_WHEEL_ANTICLOCKWISE, WheelColour.UNKNOWN);
		return this;
	}

	public State colourWheelClockwise() {
		colourAction = new ColourAction(ColourAction.ColourWheelType.ADJUST_WHEEL_CLOCKWISE, WheelColour.UNKNOWN);
		return this;
	}

	public State extendedColourWheel() {
		extendColourWheel = Boolean.valueOf(true);
		return this;
	}

	public State retractColourWheel() {
		extendColourWheel = Boolean.valueOf(false);
		return this;
	}

	//LED strip
	
	public State setColour(LEDColour c) {
		ledColour = c;
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
		drive = new DriveRoutineParameters(DriveRoutineType.ARCADE_DUTY_CYCLE);
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

	public State doVisionAim(){
		drive = new DriveRoutineParameters(DriveRoutineType.VISION_AIM);
		return this;
	}

	/**
	 * Add waypoints for the drive base to drive through.
	 * Note: The robot will come to a complete halt after each list
	 * of Waypoints, so each State will cause the robot to drive and then
	 * halt ready for the next state. This should be improved.
	 * Waypoints are relative to the robots position.
	 * @param start the assumed starting point and angle. 
	 * @param waypoints list of Waypoints to drive through.
	 * @param end the end point and angle.
	 * @param forward drive forward through waypoints.
	 */
	public State driveRelativeWaypoints(Pose2d start, List<Translation2d> interiorWaypoints, Pose2d end,
			boolean forward) {
		drive = DriveRoutineParameters.getDriveWaypoints(start, interiorWaypoints, end, forward, true);
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
					// In some cases this field in currentState can also be null.				
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
	 * Auto fill the endState to be applied when a sequence is interrupted
	 */
	public void fillInterrupt(State newState) {
		intakeRPS = fillParam(intakeRPS, newState.intakeRPS);
		loaderPassthroughDutyCycle = fillParam(loaderPassthroughDutyCycle, newState.loaderPassthroughDutyCycle);
		loaderSpinnerRPS = fillParam(loaderSpinnerRPS, newState.loaderSpinnerRPS);
		shooterRPS = fillParam(shooterRPS, newState.shooterRPS);
		intakeExtended = fillParam(intakeExtended, newState.intakeExtended);
	}

	/**
	 * Set value to newValue if newValue is non null
	 */
	private static <T> T fillParam(T value, T newValue){
		if (newValue != null)
			return newValue;
		else 
			return value;
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
		maybeAdd("buddyClimbDeployed", buddyClimbDeployed, result);
		maybeAdd("cameraMode", cameraMode, result);
		maybeAdd("colourWheelExtended", extendColourWheel, result);
		maybeAdd("colourwheelMode", colourAction, result);
		maybeAdd("climberBrakeApplied", climberBrakeApplied, result);
		maybeAdd("climbMode", climbMode, result);
		maybeAdd("drive", drive, result);
		maybeAdd("intakeExtended", intakeExtended, result);
		maybeAdd("loaderPaddleBlocking", loaderPaddleBlocking, result);
		maybeAdd("intakeRPS", intakeRPS, result);
		maybeAdd("loaderPassthroughDutyCycle", loaderPassthroughDutyCycle, result);
		maybeAdd("loaderSpinnerRPS", loaderSpinnerRPS, result);
		maybeAdd("shooterHoodExtended", shooterHoodExtended, result);
		maybeAdd("shooterRPS", shooterRPS, result);
		maybeAdd("shooterUpToSpeed", shooterUpToSpeed, result);
		maybeAdd("timeAction", timeAction, result);
		maybeAdd("waitForBalls", expectedNumberOfBalls, result);
		maybeAdd("logString", LogString, result);
		return "[" + String.join(",", result) + "]";
	}
}