package frc.robot;

import frc.robot.controller.Controller;
import frc.robot.controller.Sequence;
import frc.robot.controller.Sequences;
import frc.robot.interfaces.Log;

import edu.wpi.first.wpilibj.smartdashboard.SendableChooser;
import edu.wpi.first.wpilibj.smartdashboard.SmartDashboard;

import jaci.pathfinder.Pathfinder;
import jaci.pathfinder.Waypoint;


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
		/* Robot starts with bumper touching alliance wall and right-hand side depot.
		** Faces towards opposing alliance wall. Drives to align with nearest face of right rocket.
		*/
		Sequence seq = new Sequence("Drive test"); 
		// Go forward 10"
		Waypoint[] waypoints1 = new Waypoint[] {
		new Waypoint(0, 0, Pathfinder.d2r(0)), new Waypoint(190, -60, Pathfinder.d2r(-45))};
		seq.add().driveRelativeWaypoints(waypoints1, true);
		// Go backwards 10"
		Waypoint[] waypoints2 = new Waypoint[] {
				new Waypoint(0, 0, Pathfinder.d2r(0)),  new Waypoint(-170, 0, Pathfinder.d2r(0))};
		seq.add().driveRelativeWaypoints(waypoints2, false);
		autoProgram.addOption("Drive test", seq); 
	}
	
	private void addChooser() {
		SmartDashboard.putData("Auto program", autoProgram);
	}	
}
