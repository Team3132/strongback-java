package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * This is a system to store balls from the intake and then pass them to the shooter.
 */
public interface LoaderInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public double getTargetSpinnerMotorVelocity();
	public double getTargetPassthroughMotorOutput();
	
	public int getCurrentBallCount();
	public void setInitBallCount(int initBallCount); 

    public void setTargetSpinnerMotorVelocity(double current);
	public void setTargetPassthroughMotorOutput(double percentoutput);

	/**
	 * @return the state of the loader solenoid. 
	 * */

    public LoaderInterface setPaddleBlocking(boolean blocking);

	/**
	 * @return the state of the loader paddle solenoid. 
	 * */
	public boolean isPaddleBlocking();
	public boolean isPaddleNotBlocking();

}