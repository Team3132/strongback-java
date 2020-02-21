
package frc.robot.simulator;

import java.lang.*;
import frc.robot.interfaces.ShooterInterface;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class ShooterSimulator implements ShooterInterface{

    private double targetSpeed = 0;
    private double shooterTime = 0;

    @Override
    public String getName() {
        return "ShooterSimulator";
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
        return false;
    }

    @Override
    public void cleanup() {

    }

    @Override
    public ShooterInterface setTargetSpeed(double speed) {
        this.targetSpeed = speed;
        this.shooterTime = System.currentTimeMillis();
        return this;
    }

    @Override
    public double getTargetSpeed() {
        return targetSpeed;
    }


    @Override
    public boolean isTargetSpeed() {
        if ((System.currentTimeMillis() - this.shooterTime) < 1000) {
            return true;
        }
        return false;
    }
}
