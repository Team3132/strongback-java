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

import frc.robot.interfaces.ColourWheelInterface.Colour;
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

	//climber

	public static Sequence startClimberUp() {
		Sequence seq = new Sequence("climber up");
		seq.add().setClimberPower(CLIMBER_MAX_MOTOR_POWER);
		return seq;
	}

	public static Sequence startClimberDown() {
		Sequence seq = new Sequence("climber down");
		seq.add().setClimberPower(CLIMBER_MAX_MOTOR_POWER*-1);
		return seq;
	}

	public static Sequence startClimberLeftUp() {
		Sequence seq = new Sequence("left climber up");
		seq.add().setClimberPowerLeft(CLIMBER_MAX_MOTOR_POWER);
		return seq;
	}

	public static Sequence startClimberLeftDown() {
		Sequence seq = new Sequence("left climber down");
		seq.add().setClimberPowerLeft(CLIMBER_MAX_MOTOR_POWER*-1);
		return seq;
	}

	public static Sequence startClimberRightUp() {
		Sequence seq = new Sequence("right climber up");
		seq.add().setClimberPowerRight(CLIMBER_MAX_MOTOR_POWER);
		return seq;
	}

	public static Sequence startClimberRightDown() {
		Sequence seq = new Sequence("right climber down");
		seq.add().setClimberPowerRight(CLIMBER_MAX_MOTOR_POWER*-1);
		return seq;
	}

	public static Sequence pauseClimber() {
		Sequence seq = new Sequence("hold climb");
		seq.add().holdClimber();
		return seq;
	}

	public static Sequence stopClimber() {
		Sequence seq = new Sequence("stop climb");
		seq.add().stopClimber();
		return seq;
	}

	//Smart climber

	public static Sequence deployLeftClimber() {
		Sequence seq = new Sequence("deploy left climb");
		seq.add().setClimberLeft(CLIMBER_DEPLOY_HEIGHT);
		return seq;
	}

	
	public static Sequence deployRightClimber() {
		Sequence seq = new Sequence("deploy right climb");
		seq.add().setClimberRight(CLIMBER_DEPLOY_HEIGHT);
		return seq;
	}
	
	public static Sequence deployClimber() {
		Sequence seq = new Sequence("deploy climb");
		seq.add().setClimberBoth(CLIMBER_DEPLOY_HEIGHT);
		return seq;
	}

	public static Sequence climb() {
		Sequence seq = new Sequence("set climb");
		seq.add().setClimberBoth(CLIMBER_CLIMB_HEIGHT);
		return seq;
	}

	public static Sequence overrideClimberLeft() {
		Sequence seq = new Sequence("override left climber");
		seq.add().leftClimberOverride(3);
		return seq;
	}

	public static Sequence overrideClimberRight() {
		Sequence seq = new Sequence("override right climb");
		seq.add().rightClimberOverride(3);
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
		startClimberUp(),
		pauseClimber(),
		stopClimber(),
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
		getMicroAdjustUpSequence(), 
		getMicroAdjustDownSequence(), 
		getDriveToWaypointSequence(0, 12, 0)
	};	
}