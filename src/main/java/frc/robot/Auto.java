package frc.robot;

import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.Log;

import java.util.List;

import edu.wpi.first.wpilibj.geometry.Pose2d;
import edu.wpi.first.wpilibj.geometry.Rotation2d;
import edu.wpi.first.wpilibj.geometry.Translation2d;
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
	}
	
	private void addDriveTestSequence() {
		Sequence seq = new Sequence("Drive forward 2m then back 2m"); 
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(2, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		// Go backwards 2m
		Pose2d start = new Pose2d(2, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		autoProgram.addOption("Drive test 2m", seq); 
	}

	private void addDriveTestSplineSequence() {
		Sequence seq = new Sequence("Drive forward 2mx1m then back 2mx-1m"); 
		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(2, 1, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		// Go backwards 2m
		Pose2d start = new Pose2d(2, 1, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		autoProgram.addOption("Drive test spline 2mx1m", seq); 
	}

	private void addDriveTestUSequence() {
		Sequence seq = new Sequence("Drive u-turn 2m"); 

		Pose2d start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end1 = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		seq.add().setDelayDelta(1);
		Pose2d start = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		seq.add().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		seq.add().setDelayDelta(1);
		
		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		seq.add().setDelayDelta(1);
		
		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		seq.add().setDelayDelta(1);

		start1 = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		end1 = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		seq.add().setDelayDelta(1);
		start = new Pose2d(0, 2, new Rotation2d(Math.toRadians(180)));
		end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		seq.add().setDelayDelta(1);
		autoProgram.addOption("Drive u-turn 2m", seq); 
	}

	
	private void initAutoChooser() {
		SmartDashboard.putData("Auto program", autoProgram);
	}	
	private void initBallChooser() {
		SmartDashboard.putData("Initial Number of Balls the Robot Starts With", initBallSelector);
	}
}
