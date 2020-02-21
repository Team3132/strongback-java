package frc.robot.drive.routines;

import org.strongback.components.ui.ContinuousRange;
import frc.robot.interfaces.Log;

public class ArcadeDrive implements DriveRoutine {
	private String name = "ArcadeDrive";
	private double scale = 1;
	private Log log;
	
	private ContinuousRange move;
	private boolean squaredInputs;
	private ContinuousRange turn;
	
	public ArcadeDrive(String name, double scale, ContinuousRange move, ContinuousRange turn, Log log) {
		this(name, scale, move, turn, true, log);
	}

	public ArcadeDrive(String name, double scale, ContinuousRange move, ContinuousRange turn, boolean squaredInputs, Log log) {
		this.name = name;
		this.scale = scale;
		this.move = move;
		this.turn = turn;
		this.squaredInputs = squaredInputs;
		this.log = log;
		log.register(false, () -> move.read(), "UI/%s/Move", name)
		   .register(false, () -> turn.read(), "UI/%s/Turn", name);
	}
	
	@Override
	public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
		double m = move.read();
		double t = turn.read();
		//log.sub("%s: Move: %f, Turn: %f\n", name, m, t);
		return arcadeToTank(m, t, scale, squaredInputs);
	}
	
	@Override
	public String getName() {
		return name;
	}

	@Override
	public boolean hasFinished() {
		return true;  // Always ready for the next state.
	}
}
