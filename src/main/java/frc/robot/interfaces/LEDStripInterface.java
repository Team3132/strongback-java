package frc.robot.interfaces;
import frc.robot.lib.LEDColour;

public interface LEDStripInterface {
    public void setColour(LEDColour c);

    public void setProgressColour(LEDColour c1, LEDColour c2, int percent);

    public void setIdle();
}