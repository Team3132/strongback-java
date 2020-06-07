package frc.robot.interfaces;

import org.strongback.components.ui.InputDevice;
import org.strongback.components.ui.FlightStick;

public interface OI {
    
    /**
     * Configure the standard Operator Interface
	 * @param driverJoystick the drivers joystick
	 * @param operatorJoystick the operators joystick
     */
	public void configureJoysticks(FlightStick driverLeft, FlightStick driverRight, InputDevice operator);
    
    /**
	 * Configure the button box as a test interface device. This interface allows us
	 * a high degree of manual control over the robot
	 * 
	 * @param buttonBoxJoystick a hardware box that pretends to be a joystick
	 */
	public void configureDiagBox(InputDevice buttonBoxJoystick);
}