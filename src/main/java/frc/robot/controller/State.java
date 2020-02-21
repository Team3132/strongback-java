package frc.robot.controller;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import frc.robot.interfaces.DrivebaseInterface.DriveRoutineParameters;
import frc.robot.interfaces.DrivebaseInterface.DriveRoutineType;
import org.strongback.components.Clock;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
import frc.robot.interfaces.ClimberInterface.ClimberAction;
import frc.robot.interfaces.ColourWheelInterface.Colour;
import frc.robot.interfaces.ColourWheelInterface.ColourAction;
import frc.robot.interfaces.HatchInterface.HatchAction;
import frc.robot.interfaces.JevoisInterface.CameraMode;
import frc.robot.interfaces.LiftInterface.LiftAction;
import frc.robot.lib.TimeAction;
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

	// Lift
	public LiftAction liftAction = null;  // Lift position.
	public Boolean liftDeploy = null;

	// Intake
	public Boolean intakeExtended = null; // Intake is either extended or retracted.
	public Double intakeMotorOutput = null;  // How much current to give the intake motors.
	
	// Spitter
	public Double spitterDutyCycle = null;  // Power level to supply to spitter. -1..0..1
	public Boolean hasCargo = null; // Should the robot wait for cargo to arrive or leave?

	// Passthrough
	public Double passthroughMotorOutput = null;

	// Hatch
	public HatchAction hatchAction = null;  // How the hatch should be positioned.
	public Boolean hatchHolderEnabled = null;

	// Vision
	public CameraMode cameraMode = null;

	// Climber
	public ClimberAction climber = null;  // What the climber should do.

	// Driving.
	public DriveRoutineParameters drive = null;

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
		setLiftHeight(subsystems.lift.getTargetHeight());
		intakeMotorOutput = subsystems.intake.getMotorOutput();
		intakeExtended = subsystems.intake.isExtended();
		passthroughMotorOutput = subsystems.passthrough.getTargetMotorOutput();
		spitterDutyCycle = subsystems.spitter.getTargetDutyCycle();
		hasCargo = subsystems.spitter.hasCargo();
		climber = subsystems.climber.getDesiredAction();
		hatchAction = subsystems.hatch.getAction();
		hatchHolderEnabled = subsystems.hatch.getHeld();
		liftDeploy = subsystems.lift.shouldBeDeployed();
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



	// Lift
	/**
	 * Set absolute lift height.
	 * @param height in inches from the bottom of the lift.
	 */
	public State setLiftHeight(double height) {
		liftAction = new LiftAction(LiftAction.Type.SET_HEIGHT, height);
		return this;
	}

	/**
	 * Adjust the current lift height by delta.
	 * @param delta to apply to the current height.
	 */
	public State setLiftHeightDelta(double delta) {
		liftAction = new LiftAction(LiftAction.Type.ADJUST_HEIGHT, delta);
		return this;
	}

	/**
	 * Move the lift down a setpoint
	 */
	public State setLiftSetpointUp() {
		liftAction = new LiftAction(LiftAction.Type.SETPOINT_UP, 0); // the zero does nothing
		return this;
	}

	/**
	 * Move the lift down a setpoint
	 */
	public State setLiftSetpointDown() {
		liftAction = new LiftAction(LiftAction.Type.SETPOINT_DOWN, 0); // the zero does nothing
		return this;
	}


	public State deployLift() {
		liftDeploy = Boolean.valueOf(true);
		return this;
	}

	public State retractLift() {
		liftDeploy = Boolean.valueOf(false);
		return this;
	}

	public State setLiftDeployer(boolean state) {
		liftDeploy = Boolean.valueOf(state);
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


	// Spitter
	public State setSpitterDutyCycle(double dutyCycle) {
		spitterDutyCycle = Double.valueOf(dutyCycle);
		return this;
	}

	public State waitForCargo() {
		hasCargo = Boolean.valueOf(true);
		return this;
	}

	public State waitForCargoToLeave() {
		hasCargo = Boolean.valueOf(false);
		return this;
	}

	public State setHasCargo(boolean hasCargo) {
		this.hasCargo = Boolean.valueOf(hasCargo);
		return this;
	}


	// Passthrough
	public State setPassthroughMotorOutput(double output) {
		passthroughMotorOutput = Double.valueOf(output);
		return this;
	}


	// Hatch
	/**
	 * Sets the target position of the hatch in inches from
	 * the right side of the robot. zero is stowed.
	 */
	public State setHatchPosition(double position) {
		hatchAction = new HatchAction(HatchAction.Type.SET_POSITION, position);
		return this;
	}

	/**
	 * [Micro]adjust the position of the hatch relative to it's current target.
	 */
	public State setHatchPositionDelta(double delta) {
		hatchAction = new HatchAction(HatchAction.Type.ADJUST_POSITION, delta);
		return this;
	}

	/**
	 * Set the hatch motor power to the nominated power value.
	 */
	public State setHatchPower(double power) {
		hatchAction = new HatchAction(HatchAction.Type.SET_MOTOR_POWER, power);
		return this;
	}

	public State calibrateHatch() {
		hatchAction = new HatchAction(HatchAction.Type.CALIBRATE, 0);
		return this;
	}

	public State grabHatch() {
		hatchHolderEnabled = Boolean.valueOf(true);
		return this;
	}

	public State releaseHatch() {
		hatchHolderEnabled = Boolean.valueOf(false);
		return this;
	}

	public State setHatchHolderEnabled(boolean grabbed) {
		hatchHolderEnabled = Boolean.valueOf(grabbed);
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
		colourWheel = new ColourAction(ColourAction.ColourWheelType.ROTATION, Colour.UNKNOWN);
		return this;
	}

	public State colourWheelPositional(Colour colour) {
		colourWheel = new ColourAction(ColourAction.ColourWheelType.POSITION, colour);
		return this;
	}

	public State stopColourWheel() {
		colourWheel = new ColourAction(ColourAction.ColourWheelType.NONE, Colour.UNKNOWN);
		return this;
	}

	public State colourWheelLeft() {
		colourWheel = new ColourAction(ColourAction.ColourWheelType.ADJUST_WHEEL_ANTICLOCKWISE, Colour.UNKNOWN);
		return this;
	}

	public State colourWheelRight() {
		colourWheel = new ColourAction(ColourAction.ColourWheelType.ADJUST_WHEEL_CLOCKWISE, Colour.UNKNOWN);
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

	public State doVisionAim(){
		drive = new DriveRoutineParameters(DriveRoutineType.VISION_AIM);
		return this;
	}



	/**
	 * Add waypoints for the drive base to drive through.
	 * Note: The robot will come to a complete halt after each list
	 * of Waypoints, so each State will cause the robot to drive and then
	 * halt ready for the next state. This should be improved.
	 * Wayoints are relative to the robots position.
	 * @param start the assumed starting point and angle. Other point are relative to this.
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
		maybeAdd("spitterDutyCycle", spitterDutyCycle, result);
		maybeAdd("hasCargo", hasCargo, result);
		maybeAdd("hatchAction", hatchAction, result);
		maybeAdd("hatchHolderGrabbed", hatchHolderEnabled, result);
		maybeAdd("liftDeploy", liftDeploy, result);
		maybeAdd("liftAction", liftAction, result);
		maybeAdd("drive", drive, result);
		maybeAdd("climber", climber, result);
		maybeAdd("timeAction", timeAction, result);
		maybeAdd("cameraMode", cameraMode, result);
		maybeAdd("colourwheelMode", colourWheel, result);
	
		return "[" + String.join(",", result) + "]";
	}
}