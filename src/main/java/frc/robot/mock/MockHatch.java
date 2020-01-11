package frc.robot.mock;

import frc.robot.interfaces.HatchInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.HatchInterface.HatchAction.Type;

public class MockHatch implements HatchInterface {
    private boolean isHeld = false;
    private HatchAction action = new HatchAction(Type.CALIBRATE, 0);

    public MockHatch(Log log) {
    }

    @Override
    public void setAction(HatchAction action) {
        this.action = action;
    }

    @Override
    public HatchAction getAction() {
        return action;
    }

    @Override
    public boolean isInPosition() {
        return true;
    }

    @Override
    public boolean getHeld() {
        return isHeld;
    }

    @Override
    public boolean getReleased() {
        return !isHeld;
    }

    @Override
    public boolean setHeld(boolean held) {
        isHeld = held;
        return getHeld();
    }

    @Override
    public String getName() {
        return "MockHatch";
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