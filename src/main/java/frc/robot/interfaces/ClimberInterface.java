/*package frc.robot.interfaces;

import org.strongback.Executable;

import frc.robot.subsystems.Climber;

public interface ClimberInterface extends DashboardUpdater, SubsystemInterface, Executable {

    public class ClimberAction {

        public final ClimberType type;  // What should the climber do?
        public final double value;  // What this is for depends on the type chosen.

        public ClimberAction(ClimberType type, double value) {
            this.type = type;
            this.value = value;
        }

        public enum ClimberType{
            HOLD_HEIGHT,
            STOP_CLIMBER, 
            SET_CLIMBER_POWER_LEFT,
            SET_CLIMBER_POWER_RIGHT,
            SET_CLIMBER_POWER,
            SET_LEFT_HEIGHT,
            SET_RIGHT_HEIGHT,
            SET_BOTH_HEIGHT, 
            OVERRIDE_LEFT_PERCENT_OUTPUT, 
            OVERRIDE_RIGHT_PERCENT_OUTPUT
        }

        @Override
        public String toString() {
            return String.format("%s: %f", type.toString().toLowerCase(), value);
        }

        @Override
        public boolean equals(Object other) {
            return this.toString().equals(other.toString());
        }
    }

    /** 
     * Sets the desired action for the climber.
     * @param climberAction
     *
    public void setDesiredAction(ClimberAction climberAction);
    

    /**
     * Gets the target action of the climber.
     * @return the desired action of the climber.
     
    public ClimberAction getDesiredAction();

    public boolean isInPosition();


}

*/