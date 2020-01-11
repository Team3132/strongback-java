package frc.robot.mock;

import org.strongback.components.Clock;
import frc.robot.Constants;
import frc.robot.interfaces.LiftInterface;
import frc.robot.interfaces.Log;

/**
 * Used to test things that depend on the Lift.
 * Also used when the robot's lift has been disabled and the robot shouldn't try talking
 * to the lift hardware.
 */
public class MockLift implements LiftInterface {
	String name = "MockLift";
	
	private double height = 0;
	private double setpoint = 0;
	private boolean isRetracted = false;

	public MockLift(Clock clock, Log log) {
	}

	public double getHeight() {
		return height;
	}

	@Override
	public LiftInterface setTargetHeight(double setpoint) {
		height = setpoint;
		return this;
	}
	
	/**
	 * sets the lift height ignoring time
	 * @param newHeight
	 * @return
	 */
	public LiftInterface setLiftHeightActual(double newHeight) {
		this.setpoint = newHeight;
		height = newHeight;
		return this;
	}

	@Override
	public double getTargetHeight() {
		return setpoint;
	}

	@Override
	public boolean isInPosition() {
		return Math.abs(height - setpoint) < Constants.LIFT_DEFAULT_TOLERANCE;
	}

	@Override
	public LiftInterface retract() {
		isRetracted = true;
		return this;
	}

	@Override
	public LiftInterface deploy() {
		isRetracted = false;
		return this;
	}
	
	@Override
	public boolean isDeployed() {
		return isRetracted;
	}
	
	@Override
	public boolean isSafeToDeploy() {
		return getHeight() > Constants.LIFT_DEPLOY_THRESHOLD_HEIGHT;
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
	}

	@Override
	public boolean isEnabled() {
		return false;
	}

	@Override
	public void cleanup() {
	}

	@Override
	public boolean shouldBeDeployed() {
		return isRetracted;
	}
}
