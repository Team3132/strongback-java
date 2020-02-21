package frc.robot.mock;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.Log;

public class MockLoader implements LoaderInterface {
    private double spinnerVelocity = 0;

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
    public void setTargetPassthroughMotorOutput(double percent) {
    }


    @Override
    public double getTargetSpinnerMotorOutput() {
        return spinnerVelocity;
       
    }
    @Override
    public double getTargetPassthroughMotorOutput() {
        return 0;
    }
// 
}