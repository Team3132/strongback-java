package frc.robot.mock;

import frc.robot.interfaces.ColourWheelInterface;
import frc.robot.interfaces.Log;
import frc.robot.lib.WheelColour;
import frc.robot.interfaces.ColourWheelInterface.ColourAction.ColourWheelType;
import frc.robot.interfaces.LoaderInterface; // FIXME: Why do I need to import this?

public class MockColourWheel implements ColourWheelInterface {
    private ColourAction action = new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN);

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

    @Override
    public boolean isArmExtended() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public boolean isArmRetracted() {
        // TODO Auto-generated method stub
        return false;
    }

    @Override
    public LoaderInterface setArmExtended(boolean extended) {
        // TODO Auto-generated method stub
        return null;
    }

}