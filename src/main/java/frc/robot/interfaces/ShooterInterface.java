package frc.robot.interfaces;

import org.strongback.Executable;

public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {

    /**
     * Sets the duty cycle on the spitter.
     */
    public ShooterInterface setTargetSpeed(double speed);
    public double getTargetSpeed();

    public boolean hasCell();
}