package frc.robot.drive.routines;

import org.strongback.components.Motor.ControlMode;
import org.strongback.components.ui.ContinuousRange;
import frc.robot.interfaces.Drivebase.DriveMotion;
import frc.robot.lib.chart.Chart;

public class ArcadeDrive extends DriveRoutine {
	private double scale = 1;
	private ContinuousRange move;
	private ContinuousRange turn;
	private boolean squaredInputs;

	public ArcadeDrive(String name, ControlMode mode, double scale, ContinuousRange move, ContinuousRange turn) {
		this(name, mode, scale, move, turn, true);
	}

	public ArcadeDrive(String name, ControlMode mode, double scale, ContinuousRange move, ContinuousRange turn, boolean squaredInputs) {
		super(name, mode);
		this.scale = scale;
		this.move = move;
		this.turn = turn;
		this.squaredInputs = squaredInputs;
		Chart.register(() -> move.read(), "UI/%s/Move", name);
		Chart.register(() -> turn.read(), "UI/%s/Turn", name);
	}

	@Override
	public DriveMotion getMotion(double leftSpeed, double rightSpeed) {
		double m = move.read();
		double t = turn.read();
		// Logger.debug("%s: Move: %f, Turn: %f\n", name, m, t);
		return arcadeToTank(m, t, scale, squaredInputs);
	}
}
