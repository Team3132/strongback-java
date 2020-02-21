package frc.robot.mock;

import frc.robot.interfaces.ShooterInterface;
import frc.robot.interfaces.Log;

public class MockShooter implements ShooterInterface {

    private double targetSpeed = 0;

    public MockShooter(Log log) {
    }

    @Override
    public ShooterInterface setTargetSpeed(double speed) {
        targetSpeed = speed;
        return this;
    }

    @Override
    public boolean isTargetSpeed() {
        return true;
    }

    @Override
    public double getTargetSpeed() {
        return targetSpeed;
    }

    @Override
    public String getName() {
        return "MockShooter";
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