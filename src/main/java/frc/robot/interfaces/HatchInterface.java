package frc.robot.interfaces;

import org.strongback.Executable;

public interface HatchInterface extends SubsystemInterface, DashboardUpdater, Executable {

    public class HatchAction {
        public enum Type{
            CALIBRATE,  // Recalibrate.
            SET_POSITION,  // Set the absolute position.
            ADJUST_POSITION,  // Adjust the position.
            SET_MOTOR_POWER,  // Tell the motor to apply the power level.
            USE_VISION  // TODO: Currently not implemented
        }

        public final Type type;  // What should the hatch position do?
        public final double value;  // What this is for depends on the type chosen.

        public HatchAction(Type type, double value) {
            this.type = type;
            this.value = value;
        }

        @Override
        public String toString() {
            return String.format("%s: %f", type.toString().toLowerCase(), value);
        }
    }

    /**
     * Set the action to do for the hatch mechanism.
     * @param action
     */
    public void setAction(HatchAction action);

    /**
     * Gets the target action of the hatch position.
     * @return the desired action of the hatch.
     */
    public HatchAction getAction();

    /**
     * Returns true if the hatch is in the target position or
     * the motor power has been overriden.
     */
    public boolean isInPosition();

    /**
     * Either holds or releases the hatch. Normally by activating
     * a solenoid.
     */
    public boolean setHeld(boolean held);

    /**
     * Returns true if the cylinders have had enough time to
     * extend and hold any hatch.
     */
    public boolean getHeld();

    /**
     * Returns true if the cylinders have had enough time to
     * retract and hold any hatch.
     */
    public boolean getReleased();
}