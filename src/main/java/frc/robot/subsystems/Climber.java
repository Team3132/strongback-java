/*package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Motor;
import org.strongback.components.Motor.ControlMode;
import org.strongback.components.Clock;

import frc.robot.Constants;
import frc.robot.interfaces.ClimberInterface;
import frc.robot.interfaces.ClimberInterface.ClimberAction.ClimberType;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.NetworkTableHelperInterface;
import frc.robot.lib.Subsystem;
import frc.robot.lib.SimplePID;
import frc.robot.lib.MathUtil;

// TODO: Description of Subsystem once we know what it looks like.

public class Climber extends Subsystem implements ClimberInterface, Executable, DashboardUpdater {
    private Winch leftWinch; 
    private Winch rightWinch;
    private ClimberAction action = new ClimberAction(ClimberType.STOP_CLIMBER, 0);
    private boolean holding = false;
    private SimplePID leftPID, rightPID;
    private Clock clock;
    private double targetHeight, oldTargetHeight;
    private double climberDiffP;
    private double climberDiffTime;
    private double leftMotorPower;
    private double rightMotorPower;
    

    public Climber(Motor leftWinchMotor, Motor rightWinchMotor, DashboardInterface dashboard, Clock clock, Log log) {
        super("Climber", dashboard, log);   
        this.clock = clock;
        this.leftWinch = new Winch("leftClimber:", leftWinchMotor, dashboard, log);
        this.rightWinch = new Winch("rightClimber:", rightWinchMotor, dashboard, log);
        setDesiredAction(new ClimberAction(ClimberType.STOP_CLIMBER, 0));  
        targetHeight = 0;
        oldTargetHeight = 0;
        climberDiffP = Constants.CLIMBER_POWER_NOT_LEVEL_P;
        climberDiffTime = 0;
    }

    @Override
    public void enable() {
        rightPID = new SimplePID(Constants.CLIMBER_P, Constants.CLIMBER_I, Constants.CLIMBER_D, Constants.CLIMBER_F,
                clock::currentTime);
        leftPID = new SimplePID(Constants.CLIMBER_P, Constants.CLIMBER_I, Constants.CLIMBER_D, Constants.CLIMBER_F,
                clock::currentTime);
        super.enable();
    }

    @Override
    public void disable() {
        leftWinch.setMotorPower(0);
        rightWinch.setMotorPower(0);
        action = new ClimberAction(ClimberType.STOP_CLIMBER, 0);
        super.disable();
    }
    
    @Override
    public boolean isInPosition() {
        return leftWinch.isInPosition() && rightWinch.isInPosition();
    }


    @Override
    public void execute(long timeInMillis) {
        if (action.type != ClimberType.STOP_CLIMBER) {
            holding = false;
        }
        switch (action.type) {
            case SET_CLIMBER_POWER_LEFT:
                leftWinch.setMotorPower(action.value);
                break;
            case SET_CLIMBER_POWER_RIGHT:
                rightWinch.setMotorPower(action.value);
                break;
            case SET_CLIMBER_POWER:
                leftWinch.setMotorPower(action.value);
                rightWinch.setMotorPower(-action.value);
                break;
            case HOLD_HEIGHT:
                if (!holding) {
                    leftWinch.setMotorPower(0);
                    rightWinch.setMotorPower(0);
                    //leftWinch.setTargetHeight(leftWinch.getActualHeight());
                    //rightWinch.setTargetHeight(rightWinch.getActualHeight());
                }
                holding = true;
                break;
            case STOP_CLIMBER:
                //leftWinch.setTargetHeight(leftWinch.getActualHeight());
                //rightWinch.setTargetHeight(rightWinch.getActualHeight());
                leftWinch.setMotorPower(0);
                rightWinch.setMotorPower(0);
                break;
            case SET_LEFT_HEIGHT:
                leftWinch.setTargetHeight(targetHeight);
                break;
            case SET_RIGHT_HEIGHT:
                rightWinch.setTargetHeight(targetHeight);
                break;
            case SET_BOTH_HEIGHT:
                /*
                // when we are in SET_BOTH_HEIGHT we are trying to move the left and the back together.
                // If we are at the target height (Or close enough to it we change to position mode and let the talon control
                // the fine movement. Otherwise we use power mode to get to the position.
                //
                // We look at the location of the two heights and work out the difference. If the difference is beyond the threshold
                // we adjust power to make sure that they are moving up in parallel.
                */
