package frc.robot.interfaces;
import org.strongback.Executable;

public interface ColourWheelInterface extends SubsystemInterface, Executable, DashboardUpdater {
    public enum Colour {
        RED(0, "red"),
        YELLOW(1, "yellow"),
        BLUE(2, "blue"),
        GREEN(3, "green"),
        UNKNOWN(-1, "unknown");
        
        private final int NUM_COLOURS = 4;
        public final int id;
        public final String name;
        Colour(int id, String name) {
            this.id = id;
            this.name = name;
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

        public boolean equals (Colour colour) {
            return this.id == colour.id;
        }

        public Colour next (double direction) {
            return Colour.of((this.id + NUM_COLOURS + (direction < 0 ? 1 : -1)) % NUM_COLOURS);
        }

        @Override
        public String toString () {
            return name + "(" + id + ")";
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