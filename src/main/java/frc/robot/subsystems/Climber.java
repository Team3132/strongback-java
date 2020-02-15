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
import frc.robot.interfaces.NetworkTableHelperInterface;
import frc.robot.lib.Subsystem;

/**
 * Implements the climber subsystem. TODO: Description
 * 
 * There are three parts to the subsystem:
 *   1) A pair of front stilts, driven by a single winch with an encoder and two hall
 *      effect sensors to detect when the stilt is fully retracted. Either sensor triggered
 *      indicates that the lift is high enough.
 *   2) A pair of rear stilts, driven by a single winch with an encoder and two hall
 *      effect sensors to detect when the stilt is fully retracted. Either sensor triggered
 *      indicates that the lift is high enough.
 *   3) Both lots of stilts also have wheels, but the rear ones are powered by a single
 *      motor.
 * 
 * This class expects to be given a configured motors for the two winches
 * and the drive wheels.
 */
public class Climber extends Subsystem implements ClimberInterface, Executable, DashboardUpdater {

    private Winch frontWinch;  // Controls the front stilts.
    private Winch rearWinch;  // Controls the rear stilts.
    private Motor wheelMotor;  // The powered wheels on the rear stilts.
    private ClimberAction action;
    private boolean holding = false;

    public Climber(Motor frontWinchMotor, Motor rearWinchMotor, Motor wheelMotor, 
            NetworkTableHelperInterface networkTable, DashboardInterface dashboard, Log log) {
        super("Climber", networkTable ,dashboard,log);   
        this.frontWinch = new Winch("climber:Front", frontWinchMotor, dashboard, log);
        this.rearWinch = new Winch("climber:Rear", rearWinchMotor, dashboard, log);
        this.wheelMotor = wheelMotor;
        setDesiredAction(new ClimberAction(Type.SET_BOTH_HEIGHT, 0));
    }

    @Override
    public boolean isInPosition() {
        return frontWinch.isInPosition() && rearWinch.isInPosition();
    }

    @Override
    public void execute(long timeInMillis) {
        if (action.type != Type.STOP_BOTH_HEIGHT) {
            holding = false;
        }
        switch (action.type) {
            case SET_FRONT_HEIGHT:
                frontWinch.setTargetHeight(action.value);
                break;
            case SET_REAR_HEIGHT:
                rearWinch.setTargetHeight(action.value);
                break;
            case STOP_BOTH_HEIGHT:
                if (!holding) {
                    frontWinch.setTargetHeight(frontWinch.getActualHeight());
                    rearWinch.setTargetHeight(rearWinch.getActualHeight());
                }
                holding = true;
                break;
            case SET_BOTH_HEIGHT:
                double targetHeight = action.value;
                // Calculate if both winches are within max offset distance - i.e. safe to move.
                if (Math.abs(frontWinch.getActualHeight() - rearWinch.getActualHeight()) > Constants.MAX_WINCH_PAIR_OFFSET) {
                    // Log an error if the winches are not at safe heights. 
                    log.error("%s: Not moving winches; winches did not begin at same height", name);
                    return;
                }
                // Both winches are currently within 20mm of each other.
                // Get the lowest & highest heights from both winches.
                double lowestHeight = Math.min(frontWinch.getActualHeight(), rearWinch.getActualHeight());
                double highestHeight = Math.max(frontWinch.getActualHeight(), rearWinch.getActualHeight());

                // Calculate if deploying or retracting, and use the appropriate target height.
                boolean goingUp = targetHeight > frontWinch.getTargetHeight();
                if (goingUp){
                    // Climber is going up; Target height is greater than current height.
                    // Set the target height for faster winch to be 10mm above the slowest one.
                    targetHeight = Math.min(lowestHeight + 20./25.4,targetHeight);
                } else {
                    // Climber is going down; Target height is lower than current height.
                    // Note: Also applicable if Target height is equal to current height, but will not move climber.
                    // Set the target height for faster winch to be 10mm below the slowest one.
                    targetHeight = Math.max(highestHeight - 20./25.4, targetHeight);
                }
                
                // Tell both winches their new target heights.
                frontWinch.setTargetHeight(targetHeight);
                rearWinch.setTargetHeight(targetHeight);
                
                break;
            case SET_DRIVE_SPEED:
                wheelMotor.set(ControlMode.PercentOutput, action.value);
                break;
            case OVERRIDE_FRONT_PERCENT_OUTPUT:
                frontWinch.setMotorPower(action.value);
                break;
            case OVERRIDE_REAR_PERCENT_OUTPUT:
                rearWinch.setMotorPower(action.value);
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
        frontWinch.updateDashboard();
        rearWinch.updateDashboard();
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