package frc.robot.interfaces;

/**
 * Tests Spark MAX motor controller and Neo brushless.
 */
import org.strongback.Executable;

public interface SparkTestInterface extends SubsystemInterface, Executable, DashboardUpdater {

	public void setMotorOutput(double output);
	public double getMotorOutput();
}
