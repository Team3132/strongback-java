package frc.robot.mock;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.Log;

public class MockLoader implements LoaderInterface {
    private double output = 0;

    public MockLoader(Log log) {
    }
    // Loader
    private boolean isLoaderExtended = false;

    @Override
	public LoaderInterface setLoaderExtended(boolean extended) {
		isLoaderExtended = extended;
		return this;
	}

	@Override
	public boolean isLoaderExtended() {
		return isLoaderExtended;
	}

	@Override
	public boolean isLoaderRetracted() {
		return !isLoaderExtended;
    }
    // Paddle
    private boolean isPaddleExtended = false;

    @Override
	public LoaderInterface setPaddleExtended(boolean extended) {
		isPaddleExtended = extended;
		return this;
	}

	@Override
	public boolean isPaddleExtended() {
		return isPaddleExtended;
	}

	@Override
	public boolean isPaddleRetracted() {
		return !isPaddleExtended;
    }

    @Override
    public void setTargetMotorVelocity(double percentPower) {

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


    @Override
    public void setTargetInMotorVelocity(double InMotorCurrent) {
        // TODO Auto-generated method stub

    }

    @Override
    public void setTargetOutMotorOutput(double OutMotorCurrent) {
        // TODO Auto-generated method stub

    }

    @Override
    public double getTargetOutMotorOutput() {
        // TODO Auto-generated method stub
        return 0;
    }

}