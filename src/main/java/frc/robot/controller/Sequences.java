/**
 * Sequences for doing most actions on the robot.
 * 
 * If you add a new sequence, add it to allSequences at the end of this file.
 * 
 * Design doc:
 *   https://docs.google.com/document/d/1IBAw5dKG8hiahRkd8FzU75j3yF6No33oQXtNEKi3LXc/edit#
 */
package frc.robot.controller;

import static frc.robot.Constants.*;

import frc.robot.lib.Colour;
import frc.robot.lib.WaypointUtil;

import jaci.pathfinder.Waypoint;

/**
 * Control sequences for most robot operations.
 */
public class Sequences {
	
	/**
	 * Do nothing sequence.
	 */
	public static Sequence getEmptySequence() {
		if (emptySeq == null) {
			emptySeq = new Sequence("empty");
		}
		return emptySeq;
	}
	private static Sequence emptySeq = null;

	/**
	 * The first sequence run in the autonomous period.
	 */
	public static Sequence getStartSequence() {
		if (startSeq == null) {
			startSeq = new Sequence("start");
		}
		startSeq.add().doArcadeVelocityDrive();
		return startSeq;
	}
	private static Sequence startSeq = null;

	/**
	 * Returns the sequence to reset the robot. Used to stop ejecting etc.
	 * The lift is at intake height, the intake is stowed, all motors are off.
	 * @return
	 */
	public static Sequence getResetSequence() {
		if (resetSeq == null) {
			resetSeq = new Sequence("reset");
			resetSeq.add().doArcadeDrive();
		}
		return resetSeq;
	}
	private static Sequence resetSeq = null;

	/**
	 * Turn to face the driver station wall and then switch back to arcade.
	 */
	public static Sequence turnToWall() {
		if (driveTestSeq == null) {
			driveTestSeq = new Sequence("turn to wall");
			driveTestSeq.add().doTurnToHeading(180);
			//driveTestSeq.add().doArcadeDrive();
		}
		return driveTestSeq;
	}
	private static Sequence driveTestSeq = null;
	
	/**
	 * Drive to a point on the field, relative to the starting point.
	 */
	public static Sequence getDriveToWaypointSequence(double x, double y, double angle) {
		if (driveToWaypointSeq == null) {
			Waypoint waypoint = new Waypoint(x, y, angle);
			driveToWaypointSeq = new Sequence(String.format("drive to %s", WaypointUtil.toString(waypoint)));
			driveToWaypointSeq.add().driveRelativeWaypoints(new Waypoint[]{waypoint}, true);
		}
		return driveToWaypointSeq;
	}	
	private static Sequence driveToWaypointSeq = null;

	public static Sequence startSlowDriveForward() {
		Sequence seq = new Sequence("Slow drive forward");
		seq.add().setDrivebasePower(DRIVE_OFF_LEVEL_TWO_POWER);
		return seq;
	}

	public static Sequence setDrivebaseToArcade() {
		Sequence seq = new Sequence("Slow drive forward");
		seq.add().doArcadeDrive();
		return seq;
	}

	/**
	 * Extends the intake and then runs the motor to intake the cargo.
	 * @return
	 */

