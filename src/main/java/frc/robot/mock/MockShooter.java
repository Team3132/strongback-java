package frc.robot.mock;

import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.Log;

public class MockShooter implements ShooterInterface {

    private double targetSpeed = 0;
	private boolean isExtended = false;

    public MockShooter(Log log) {
    }

    @Override
    public ShooterInterface setTargetSpeed(double speed) {
        targetSpeed = speed;
        return this;
    }

    @Override
    public boolean isTargetSpeed() {
        return true;
    }

    @Override
    public double getTargetSpeed() {
        return targetSpeed;
    }

	@Override
	public ShooterInterface setExtended(boolean extended) {
		isExtended = extended;
		return this;
	}

	@Override
	public boolean isExtended() {
		return isExtended;
	}

	@Override
	public boolean isRetracted() {
		return !isExtended;
	}

    @Override
    public String getName() {
        return "MockShooter";
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
        return true;
    }

    @Override
    public void cleanup() {
    }
}