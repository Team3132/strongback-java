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

import frc.robot.lib.WheelColour;

import java.util.List;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;

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
		//startSeq.add().doArcadeVelocityDrive();
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
	 * @param angle the final angle (relative to the field) in degrees.
	 */
	public static Sequence getDriveToWaypointSequence(double x, double y, double angle) {
		Pose2d start = new Pose2d();
		Pose2d end = new Pose2d(x, y, new Rotation2d(Math.toRadians(angle)));
		driveToWaypointSeq = new Sequence(String.format("drive to %s", end));
		driveToWaypointSeq.add().driveRelativeWaypoints(start, List.of(), end, true);
		return driveToWaypointSeq;
	}	
	private static Sequence driveToWaypointSeq = null;

	public static Sequence startSlowDriveForward() {
		Sequence seq = new Sequence("Slow drive forward");
		seq.add().setDrivebasePower(DRIVE_OFF_LEVEL_TWO_POWER);
		return seq;
	}

	public static Sequence setDrivebaseToArcade() {
		Sequence seq = new Sequence("Arcade");
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
		seq.add().setIntakeMotorOutput(INTAKE_MOTOR_CURRENT)
			.setLoaderSpinnerMotorRPM(LOADER_MOTOR_RPM)
			.setLoaderPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT)
			.setPaddleNotBlocking(false);
		seq.add().waitForBalls(5);
		// Reverse to eject excess > 5 balls to avoid penalty
		seq.add().setIntakeMotorOutput(-INTAKE_MOTOR_CURRENT) 
				.setLoaderSpinnerMotorRPM(0);
		seq.add().setDelayDelta(0.5);
		seq.add().setIntakeMotorOutput(0)
			.setLoaderPassthroughMotorOutput(0);
		return seq;
	}

	public static Sequence stopIntaking() {
		Sequence seq = new Sequence("Stop intake");

		// Reverse to eject excess > 5 balls to avoid penalty
		seq.add().setIntakeMotorOutput(-INTAKE_MOTOR_CURRENT);
		seq.add().setDelayDelta(0.25);
		seq.add().setIntakeMotorOutput(0);
		seq.add().setDelayDelta(0.25);
		seq.add().setLoaderPassthroughMotorOutput(0);
		return seq;
	}

	public static Sequence raiseIntake() {
		Sequence seq = new Sequence("Raise intake");
		seq.add().stowIntake();
		return seq;
	}

	/**
	 * Start Test Loader Sequence
	 * 
	 */
	public static Sequence startLoaderTest() {
		Sequence seq = new Sequence("Start Loader Test Sequence");
		seq.add().setLoaderPassthroughMotorOutput(0.5);
		seq.add().setLoaderSpinnerMotorRPM(0.3);
		seq.add().setDelayDelta(10);
		seq.add().setLoaderPassthroughMotorOutput(0);
		seq.add().setLoaderSpinnerMotorRPM(0);
		seq.add().setDelayDelta(5);
		//Switch/Extend Occurs here
		seq.add().setLoaderSpinnerMotorRPM(0.2);
		seq.add().setDelayDelta(5);
		seq.add().setLoaderSpinnerMotorRPM(0);

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
	// This is to test the Loader system
	public static Sequence startLoader() {
		Sequence seq = new Sequence("start Loader");
		seq.add().setLoaderSpinnerMotorRPM(LOADER_MOTOR_RPM);
		return seq;
	}

	public static Sequence stopLoader() {
		Sequence seq = new Sequence("stop Loader");
		seq.add().setLoaderSpinnerMotorRPM(0.0);
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
	

	public static Sequence visionAim(){
		Sequence seq = new Sequence("vision aim");
		seq.add().doVisionAim(); 
		seq.add().doArcadeDrive();
		return seq;
	}

	public static Sequence colourWheelRotational() {
		Sequence seq = new Sequence("start rotational control");
		seq.add().colourWheelRotational();
		return seq;
	}
	
	public static Sequence colourWheelPositional(WheelColour colour) {
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
		getDriveToWaypointSequence(0, 12, 0),
		startLoader(),
		stopLoader(),
		visionAim(),
	};	
}