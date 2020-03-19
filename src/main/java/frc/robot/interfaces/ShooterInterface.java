package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * Single wheel hooded shooter driven by three motors.
 * 
 * The hood can be extended and retracted for use directly against the goal and
 * shooting across the field respectively.
 */
public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {

	/**
	 * setTargetSpeed() sets the speed on the shooter wheels.
	 * 
	 * @param rps is the target speed that is being given to the shooter.
	 */
	public ShooterInterface setTargetRPS(double rps);

	public double getTargetRPS();

	public boolean isAtTargetSpeed();


	public ShooterInterface setHoodExtended(boolean extended);

	/**
	 * @return the state of the shooter solenoid.
	 */
	public boolean isHoodExtended();

	public boolean isHoodRetracted();
}
