package frc.robot.simulator;

import frc.robot.Constants;
import frc.robot.interfaces.LiftInterface;
import frc.robot.lib.MovementSimulator;

/**
 * Very basic lift simulator used for unit testing.
 * Does not do gravity etc.
 */
public class LiftSimulator implements LiftInterface {
	String name = "LiftSimulator";
	
	private final double kMaxSpeed = 20;  // inches/sec
	private final double kMaxAccel = 10;   // inches/sec/sec
	private final double kMinPos = 0;
	private final double kMaxPos = 4 * 12; // Four feet tall.
	private final double kTolerance = 0.5;
	private MovementSimulator calc = new MovementSimulator("lift", kMaxSpeed, kMaxAccel, kMinPos, kMaxPos, kTolerance);
	private boolean isDeployed = false;
	private long lastTimeMs = 0;
		
	public LiftSimulator() {
	}

	@Override
	public double getHeight() {
		return calc.getPos();
	}

	@Override
	public LiftInterface setTargetHeight(double setpoint) {
		if (calc.getTargetPos() == setpoint) return this;
		System.out.printf("  Setting lift height to %.1f\n", setpoint);
		calc.setTargetPos(setpoint);
		return this;
	}
	
	/**
	 * Overrides the lift height ignoring time and simulator.
	 * @param height
	 * @return
	 */
	public LiftInterface setLiftHeightActual(double height) {
		calc.setPos(height);
		calc.setSpeed(0);  // Reset speed.
		return this;
	}

	@Override
	public double getTargetHeight() {
		return calc.getTargetPos();
	}

	@Override
	public boolean isInPosition() {
		return Math.abs(calc.getPos() - calc.getTargetPos()) < Constants.LIFT_DEFAULT_TOLERANCE;
	}

	@Override
	public LiftInterface retract() {
		isDeployed = true;
		return this;
	}

	@Override
	public LiftInterface deploy() {
		isDeployed = false;
		return this;
	}
	
	@Override
	public boolean isDeployed() {
		return isDeployed;
	}

	@Override
	public boolean shouldBeDeployed() {
		return isDeployed;
	}
	
	@Override
	public boolean isSafeToDeploy() {
		return getHeight() < 0.0;
	}
	
	@Override
	public String getName() {
		return name;
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
		calc.step((timeInMillis - lastTimeMs) / 1000.);
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
		return calc.toString();
	}
}