/*                targetHeight = action.value;

                double leftHeight = leftWinch.getActualHeight();
                double rightHeight = rightWinch.getActualHeight();

                // // Calculate if both winches are within max offset distance - i.e. safe to move.
                // if (Math.abs(leftHeight - rightHeight) > Constants.CLIMBER_MAX_WINCH_PAIR_OFFSET) {
                //     // Log an error if the winches are not at safe heights. 
                //     log.error("%s: Not moving winches; winches did not begin at same height %f, %f (%f)", name, leftHeight, rightHeight, targetHeight);
                //     return;
                // }

                if ((Math.abs(leftHeight - targetHeight) < Constants.CLIMBER_MAX_DISTANCE_FROM_TOP) && 
                    (Math.abs(rightHeight - targetHeight) < Constants.CLIMBER_MAX_DISTANCE_FROM_TOP)) {
                    // We are at the target position. Change over to maintaining the position.
                    log.sub("%s: At holding height %f,%f setting target to %f", name, leftHeight, rightHeight, targetHeight);
                    leftWinch.setTargetHeight(targetHeight);
                    rightWinch.setTargetHeight(targetHeight);
                    setDesiredAction(new ClimberAction(ClimberType.STOP_CLIMBER, 0));  
                    break;
                }//
                // We run a modified PID loop for both the left and back motors.
                //
                if (oldTargetHeight != targetHeight) {
                    // we set the target setpoint as it has changed.
                    leftPID.setSetpoint(targetHeight);
                    rightPID.setSetpoint(targetHeight);
                    oldTargetHeight = targetHeight;
                }

                // one or both are not at the target height yet. Set the power and direction depending on whether we are moving up or down.
                //
                // There are four possibilities:
                // 1.  left < target, back < target
                // 2.  left > target, back > target
                // 3.  left < target, back > target
                // 4.  left > target, back < target
                // 1 and 2 are the most common. Check them first.

                leftMotorPower = MathUtil.clamp(leftPID.getOutput(leftHeight), -Constants.CLIMBER_MAX_MOTOR_POWER, Constants.CLIMBER_MAX_MOTOR_POWER);
                rightMotorPower = MathUtil.clamp(rightPID.getOutput(rightHeight), -Constants.CLIMBER_MAX_MOTOR_POWER, Constants.CLIMBER_MAX_MOTOR_POWER);
                 
                if (leftHeight > rightHeight) {
                // left is too far up, speed up the right to try and compensate
                rightMotorPower += (climberDiffP * (leftHeight - rightHeight));
                if (rightMotorPower > Constants.CLIMBER_MAX_MOTOR_POWER) {
                    leftMotorPower -= (rightMotorPower - Constants.CLIMBER_MAX_MOTOR_POWER);
                    rightMotorPower = Constants.CLIMBER_MAX_MOTOR_POWER;
                }
            } else {
                // right is too far up, speed up the left to try and compensate
                leftMotorPower += (climberDiffP * (rightHeight - leftHeight));
                if (leftMotorPower > Constants.CLIMBER_MAX_MOTOR_POWER) {
                    rightMotorPower -= (leftMotorPower - Constants.CLIMBER_MAX_MOTOR_POWER);
                    leftMotorPower = Constants.CLIMBER_MAX_MOTOR_POWER;
                }
            }

                //log.debug("%s: SET_BOTH_HEIGHT: %f,%f => %f,%f", name, leftHeight, rightHeight, leftMotorPower, rightMotorPower);
                leftWinch.setMotorPower(leftMotorPower);
                rightWinch.setMotorPower(rightMotorPower);
                break;

            case OVERRIDE_LEFT_PERCENT_OUTPUT:
                leftWinch.setMotorPower(action.value);
                break;

            case OVERRIDE_RIGHT_PERCENT_OUTPUT:
                rightWinch.setMotorPower(action.value);
                break;


            default:
                log.error("%s: Unknown Type %s", name, action.type);
                break;
        }
    }
    

    /**
     * Update the operator console with the status of the subsystem.
     */
/*	@Override
	public void updateDashboard() {
        leftWinch.updateDashboard();
        rightWinch.updateDashboard();        
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
            // On the right, either sensor will do.
            // On the left, it doesn't matter which it is wired into.
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
}*/
