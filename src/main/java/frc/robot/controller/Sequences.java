/**
 * Sequences for doing most actions on the robot.
 * 
 * If you add a new sequence, add it to allSequences at the end of this file.
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
		seq.add().doArcadeDrive()
			.setShooterRPS(0) // Turn off everything that may be on.
			.retractShooterHood()
			.setLoaderSpinnerMotorRPS(0);
		return seq;
	}

	/**
	 * Extends the intake and then runs the motor to intake the cargo.
	 * @return
	 */

	public static Sequence startIntaking() {
		Sequence seq = new Sequence("Start intake");
		// Wait for the intake to extend before turning motor
		seq.add().deployIntake()
			.blockShooter();
		//seq.add().setIntakeMotorOutput(INTAKE_MOTOR_OUTPUT)
		seq.add().setIntakeRPS(INTAKE_TARGET_RPS)
			.setLoaderSpinnerMotorRPS(LOADER_MOTOR_INTAKING_RPS)
			.setLoaderPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT);
		//seq.add().waitForBalls(5);
		// Reverse to eject excess > 5 balls to avoid penalty
		/*seq.add().setIntakeRPS(-INTAKE_TARGET_RPS);
		seq.add().setDelayDelta(1);
		seq.add().setIntakeRPS(0)
			.setLoaderSpinnerMotorRPS(0)
			.setLoaderPassthroughMotorOutput(0);*/
		return seq;
	}

	public static Sequence stopIntaking() {
		Sequence seq = new Sequence("Stop intake");

		seq.add().setIntakeRPS(0);
		// Let passthrough run for 0.25s longer to get all balls through
		seq.add().setDelayDelta(0.25);
		seq.add().setLoaderPassthroughMotorOutput(0);
		seq.add().setLoaderSpinnerMotorRPS(0);
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
		seq.add().setLoaderSpinnerMotorRPS(0.3);
		seq.add().setDelayDelta(10);
		seq.add().setLoaderPassthroughMotorOutput(0);
		seq.add().setLoaderSpinnerMotorRPS(0);
		seq.add().setDelayDelta(5);
		//Switch/Extend Occurs here
		seq.add().setLoaderSpinnerMotorRPS(0.2);
		seq.add().setDelayDelta(5);
		seq.add().setLoaderSpinnerMotorRPS(0);

		return seq;
	}

	// Testing methods
	public static Sequence startIntakingOnly() {
		Sequence seq = new Sequence("start intaking");
		seq.add().deployIntake();
		seq.add().setIntakeRPS(INTAKE_TARGET_RPS).deployIntake();
		return seq;
	}

	public static Sequence stopIntakingOnly() {
		Sequence seq = new Sequence("stop intaking");
		seq.add().setIntakeRPS(0);
		seq.add().stowIntake();
		return seq;
	}

	// This is to test the Loader system
	public static Sequence startLoader() {
		Sequence seq = new Sequence("start loader");
		seq.add().setLoaderSpinnerMotorRPS(LOADER_MOTOR_INTAKING_RPS);
		return seq;
	}

	public static Sequence stopLoader() {
		Sequence seq = new Sequence("stop loader");
		seq.add().setLoaderSpinnerMotorRPS(0.0);
		return seq;
	}

	/**
	 * As the shooter takes time to spin up, enable spinning
	 * it up in advance.
	 * Use the button mapped for near/far shooting to halt.
	 */
	public static Sequence spinUpShooter(double speed) {
		Sequence seq = new Sequence("spin up shooter " + speed);
		seq.add().setShooterRPS(speed);
		return seq;
	}

	public static Sequence startShooting(boolean closeToGoal) {
		Sequence seq = new Sequence("start shooting");
		// Shooter wheel may already be up to speed.
		if (closeToGoal) {
			// Shooter wheel may already be up to speed.
			seq.add().setShooterRPS(SHOOTER_CLOSE_TARGET_SPEED_RPS)
			// Shooting from just below the goal straight up.
					.extendShooterHood();
		} else {
			// Shooter wheel may already be up to speed.
			seq.add().setShooterRPS(SHOOTER_FAR_TARGET_SPEED_RPS)
			// Shooting from far from the goal at a flat angle.
				.retractShooterHood();
		}
		// Wait for the shooter wheel to settle.
		seq.add().waitForShooter();
		// Briefly back off loader to prevent balls jamming against shooter blocker
		// seq.add().setLoaderSpinnerMotorRPS(-LOADER_MOTOR_SHOOTING_RPS);
		// Let the balls out of the loader and into the shooter.
		seq.add().unblockShooter();
		// Spin passthrough
		seq.add().setLoaderPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT)
		// Start the loader to push the balls.
				.setLoaderSpinnerMotorRPS(LOADER_MOTOR_SHOOTING_RPS);
		/*
		// Wait for all of the balls to leave.
		seq.add().waitForBalls(0);
		// Turn off everything.
		seq.add().setShooterRPS(0)
			.setLoaderPassthroughMotorOutput(0)
			.setLoaderSpinnerMotorRPS(0)
			.blockShooter();
		*/
		return seq;
	}

	public static Sequence stopShooting() {
		Sequence seq = new Sequence("stop shooting");
		// Turn off everything.
		seq.add().setShooterRPS(0)
			.setLoaderPassthroughMotorOutput(0)
			.setLoaderSpinnerMotorRPS(0)
			.blockShooter();
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
	
		return seq;
	}

	public static Sequence startColourWheelRotational() {
		Sequence seq = new Sequence("start rotational control");
		seq.add().extendedColourWheel();
		seq.add().colourWheelRotational();
		seq.add().retractColourWheel();
		return seq;
	}
	
	public static Sequence startColourWheelPositional(WheelColour colour) {
		Sequence seq = new Sequence("start positional control");
		seq.add().extendedColourWheel();
		seq.add().startColourWheelPositional(colour);
		seq.add().retractColourWheel();
		return seq;
	}

	public static Sequence stopColourWheel() {
		Sequence seq = new Sequence("stop colour wheel spinner");
		seq.add().stopColourWheel();
		seq.add().retractColourWheel();
		return seq;
	}

	public static Sequence colourWheelAnticlockwise() {
		Sequence seq = new Sequence("moving colour wheel anticlockwise");
		seq.add().extendedColourWheel();
		seq.add().colourWheelAnticlockwise();
		return seq;
	}

	public static Sequence colourWheelClockwise() {
		Sequence seq = new Sequence("moving colour wheel clockwise");
		seq.add().extendedColourWheel();
		seq.add().colourWheelClockwise();
		return seq;
	}

	// Drive / climb mode.
	public static Sequence toggleDriveClimbModes() {
		Sequence seq = new Sequence("toggle drive / climb modes");
		seq.add().toggleDriveClimbMode();
		return seq;
	}

	public static Sequence applyClimberBrake() {
		Sequence seq = new Sequence("apply climber brake");
		seq.add().applyClimberBrake();
		return seq;
	}

	public static Sequence releaseClimberBrake() {
		Sequence seq = new Sequence("release climber brake");
		seq.add().releaseClimberBrake();
		return seq;	
	}

	// Toggle buddy climb (deploy / retract)
	public static Sequence toggleBuddyClimb() {
		Sequence seq = new Sequence("toggle buddy climb attatchment");
		seq.add().toggleBuddyClimb();
		return seq;
	}

	// For testing. Needs to be at the end of the file.
	public static Sequence[] allSequences = new Sequence[] { 
		
		getEmptySequence(), 
		getStartSequence(), 
		getResetSequence(),
		startIntaking(),
		stopIntaking(),
		startShooting(true),
		stopShooting(),
		startIntakingOnly(),
		stopIntakingOnly(),
		getDriveToWaypointSequence(0, 12, 0),
		startLoader(),
		stopLoader(),
		toggleBuddyClimb(),
		toggleDriveClimbModes(),
		visionAim(),
	};	
}  