package frc.robot.subsystems;

import org.strongback.Executable;
import org.strongback.components.Clock;
import org.strongback.components.Motor;
import org.strongback.components.Solenoid;
import org.strongback.components.Motor.ControlMode;

import frc.robot.Constants;
import frc.robot.interfaces.DashboardInterface;
import frc.robot.interfaces.DashboardUpdater;
import frc.robot.interfaces.HatchInterface;
import frc.robot.interfaces.Log;
import frc.robot.interfaces.NetworkTableHelperInterface;
import frc.robot.interfaces.HatchInterface.HatchAction.Type;
import frc.robot.lib.Subsystem;

/**
 * Implements the hatch subsystem.
 * 
 * There are two parts to the subsystem:
 *   1) A solenoid for locking a hatch in place.
 *   2) A linear slide driven by a motor with an encoder and a hall effect
 *      sensor at each end.
 * 
 * This class expects to be given a configured TalonSRX that it can give
 * positional commands to.
 * 
 * There is a problem with the encoder that means that when the hatch
 * is told back to the zero position, it mechanically won't move
 * completely there, but it shows as being in the zero position.
 * This subsystem has a short term hack that when the position
 * is set to zero, it will be set to -1 and rely on the zeroing
 * done when it hits the end limit, causing it to reset to zero.
 */
public class Hatch extends Subsystem implements HatchInterface, Executable, DashboardUpdater {

    private Motor motor;  // Positioning motor.
    private Solenoid holder;  // Holds the hatch in position.
    private Clock clock;

    private double targetPosition = 0;
    private double calibrationEndTime = 0;
    private HatchAction action = new HatchAction(Type.CALIBRATE, 0);

    private enum State {
        UNCALIBRATED,
        CALIBRATING,
        CALIBRATION_FAILED,
        CALIBRATED
    }
    private State state = State.UNCALIBRATED;
    // Enable reset hack to work around elec/hardware issue.
    private final boolean kEnableResetHack = true;


    public Hatch(Motor motor, Solenoid holder, NetworkTableHelperInterface networkTable ,DashboardInterface dashboard, Clock clock, Log log) {
        super("Hatch", networkTable ,dashboard, log);   
        this.motor = motor;
        this.holder = holder;
        this.clock = clock;

        log.register(true, () -> getHeld(), "%s/held", name)
        .register(true, () -> getReleased(), "%s/released", name)
        .register(false, () -> targetPosition, "%s/targetPosition", name)
        .register(false, () -> motor.getPosition(), "%s/actualPosition", name)
        .register(false, () -> motor.isAtReverseLimit(), "%s/minSensor", name)
        .register(false, () -> motor.isAtForwardLimit(), "%s/maxSensor", name)
        .register(false, motor::getOutputVoltage, "%s/outputVoltage", name)
        .register(false, motor::getOutputPercent, "%s/outputPercent", name)
        .register(false, motor::getOutputCurrent, "%s/outputCurrent", name);

        targetPosition = getPosition();
    }

    /**
     * Set the action to do for the position of the hatch mechanism.
     * @param action
     */
    @Override
    public void setAction(HatchAction action) {
        // Hack, the adjust doesn't work if the same action is resent.
        // Need a better way to handle repeated relative commands.
        // The lift does it, but it has multiple State parameters to do so.
        if (this.action != null && action == this.action)
            return;
        // Action has changed.
        this.action = action;  // Remember it for next time.
        if (state != State.CALIBRATED && action.type != Type.CALIBRATE) {
            log.error("Error: Hatch is not calibrated, ignoring action %s.", action);
            return;
        }
        switch (action.type) {
        case CALIBRATE:
            startCalibration();
            return;
        case SET_MOTOR_POWER:
            motor.set(ControlMode.PercentOutput, action.value);
            return;
        case SET_POSITION:
            targetPosition = action.value;
            break;
        case ADJUST_POSITION:
            targetPosition += action.value;
            break;
        default:
            return;
        }
        // There is a mechanical or electrical issue with the hatch that means
        // that the encoder shows as being in the zero position when it's an
        // inch out. To force it to reset the zero position, when the zero
        // position is set, this overrides the target to be position -1 so that
        // it will hit the end limit and reset to zero.
        double position = targetPosition;
        if (kEnableResetHack && position == 0) position = -1;
        log.sub("Sending updated hatch position of %f", position);
        motor.set(ControlMode.Position, position);
    }

