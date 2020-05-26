/**
 * Sequences for doing most actions on the robot.
 * 
 * If you add a new sequence, add it to allSequences at the end of this file.
 */
package frc.robot.controller;

import static frc.robot.Constants.*;

import frc.robot.lib.LEDColour;
import frc.robot.lib.WheelColour;

import java.util.List;

import edu.wpi.first.wpilibj.geometry.Pose2d;

import frc.robot.controller.Sequence.SequenceBuilder;
import static frc.robot.lib.PoseHelper.createPose2d;

/**
 * Control sequences for most robot operations.
 */
public class Sequences {
	
	/**
	 * Do nothing sequence.
	 */
	public static Sequence getEmptySequence() {
		if (emptySeq == null) {
			emptySeq = new SequenceBuilder("empty").build();
		}
		return emptySeq;
	}
	private static Sequence emptySeq = null;

	/**
	 * The first sequence run in the autonomous period.
	 */
	public static Sequence getStartSequence() {
		if (startSeq == null) {
			startSeq = new SequenceBuilder("start").build();
		}
		//startbuilder.add().doArcadeVelocityDrive();
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
			SequenceBuilder builder = new SequenceBuilder("empty");
			builder.then().doArcadeDrive(); 
			resetSeq = builder.build();
		}
		return resetSeq;
	}
	private static Sequence resetSeq = null;

	/**
	 * Turn to face the driver station wall and then switch back to arcade.
	 */
	public static Sequence turnToWall() {
		if (driveTestSeq == null) {
			SequenceBuilder builder = new SequenceBuilder("turn to wall");
			builder.then().doTurnToHeading(180);
			// builder.add().doArcadeDrive();
			driveTestSeq = builder.build();
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
		Pose2d end = createPose2d(x,y,angle);
		SequenceBuilder builder = new SequenceBuilder(String.format("drive to %s", end));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		driveToWaypointSeq = builder.build();
		return driveToWaypointSeq;
	}	
	private static Sequence driveToWaypointSeq = null;

	public static Sequence startSlowDriveForward() {
		SequenceBuilder builder = new SequenceBuilder("Slow drive forward");
		builder.then().setDrivebasePower(DRIVE_SLOW_POWER);
		return builder.build();
	}

	public static Sequence setDrivebaseToArcade() {
		SequenceBuilder builder = new SequenceBuilder("Arcade");
		builder.then().doArcadeDrive();
		return builder.build();
	}

	/**
	 * Extends the intake and then runs the motor to intake the cargo.
	 * @return
	 */

	public static Sequence startIntaking() {
		SequenceBuilder builder = new SequenceBuilder("Start intaking");
		// Wait for the intake to extend before turning motor
		builder.then().deployIntake()
			.blockShooter();
		//builder.add().setIntakeMotorOutput(INTAKE_MOTOR_OUTPUT)
		builder.then().setIntakeRPS(INTAKE_TARGET_RPS)
			.setSpinnerRPS(LOADER_MOTOR_INTAKING_RPS)
			.setPassthroughDutyCycle(PASSTHROUGH_MOTOR_CURRENT);
		//builder.add().waitForBalls(5);
		// Reverse to eject excess > 5 balls to avoid penalty
		/*builder.add().setIntakeRPS(-INTAKE_TARGET_RPS);
		builder.add().setDelayDelta(1);
		builder.add().setIntakeRPS(0)
			.setSpinnerRPS(0)
			.setPassthroughDutyCycle(0);*/
		return builder.build();
	}

	public static Sequence reverseIntaking() {
		SequenceBuilder builder = new SequenceBuilder("Reverse intaking");
		builder.then().setIntakeRPS(-INTAKE_TARGET_RPS)
			.setPassthroughDutyCycle(-LOADER_MOTOR_INTAKING_RPS);
		return builder.build();
	}

	public static Sequence stopIntaking() {
		SequenceBuilder builder = new SequenceBuilder("Stop intaking", true);
		builder.then().setIntakeRPS(0);
		// Let passthrough run for 0.25s longer to get all balls through
		builder.then().setDelayDelta(0.25);
		builder.then().setPassthroughDutyCycle(0);
		builder.then().setSpinnerRPS(0);

		return builder.build();
	}

	public static Sequence raiseIntake() {
		SequenceBuilder builder = new SequenceBuilder("Raise intake");
		builder.then().stowIntake();
		return builder.build();
	}

	/**
	 * Start Test Loader Sequence
	 * 
	 */
	public static Sequence startLoaderTest() {
		SequenceBuilder builder = new SequenceBuilder("Start Loader Test");
		builder.then().setPassthroughDutyCycle(0.5);
		builder.then().setSpinnerRPS(0.3);
		builder.then().setDelayDelta(10);
		builder.then().setPassthroughDutyCycle(0);
		builder.then().setSpinnerRPS(0);
		builder.then().setDelayDelta(5);
		//Switch/Extend Occurs here
		builder.then().setSpinnerRPS(0.2);
		builder.then().setDelayDelta(5);
		builder.then().setSpinnerRPS(0);

		return builder.build();
	}

	// Testing methods
	public static Sequence startIntakingOnly() {
		SequenceBuilder builder = new SequenceBuilder("Start Intaking only");
		builder.then().deployIntake();
		builder.then().setIntakeRPS(INTAKE_TARGET_RPS).deployIntake();
		return builder.build();
	}

	public static Sequence stopIntakingOnly() {
		SequenceBuilder builder = new SequenceBuilder("Stop intaking only");
		builder.then().setIntakeRPS(0);
		builder.then().stowIntake();
		return builder.build();
	}

	// This is to test the Loader system
	public static Sequence startLoader() {
		SequenceBuilder builder = new SequenceBuilder("Start loader");
		builder.then().setSpinnerRPS(LOADER_MOTOR_INTAKING_RPS);
		return builder.build();
	}

	public static Sequence stopLoader() {
		SequenceBuilder builder = new SequenceBuilder("Stop loader");
		builder.then().setSpinnerRPS(0.0);
		return builder.build();
	}

	/**
	 * Spin up the shooter and position the hood to get ready for a far shot.
	 * To spin down use a button mapped to stopShooting()
	 */
	public static Sequence spinUpCloseShot(double speed) {
		SequenceBuilder builder = new SequenceBuilder("spinUpCloseShot" + speed);
		builder.then().setShooterRPS(speed)
			.extendShooterHood();
		return builder.build();
	}

	/**
	 * Spin up the shooter and position the hood to get ready for a far shot.
	 * To spin down use a button mapped to stopShooting()
	 */
	public static Sequence spinUpFarShot(double speed) {
		SequenceBuilder builder = new SequenceBuilder("spinUpFarShot" + speed);
		builder.then().setShooterRPS(speed)
			.retractShooterHood();
		return builder.build();
	}

	/**
	 * Shoot the balls using whatever hood position and shooter speed is currently set
	 * 
	 * WARNING: This sequence will never finish if the shooter speed is currently set to zero
	 * 			It sets the LEDs to purple if this happens
	 */
	public static Sequence startShooting() {
		SequenceBuilder builder = new SequenceBuilder("Start Shooting");
		// Another sequence should have set the shooter speed and hood position already

		// Wait for the shooter wheel to settle.
		builder.then().waitForShooter();
		// Let the balls out of the loader and into the shooter.
		builder.then().unblockShooter();
		// Spin passthrough
		builder.then().setPassthroughDutyCycle(PASSTHROUGH_MOTOR_CURRENT)
		// Start the loader to push the balls.
				.setSpinnerRPS(LOADER_MOTOR_SHOOTING_RPS);
		/*
		// Wait for all of the balls to leave.
		builder.add().waitForBalls(0);
		// Turn off everything.
		builder.add().setShooterRPS(0)
			.setLoaderPassthroughMotorOutput(0)
			.setLoaderSpinnerMotorRPS(0)
			.blockShooter();
		*/
		return builder.build();
	}

	public static Sequence stopShooting() {
		SequenceBuilder builder = new SequenceBuilder("Stop shooting");
		// Turn off everything.
		builder.then().setShooterRPS(0)
			.setPassthroughDutyCycle(0)
			.setSpinnerRPS(0)
			.blockShooter();
		return builder.build();
	}

	public static Sequence startDriveByVision() {
		SequenceBuilder builder = new SequenceBuilder("Start drive by vision");
		builder.then().doVisionAssistDrive();
		return builder.build();
	}

	public static Sequence stopDriveByVision() {
		SequenceBuilder builder = new SequenceBuilder("Stop drive by vision");
		builder.then().doArcadeDrive();
		return builder.build();
	}
	

	public static Sequence visionAim(){
		SequenceBuilder builder = new SequenceBuilder("Vision aim");
		builder.then().doVisionAim(); 
		builder.then().doArcadeDrive();
		return builder.build();
	}

	public static Sequence startColourWheelRotational() {
		SequenceBuilder builder = new SequenceBuilder("Start rotational control");
		builder.then().extendedColourWheel();
		builder.then().colourWheelRotational();
		builder.then().retractColourWheel();
		return builder.build();
	}
	
	public static Sequence startColourWheelPositional(WheelColour colour) {
		SequenceBuilder builder = new SequenceBuilder("Start positional control");
		builder.then().extendedColourWheel();
		builder.then().startColourWheelPositional(colour);
		builder.then().retractColourWheel();
		return builder.build();
	}

	public static Sequence stopColourWheel() {
		SequenceBuilder builder = new SequenceBuilder("Stop colour wheel spinner");
		builder.then().stopColourWheel();
		builder.then().retractColourWheel();
		return builder.build();
	}

	public static Sequence colourWheelAnticlockwise() {
		SequenceBuilder builder = new SequenceBuilder("Moving colour wheel anticlockwise");
		builder.then().extendedColourWheel();
		builder.then().colourWheelAnticlockwise();
		return builder.build();
	}

	public static Sequence colourWheelClockwise() {
		SequenceBuilder builder = new SequenceBuilder("Moving colour wheel clockwise");
		builder.then().extendedColourWheel();
		builder.then().colourWheelClockwise();
		return builder.build();
	}

	public static Sequence enableClimbMode() {
		SequenceBuilder builder = new SequenceBuilder("enable climb mode");
		builder.then().enableClimbMode();
		return builder.build();
	}

	public static Sequence enableDriveMode() {
		SequenceBuilder builder = new SequenceBuilder("enable drive mode");
		builder.then().enableDriveMode();
		return builder.build();
	}

	public static Sequence applyClimberBrake() {
		SequenceBuilder builder = new SequenceBuilder("Apply climber brake");
		builder.then().applyClimberBrake();
		return builder.build();
	}

	public static Sequence releaseClimberBrake() {
		SequenceBuilder builder = new SequenceBuilder("Release climber brake");
		builder.then().releaseClimberBrake();
		return builder.build();	
	}

	public static Sequence deployBuddyClimb() {
		SequenceBuilder builder = new SequenceBuilder("deploy buddy climb attachment");
		builder.then().deployBuddyClimb();
		return builder.build();
	}

	public static Sequence stowBuddyClimb() {
		SequenceBuilder builder = new SequenceBuilder("stow buddy climb attachment");
		builder.then().stowBuddyClimb();
		return builder.build();
	}

	public static Sequence setLEDColour(LEDColour c) {
		SequenceBuilder builder = new SequenceBuilder("set LEDS to " + c);
		builder.then().setColour(c);
		return builder.build();
	}

	// For testing. Needs to be at the end of the file.
	public static Sequence[] allSequences = new Sequence[] { 
		
		getEmptySequence(), 
		getStartSequence(), 
		getResetSequence(),
		startIntaking(),
		stopIntaking(),
		startShooting(),
		stopShooting(),
		startIntakingOnly(),
		stopIntakingOnly(),
		startLoader(),
		stopLoader(),
		deployBuddyClimb(),
		stowBuddyClimb(),
		enableClimbMode(),
		enableDriveMode(),
		visionAim(),
	};	
}  
