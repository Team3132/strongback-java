package frc.robot.interfaces;

import org.strongback.Executable;

public interface LEDStripInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public class LEDAction {
        public final Type type;
        public final int r;
        public final int g;
        public final int b;

        public enum Type {
            SET_ALL,
            
            NONE
        }

        public LEDAction(Type type, int r, int g, int b) {
            this.type = type;
            this.r = r;
            this.g = g;
            this.b = b;
        }
    }

    public LEDStripInterface setColour(int index, int r, int g, int b);

    public LEDStripInterface setColour(int indexS, int indexE, int r, int g, int b);

    public LEDStripInterface setColour(int r, int g, int b);

    public LEDStripInterface setProgressColour(int current, int total);

    public LEDStripInterface setDefault();

    public LEDStripInterface setData();
    
    /** 
     * Sets the desired action for the colour sensor.
     * @param action
     */
    public LEDStripInterface setDesiredAction(LEDAction action);
}