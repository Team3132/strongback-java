package frc.robot.mock;

import frc.robot.interfaces.PassthroughInterface;
import frc.robot.interfaces.Log;

public class MockPassthrough implements PassthroughInterface {
    private double output = 0;

    public MockPassthrough(Log log) {
    }
    
    @Override
    public double getTargetMotorOutput() {
        return output;
    }

    @Override
    public void setTargetMotorOutput(double percentPower) {

    }

    @Override
    public String getName() {
        return null;
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

}