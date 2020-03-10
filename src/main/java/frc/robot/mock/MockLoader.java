package frc.robot.mock;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.Log;

public class MockLoader implements LoaderInterface {
    private double spinnerRPS = 0;

    public MockLoader(Log log) {
    }
    // Paddle
    private boolean isPaddleNotBlocking = false;

    @Override
	public LoaderInterface setPaddleNotBlocking(boolean notBlocking) {
		isPaddleNotBlocking = notBlocking;
		return this;
	}

	@Override
	public boolean isPaddleNotBlocking() {
		return isPaddleNotBlocking;
	}

	@Override
	public boolean isPaddleBlocking() {
		return !isPaddleNotBlocking;
    }

    @Override
    public void setTargetSpinnerMotorRPS(double rps) {
        spinnerRPS = rps;
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
    public double getTargetSpinnerMotorRPS() {
        return spinnerRPS;
       
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