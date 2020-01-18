package frc.robot.interfaces;

import org.strongback.Executable;

public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {

    /**
     * Sets the duty cycle on the spitter.
     */
    public ShooterInterface setTargetDutyCycle(double dutyCycle);
    public double getTargetDutyCycle();

    public boolean hasCell();
}