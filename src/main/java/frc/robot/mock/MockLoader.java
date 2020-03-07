package frc.robot.mock;

import frc.robot.interfaces.LoaderInterface;
import frc.robot.interfaces.Log;

public class MockLoader implements LoaderInterface {
    private double spinnerRPM = 0;

    public MockLoader(Log log) {
    }
    // Paddle
    private boolean isPaddleBlocking = false;

    @Override
	public LoaderInterface setPaddleBlocking(boolean blocking) {
		isPaddleBlocking = blocking;
		return this;
	}

	@Override
	public boolean isPaddleNotBlocking() {
		return !isPaddleBlocking;
	}

	@Override
	public boolean isPaddleBlocking() {
		return !isPaddleBlocking;
    }

    @Override
    public void setTargetSpinnerMotorRPM(double rpm) {
        spinnerRPM = rpm;
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
    public double getTargetSpinnerMotorRPM() {
        return spinnerRPM;
       
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