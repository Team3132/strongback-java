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
	 * @param rpm is the target speed in RPM that is being given to the shooter.
	 */
	public ShooterInterface setTargetRPM(double rpm);

	public double getTargetRPM();

	public boolean isAtTargetSpeed();


	public ShooterInterface setHoodExtended(boolean extended);

	/**
	 * @return the state of the shooter solenoid.
	 */
	public boolean isHoodExtended();

	public boolean isHoodRetracted();
}
