package frc.robot.simulator;

import org.strongback.Executable;
import frc.robot.interfaces.IntakeInterface;
import frc.robot.lib.MovementSimulator;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class IntakeSimulator implements IntakeInterface, Executable {
	private final double kMaxSpeed = 180;  // degrees/sec
	private final double kMaxAccel = 200;   // degrees/sec/sec
	private final double kMinAngle = 0;
	private final double kMaxAngle = 45;
	private final double kMovementTolerance = 1;  // How close before it's classed as being in position.
	private MovementSimulator arm = new MovementSimulator("arm intake", kMaxSpeed, kMaxAccel, kMinAngle, kMaxAngle, kMovementTolerance);
	private long lastTimeMs = 0;

	private double current;
	
	public IntakeSimulator() {
	}
	
	@Override
	public IntakeInterface setExtended(boolean extend) {
		arm.setTargetPos(kMaxAngle);
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
	public void setMotorOutput(double current) {
		this.current = current;
	}

	@Override
	public double getMotorOutput() {
		return current;
	}

	@Override
	public boolean hasCargo() {
		// TODO: it is unknown that cargo sensor is needed
		return false;
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
