package frc.robot.interfaces;

/**
 * Cargo intake. Spinning omniwheels on a extendable pneumatically driven arm.
 */
import org.strongback.Executable;

public interface Intake extends Subsystem, Executable, DashboardUpdater {

	public Intake setExtended(boolean extended);

	/**
	 * @return the state of the intake solenoid. 
	 * */
	public boolean isExtended();
	public boolean isRetracted();

	//Set intake speed
	public Intake setTargetRPS(double rps);

	public double getTargetRPS();
}
