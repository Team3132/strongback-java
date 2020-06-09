package frc.robot.simulator;

import frc.robot.interfaces.Intake;
import frc.robot.lib.MovementSimulator;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class IntakeSimulator implements Intake {
	private final double kMaxSpeed = 180;  // degrees/sec
	private final double kMaxAccel = 200;   // degrees/sec/sec
	private final double kMinAngle = 0;
	private final double kMaxAngle = 45;
	private final double kMovementTolerance = 1;  // How close before it's classed as being in position.
	private MovementSimulator arm = new MovementSimulator("arm intake", kMaxSpeed, kMaxAccel, kMinAngle, kMaxAngle, kMovementTolerance);
	private long lastTimeMs = 0;

	private double rps;
	
	public IntakeSimulator() {
	}
	
	@Override
	public Intake setExtended(boolean extend) {
		arm.setTargetPos(extend?kMaxAngle:kMinAngle);
		return this;
	}

	@Override
	public boolean isExtended() {
		return arm.getTargetPos() == kMaxAngle && arm.isInPosition();
	}

	@Override
	public boolean isRetracted() {
		return arm.getTargetPos() == kMinAngle && arm.isInPosition();
	}


	@Override
	public Intake setTargetRPS(double rps) {
		this.rps = rps;
		return this;
	}

	@Override
	public double getTargetRPS() {
		return rps;
	}

	@Override
	public String getName() {
		return "IntakeSimulator";
	}

	@Override
	public void enable() {
	}

	@Override
	public void disable() {
	}

	@Override
	public void execute(long timeInMillis) {
		if (lastTimeMs == 0) {
			lastTimeMs = timeInMillis;
			return;
		}
		// Update the lift position.
		arm.step((timeInMillis - lastTimeMs) / 1000.);
		lastTimeMs = timeInMillis;
	}

	@Override
	public boolean isEnabled() {
		return true;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public String toString() {
		return arm.toString();
	}	
}
