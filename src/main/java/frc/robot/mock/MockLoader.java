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
	public LoaderInterface setPaddleBlocking(boolean extended) {
		isPaddleExtended = extended;
		return this;
	}

	@Override
	public boolean isPaddleBlocking() {
		return isPaddleExtended;
	}

	@Override
	public boolean isPaddleNotBlocking() {
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
    public double getTargetSpinnerMotorVelocity() {
        return spinnerVelocity;
       
    }
    @Override
    public double getTargetPassthroughMotorOutput() {
        return 0;
    }
// 

    @Override
    public int getCurrentBallCount() {
        // TODO Auto-generated method stub
        return 0;
    }

    @Override
    public void setInitBallCount(int initBallCount) {
        // TODO Auto-generated method stub
    }
}