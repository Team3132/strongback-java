package frc.robot.drive.routines;

import org.strongback.components.Motor.ControlMode;
import org.strongback.components.ui.ContinuousRange;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.DrivebaseInterface.DriveMotion;

public class ArcadeDrive extends DriveRoutine {
	private double scale = 1;
	private ContinuousRange move;
	private ContinuousRange turn;
	private boolean squaredInputs;

	public ArcadeDrive(String name, ControlMode mode, double scale, ContinuousRange move, ContinuousRange turn,
			Log log) {
		this(name, mode, scale, move, turn, true, log);
	}

	public ArcadeDrive(String name, ControlMode mode, double scale, ContinuousRange move, ContinuousRange turn, boolean squaredInputs,
			Log log) {
		super(name, mode, log);
		this.scale = scale;
		this.move = move;
		this.turn = turn;
		this.squaredInputs = squaredInputs;
		log.register(false, () -> move.read(), "UI/%s/Move", name).register(false, () -> turn.read(), "UI/%s/Turn",
				name);
	}

	@Override
	public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
		double m = move.read();
		double t = turn.read();
		// log.sub("%s: Move: %f, Turn: %f\n", name, m, t);
		return arcadeToTank(m, t, scale, squaredInputs);
	}
}
