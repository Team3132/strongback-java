package frc.robot.mock;

import frc.robot.interfaces.ColourWheel;
import frc.robot.lib.WheelColour;
import frc.robot.interfaces.ColourWheel.ColourAction.ColourWheelType;

public class MockColourWheelImpl implements ColourWheel {
    private ColourAction action = new ColourAction(ColourWheelType.NONE, WheelColour.UNKNOWN);
    private boolean extended = false;

    public MockColourWheelImpl() {
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
    public ColourWheel setDesiredAction(ColourAction action) {
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
        return extended;
    }

    @Override
    public boolean isArmRetracted() {
        return extended == false;
    }

    @Override
    public void setArmExtended(boolean extended) {
        this.extended = extended;
    }

}