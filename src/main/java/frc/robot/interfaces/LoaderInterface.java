package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * This is a conveyor to move the ball from the intake through to the spitter (shooter).
 */
public interface LoaderInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public double getTargetOutMotorOutput();

    public void setTargetMotorVelocity(double current);
    public void setTargetInMotorVelocity(double InMotorCurrent);
    public void setTargetOutMotorOutput(double OutMotorCurrent);

    public LoaderInterface setLoaderExtended(boolean extended);

	/**
	 * @return the state of the intake solenoid. 
	 * */
	public boolean isLoaderExtended();
	public boolean isLoaderRetracted();

    public LoaderInterface setPaddleExtended(boolean extended);

	/**
	 * @return the state of the intake solenoid. 
	 * */
	public boolean isPaddleExtended();
	public boolean isPaddleRetracted();

}