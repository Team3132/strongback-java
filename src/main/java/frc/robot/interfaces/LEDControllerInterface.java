package frc.robot.interfaces;

import org.strongback.Executable;

public interface LEDControllerInterface extends SubsystemInterface, Executable {
	
	public void setColour(double red, double green, double blue);
	
	public void doIdleColours();
}
