package frc.robot.mock;

import frc.robot.interfaces.Loader;

public class MockLoader implements Loader {
    private double spinnerRPS = 0;
    private double passthroughPower = 0;

    public MockLoader() {
    }
    // Paddle
    private boolean isPaddleBlocking = false;

    @Override
	public Loader setPaddleBlocking(boolean blocking) {
		isPaddleBlocking = blocking;
		return this;
	}

	@Override
	public boolean isPaddleNotBlocking() {
		return !isPaddleBlocking;
	}

	@Override
	public boolean isPaddleBlocking() {
		return isPaddleBlocking;
    }

    @Override
    public void setTargetSpinnerRPS(double rps) {
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
    public void setTargetPassthroughDutyCycle(double percent) {
        passthroughPower = percent;
    }


    @Override
    public double getTargetSpinnerRPS() {
        return spinnerRPS;
       
    }
    @Override
    public double getTargetPassthroughDutyCycle() {
        return passthroughPower;
    } 

    @Override
    public int getCurrentBallCount() {
        return 0;
    }

    @Override
    public void setInitBallCount(int initBallCount) {
    }
}