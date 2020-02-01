package frc.robot.interfaces;

import org.strongback.Executable;

public interface ShooterInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public ShooterInterface setTargetSpeed(double speed);
    public double getTargetSpeed();
    public boolean isTargetSpeed();
    
    public ShooterInterface setFeederPower(double percent);
    public double getFeederPower();
    //public boolean hasCell();
}