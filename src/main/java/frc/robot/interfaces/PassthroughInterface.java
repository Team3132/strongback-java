package frc.robot.interfaces;

import org.strongback.Executable;

/**
 * This is a conveyor to move the ball from the intake through to the spitter (shooter).
 */
public interface PassthroughInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public double getTargetMotorOutput();

    public void setTargetMotorOutput(double current);

}