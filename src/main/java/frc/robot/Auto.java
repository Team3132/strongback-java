package frc.robot;

import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.Log;
import static frc.robot.Constants.*;

import java.util.List;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

/**
 * Handles auto routine selection.
 * 
 * Auto routines should be defined in Sequences.java
 */
public class Auto {
	private final Log log;
	private SendableChooser<Sequence> autoProgram = new SendableChooser<Sequence>();
	private SendableChooser<Integer> initBallSelector = new SendableChooser<Integer>();
	
	
	public Auto(Log log) {
		this.log = log;
		addAutoOptions();
		initAutoChooser();
		addBallOptions();
		initBallChooser();
	}

	private void addBallOptions() {
		initBallSelector.addOption("0", Integer.valueOf(0));
		initBallSelector.addOption("1", Integer.valueOf(1));
		initBallSelector.addOption("2", Integer.valueOf(2));
		initBallSelector.setDefaultOption("3", Integer.valueOf(3));
		initBallSelector.addOption("4", Integer.valueOf(4));
		initBallSelector.addOption("5", Integer.valueOf(5));
	}

	public void executedSelectedSequence(Controller controller) {
		Sequence seq = autoProgram.getSelected();
		log.info("Starting selected auto program %s", seq.getName());
		controller.doSequence(seq);
	}
	public int getSelectedBallAmount() {
		Integer numBalls = initBallSelector.getSelected();
		log.info("Starting with %s balls", numBalls);
		return numBalls;
	}

	private void addAutoOptions() {
		autoProgram.setDefaultOption("Nothing", Sequences.getEmptySequence());
		autoProgram.addOption("Drive forward 10in", Sequences.getDriveToWaypointSequence(10 * Constants.INCHES_TO_METRES, 0, 0));
		addDriveTestSequence();
		addDriveTestSplineSequence();
		addDriveTestUSequence();
		addBasicShootIntakeDriveShootSequence();
	}
	
	private void addDriveTestSequence() {
		Sequence seq = new Sequence("Drive backwards 2m then forwards 2m"); 
		// Go backwards 2m
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(-2, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		// Go forwards 2m
		Pose2d start = new Pose2d(-2, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);
		autoProgram.addOption("Drive test 2m", seq); 
	}

	private void addDriveTestSplineSequence() {
		Sequence seq = new Sequence("Drive backwards 2mx1m then forward 2mx-1m"); 
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(-2, -1, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		// Go backwards 2m
		Pose2d start = new Pose2d(-2, -1, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);
		autoProgram.addOption("Drive test spline 2mx1m", seq); 
	}

	private void addDriveTestUSequence() {
		Sequence seq = new Sequence("Drive u-turn 2m"); 

		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);
		seq.add().setDelayDelta(1);
		Pose2d start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);  // backwards.
		seq.add().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);
		seq.add().setDelayDelta(1);
		
		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);
		seq.add().setDelayDelta(1);
		
		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);
		seq.add().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, true);
		seq.add().setDelayDelta(1);
		autoProgram.addOption("Drive u-turn 2m", seq); 
	}

	private void addShootStates(Sequence seq) {
		// Shooter wheel may already be up to speed.
		seq.add().setShooterRPS(SHOOTER_FAR_TARGET_SPEED_RPS);
		// Shooting from far from the goal at a flat angle.
		seq.add().retractShooterHood();
		// Wait for the shooter wheel to settle.
		seq.add().waitForShooter();
		// Let the balls out of the loader and into the shooter.
		seq.add().unblockShooter();
		// Start the loader to push the balls.
		seq.add().setLoaderSpinnerMotorRPS(LOADER_MOTOR_SHOOTING_RPS);
		// Wait for all of the balls to leave.
		seq.add().waitForBalls(0);
		// Turn off everything.
		seq.add().setShooterRPS(0)
			.setLoaderPassthroughMotorOutput(0)
			.setLoaderSpinnerMotorRPS(0)
			.blockShooter();		
	}

	private void addBasicShootIntakeDriveShootSequence() {
		Sequence seq = new Sequence("Basic shoot intake drive shoot");

		addShootStates(seq);

		// Start intaking
		seq.add().deployIntake()
			.blockShooter();
		seq.add().setIntakeRPS(INTAKE_TARGET_RPS)
			.setLoaderSpinnerMotorRPS(LOADER_MOTOR_INTAKING_RPS)
			.setLoaderPassthroughMotorOutput(PASSTHROUGH_MOTOR_CURRENT);

		// Drive backwards to pick up the three balls.
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d thirdBall = new Pose2d(-4, -1, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start1, List.of(), thirdBall, false);  // backwards.

		// Stop intaking
		seq.add().setIntakeRPS(0)
			.setLoaderSpinnerMotorRPS(0)
			.setLoaderPassthroughMotorOutput(0);

		// Go forwards 2m to shoot.
		Pose2d end = new Pose2d(-2, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(thirdBall, List.of(), end, true);

		// Shoot the balls.
		addShootStates(seq);

		autoProgram.addOption("Basic shoot intake drive shoot", seq); 
	}

	
	private void initAutoChooser() {
		SmartDashboard.putData("Auto program", autoProgram);
	}	
	private void initBallChooser() {
		SmartDashboard.putData("Initial balls", initBallSelector);
	}
}
