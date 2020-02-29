package frc.robot.interfaces;
import frc.robot.lib.LEDColour;

public interface LEDStripInterface {
    public void setColour(LEDColour c);

    /**
     * Fills the led strip like a progress bar.
     * @param c1 Colour for completed area
     * @param c2 Colour for unfinished area
     * @param percent Percet of leds to fill from 0-1
     */
    public void setProgressColour(LEDColour c1, LEDColour c2, double percent);

    public void setIdle();
}