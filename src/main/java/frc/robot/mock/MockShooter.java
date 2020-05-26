package frc.robot.mock;

import frc.robot.interfaces.ShooterInterface;

public class MockShooter implements ShooterInterface {

    private double targetRPS = 0;
	private boolean isExtended = false;

    public MockShooter() {
    }

    @Override
    public ShooterInterface setTargetRPS(double rps) {
        targetRPS = rps;
        return this;
    }

    @Override
    public boolean isAtTargetSpeed() {
        return true;
    }

    @Override
    public double getTargetRPS() {
        return targetRPS;
    }

	@Override
	public ShooterInterface setHoodExtended(boolean extended) {
		isExtended = extended;
		return this;
	}

	@Override
	public boolean isHoodExtended() {
		return isExtended;
	}

	@Override
	public boolean isHoodRetracted() {
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