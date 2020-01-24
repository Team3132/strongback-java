package frc.robot.interfaces;

public interface ColourWheelInterface extends SubsystemInterface {
    public enum Colour {
        RED,
        YELLOW,
        BLUE,
        GREEN,
        UNKNOWN
    }

    public class ColourAction {
        public final Type type;
        public final Colour colour;

        public ColourAction(Type type, Colour colour) {
            this.type = type;
            this.colour = colour;
        }

        public enum Type {
            ROTATION,
            POSITION,
            NONE
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
}