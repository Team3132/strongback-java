package frc.robot.interfaces;
import org.strongback.Executable;

public interface ColourWheelInterface extends SubsystemInterface, Executable, DashboardUpdater {
    public enum Colour {
        RED(0),
        YELLOW(1),
        BLUE(2),
        GREEN(3),
        UNKNOWN(-1);

        public final int id;
        Colour(int id) {
            this.id = id;
        }

        public static Colour of(int id) {
            switch(id) {
                case 0:
                    return RED;
                case 1:
                    return YELLOW;
                case 2:
                    return BLUE;
                case 3:
                    return GREEN;
                default:
                    return UNKNOWN;
            }
        }
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
            ADJUST_WHEEL_ANTICLOCKWISE,
            ADJUST_WHEEL_CLOCKWISE,
            NONE
        }

        @Override
        public String toString() {
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
}