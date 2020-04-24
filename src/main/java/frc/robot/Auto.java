package frc.robot;

import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequence.SequenceBuilder;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.Log;
import static frc.robot.lib.PoseHelper.*;

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
		autoProgram.addOption("Drive forward 10in", Sequences.getDriveToWaypointSequence(10 * INCHES_TO_METRES, 0, 0));
		addDriveTestSequence();
		addDriveTestSplineSequence();
		addDriveTestUSequence();
		addBasicShootIntakeDriveShootSequence();
		addTrenchAutoSequence();
	}

	private void addDriveTestSequence() {
		SequenceBuilder builder = new SequenceBuilder("Drive backwards 2m then forwards 2m", false);
		// Go backwards 2m
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(-2, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		// Go forwards 2m
		Pose2d start = new Pose2d(-2, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		autoProgram.addOption("Drive test 2m", builder.build()); 
	}

	private void addDriveTestSplineSequence() {
		SequenceBuilder builder = new SequenceBuilder("Drive backwards 2mx1m then forward 2mx-1m", false);
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(-2, -1, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		// Go backwards 2m
		Pose2d start = new Pose2d(-2, -1, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		autoProgram.addOption("Drive test spline 2mx1m", builder.build()); 
	}

	private void addDriveTestUSequence() {
		SequenceBuilder builder = new SequenceBuilder("Drive u-turn 2m", false);
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);
		builder.then().setDelayDelta(1);
		Pose2d start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);  // backwards.
		builder.then().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		builder.then().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		builder.then().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		builder.then().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		builder.then().setDelayDelta(1);
		
		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		builder.then().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		builder.then().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		builder.then().driveRelativeWaypoints(start1, List.of(), end1, false);  // backwards.
		builder.then().setDelayDelta(1);
		start = new Pose2d(0, -2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		builder.then().driveRelativeWaypoints(start, List.of(), end, true);
		builder.then().setDelayDelta(1);
		autoProgram.addOption("Drive u-turn 2m", builder.build()); 
	}


	private void addBasicShootIntakeDriveShootSequence() {
		SequenceBuilder builder = new SequenceBuilder("Basic shoot intake drive shoot", false);

		builder.then().setCurrentPostion(AUTO_LINE_GOAL);

		// Start shooting
		builder.appendSequence(Sequences.spinUpFarShot(SHOOTER_AUTO_LINE_TARGET_SPEED_RPS));
		builder.appendSequence(Sequences.startShooting());
		builder.then().setDelayDelta(2);		

		// Start intaking
		builder.appendSequence(Sequences.startIntaking());
		
		// Drive backwards to pick up the three balls.
		// Drive to third ball via the first ball
		builder.then().driveRelativeWaypoints(AUTO_LINE_GOAL, List.of(ALLIANCE_TRENCH_FIRST_BALL.getTranslation()), intakeAt(ALLIANCE_TRENCH_THIRD_BALL, 0), false);  // backwards.

		// Stop intaking
		builder.appendSequence(Sequences.stopIntaking());

		// Go forwards 2m to shoot.
		Pose2d end = approachPose(ALLIANCE_TRENCH_THIRD_BALL, 2, 0);

		builder.then().driveRelativeWaypoints(intakeAt(ALLIANCE_TRENCH_THIRD_BALL, 0), List.of(), end, true);

		builder.then().doVisionAim();
		// Shoot the balls.
		builder.appendSequence(Sequences.spinUpFarShot(SHOOTER_FAR_TARGET_SPEED_RPS));
		builder.appendSequence(Sequences.startShooting());
		builder.then().setDelayDelta(2);	

		builder.appendSequence(Sequences.stopShooting());

		autoProgram.addOption("Basic shoot intake drive shoot", builder.build());
	}

	private void addTrenchAutoSequence() {
		
		SequenceBuilder builder = new SequenceBuilder("Basic trench routine", false);

		builder.then().setCurrentPostion(AUTO_LINE_ALLIANCE_TRENCH);

		builder.appendSequence(Sequences.spinUpFarShot(SHOOTER_AUTO_LINE_TARGET_SPEED_RPS));
		builder.appendSequence(Sequences.startShooting());		
		builder.then().deployIntake();

		// Let shooter spin up a little before running every other motor
		// builder.add().setDelayDelta(0.5);		

		// Start intaking
		builder.appendSequence(Sequences.startIntaking());

		// Drive backwards to pick up the two balls, starting with front bumpers on auto line

		builder.then().driveRelativeWaypoints(AUTO_LINE_ALLIANCE_TRENCH, List.of(), intakeAt(ALLIANCE_TRENCH_SECOND_BALL, 0), false); // backwards

		// Stop intaking
		builder.then().setIntakeRPS(0)
			.setPassthroughDutyCycle(0)
			.setSpinnerRPS(0);

		builder.then().doVisionAim();

		// Start shooting
		builder.appendSequence(Sequences.spinUpFarShot(SHOOTER_AUTO_LINE_TARGET_SPEED_RPS));
		builder.appendSequence(Sequences.startShooting());
		builder.then().setDelayDelta(2);		

		// Pick up the last 3 balls 
		builder.appendSequence(Sequences.startIntaking());
	
		builder.then().driveRelativeWaypoints(intakeAt(ALLIANCE_TRENCH_SECOND_BALL, 0), List.of(), intakeAt(ALLIANCE_TRENCH_FIFTH_BALL, 0), false);

		// Drive forward and shoot
		builder.then().driveRelativeWaypoints(intakeAt(ALLIANCE_TRENCH_FIFTH_BALL, 0), List.of(), intakeAt(ALLIANCE_TRENCH_SECOND_BALL, 0), true);

		// Stop intaking
		builder.then().setIntakeRPS(0)
			.setPassthroughDutyCycle(0)
			.setSpinnerRPS(0);

		builder.then().doVisionAim();

		// Shoot the balls.
		builder.appendSequence(Sequences.spinUpFarShot(SHOOTER_AUTO_LINE_TARGET_SPEED_RPS));
		builder.appendSequence(Sequences.startShooting());
		builder.then().setDelayDelta(2);	

		builder.appendSequence(Sequences.stopShooting());

		autoProgram.addOption("Basic trench auto sequence", builder.build()); 
	}

	private void initAutoChooser() {
		SmartDashboard.putData("Auto program", autoProgram);
	}	
	private void initBallChooser() {
		SmartDashboard.putData("Initial balls", initBallSelector);
	}
}
