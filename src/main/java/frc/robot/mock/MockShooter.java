package frc.robot.mock;

import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.Log;

public class MockShooter implements ShooterInterface {

    private double targetDutyCycle = 0; 

    public MockShooter(Log log) {
    }

    @Override
    public ShooterInterface setTargetDutyCycle(double dutyCycle) {
        targetDutyCycle = dutyCycle;
        return this;
    }

    @Override
    public double getTargetDutyCycle() {
        return targetDutyCycle;
    }

    @Override
    public boolean hasCell() {
        return false;
    }

    @Override
    public String getName() {
        return "MockSplitter";
    }

    @Override
    public void enable() {
    }

    @Override
    public void disable() {
    }

    @Override
    public void execute(long timeInMillis) {
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    @Override
    public void cleanup() {
    }
}