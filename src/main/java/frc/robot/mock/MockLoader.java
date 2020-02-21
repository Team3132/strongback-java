package frc.robot.mock;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.Log;

public class MockLoader implements LoaderInterface {
    private double spinnerVelocity = 0;
    private double passthroughVelocity = 0;
    private double feederVelocity = 0;

    public MockLoader(Log log) {
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
    public void setTargetSpinnerMotorVelocity(double velocity) {
        spinnerVelocity = velocity;
    }

    @Override
    public String getName() {
        return "MockLoader";
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
    public void setTargetPassthroughMotorVelocity(double velocity) {
        passthroughVelocity = velocity;
    }


    @Override
    public double getTargetSpinnerMotorVelocity() {
        return spinnerVelocity;
       
    }

    @Override
    public void setTargetFeederMotorOutput(double FeederMotorCurrent) {
        // TODO Auto-generated method stub
        feederVelocity = FeederMotorCurrent;

    }
    @Override
    public double getTargetFeederMotorOutput() {
        return feederVelocity;

    }
    @Override
    public double getTargetPassthroughMotorVelocity() {
        return passthroughVelocity;
    }
// 
}