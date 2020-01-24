
package frc.robot.simulator;

import frc.robot.interfaces.ShooterInterface;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class ShooterSimulator implements ShooterInterface{

    private double dutyCycle = 0;

    @Override
    public String getName() {
        return "SpitterSimulator";
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
    public ShooterInterface setTargetSpeed(double dutyCycle) {
        this.dutyCycle = dutyCycle;
        return this;
    }

    @Override
    public double getTargetSpeed() {
        return dutyCycle;
    }

    @Override
    public boolean hasCell() {
        return false;
    }
}
