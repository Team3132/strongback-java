package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * This is a system to store balls from the intake and then pass them to the shooter.
 */
public interface LoaderInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public double getTargetSpinnerMotorRPS();
	public double getTargetPassthroughMotorOutput();
	
	public int getCurrentBallCount();
	public void setInitBallCount(int initBallCount); 

    public void setTargetSpinnerMotorRPS(double current);
	public void setTargetPassthroughMotorOutput(double percentoutput);

	/**
	 * @return the state of the loader solenoid. 
	 * */

    public LoaderInterface setPaddleBlocking(boolean blocking);

	/**
	 * @return the state of the loader paddle solenoid. 
	 * */
	public boolean isPaddleNotBlocking();
	public boolean isPaddleBlocking();

}