package frc.robot.interfaces;

import org.strongback.Executable;

public interface LEDStripInterface extends SubsystemInterface, Executable, DashboardUpdater {

    public LEDStripInterface setColour(int index, int r, int g, int b);

    public LEDStripInterface setColour(int indexS, int indexE, int r, int g, int b);

    public LEDStripInterface setData();
}