    /**
     * Gets the target action of the hatch position.
     * @return the desired action of the hatch.
     */
    @Override
    public HatchAction getAction() {
        return action;
    }

    /**
     * Returns true if the hatch is in the target position.
     */
    @Override
    public boolean isInPosition() {
        if (action.type != Type.ADJUST_POSITION && action.type != Type.SET_POSITION) return true; // Motor power has been overridden.
        double position = Math.max(0, getPosition());
        if (isMinSensorTriggered() && targetPosition < position) {
            // Hitting the right side stop. Prevent the target going any more negative.
            targetPosition = position;
            motor.set(ControlMode.Position, position);
            return true;
        }
        if (isMaxSensorTriggered() && targetPosition > position) {
            // Hitting the left side stop. Prevent the target going any more positive.
            targetPosition = position;
            motor.set(ControlMode.Position, position);
            return true;
        }
        return Math.abs(targetPosition - position) < Constants.HATCH_POSITION_TOLERANCE;
    } 

    /**
     * Either holds or releases the hatch. Normally by activating
     * a solenoid.
     */
    @Override
    public boolean setHeld(boolean held) {
        if(held) {
            holder.extend();
            return holder.isExtended();
        }

        holder.retract();
        return holder.isRetracted();
    }

    /**
     * Returns true if the cylinders have had enough time to
     * extend and hold any hatch.
     */
    @Override
    public boolean getHeld() {
        return holder.isExtended();
    }

    /**
     * Returns true if the cylinders have had enough time to
     * retract and hold any hatch.
     */
    @Override
    public boolean getReleased() {
        return holder.isRetracted();
    }

    private boolean isMinSensorTriggered() {
        return motor.isAtReverseLimit();
    }

    private boolean isMaxSensorTriggered() {
        return motor.isAtForwardLimit();
    }

    @Override
    public void enable() {
        super.enable();
        if (state != State.CALIBRATED) {
            startCalibration();
        }
    }

    /*
    * Hatch should start against the stowed side, if not, we assume it is between the two hall 
    * effect sensors, and we move the motor so that it will touch the sensor on the stowed side.
    */
    public void startCalibration() {

        state = isMinSensorTriggered() ? State.CALIBRATED : State.CALIBRATING;
        
        if (state == State.CALIBRATED) {
            log.sub("Hatch has started in the calibrated position");
            return;
        }

        log.sub("Starting hatch calibration.");

        motor.set(ControlMode.PercentOutput, Constants.HATCH_CALIBRATION_SPEED);

        calibrationEndTime = clock.currentTime() + 20;
    }


    @Override
	synchronized public void update() {
        if (state != State.CALIBRATING) return;

        // Finish the calibration.
        state = isMinSensorTriggered() ? State.CALIBRATED : State.CALIBRATING;
        if (state == State.CALIBRATED) {
            motor.set(ControlMode.PercentOutput, 0);
            log.sub("The hatch has been calibrated.");
            // Update the target position.
            targetPosition = getPosition();
            return;
        }
        if (clock.currentTime() < calibrationEndTime) {
            // Still calibrating.
            return;
        }

        log.error("Failed to calibrate hatch. Hatch subsystem is unusable");
        motor.set(ControlMode.PercentOutput, 0);
        state = State.CALIBRATION_FAILED;
    }

    public double getPosition() {
        return motor.getPosition();
    }

    /**
     * Update the operator console with the status of the hatch subsystem.
     */
	@Override
	public void updateDashboard() {
		dashboard.putString("Hatch calibrated?", state.toString());
		dashboard.putString("Hatch status", getHeld() ? "held" : getReleased() ? "released" : "moving");
		dashboard.putNumber("Hatch position", getPosition());
        dashboard.putString("Hatch min sensor", isMinSensorTriggered() ? "detected" : "not detected");
        dashboard.putString("Hatch max sensor", isMaxSensorTriggered() ? "detected" : "not detected");
		dashboard.putNumber("Hatch motor current", motor.getOutputCurrent());
		dashboard.putNumber("Hatch motor percent", motor.getOutputPercent());
	}
}