package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * The Shooter subsystem is responsible for dealing with the shooter.
 * 22/02/20
 * The shooter currently is a single wheel hooded shooter.
 * The hood can be extended and retracted for use directly against the goal and across the field respectively.
 * It uses a set of three motors. One has an encoder while the rest without encoders.
 * It uses PIDF control to shoot.
 */
public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {
	
	/**
	 * setTargetSpeed() sets the speed on the shooter wheels.
	 * isTargetSpeed() clarifies whether the speed is greater than or equal to the target speed.
	 * @param speed is the target speed in RPM that is being given to the shooter.
	 */
    public ShooterInterface setTargetSpeed(double speed); 
    public double getTargetRPM(); 
	public boolean isAtTargetSpeed();
	
	public ShooterInterface setHoodExtended(boolean extended);

	/**
	 * @return the state of the shooter solenoid. 
	 * */
	public boolean isHoodExtended();
	public boolean isHoodRetracted();
}