	public static Sequence startIntaking() {
		Sequence seq = new Sequence("Start intake");
		// Wait for the intake to extend before turning motor
		seq.add().deployIntake().retractLift();
		seq.add().setIntakeMotorOutput(INTAKE_MOTOR_CURRENT);
		seq.add().setLiftHeight(LIFT_DEFAULT_MIN_HEIGHT)
 				 .setHatchPosition(HATCH_INTAKE_HOLD_POSITION);
		// Waits for the lift to go to the set height before turning on to the motor
		// The spitter speed should be set at a small speed.
		seq.add().setPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT)
				 .setSpitterDutyCycle(SPITTER_SPEED); 
		seq.add().waitForCargo();
		seq.add().setSpitterDutyCycle(0)
				 .setIntakeMotorOutput(0)
				 .setPassthroughMotorOutput(0);
		return seq;
	}

	public static Sequence stopIntaking() {
		Sequence seq = new Sequence("Stop intake");
		seq.add().setSpitterDutyCycle(0)
				 .setIntakeMotorOutput(0)
				 .setPassthroughMotorOutput(0);
		seq.add().setDelayDelta(0.1);
		seq.add().setHatchPosition(HATCH_STOWED_POSITION);
		return seq;
	}

	public static Sequence raiseIntake() {
		Sequence seq = new Sequence("Raise intake");
		seq.add().stowIntake();
		return seq;
	}

	public static Sequence startCargoSpit() {
		Sequence seq = new Sequence("Start CargoSpit");
		seq.add().setHatchPosition(HATCH_STOWED_POSITION);
		seq.add().setSpitterDutyCycle(SPITTER_SCORE_SPEED);
		return seq;
	}

	public static Sequence stopCargoSpit() {
		Sequence seq = new Sequence("Stop CargoSpit");
		seq.add().setSpitterDutyCycle(0);
		// Doesn't lower the lift like startCargoSpit so that the lift doesn't drop
		// every time the operator wishes to abort the cargo spit.
		return seq;
	}

	public static Sequence startReverseCycle() {
		Sequence seq = new Sequence("Start reverse cycle");
		seq.add().setSpitterDutyCycle(-SPITTER_SPEED);
		seq.add().setPassthroughMotorOutput(-PASSTHROUGH_MOTOR_CURRENT);
		seq.add().setIntakeMotorOutput(-INTAKE_MOTOR_CURRENT);
		return seq;
	}

	public static Sequence stopReverseCycle() {
		Sequence seq = new Sequence("Stop reverse cycle");
		seq.add().setSpitterDutyCycle(0);
		seq.add().setPassthroughMotorOutput(0);
		seq.add().setIntakeMotorOutput(0);
		return seq;
	}

	public static Sequence moveLift(String name, double height) {
		Sequence seq = new Sequence("Set lift to " + name);
		seq.add().setLiftHeight(height);
		return seq;
	}

	public static Sequence moveLift(LiftSetpoint setpoint) {
		Sequence seq = new Sequence("Set lift to " + setpoint.toString());
		seq.add().setLiftHeight(setpoint.height);
		return seq;
	}

	/**
	 * Move up to the next lift setpoint.
	 */
	public static Sequence liftSetpointUp() {
		Sequence seq = new Sequence("lift setpoint up");
		seq.add().setLiftSetpointUp();
		return seq;	
	}

	/**
	 * Move down to the next lift setpoint.
	 */
	public static Sequence liftSetpointDown() {
		Sequence seq = new Sequence("lift setpoint down");
		seq.add().setLiftSetpointDown();
		return seq;	
	}
	
	/**
	 * Micro adjust lift up.
	 */
	public static Sequence getMicroAdjustUpSequence() {
		return microAdjustUpSeq;
	}

	/**
	 * Micro adjust lift down.
	 */
	public static Sequence getMicroAdjustDownSequence() {
		return microAdjustDownSeq;
	}

	private static Sequence microAdjustUpSeq = getMicroAdjustSequence(LIFT_MICRO_ADJUST_HEIGHT);
	private static Sequence microAdjustDownSeq = getMicroAdjustSequence(-LIFT_MICRO_ADJUST_HEIGHT);
	
	static public Sequence getMicroAdjustSequence(double delta) {
		Sequence seq = new Sequence("micro adjust");
		seq.add().setLiftHeightDelta(delta);
		return seq;
	}

	// Testing methods
	public static Sequence startIntakingOnly() {
		Sequence seq = new Sequence("start Intaking");
		seq.add().deployIntake();
		seq.add().setIntakeMotorOutput(INTAKE_MOTOR_CURRENT).deployIntake();
		return seq;
	}

	public static Sequence stopIntakingOnly() {
		Sequence seq = new Sequence("stop Intaking");
		seq.add().setIntakeMotorOutput(0.0);
		seq.add().stowIntake();
		return seq;
	}
	// This is to test the Passthrough system
	public static Sequence startPassthrough() {
		Sequence seq = new Sequence("start Passthrough");
		seq.add().setPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT);
		return seq;
	}

	public static Sequence stopPassthrough() {
		Sequence seq = new Sequence("stop Passthrough");
		seq.add().setPassthroughMotorOutput(0.0);
		return seq;
	}

	/*public static Sequence startSpitterOnly() {
		Sequence seq = new Sequence("Start Spitter");
		seq.add().grabHatch(); //To allow the cargo ball to pass by the hatch mechanism unobstructed
		seq.add().setSpitterDutyCycle(SPITTER_SPEED);
		return seq;
	}

	public static Sequence stopSpitterOnly() {
		Sequence seq = new Sequence("Stop Spitter");
		seq.add().setSpitterDutyCycle(0.0);
		seq.add().releaseHatch();
		return seq;
	}*/

	public static Sequence holdHatch() { 
		Sequence seq = new Sequence("Hatch hold");
		seq.add().releaseHatch();
		return seq;
	}
	public static Sequence releaseHatch() {
		Sequence seq = new Sequence("Hatch release");
		seq.add().grabHatch();
		return seq;
	}

	public static Sequence getStowHatchSequence() {
		Sequence seq = new Sequence("stow hatch");
		seq.add().setHatchPosition(HATCH_STOWED_POSITION);
		return seq;
	}

	public static Sequence getReadyHatchSequence() {
		Sequence seq = new Sequence("ready hatch");
		seq.add().setHatchPosition(HATCH_READY_POSITION);
		return seq;
	}

	public static Sequence getHatchDeltaPositionSequence(double delta) {
		Sequence seq = new Sequence("set hatch position");
		seq.add().setHatchPositionDelta(delta);
		return seq;
	}

	public static Sequence setHatchPosition(double position) {
		Sequence seq = new Sequence("move hatch to position");
		seq.add().setHatchPosition(position);
		return seq;
	}

	public static Sequence setHatchPower(double power) {
		Sequence seq = new Sequence(String.format("set hatch power: %f", power));
		seq.add().setHatchPower(power);
		return seq;
	}

	public static Sequence hatchCalibrate() {
		Sequence seq = new Sequence("calibrating hatch");
		seq.add().calibrateHatch();
		return seq;
	}

	public static Sequence startDriveByVision() {
		Sequence seq = new Sequence("start drive by vision");
		seq.add().doVisionAssistDrive();
		return seq;
	}

	public static Sequence stopDriveByVision() {
		Sequence seq = new Sequence("stop drive by vision");
		seq.add().doArcadeDrive();
		return seq;
	}

	public static Sequence liftDeploy() {
		Sequence seq = new Sequence("lift deploy");
		seq.add().deployLift();
		return seq;
	}

	public static Sequence liftRetract() {
		Sequence seq = new Sequence("lift retract");
		seq.add().retractLift();
		return seq;
	}

	/** 
	 * Climber Levels 2/3
	 * 
	 * There are six buttons (button level 2/level 3):
	 * On The RIGHT driver's joystick
	 * Step 1: Press and hold button 11/12; lift comes down and we start to climb to the appropriate height
	 * Step 2: Release button 11/12: climbing stops when the button is released (and also if the height was reached)
	 * Step 3: Press and hold button 9/10: we start driving towards the platform on the stilts
	 * Step 4: Release button 9/10: we stop driving towards the platform
	 * Step 5: Press and hold button 7/8: we start retracting the rear stilts
	 * Step 6: Release button 7/8: we stop retracting the rear stilts
	 * On The LEFT driver's joystick
	 * Step 1: Press and hold button 11/12; we start driving towards the platform on the stilts
	 * Step 2: Release button 11/12: we stop driving towards the platform
	 * Step 3: Press and hold button 9/10: we start retracting the front stilts
	 * Step 4: Release button 9/10: we stop retracting the front stilts
	 * Step 5: Press and hold button 7/8: we start driving towards the platform on the stilts
	 * Step 6: Release button 7/8: we stop driving towards the platform
	 * 
	 * Button 3 on the driver's right joystick will try and retract BOTh stilts, i.e. bring the robot back down if incorrectly raised.
	 * 
	 * The CG of the robot should keep it on the platfom. if this is not correct we need to modify this pattern.
	 * 
	 * For L3 we use the button sequence 
	 */

	public static Sequence startLevel2climb() { // Stage 1 of L2 Climb: Raise both winchs & drive towards platform (12)
		Sequence seq = new Sequence("start level 2 climb");
		seq.add().retractLift(); 
		// seq.add().setLiftHeight(LIFT_BOTTOM_HEIGHT); // Need to be lowest lift height to climb
		seq.add().setBothHeight(CLIMBER_L2_CLIMB_HEIGHT); // Front winch goes up to L2 height.
		// Keep Climbing until the driver releases the button.
		return seq;
	}
	
	public static Sequence startLevel3climb() { // Stage 1 of L3 Climb: Raise both winchs & drive towards platform (10)
		Sequence seq = new Sequence("start level 3 climb");
		seq.add().retractLift(); 
		// seq.add().setLiftHeight(LIFT_BOTTOM_HEIGHT); // Need to be lowest lift height to climb
		seq.add().setBothHeight(CLIMBER_L3_CLIMB_HEIGHT); // Front winch goes up to L3 height.
		// Keep Climbing until the driver releases the button.
		return seq;
	}
	
	public static Sequence stopLevelNclimb() { // Stage 1 of L2 Climb: stop the climb
		Sequence seq = new Sequence("stop level N climb");
		seq.add().stopBothHeight(); // Front winch goes up to L2 height.
		return seq;
	}

	public static Sequence startLevelDriveForward() { // Start Driving forward as part of L2/3 Stage 1 (L9)
		Sequence seq = new Sequence("start level drive forward");
		seq.add().setDrivebasePower(-DRIVEBASE_L3_DRIVE_SLOW_POWER).setClimberDriveSpeed(CLIMBER_DRIVE_POWER);
		return seq;
	}

	public static Sequence startLevelDriveBackward() { // Start Driving backward as part of L2/3 Stage 1 (L11)
		Sequence seq = new Sequence("start level drive backward");
		// seq.add().setDriveSpeed(-CLIMBER_DRIVE_POWER);
		// seq.add().setDelayDelta(0.05);
		// seq.add().setDriveBaseSpeed(-DRIVEBASE_CLIMBER_DRIVE_POWER);
		seq.add().setDrivebasePower(-DRIVEBASE_CLIMBER_DRIVE_SPEED).setClimberDriveSpeed(-CLIMBER_DRIVE_POWER);
		return seq;
	}

	public static Sequence stopLevelDrive() { // Stage 2 of L2/3 Climb: stop driving
		Sequence seq = new Sequence("stop level drive");
		seq.add().setDrivebasePower(0).setClimberDriveSpeed(0);
		return seq;
	}

	public static Sequence startFrontRaise() { // (9)
		Sequence seq = new Sequence("start front raise");
		seq.add().setFrontHeight(0); // Front winch retracts.
		return seq;
	}

	public static Sequence startRearRaise() { // (11)
		Sequence seq = new Sequence("start front raise");
		seq.add().deployIntake();
		seq.add().setRearHeight(0); // Rear winch retracts.
		return seq;
	}
	
	public static Sequence abortLevelStage() { // Abort the climb - bring both winches back to home. Stop driving.
		Sequence seq = new Sequence("abort level climb");
		seq.add().retractLift(); 
		seq.add().setLiftHeight(LIFT_DEFAULT_MIN_HEIGHT); // Need to be lowest lift height during climb
		seq.add().setClimberDriveSpeed(0); // Turn off stilt driving.
		seq.add().setDelayDelta(0.05);
		seq.add().setDrivebasePower(0); // Stop drivebase just in case.
		seq.add().setBothHeight(0); // Winches go back to ground level
		return seq;
	}

	public static Sequence colourWheelRotational() {
		Sequence seq = new Sequence("start rotational control");
		seq.add().colourWheelRotational();
		return seq;
	}
	
	public static Sequence colourWheelPositional(Colour colour) {
		Sequence seq = new Sequence("start positional control");
		seq.add().colourWheelPositional(colour);
		return seq;
	}

	public static Sequence stopColourWheel() {
		Sequence seq = new Sequence("stop colour wheel spinner");
		seq.add().stopColourWheel();
		return seq;
	}

	public static Sequence colourWheelLeft() {
		Sequence seq = new Sequence("moving colour wheel left");
		seq.add().colourWheelLeft();
		return seq;
	}

	public static Sequence colourWheelRight() {
		Sequence seq = new Sequence("moving colour wheel right");
		seq.add().colourWheelRight();
		return seq;
	}

	// For testing. Needs to be at the end of the file.
	public static Sequence[] allSequences = new Sequence[] { 
		getEmptySequence(), 
		getStartSequence(), 
		getResetSequence(),
		startIntaking(),
		stopIntaking(), 
		startCargoSpit(),
		stopCargoSpit(),
		startIntakingOnly(),
		stopIntakingOnly(),
		startPassthrough(),
		stopPassthrough(),
		//startSpitterOnly(),
		//stopSpitterOnly(),
		holdHatch(),
		releaseHatch(),
		getStowHatchSequence(),
		getReadyHatchSequence(),
		liftDeploy(),
		liftRetract(),
		startLevel2climb(),
		startLevelDriveForward(),
		stopLevelDrive(),
		startFrontRaise(),
		abortLevelStage(),
		getMicroAdjustUpSequence(), 
		getMicroAdjustDownSequence(), 
		getDriveToWaypointSequence(0, 12, 0)
	};	
}