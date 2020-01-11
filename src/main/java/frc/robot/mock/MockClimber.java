package frc.robot.mock;

import frc.robot.interfaces.ClimberInterface;
import frc.robot.interfaces.Log;

public class MockClimber implements ClimberInterface {

    public MockClimber(Log log) {
    }

    @Override
    public String getName() {
        return "MockClimber";
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

    @Override
    public void setDesiredAction(ClimberAction action) {

    }

    @Override
    public ClimberAction getDesiredAction() {
        return null;
    }

    @Override
    public boolean isInPosition() {
        return true;
    }
}