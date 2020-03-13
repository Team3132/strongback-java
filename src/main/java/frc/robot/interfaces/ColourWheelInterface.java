package frc.robot.interfaces;

import org.strongback.Executable;

import frc.robot.lib.WheelColour;

public interface ColourWheelInterface extends SubsystemInterface, Executable, DashboardUpdater {
    public class ColourAction {
        public final ColourWheelType type;
        public final WheelColour colour;

        public ColourAction(ColourWheelType type, WheelColour colour) {
            this.type = type;
            this.colour = colour;
        }

        public enum ColourWheelType {
            ROTATION,
            POSITION,
            ADJUST_WHEEL_ANTICLOCKWISE,
            ADJUST_WHEEL_CLOCKWISE,
            NONE
        }

        public boolean movingToUnknownColour() {
            return type.equals(ColourWheelType.POSITION) && colour.equals(WheelColour.UNKNOWN);
        }

        @Override
        public boolean equals(Object obj) {
            if(this == obj)
                return true;
            if(obj == null)
                return false;
            if(getClass() != obj.getClass())
                return false;
            ColourAction other = (ColourAction) obj;
            if (other.colour != colour) return false;
            if (other.type != type) return false;
            return true;
        }

        @Override
        public String toString() {
            if (type.equals(type.NONE)) {
                return String.format("%s", type.toString().toLowerCase());
            }
            return String.format("%s: %s", type.toString().toLowerCase(), colour);
        }
    }

    /** 
     * Sets the desired action for the colour sensor.
     * @param action
     */
    public ColourWheelInterface setDesiredAction(ColourAction action);

    /**
     * Gets the target action of the colour sensor.
     * @return the desired action of the colour sensor.
     */
    public ColourAction getDesiredAction();

    /** @return if colour wheel has finished spinning.
     */
    public boolean isFinished();

    /**
	 * Sets the state of the colour wheel arm solenoid.
	 * */
    public void setArmExtended(boolean extended);

	/**
	 * @return the state of the colour wheel arm solenoid. 
	 * */
	public boolean isArmExtended();
	public boolean isArmRetracted();

}