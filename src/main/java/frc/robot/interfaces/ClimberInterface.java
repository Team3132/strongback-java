package frc.robot.interfaces;

import org.strongback.Executable;

public interface ClimberInterface extends DashboardUpdater, SubsystemInterface, Executable {

    public class ClimberAction {

        public final Type type;  // What should the climber do?
        public final double value;  // What this is for depends on the type chosen.

        public ClimberAction(Type type, double value) {
            this.type = type;
            this.value = value;
        }

        public enum Type {
            SET_FRONT_HEIGHT, 
            SET_REAR_HEIGHT,
            SET_BOTH_HEIGHT,
            STOP_BOTH_HEIGHT,
            SET_DRIVE_SPEED,
            OVERRIDE_FRONT_PERCENT_OUTPUT,
            OVERRIDE_REAR_PERCENT_OUTPUT
        }

        @Override
        public String toString() {
            return String.format("%s: %f", type.toString().toLowerCase(), value);
        }
    }

    /** 
     * Sets the desired action for the climber.
     * @param action
     */
    public void setDesiredAction(ClimberAction action);

    /**
     * Gets the target action of the climber.
     * @return the desired action of the climber.
     */
    public ClimberAction getDesiredAction();

    public boolean isInPosition();
}

