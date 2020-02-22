package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * This is a system to store balls from the intake and then pass them to the shooter.
 */
public interface LoaderInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public double getTargetSpinnerMotorVelocity();
	public double getTargetPassthroughMotorOutput();
	
	public double getCurrentCount();
	public void setInitBallCount(double initBallCount); 

    public void setTargetSpinnerMotorVelocity(double current);
	public void setTargetPassthroughMotorOutput(double percentoutput);

	/**
	 * @return the state of the loader solenoid. 
	 * */

    public LoaderInterface setPaddleExtended(boolean extended);

	/**
	 * @return the state of the loader paddle solenoid. 
	 * */
	public boolean isPaddleExtended();
	public boolean isPaddleRetracted();

}