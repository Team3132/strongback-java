package frc.robot.interfaces;

import org.strongback.Executable;

public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public ShooterInterface setTargetSpeed(double speed);
    public double getTargetSpeed();
    public boolean isTargetSpeed();

	public ShooterInterface setExtended(boolean extended);

	/**
	 * @return the state of the shooter solenoid. 
	 * */
	public boolean isExtended();
	public boolean isRetracted();
}