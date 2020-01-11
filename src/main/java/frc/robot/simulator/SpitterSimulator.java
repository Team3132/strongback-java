
package frc.robot.simulator;

import frc.robot.interfaces.SpitterInterface;

/**
 * Very basic intake simulator used for unit testing.
 * Does not do gravity/friction etc.
 */
public class SpitterSimulator implements SpitterInterface{

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
    public SpitterInterface setTargetDutyCycle(double dutyCycle) {
        this.dutyCycle = dutyCycle;
        return this;
    }

    @Override
    public double getTargetDutyCycle() {
        return dutyCycle;
    }

    @Override
    public boolean hasCargo() {
        return false;
    }
}
