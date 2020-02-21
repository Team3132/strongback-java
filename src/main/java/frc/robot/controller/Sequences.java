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
		seq.add().deployIntake();
		seq.add().setIntakeMotorOutput(INTAKE_MOTOR_CURRENT);;
		seq.add().setPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT);
		return seq;
	}

	public static Sequence stopIntaking() {
		Sequence seq = new Sequence("Stop intake");
		seq.add().setIntakeMotorOutput(0)
				 .setPassthroughMotorOutput(0);
		seq.add().setDelayDelta(0.1);
		return seq;
	}

	public static Sequence raiseIntake() {
		Sequence seq = new Sequence("Raise intake");
		seq.add().stowIntake();
		return seq;
	}

	

	public static Sequence startReverseCycle() {
		Sequence seq = new Sequence("Start reverse cycle");
		seq.add().setPassthroughMotorOutput(-PASSTHROUGH_MOTOR_CURRENT);
		seq.add().setIntakeMotorOutput(-INTAKE_MOTOR_CURRENT);
		return seq;
	}

	public static Sequence stopReverseCycle() {
		Sequence seq = new Sequence("Stop reverse cycle");
		seq.add().setPassthroughMotorOutput(0);
		seq.add().setIntakeMotorOutput(0);
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
		startIntakingOnly(),
		stopIntakingOnly(),
		startPassthrough(),
		stopPassthrough(), 
		getDriveToWaypointSequence(0, 12, 0)
	};	
}