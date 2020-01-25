package frc.robot.interfaces;

import org.strongback.Executable;

public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public ShooterInterface setTargetSpeed(double speed);
    public double getTargetSpeed();

    public ShooterInterface setFeederSpeed(double percent);

    public boolean hasCell();
}