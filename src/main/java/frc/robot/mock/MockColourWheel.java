package frc.robot.mock;

import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.Type;
import frc.robot.lib.Colour;

public class MockColourWheel implements ColourWheelInterface {
    private ColourAction action = new ColourAction(Type.NONE, Colour.UNKNOWN);

    public MockColourWheel(Log log) {
    }

    @Override
    public String getName() {
        return "MockColourWheel";
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
    public ColourWheelInterface setDesiredAction(ColourAction action) {
        this.action = action;
        return this;
    }

    @Override
    public ColourAction getDesiredAction() {
        return action;
    }

    @Override
    public boolean isFinished() {
        return true;
    }

}