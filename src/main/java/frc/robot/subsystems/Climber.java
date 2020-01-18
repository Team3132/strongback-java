package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;

import frc.robot.Constants;
import frc.robot.interfaces.ClimberInterface;
import frc.robot.interfaces.ClimberInterface.ClimberAction.Type;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.lib.Subsystem;

// TODO: Description of Subsystem once we know what it looks like.

public class Climber extends Subsystem implements ClimberInterface, Executable, DashboardUpdater {
    private Winch climbWinch;  // Controls the front stilts.
    private ClimberAction action;
    private boolean holding = false;

    public Climber(Motor WinchMotor, DashboardInterface dashboard, Log log) {
        super("Climber", dashboard, log);   
        this.climbWinch = new Winch("climber:", WinchMotor, dashboard, log);
        setDesiredAction(new ClimberAction(Type.STOP_CLIMBER, 0));
    }

    @Override
    public boolean isInPosition() {
        return climbWinch.isInPosition();
    }

    @Override
    public void execute(long timeInMillis) {
        if (action.type != Type.STOP_CLIMBER) {
            holding = false;
        }
        switch (action.type) {
            case SET_CLIMBER_POWER:
                climbWinch.setMotorPower(action.value);
                break;
            case HOLD_HEIGHT:
                if (!holding) {
                    climbWinch.setTargetHeight(climbWinch.getActualHeight());
                }
                holding = true;
                break;
            case STOP_CLIMBER:
                climbWinch.setMotorPower(0);
                break;                
            default:
                log.error("%s: Unknown Type %s", name, action.type);
                break;
        }
    }
    

    /**
     * Update the operator console with the status of the subsystem.
     */
	@Override
	public void updateDashboard() {
        climbWinch.updateDashboard();
        dashboard.putString("Climber type", action.type.toString());
    }
    
    private class Winch implements DashboardUpdater {
        private String name;
        private Motor motor;
        private Log log;
        private double targetHeight = 0;
        private DashboardInterface dashboard;

        public Winch(String name, Motor motor, DashboardInterface dashboard, Log log) {
            this.name = name;
            this.motor = motor;
            this.log = log;
            this.dashboard = dashboard;

            // Reset the encoder as the robot starts with the stilts at zero height.
            motor.setPosition(0);
            log.register(false, () -> getTargetHeight(), "%s/targetHeight", name)
               .register(false, () -> getActualHeight(), "%s/actualHeight", name)
               .register(false, () -> getHallEffectTriggered(), "%s/hallEffect", name)
               .register(false, motor::getOutputVoltage, "%s/outputVoltage", name)
               .register(false, motor::getOutputPercent, "%s/outputPercent", name)
               .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);
        }

        public void setTargetHeight(double height) {
            if (height == targetHeight) return;
            log.sub("%s: setting target height to %.1f", name, height);
            targetHeight = height;
            motor.set(ControlMode.Position, height);
        }

        public void setMotorPower(double power) {
            motor.set(ControlMode.PercentOutput, power);
        }

        public double getActualHeight() {
            return motor.getPosition();
        }

        public double getTargetHeight() {
            return targetHeight;
        }

        public boolean isInPosition() {
            return Math.abs(getActualHeight() - getTargetHeight()) < Constants.CLIMBER_TOLERANCE;
        }

        public boolean getHallEffectTriggered() {
            // On the rear, either sensor will do.
            // On the front, it doesn't matter which it is wired into.
            return motor.isAtReverseLimit() || motor.isAtForwardLimit();
        }

    
        @Override
        public void updateDashboard() {
            dashboard.putNumber(name + " actual height", getActualHeight());
            dashboard.putNumber(name + " target height", getTargetHeight());
            dashboard.putString(name + " at target height", isInPosition() ? "yes" : "no");
            dashboard.putNumber(name + " current", motor.getOutputCurrent());
            dashboard.putString(name + " hall effect", getHallEffectTriggered() ? "triggered" : "untriggered");
        }
    }

    @Override
    public void setDesiredAction(ClimberAction action) {
        if (this.action != null && action.equals(this.action))
            return;
        // Climb action has changed.
        this.action = action;  // Remember it for next time.
    }

    @Override
    public ClimberAction getDesiredAction() {
        return action;
    }
}