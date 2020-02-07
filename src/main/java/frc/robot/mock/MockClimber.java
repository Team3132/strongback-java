package frc.robot.mock;

import frc.robot.interfaces.ClimberInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.ClimberInterface.ClimberAction.Type;

public class MockClimber implements ClimberInterface {

    private ClimberAction action = new ClimberAction(Type.STOP_CLIMBER, 0);
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
        this.action = action;
    }

    @Override
    public ClimberAction getDesiredAction() {
        return action;
    }

    @Override
    public boolean isInPosition() {
        return true;
    }
}