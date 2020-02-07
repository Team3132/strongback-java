package frc.robot.mock;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.Log;

public class MockLoader implements LoaderInterface {
    private double output = 0;

    public MockLoader(Log log) {
    }
    private boolean isExtended = false;

    @Override
	public LoaderInterface setExtended(boolean extended) {
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
    public double getTargetMotorOutput() {
        return output;
    }

    @Override
    public void setTargetMotorOutput(double percentPower) {

    }

    @Override
    public String getName() {
        return null;
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

}