package frc.robot;

import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.Log;

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
	
	public Auto(Log log) {
		this.log = log;
		addAutoSequences();
		addChooser();
	}

	public void executedSelectedSequence(Controller controller) {
		Sequence seq = autoProgram.getSelected();
		log.info("Starting selected auto program %s", seq.getName());
		controller.doSequence(seq);
	}

	private void addAutoSequences() {
		autoProgram.setDefaultOption("Nothing", Sequences.getEmptySequence());
		autoProgram.addOption("Drive forward 10in", Sequences.getDriveToWaypointSequence(10, 0, 0));
		addDriveTestSequence();
	}
	
	private void addDriveTestSequence() {
		Sequence seq = new Sequence("Drive forward 2m then back 2m"); 
		Pose2d start1 = new Pose2d();
		Pose2d end1 = new Pose2d(2, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start1, List.of(), end1, true);
		// Go backwards 2m
		Pose2d start = new Pose2d(2, 0, new Rotation2d(Math.toRadians(0)));
		Pose2d end = new Pose2d(0, 0, new Rotation2d(Math.toRadians(0)));
		seq.add().driveRelativeWaypoints(start, List.of(), end, false);  // backwards.
		autoProgram.addOption("Drive test 2m", seq); 
	}
	
	private void addChooser() {
		SmartDashboard.putData("Auto program", autoProgram);
	}	
}
