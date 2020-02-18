package frc.robot.interfaces;
import frc.robot.lib.Colour;

public interface LEDStripInterface {
    public void setColour(Colour c);

    public void setProgressColour(Colour c1, Colour c2, int percent);

    public void setIdle();
}